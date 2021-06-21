package ir.namoo.religiousprayers.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.helper.widget.Flow
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.work.*
import com.google.android.material.snackbar.Snackbar
import io.github.persiancalendar.Equinox
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.religiousprayers.*
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.db.AthanSetting
import ir.namoo.religiousprayers.db.AthanSettingsDB
import ir.namoo.religiousprayers.entities.CalendarTypeItem
import ir.namoo.religiousprayers.praytimes.PrayTimeProvider
import ir.namoo.religiousprayers.service.ApplicationService
import ir.namoo.religiousprayers.service.BroadcastReceivers
import ir.namoo.religiousprayers.service.UpdateWorker
import net.lingala.zip4j.ZipFile
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

// This should be called before any use of Utils on the activity and services
fun initUtils(context: Context) {
    updateStoredPreference(context)
    createAthansSettingDB(context)
    applyAppLanguage(context)
    loadLanguageResource()
    loadAlarms(context)
    loadEvents(context)
}

val supportedYearOfIranCalendar: Int
    get() = IranianIslamicDateConverter.latestSupportedYearOfIran

fun isArabicDigitSelected(): Boolean = when (preferredDigits) {
    ARABIC_DIGITS -> true
    else -> false
}

fun goForWorker(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun toLinearDate(date: AbstractDate): String = "%s/%s/%s".format(
    formatNumber(date.year), formatNumber(date.month), formatNumber(date.dayOfMonth)
)

fun isNightModeEnabled(context: Context): Boolean =
    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun formatDate(
    date: AbstractDate, calendarNameInLinear: Boolean = true, forceNonNumerical: Boolean = false
): String = if (numericalDatePreferred && !forceNonNumerical)
    (toLinearDate(date) + if (calendarNameInLinear) (" " + getCalendarNameAbbr(date)) else "").trim()
else when (language) {
    LANG_CKB -> "%sی %sی %s"
    else -> "%s %s %s"
}.format(formatNumber(date.dayOfMonth), date.monthName, formatNumber(date.year))

fun isNonArabicScriptSelected() = when (language) {
    LANG_EN_US, LANG_JA -> true
    else -> false
}

// en-US and ja are our only real LTR locales for now
fun isLocaleRTL(): Boolean = when (language) {
    LANG_EN_US, LANG_JA -> false
    else -> true
}

fun formatNumber(number: Double): String = when (preferredDigits) {
    ARABIC_DIGITS -> number.toString()
    else -> formatNumber(number.toString())
        .replace(".", "٫" /* U+066B, Arabic Decimal Separator */)
}

fun formatNumber(number: Int): String = formatNumber(number.toString())

fun formatNumber(number: String): String = when (preferredDigits) {
    ARABIC_DIGITS -> number
    else -> number.map {
        preferredDigits.getOrNull(Character.getNumericValue(it)) ?: it
    }.joinToString("")
}

// It should match with calendar_type_abbr array
fun getCalendarNameAbbr(date: AbstractDate) = calendarTypesTitleAbbr.getOrNull(
    when (date) {
        is PersianDate -> 0
        is IslamicDate -> 1
        is CivilDate -> 2
        else -> -1
    }
) ?: ""

fun getThemeFromPreference(context: Context, prefs: SharedPreferences): String =
    prefs.getString(PREF_THEME, null)?.takeIf { it != SYSTEM_DEFAULT_THEME }
        ?: if (isNightModeEnabled(context)) DARK_THEME else CYAN_THEME

fun getEnabledCalendarTypes() = listOf(mainCalendar) + otherCalendars

fun loadApp(context: Context): Unit = if (!goForWorker()) runCatching {
    val alarmManager = context.getSystemService<AlarmManager>() ?: return@runCatching

    val startTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 1)
        add(Calendar.DATE, 1)
    }

    val dailyPendingIntent = PendingIntent.getBroadcast(
        context, LOAD_APP_ID,
        Intent(context, BroadcastReceivers::class.java).setAction(BROADCAST_RESTART_APP),
        PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )
    alarmManager.set(AlarmManager.RTC, startTime.timeInMillis, dailyPendingIntent)

    // There are simpler triggers on older Androids like SCREEN_ON but they
    // are not available anymore, lets register an hourly alarm for >= Oreo
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val threeHoursPendingIntent = PendingIntent.getBroadcast(
            context,
            THREE_HOURS_APP_ID,
            Intent(context, BroadcastReceivers::class.java).setAction(BROADCAST_UPDATE_APP),
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        alarmManager.setInexactRepeating(
            AlarmManager.RTC,
            // Start from one hour from now
            Calendar.getInstance().timeInMillis + TimeUnit.HOURS.toMillis(1),
            TimeUnit.HOURS.toMillis(3), threeHoursPendingIntent
        )
    }
}.getOrElse(logException) else Unit

fun getOrderedCalendarTypes(): List<CalendarType> = getEnabledCalendarTypes().let {
    it + (CalendarType.values().toList() - it)
}

fun loadAlarms(context: Context) {
    val athans = AthanSettingsDB.getInstance(context.applicationContext).athanSettingsDAO()
        .getAllAthanSettings()
    logDebug(TAG, "reading and loading all alarms")

    if (coordinate != null && !athans.isNullOrEmpty()) {
        val athanGap =
            ((context.appPrefs.getString(PREF_ATHAN_GAP, null)?.toDoubleOrNull() ?: .0)
                    * 60.0 * 1000.0).toLong()

        val prayTimes = PrayTimeProvider.calculate(
            calculationMethod,
            Jdn.today, coordinate!!, context
        )
        // convert spacedComma separated string to a set
        athans.filter { it.state }.forEach {
            val alarmTime: Clock = when (it.athanKey) {
                "DHUHR" -> prayTimes.dhuhrClock
                "ASR" -> prayTimes.asrClock
                "MAGHRIB" -> prayTimes.maghribClock
                "ISHA" -> prayTimes.ishaClock
                "FAJR" -> prayTimes.fajrClock
                "SUNRISE" -> prayTimes.sunriseClock
                // a better to have default
                else -> prayTimes.fajrClock
            }
            val time = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarmTime.hour)
                set(Calendar.MINUTE, alarmTime.minute)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            set(it, context, time, it.athanKey, it.id, athanGap, alarmTime)
            if (it.isBeforeEnabled) {
                val at: Clock = Clock.fromInt(
                    alarmTime.toInt() - it.beforeAlertMinute
                )
                set(it, context, Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, at.hour)
                    set(Calendar.MINUTE, at.minute)
                    set(Calendar.SECOND, 0)
                }.timeInMillis, "B${it.athanKey}", it.id + 10, athanGap, at)
            }
        }
    }
}

private fun set(
    athanSetting: AthanSetting,
    context: Context,
    time: Long,
    athanKey: String,
    ord: Int,
    athanGap: Long,
    alarmTime: Clock
) {
    logDebug(TAG, "set for key= $athanKey ord=$ord --> time=${alarmTime.toFormattedString()}")
    if (athanSetting.playType == 0)// full screen
        if (context.appPrefsLite.getInt(
                PREF_FULL_SCREEN_METHOD,
                DEFAULT_FULL_SCREEN_METHOD
            ) == 1
        )//method 1
            setAlarm(
                context,
                athanKey,
                time,
                ord,
                athanGap,
                alarmTime.toFormattedString()
            )
        else// method 2
            setAlarm2(
                context,
                athanKey,
                time,
                ord,
                athanGap,
                alarmTime.toFormattedString()
            )
    else//notification
        if (context.appPrefsLite.getInt(
                PREF_NOTIFICATION_METHOD,
                DEFAULT_NOTIFICATION_METHOD
            ) == 1
        ) // method 1
            setAlarm(
                context,
                athanKey,
                time,
                ord,
                athanGap,
                alarmTime.toFormattedString()
            )
        else// method 2
            setAlarm2(
                context,
                athanKey,
                time,
                ord,
                athanGap,
                alarmTime.toFormattedString()
            )
}

private fun setAlarm(
    context: Context,
    alarmTimeName: String,
    timeInMillis: Long,
    ord: Int,
    athanGap: Long,
    alarmTime: String
) {
    val triggerTime = Calendar.getInstance()
    triggerTime.timeInMillis = timeInMillis - athanGap
    val alarmManager = context.getSystemService<AlarmManager>()

    // don't set an alarm in the past
    if (alarmManager != null && !triggerTime.before(Calendar.getInstance())) {
        logDebug(TAG, "setting alarm1 for $alarmTimeName in -> " + triggerTime.time)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARMS_BASE_ID + ord,
            Intent(context, BroadcastReceivers::class.java)
                .putExtra(KEY_EXTRA_PRAYER_KEY, alarmTimeName)
                .putExtra(KEY_EXTRA_PRAYER_TIME, alarmTime)
                .setAction(BROADCAST_ALARM),
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        when {
            Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP -> {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(
                        triggerTime.timeInMillis,
                        pendingIntent
                    ), pendingIntent
                )
//                appendLog(context, "setAlarmClock alarm for: " + triggerTime.time)
            }
            Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2 -> {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.timeInMillis,
                    pendingIntent
                )
//                appendLog(context, "setExact alarm for: " + triggerTime.time)
            }
            else -> {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.timeInMillis,
                    pendingIntent
                )
//                appendLog(context, "set alarm for: " + triggerTime.time)
            }
        }
    }
}

private fun setAlarm2(
    context: Context,
    alarmTimeName: String,
    timeInMillis: Long,
    ord: Int,
    athanGap: Long,
    alarmTime: String
) {
    val triggerTime = Calendar.getInstance()
    triggerTime.timeInMillis = timeInMillis - athanGap
    val alarmManager = context.getSystemService<AlarmManager>()

    // don't set an alarm in the past
    if (alarmManager != null && !triggerTime.before(Calendar.getInstance())) {
        logDebug(TAG, "setting alarm2 for $alarmTimeName in -> " + triggerTime.time)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARMS_BASE_ID + ord,
            Intent(context, BroadcastReceivers::class.java)
                .putExtra(KEY_EXTRA_PRAYER_KEY, alarmTimeName)
                .putExtra(KEY_EXTRA_PRAYER_TIME, alarmTime)
                .setAction(BROADCAST_ALARM),
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        when {
            Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 -> alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime.timeInMillis,
                pendingIntent
            )
            Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2 -> alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime.timeInMillis,
                pendingIntent
            )
            else -> alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime.timeInMillis,
                pendingIntent
            )
        }
    }
}

fun getOrderedCalendarEntities(
    context: Context, abbreviation: Boolean = false
): List<CalendarTypeItem> {
    applyAppLanguage(context)
    val typeTitleMap =
        context.resources.getStringArray(R.array.calendar_values)
            .map(CalendarType::valueOf)
            .zip(context.resources.getStringArray(if (abbreviation) R.array.calendar_type_abbr else R.array.calendar_type))
            .toMap()
    return getOrderedCalendarTypes().mapNotNull {
        typeTitleMap[it]?.run { CalendarTypeItem(it, this) }
    }
}

fun getDayIconResource(day: Int): Int = when (preferredDigits) {
    ARABIC_DIGITS -> DAYS_ICONS_ARABIC
    ARABIC_INDIC_DIGITS -> DAYS_ICONS_ARABIC_INDIC
    else -> DAYS_ICONS_PERSIAN
}.getOrNull(day - 1) ?: 0

fun formatCoordinate(context: Context, coordinate: Coordinate, separator: String) =
    "%s: %.7f%s%s: %.7f".format(
        Locale.getDefault(),
        context.getString(R.string.latitude), coordinate.latitude, separator,
        context.getString(R.string.longitude), coordinate.longitude
    )

// https://stackoverflow.com/a/62499553
// https://en.wikipedia.org/wiki/ISO_6709#Representation_at_the_human_interface_(Annex_D)
fun formatCoordinateISO6709(lat: Double, long: Double, alt: Double? = null) = listOf(
    abs(lat) to if (lat >= 0) "N" else "S", abs(long) to if (long >= 0) "E" else "W"
).joinToString(" ") { (degree: Double, direction: String) ->
    val minutes = ((degree - degree.toInt()) * 60).toInt()
    val seconds = ((degree - degree.toInt()) * 3600 % 60).toInt()
    "%d°%02d′%02d″%s".format(Locale.US, degree.toInt(), minutes, seconds, direction)
} + (alt?.let { " %s%.1fm".format(Locale.US, if (alt < 0) "−" else "", abs(alt)) } ?: "")

fun getCityName(context: Context, fallbackToCoord: Boolean): String =
    getCityFromPreference(context)?.let {
        when (language) {
            LANG_EN_IR, LANG_EN_US, LANG_JA -> it.en
            LANG_CKB -> it.ckb
            else -> it.fa
        }
    } ?: context.appPrefs.getString(PREF_GEOCODED_CITYNAME, null)?.takeIf { it.isNotEmpty() }
    ?: coordinate?.takeIf { fallbackToCoord }?.let { formatCoordinate(context, it, spacedComma) }
    ?: ""

fun getCoordinate(context: Context): Coordinate? =
    getCityFromPreference(context)?.coordinate ?: context.appPrefs.run {
        Coordinate(
            getString(PREF_LATITUDE, null)?.toDoubleOrNull() ?: .0,
            getString(PREF_LONGITUDE, null)?.toDoubleOrNull() ?: .0,
            getString(PREF_ALTITUDE, null)?.toDoubleOrNull() ?: .0
        ).takeIf { it.latitude != 0.0 || it.longitude != 0.0 }
        // If latitude or longitude is zero probably preference is not set yet
    }

fun CivilDate.getSpringEquinox() = Equinox.northwardEquinox(this.year).toJavaCalendar()

@StringRes
fun getPrayTimeText(athanKey: String?): Int = when (athanKey) {
    "FAJR" -> R.string.fajr
    "BFAJR" -> R.string.bfajr
    "DHUHR" -> R.string.dhuhr
    "BDHUHR" -> R.string.bdhuhr
    "ASR" -> R.string.asr
    "BASR" -> R.string.basr
    "MAGHRIB" -> R.string.maghrib
    "BMAGHRIB" -> R.string.bmaghrib
    "ISHA" -> R.string.isha
    "BISHA" -> R.string.bisha
    "SUNRISE" -> R.string.sunrise
    else -> R.string.isha
}

@DrawableRes
fun getPrayTimeImage(athanKey: String?): Int = when (athanKey) {
    "FAJR" -> R.drawable.fajr
    "DHUHR" -> R.drawable.dhuhr
    "ASR" -> R.drawable.asr
    "MAGHRIB" -> R.drawable.maghrib
    "ISHA" -> R.drawable.isha
    "SUNRISE" -> R.drawable.fajr
    else -> R.drawable.isha
}

@StyleRes
fun getThemeFromName(name: String): Int = when (name) {
    CYAN_THEME -> R.style.CyanTheme
    PURPLE_THEME -> R.style.PurpleTheme
    DEEP_PURPLE_THEME -> R.style.DeepPurpleTheme
    INDIGO_THEME -> R.style.IndigoTheme
    PINK_THEME -> R.style.PinkTheme
    GREEN_THEME -> R.style.GreenTheme
    BROWN_THEME -> R.style.BrownTheme
    NEW_BLUE_THEME -> R.style.NewBlueTheme
    DARK_THEME -> R.style.DarkTheme
    MODERN_THEME -> R.style.ModernTheme
    BLUE_THEME -> R.style.BlueTheme
    LIGHT_THEME -> R.style.LightTheme
    else -> R.style.CyanTheme
}

fun isRTL(context: Context): Boolean =
    context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

fun toggleShowDeviceCalendarOnPreference(context: Context, enable: Boolean) =
    context.appPrefs.edit { putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, enable) }

fun askForLocationPermission(activity: Activity?) {
    if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

    AlertDialog.Builder(activity)
        .setTitle(R.string.location_access)
        .setMessage(R.string.phone_location_required)
        .setPositiveButton(R.string.continue_button) { _, _ ->
            activity.requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        .show()
}

fun askForCalendarPermission(activity: Activity?) {
    if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

    AlertDialog.Builder(activity)
        .setTitle(R.string.calendar_access)
        .setMessage(R.string.phone_calendar_required)
        .setPositiveButton(R.string.continue_button) { _, _ ->
            activity.requestPermissions(
                arrayOf(Manifest.permission.READ_CALENDAR), CALENDAR_READ_PERMISSION_REQUEST_CODE
            )
        }
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        .show()
}

fun copyToClipboard(
    view: View?, label: CharSequence?, text: CharSequence?, showToastInstead: Boolean = false
) = runCatching {
    view ?: return@runCatching null
    val clipboardService = view.context.getSystemService<ClipboardManager>()
    if (clipboardService == null || label == null || text == null) return@runCatching null
    clipboardService.setPrimaryClip(ClipData.newPlainText(label, text))
    val textToShow =
        view.context.getString(R.string.date_copied_clipboard).format(text)
    if (showToastInstead)
        Toast.makeText(view.context, textToShow, Toast.LENGTH_SHORT).show()
    else
        Snackbar.make(view, textToShow, Snackbar.LENGTH_SHORT).show()
}.onFailure(logException).getOrNull().debugAssertNotNull ?: Unit

fun dateStringOfOtherCalendars(jdn: Jdn, separator: String) =
    otherCalendars.joinToString(separator) { formatDate(jdn.toCalendar(it)) }

private fun calculateDiffToChangeDate(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 1)
}.timeInMillis / 1000 + DAY_IN_SECOND - Calendar.getInstance().time.time / 1000

fun setChangeDateWorker(context: Context) {
    val remainedSeconds = calculateDiffToChangeDate()
    val changeDateWorker = OneTimeWorkRequest.Builder(UpdateWorker::class.java)
        // Use this when you want to add initial delay or schedule initial work
        // to `OneTimeWorkRequest` e.g. setInitialDelay(2, TimeUnit.HOURS)
        .setInitialDelay(remainedSeconds, TimeUnit.SECONDS)
        .build()

    WorkManager.getInstance(context).beginUniqueWork(
        CHANGE_DATE_TAG, ExistingWorkPolicy.REPLACE, changeDateWorker
    ).enqueue()
}

fun String.splitIgnoreEmpty(delim: String) = this.split(delim).filter { it.isNotEmpty() }

fun startEitherServiceOrWorker(context: Context) {
    if (goForWorker()) {
        runCatching {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UPDATE_TAG, ExistingPeriodicWorkPolicy.REPLACE,
                // An hourly task to call UpdateWorker.doWork
                PeriodicWorkRequest.Builder(UpdateWorker::class.java, 1L, TimeUnit.HOURS).build()
            )
        }.onFailure(logException).getOrNull().debugAssertNotNull
    } else {
        val isRunning = context.getSystemService<ActivityManager>()?.let { am ->
            runCatching {
                am.getRunningServices(Integer.MAX_VALUE).any {
                    ApplicationService::class.java.name == it.service.className
                }
            }.onFailure(logException).getOrNull()
        } ?: false

        if (!isRunning) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    ContextCompat.startForegroundService(
                        context,
                        Intent(context, ApplicationService::class.java)
                    )

                context.startService(Intent(context, ApplicationService::class.java))
            }.onFailure(logException)
        }
    }
}

fun getShiftWorkTitle(jdn: Jdn, abbreviated: Boolean): String {
    val shiftWorkStartingJdn = shiftWorkStartingJdn ?: return ""
    if (jdn < shiftWorkStartingJdn || shiftWorkPeriod == 0)
        return ""

    val passedDays = jdn - shiftWorkStartingJdn
    if (!shiftWorkRecurs && passedDays >= shiftWorkPeriod) return ""

    val dayInPeriod = (passedDays % shiftWorkPeriod).toInt()

    var accumulation = 0
    val type = shiftWorks.firstOrNull {
        accumulation += it.length
        accumulation > dayInPeriod
    }?.type ?: return ""

    // Skip rests on abbreviated mode
    if (shiftWorkRecurs && abbreviated && (type == "r" || type == shiftWorkTitles["r"])) return ""

    val title = shiftWorkTitles[type] ?: type
    return if (abbreviated && title.isNotEmpty()) title.substring(0, 1) +
            (if (language != LANG_AR) ZWJ else "")
    else title
}

val Context.appPrefs: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

val Context.appPrefsLite: SharedPreferences
    get() = this.getSharedPreferences("lite_prefs", Context.MODE_PRIVATE)

fun SharedPreferences.Editor.putJdn(key: String, jdn: Jdn?) {
    if (jdn == null) remove(jdn) else putLong(key, jdn.value)
}

fun SharedPreferences.getJdnOrNull(key: String): Jdn? =
    getLong(key, -1).takeIf { it != -1L }?.let(::Jdn)

val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

fun isNetworkConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}

fun createBitmapFromView(v: View): Bitmap {
    v.layoutParams = RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.WRAP_CONTENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT
    )
    v.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    v.layout(0, 0, v.measuredWidth, v.measuredHeight)
    val bitmap = Bitmap.createBitmap(
        v.measuredWidth,
        v.measuredHeight,
        Bitmap.Config.ARGB_8888
    )

    val c = Canvas(bitmap)
    v.layout(v.left, v.top, v.right, v.bottom)
    v.draw(c)
    return bitmap
}

fun createBitmapFromView2(v: View): Bitmap {
    v.layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    )
    v.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    v.layout(0, 0, v.measuredWidth, v.measuredHeight)
    val bitmap = Bitmap.createBitmap(
        v.measuredWidth,
        v.measuredHeight,
        Bitmap.Config.ARGB_8888
    )

    val c = Canvas(bitmap)
    v.layout(v.left, v.top, v.right, v.bottom)
    v.draw(c)
    return bitmap
}

fun createBitmapFromView3(v: View): Bitmap {
    v.layoutParams = LinearLayoutCompat.LayoutParams(
        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
        LinearLayoutCompat.LayoutParams.WRAP_CONTENT
    )
    v.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    v.layout(0, 0, v.measuredWidth, v.measuredHeight)
    val bitmap = Bitmap.createBitmap(
        v.measuredWidth,
        v.measuredHeight,
        Bitmap.Config.ARGB_8888
    )

    val c = Canvas(bitmap)
    v.layout(v.left, v.top, v.right, v.bottom)
    v.draw(c)
    return bitmap
}

fun hideKeyBoard(view: View) {
    val imm =
        view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as (InputMethodManager)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun animateVisibility(view: View, visible: Boolean) {
    val targetAlpha = if (visible) 1f else 0f
    view.alpha = if (visible) 0f else 1f
    if (view.alpha == targetAlpha) return
    view.visibility = View.VISIBLE
    val anim = view.animate().alpha(targetAlpha)
    if (!visible)
        anim.withEndAction { view.visibility = View.GONE }
}

fun getDayNum(month: Int, day: Int): Int {
    var result = 0
    when (month) {
        1 -> {
            result = day
        }
        2 -> {
            result = 31 + day
        }
        3 -> {
            result = 62 + day
        }
        4 -> {
            result = 93 + day
        }
        5 -> {
            result = 124 + day
        }
        6 -> {
            result = 155 + day
        }
        7 -> {
            result = 186 + day
        }
        8 -> {
            result = 216 + day
        }
        9 -> {
            result = 246 + day
        }
        10 -> {
            result = 276 + day
        }
        11 -> {
            result = 306 + day
        }
        12 -> {
            result = 336 + day
        }
    }
    return result
}

fun getDayMonthForDayOfYear(day: Int): String {
    val res: String
    var m = 0
    var d = 0
    when {
        day <= 31 -> {// 1
            m = 1
            d = day
        }
        day <= 62 -> {// 2
            m = 2
            d = day - 31
        }
        day <= 93 -> {// 3
            m = 3
            d = day - 62
        }
        day <= 124 -> {//4
            m = 4
            d = day - 93
        }
        day <= 155 -> {//5
            m = 5
            d = day - 124
        }
        day <= 186 -> {//6
            m = 6
            d = day - 155
        }
        day <= 216 -> {//7
            m = 7
            d = day - 186
        }
        day <= 246 -> {//8
            m = 8
            d = day - 216
        }
        day <= 276 -> {//9
            m = 9
            d = day - 246
        }
        day <= 306 -> {//10
            m = 10
            d = day - 276
        }
        day <= 336 -> {//11
            m = 11
            d = day - 306
        }
        day <= 366 -> {//12
            m = 12
            d = day - 336
        }
    }
    val strM: String = if (m <= 9) "0$m" else m.toString()
    val strD: String = if (d <= 9) "0$d" else d.toString()
    res = "$strM/$strD"
    return res
}

fun fixTime(time: String, min: Int): String {
    var sh: Int
    var sm: Int // source hour and min
    val t = time.split(":".toRegex()).toTypedArray()
    sh = t[0].toInt()
    sm = t[1].toInt()
    sm += min
    if (sm >= 60) {
        sm -= 60
        sh++
        if (sh >= 24) sh = 0
    } else if (sm < 0) {
        sm += 60
        sh--
        if (sh < 0) sh = 23
    }
    val nh: String = sh.toString() + ""
    val nm: String = sm.toString() + ""
//    if (sh <= 9) nh = "0$nh"
//    if (sm <= 9) nm = "0$nm"
    return "$nh:$nm"
}


fun snackMessage(view: View?, msg: String) {
    runCatching {
        view ?: return
        val snack = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
        (snack.view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView).typeface =
            getAppFont(view.context)
        snack.view.setBackgroundColor(view.context.resolveColor(R.attr.colorSnack))
        snack.show()
    }.onFailure(logException)
}

fun getTimesDirectoryPath(context: Context): String =
    context.getExternalFilesDir("times")?.absolutePath ?: ""

fun getAthansDirectoryPath(context: Context): String =
    context.getExternalFilesDir("athans")?.absolutePath ?: ""

fun askForStoragePermission(activity: Activity?) {
    if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

    AlertDialog.Builder(activity)
        .setTitle(R.string.storage_access)
        .setMessage(R.string.phone_storage_required)
        .setPositiveButton(R.string.continue_button) { _, _ ->
            activity.requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }.show()
}

fun isHaveStoragePermission(activity: Activity?): Boolean {
    return when {
        activity == null -> false
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> true
        else -> ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun getFileNameFromLink(link: String): String =
    link.substring(link.lastIndexOf('/') + 1, link.length)

@SuppressLint("SdCardPath")
fun getDatabasesDirectory(applicationContext: Context): String =
    "/data/data/${applicationContext.packageName}/databases/"

@SuppressLint("SdCardPath")
fun copyCityDB(applicationContext: Context) = runCatching {
    val dis = File("/data/data/${applicationContext.packageName}/databases")
    if (!dis.exists())
        dis.mkdir()
    val outPutFile =
        File("/data/data/${applicationContext.packageName}/databases/city.zip")
    val fileOutputStream = FileOutputStream(outPutFile)
    applicationContext.assets.open("city.zip").copyTo(fileOutputStream)
    fileOutputStream.close()
    ZipFile(
        "/data/data/${applicationContext.packageName}/databases/city.zip",
        ("@zKa6").toCharArray()
    ).extractAll("/data/data/${applicationContext.packageName}/databases/")
    File("/data/data/${applicationContext.packageName}/databases/city.zip").delete()
    applicationContext.appPrefs.edit {
        putBoolean("pref_first_city_copy", true)
    }
}.onFailure(logException).getOrNull() ?: Unit

fun toDouble(hour: Int, minute: Int): Double {
    val ashari = (minute * 100) / 60
    return if (minute < 6)
        "$hour.0$ashari".toDouble()
    else
        "$hour.$ashari".toDouble()
}

fun deleteSummerTimes(times: PrayTimes): PrayTimes {
    return PrayTimes(
        toDouble(times.imsakClock.hour - 1, times.imsakClock.minute),
        toDouble(times.fajrClock.hour - 1, times.fajrClock.minute),
        toDouble(times.sunriseClock.hour - 1, times.sunriseClock.minute),
        toDouble(times.dhuhrClock.hour - 1, times.dhuhrClock.minute),
        toDouble(times.asrClock.hour - 1, times.asrClock.minute),
        toDouble(times.sunsetClock.hour - 1, times.sunsetClock.minute),
        toDouble(times.maghribClock.hour - 1, times.maghribClock.minute),
        toDouble(times.ishaClock.hour - 1, times.ishaClock.minute),
        toDouble(
            if (times.midnightClock.hour + 1 == 25) 1 else times.midnightClock.hour + 1,
            times.midnightClock.minute
        )
    )
}

fun addSummerTimes(times: PrayTimes): PrayTimes {
    return PrayTimes(
        toDouble(times.imsakClock.hour + 1, times.imsakClock.minute),
        toDouble(times.fajrClock.hour + 1, times.fajrClock.minute),
        toDouble(times.sunriseClock.hour + 1, times.sunriseClock.minute),
        toDouble(times.dhuhrClock.hour + 1, times.dhuhrClock.minute),
        toDouble(times.asrClock.hour + 1, times.asrClock.minute),
        toDouble(times.sunsetClock.hour + 1, times.sunsetClock.minute),
        toDouble(times.maghribClock.hour + 1, times.maghribClock.minute),
        toDouble(times.ishaClock.hour + 1, times.ishaClock.minute),
        toDouble(
            if (times.midnightClock.hour + 1 == 25) 1 else times.midnightClock.hour + 1,
            times.midnightClock.minute
        )
    )
}

fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
    return runCatching {
        packageManager.getPackageInfo(packageName, 0)
        true
    }.onFailure(logException).getOrDefault(false)
}

fun appendLog(context: Context, text: String?) {
    if (text.isNullOrEmpty() || text.isNullOrBlank()) return
    val logFile = File("${context.getExternalFilesDir("logs")?.absolutePath ?: ""}/log.file")
    if (logFile.exists() && logFile.length() > 1024 * 500) logFile.delete()//if log size > 500kb delete it
    if (!logFile.exists()) {
        runCatching {
            if (!File(context.getExternalFilesDir("logs")?.absolutePath ?: "").exists())
                File(context.getExternalFilesDir("logs")?.absolutePath ?: "").mkdirs()
            logFile.createNewFile()
        }.onFailure(logException)
    }
    runCatching {
        val buf = BufferedWriter(FileWriter(logFile, true))
        buf.append(text)
        buf.newLine()
        buf.close()
    }.onFailure(logException)
}

fun bringMarketPage(activity: Activity): Unit = runCatching {
    activity.startActivity(
        Intent(Intent.ACTION_VIEW, "market://details?id=${activity.packageName}".toUri())
    )
}.onFailure(logException).getOrElse {
    runCatching {
        activity.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=${activity.packageName}".toUri()
            )
        )
    }.onFailure(logException)
}

fun createAthansSettingDB(context: Context) {
    val athanSettingDB =
        AthanSettingsDB.getInstance(context.applicationContext).athanSettingsDAO()
    if (athanSettingDB.getAllAthanSettings().isNullOrEmpty()) {
        athanSettingDB.insert(
            AthanSetting(
                "FAJR",
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAscending = false,
                athanVolume = 1,
                athanURI = "",
                alertURI = ""
            )
        )
        athanSettingDB.insert(
            AthanSetting(
                "SUNRISE",
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAscending = false,
                athanVolume = 1,
                athanURI = "",
                alertURI = ""
            )
        )
        athanSettingDB.insert(
            AthanSetting(
                "DHUHR",
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAscending = false,
                athanVolume = 1,
                athanURI = "",
                alertURI = ""
            )
        )
        athanSettingDB.insert(
            AthanSetting(
                "ASR",
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAscending = false,
                athanVolume = 1,
                athanURI = "",
                alertURI = ""
            )
        )
        athanSettingDB.insert(
            AthanSetting(
                "MAGHRIB",
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAscending = false,
                athanVolume = 1,
                athanURI = "",
                alertURI = ""
            )
        )
        athanSettingDB.insert(
            AthanSetting(
                "ISHA",
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAscending = false,
                athanVolume = 1,
                athanURI = "",
                alertURI = ""
            )
        )
    }
}

val Number.dp: Int get() = (this.toFloat() * Resources.getSystem().displayMetrics.density).toInt()


val logException = fun(e: Throwable) { logDebug(TAG, e) }

fun Toolbar.setupUpNavigation() {
    navigationIcon = DrawerArrowDrawable(context).apply { progress = 1f }
    setNavigationContentDescription(androidx.navigation.ui.R.string.nav_app_bar_navigate_up_description)
    setNavigationOnClickListener { findNavController().navigateUp() }
}

@ColorInt
fun Context.resolveColor(attr: Int) = TypedValue().let {
    theme.resolveAttribute(attr, it, true)
    ContextCompat.getColor(this, it.resourceId)
}

fun Flow.addViewsToFlow(viewList: List<View>) {
    val parentView = (this.parent as? ViewGroup).debugAssertNotNull ?: return
    this.referencedIds = viewList.map {
        View.generateViewId().also { id ->
            it.id = id
            parentView.addView(it)
        }
    }.toIntArray()
}

inline fun Preference.setOnClickListener(crossinline listener: () -> Unit) {
    this.setOnPreferenceClickListener {
        listener()
        true // means it captures the click event
    }
}

fun <T> listOf31Items(
    x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T,
    x13: T, x14: T, x15: T, x16: T, x17: T, x18: T, x19: T, x20: T, x21: T, x22: T,
    x23: T, x24: T, x25: T, x26: T, x27: T, x28: T, x29: T, x30: T, x31: T
) = listOf(
    x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12,
    x13, x14, x15, x16, x17, x18, x19, x20, x21, x22,
    x23, x24, x25, x26, x27, x28, x29, x30, x31
)

fun <T> listOf12Items(
    x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T, x11: T, x12: T
) = listOf(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12)

fun <T> listOf7Items(
    x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T
) = listOf(x1, x2, x3, x4, x5, x6, x7)

fun logDebug(tag: String, msg: String) = Log.e(tag, msg)
fun logDebug(tag: String, exception: Throwable) = Log.e(tag, exception.message, exception)

inline val <T> T.debugAssertNotNull: T
    inline get() = this ?: throw NullPointerException("A debug only assert has happened")
