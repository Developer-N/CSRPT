package ir.namoo.religiousprayers.utils


import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.preference.PreferenceManager
import com.google.android.material.circularreveal.CircularRevealCompat
import com.google.android.material.circularreveal.CircularRevealWidget
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
import ir.namoo.religiousprayers.entities.CalendarTypeItem
import ir.namoo.religiousprayers.entities.CityItem
import ir.namoo.religiousprayers.praytimes.PrayTimeProvider
import ir.namoo.religiousprayers.service.ApplicationService
import ir.namoo.religiousprayers.service.BroadcastReceivers
import net.lingala.zip4j.ZipFile
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

// This should be called before any use of Utils on the activity and services
fun initUtils(context: Context) {
    updateStoredPreference(context)
    applyAppLanguage(context)
    loadLanguageResource(context)
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

fun formatDate(date: AbstractDate): String = if (numericalDatePreferred)
    (toLinearDate(date) + " " + getCalendarNameAbbr(date)).trim()
else when (language) {
    LANG_CKB -> "%sی %sی %s"
    else -> "%s %s %s"
}.format(formatNumber(date.dayOfMonth), getMonthName(date), formatNumber(date.year))

fun isNonArabicScriptSelected() = when (language) {
    LANG_EN_US, LANG_JA -> true
    else -> false
}

// en-US and ja are our only real LTR locales for now
fun isLocaleRTL(): Boolean = when (language) {
    LANG_EN_US, LANG_JA -> false
    else -> true
}

fun formatNumber(number: Int): String = formatNumber(number.toString())

fun formatNumber(number: String): String = when (preferredDigits) {
    ARABIC_DIGITS -> number
    else -> number.toCharArray().map {
        if (Character.isDigit(it)) preferredDigits[Character.getNumericValue(it)] else it
    }.joinToString("")
}

// https://stackoverflow.com/a/52557989
fun <T> circularRevealFromMiddle(circularRevealWidget: T) where T : View?, T : CircularRevealWidget {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        circularRevealWidget.post {
            val viewWidth = circularRevealWidget.width
            val viewHeight = circularRevealWidget.height

            val viewDiagonal =
                sqrt((viewWidth * viewWidth + viewHeight * viewHeight).toDouble()).toInt()

            AnimatorSet().apply {
                playTogether(
                    CircularRevealCompat.createCircularReveal(
                        circularRevealWidget,
                        (viewWidth / 2).toFloat(), (viewHeight / 2).toFloat(),
                        10f, (viewDiagonal / 2).toFloat()
                    ),
                    ObjectAnimator.ofArgb(
                        circularRevealWidget,
                        CircularRevealWidget.CircularRevealScrimColorProperty
                            .CIRCULAR_REVEAL_SCRIM_COLOR,
                        Color.GRAY, Color.TRANSPARENT
                    )
                )
                duration = 500
            }.start()
        }
    }
}

fun getCalendarNameAbbr(date: AbstractDate): String {
    if (calendarTypesTitleAbbr.size < 3) return ""
    // It should match with calendar_type array
    return when (date) {
        is PersianDate -> calendarTypesTitleAbbr[0]
        is IslamicDate -> calendarTypesTitleAbbr[1]
        is CivilDate -> calendarTypesTitleAbbr[2]
        else -> ""
    }
}

fun getThemeFromPreference(context: Context, prefs: SharedPreferences): String =
    prefs.getString(PREF_THEME, null)
        ?: if (isNightModeEnabled(context)) DARK_THEME else CYAN_THEME

fun getEnabledCalendarTypes(): List<CalendarType> = listOf(mainCalendar) + otherCalendars

fun loadApp(context: Context) {
//    if (goForWorker()) return

    try {
        val alarmManager = context.getSystemService<AlarmManager>() ?: return

        val startTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 1)
            add(Calendar.DATE, 1)
        }

        val dailyPendingIntent = PendingIntent.getBroadcast(
            context, LOAD_APP_ID,
            Intent(context, BroadcastReceivers::class.java).setAction(BROADCAST_RESTART_APP),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.set(AlarmManager.RTC, startTime.timeInMillis, dailyPendingIntent)

        // There are simpler triggers on older Androids like SCREEN_ON but they
        // are not available anymore, lets register an hourly alarm for >= Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val threeHoursPendingIntent = PendingIntent.getBroadcast(
                context,
                THREE_HOURS_APP_ID,
                Intent(context, BroadcastReceivers::class.java).setAction(BROADCAST_UPDATE_APP),
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.setInexactRepeating(
                AlarmManager.RTC,
                // Start from one hour from now
                Calendar.getInstance().timeInMillis + TimeUnit.HOURS.toMillis(1),
                TimeUnit.HOURS.toMillis(3), threeHoursPendingIntent
            )
        }
    } catch (e: Exception) {
        Log.e(TAG, "loadApp fail", e)
    }
}

fun getOrderedCalendarTypes(): List<CalendarType> = getEnabledCalendarTypes().let {
    it + (CalendarType.values().toList() - it)
}

fun loadAlarms(context: Context) {
    val prefString = context.appPrefs.getString(PREF_ATHAN_ALARM, null)?.trim() ?: ""
    Log.d(TAG, "reading and loading all alarms from prefs: $prefString")

    if (coordinate != null && prefString.isNotEmpty()) {
        val athanGap =
            ((context.appPrefs.getString(PREF_ATHAN_GAP, null)?.toDoubleOrNull() ?: .0)
                    * 60.0 * 1000.0).toLong()

        val prayTimes = PrayTimeProvider.calculate(
            calculationMethod,
            Date(), coordinate!!, context
        )
        // convert spacedComma separated string to a set
        prefString.split(",").toSet().forEachIndexed { i, name ->
            val alarmTime: Clock = when (name) {
                "DHUHR" -> prayTimes.dhuhrClock
                "ASR" -> prayTimes.asrClock
                "MAGHRIB" -> prayTimes.maghribClock
                "ISHA" -> prayTimes.ishaClock
                "FAJR" -> prayTimes.fajrClock
                "SUNRISE" -> prayTimes.sunriseClock
                // a better to have default
                else -> prayTimes.fajrClock
            }

            setAlarm(context, name, Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarmTime.hour)
                set(Calendar.MINUTE, alarmTime.minute)
                set(Calendar.SECOND, 0)
            }.timeInMillis, i, athanGap, alarmTime.toFormattedString())
        }
        if (context.appPrefs.getBoolean(PREF_ALARM_BEFORE_FAJR_ENABLE, false)) {
            val alarmTime: Clock = Clock.fromInt(
                prayTimes.fajrClock.toInt() - context.appPrefs.getInt(
                    PREF_ALARM_BEFORE_FAJR_MIN, DEFAULT_ALARM_BEFORE_FAJR
                )
            )

            setAlarm(
                context,
                context.resources.getString(R.string.alarm_before_fajr_name),
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, alarmTime.hour)
                    set(Calendar.MINUTE, alarmTime.minute)
                    set(Calendar.SECOND, 0)
                }.timeInMillis,
                7,
                athanGap, alarmTime.toFormattedString()
            )
        }
    }
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
        Log.e(TAG, "setting alarm for: " + triggerTime.time)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARMS_BASE_ID + ord,
            Intent(context, BroadcastReceivers::class.java)
                .putExtra(KEY_EXTRA_PRAYER_KEY, alarmTimeName)
                .putExtra(KEY_EXTRA_PRAYER_TIME, alarmTime)
                .setAction(BROADCAST_ALARM),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        when {
            Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 ->
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.timeInMillis,
                    pendingIntent
                )
            Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2 ->
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.timeInMillis,
                    pendingIntent
                )
            else ->
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.timeInMillis,
                    pendingIntent
                )
        }
    }
}

fun getOrderedCalendarEntities(context: Context): List<CalendarTypeItem> {
    applyAppLanguage(context)
    val typeTitleMap = context.resources.getStringArray(R.array.calendar_values)
        .map(CalendarType::valueOf)
        .zip(context.resources.getStringArray(R.array.calendar_type))
        .toMap()
    return getOrderedCalendarTypes().mapNotNull {
        typeTitleMap[it]?.run { CalendarTypeItem(it, this) }
    }
}

fun getDayIconResource(day: Int): Int = try {
    when (preferredDigits) {
        ARABIC_DIGITS -> DAYS_ICONS_ARABIC[day]
        ARABIC_INDIC_DIGITS -> DAYS_ICONS_ARABIC_INDIC[day]
        else -> DAYS_ICONS_PERSIAN[day]
    }
} catch (e: IndexOutOfBoundsException) {
    Log.e(TAG, "No such field is available", e)
    0
}

fun readRawResource(context: Context, @RawRes res: Int) =
    context.resources.openRawResource(res).use { String(it.readBytes()) }

fun formatCoordinate(context: Context, coordinate: Coordinate, separator: String) =
    "%s: %.4f%s%s: %.4f".format(
        Locale.getDefault(),
        context.getString(R.string.latitude), coordinate.latitude, separator,
        context.getString(R.string.longitude), coordinate.longitude
    )

fun getCityName(context: Context, fallbackToCoord: Boolean): String =
    getCityFromPreference(context)?.let {
        when (language) {
            LANG_EN_IR, LANG_EN_US, LANG_JA -> it.en
            LANG_CKB -> it.ckb
            else -> it.fa
        }
    } ?: context.appPrefs.getString(PREF_GEOCODED_CITYNAME, null)?.takeUnless { it.isEmpty() }
    ?: coordinate?.takeIf { fallbackToCoord }?.let { formatCoordinate(context, it, spacedComma) }
    ?: ""

fun getCoordinate(context: Context): Coordinate? =
    getCityFromPreference(context)?.coordinate ?: context.appPrefs.run {
        Coordinate(
            getString(PREF_LATITUDE, null)?.toDoubleOrNull() ?: .0,
            getString(PREF_LONGITUDE, null)?.toDoubleOrNull() ?: .0,
            getString(PREF_ALTITUDE, null)?.toDoubleOrNull() ?: .0
        ).takeUnless { it.latitude == 0.0 && it.longitude == 0.0 }
        // If latitude or longitude is zero probably preference is not set yet
    }

fun getTodayOfCalendar(calendar: CalendarType) = getDateFromJdnOfCalendar(calendar, getTodayJdn())

fun getTodayJdn(): Long = calendarToCivilDate(makeCalendarFromDate(Date())).toJdn()

fun getSpringEquinox(jdn: Long) =
    makeCalendarFromDate(Equinox.northwardEquinox(CivilDate(jdn).year))

@StringRes
fun getPrayTimeText(athanKey: String?): Int = when (athanKey) {
    "FAJR" -> R.string.fajr
    "DHUHR" -> R.string.dhuhr
    "ASR" -> R.string.asr
    "MAGHRIB" -> R.string.maghrib
    "ISHA" -> R.string.isha
    "SUNRISE" -> R.string.sunrise
    "BFAJR" -> R.string.alarm_before_fajr
    else -> R.string.isha
}

fun getDateFromJdnOfCalendar(calendar: CalendarType, jdn: Long): AbstractDate = when (calendar) {
    CalendarType.ISLAMIC -> IslamicDate(jdn)
    CalendarType.GREGORIAN -> CivilDate(jdn)
    CalendarType.SHAMSI -> PersianDate(jdn)
}

@StyleRes
fun getThemeFromName(name: String): Int = when (name) {
    CYAN_THEME -> R.style.CyanTheme
    DARK_THEME -> R.style.DarkTheme
    MODERN_THEME -> R.style.ModernTheme
    BLUE_THEME -> R.style.BlueTheme
    LIGHT_THEME -> R.style.LightTheme
    else -> R.style.CyanTheme
}

fun isRTL(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    else false

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
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }.show()
}

fun askForCalendarPermission(activity: Activity?) {
    if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

    AlertDialog.Builder(activity)
        .setTitle(R.string.calendar_access)
        .setMessage(R.string.phone_calendar_required)
        .setPositiveButton(R.string.continue_button) { _, _ ->
            activity.requestPermissions(
                arrayOf(Manifest.permission.READ_CALENDAR),
                CALENDAR_READ_PERMISSION_REQUEST_CODE
            )
        }
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }.show()
}

fun copyToClipboard(view: View?, label: CharSequence?, text: CharSequence?) {
    view ?: return
    val clipboardService = view.context.getSystemService<ClipboardManager>()

    if (clipboardService == null || label == null || text == null) return

    clipboardService.setPrimaryClip(ClipData.newPlainText(label, text))
    Snackbar.make(
        view, view.context.getString(R.string.date_copied_clipboard).format(text),
        Snackbar.LENGTH_SHORT
    ).show()
}

fun dateStringOfOtherCalendars(jdn: Long, separator: String) =
    otherCalendars.joinToString(separator) { formatDate(getDateFromJdnOfCalendar(it, jdn)) }


fun String.splitIgnoreEmpty(delim: String) = this.split(delim).filter { it.isNotEmpty() }

fun startEitherServiceOrWorker(context: Context) {
    val isRunning = context.getSystemService<ActivityManager>()?.let { am ->
        try {
            am.getRunningServices(Integer.MAX_VALUE).any {
                ApplicationService::class.java.name == it.service.className
            }
        } catch (e: Exception) {
            Log.e(TAG, "startEitherServiceOrWorker service's first part fail", e)
            false
        }
    } ?: false

    if (!isRunning) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, ApplicationService::class.java)
                )

            context.startService(Intent(context, ApplicationService::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "startEitherServiceOrWorker service's second part fail", e)
        }
    }
}

fun getShiftWorkTitle(jdn: Long, abbreviated: Boolean): String {
    if (shiftWorkStartingJdn == -1L || jdn < shiftWorkStartingJdn || shiftWorkPeriod == 0)
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
    return if (abbreviated && title.isNotEmpty()) title.substring(0, 1) + when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && language != LANG_AR -> ZWJ
        else -> ""
    } else title
}

fun getAllCities(context: Context, needsSort: Boolean): List<CityItem> {
    val result = mutableListOf<CityItem>()
    try {
        fun JSONObject.forEach(f: (String, JSONObject) -> Unit) =
            this.keys().asSequence().forEach { f(it, this.getJSONObject(it)) }

        JSONObject(readRawResource(context, R.raw.cities)).forEach { countryCode, country ->
            val countryEn = country.getString("en")
            val countryFa = country.getString("fa")
            val countryCkb = country.getString("ckb")
            val countryAr = country.getString("ar")

            country.getJSONObject("cities").forEach { key, city ->
                result.add(
                    CityItem(
                        key = key,
                        en = city.getString("en"), fa = city.getString("fa"),
                        ckb = city.getString("ckb"), ar = city.getString("ar"),
                        countryCode = countryCode,
                        countryEn = countryEn, countryFa = countryFa,
                        countryCkb = countryCkb, countryAr = countryAr,
                        coordinate = Coordinate(
                            city.getDouble("latitude"),
                            city.getDouble("longitude"),
                            // Don't Consider elevation for Iran
                            if (countryCode == "ir") 0.0 else city.getDouble("elevation")
                        )
                    )
                )
            }
        }
    } catch (e: JSONException) {
        e.printStackTrace()
    }

    if (!needsSort) return result

    val irCodeOrder = listOf("zz", "ir", "af", "iq")
    val afCodeOrder = listOf("zz", "af", "ir", "iq")
    val arCodeOrder = listOf("zz", "iq", "ir", "af")

    fun getCountryCodeOrder(countryCode: String): Int =
        when (language) {
            LANG_FA_AF, LANG_PS -> afCodeOrder.indexOf(countryCode)
            LANG_AR -> arCodeOrder.indexOf(countryCode)
            LANG_FA, LANG_GLK, LANG_AZB -> irCodeOrder.indexOf(countryCode)
            else -> irCodeOrder.indexOf(countryCode)
        }

    fun prepareForArabicSort(text: String): String =
        text
            .replace("ی", "ي")
            .replace("ک", "ك")
            .replace("گ", "كی")
            .replace("ژ", "زی")
            .replace("چ", "جی")
            .replace("پ", "بی")
            .replace("ڕ", "ری")
            .replace("ڵ", "لی")
            .replace("ڤ", "فی")
            .replace("ۆ", "وی")
            .replace("ێ", "یی")
            .replace("ھ", "نی")
            .replace("ە", "هی")

    return result.sortedWith(kotlin.Comparator { l, r ->
        if (l.key == "") return@Comparator -1

        if (r.key == DEFAULT_CITY) return@Comparator 1

        val compare = getCountryCodeOrder(l.countryCode) - getCountryCodeOrder(r.countryCode)
        if (compare != 0) return@Comparator compare

        when (language) {
            LANG_EN_US, LANG_JA, LANG_EN_IR -> l.en.compareTo(r.en)
            LANG_AR -> l.ar.compareTo(r.ar)
            LANG_CKB -> prepareForArabicSort(l.ckb).compareTo(prepareForArabicSort(r.ckb))
            else -> prepareForArabicSort(l.fa).compareTo(prepareForArabicSort(r.fa))
        }
    })
}

val Context.appPrefs: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

fun getColorFromAttr(context: Context, @AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    val theme: Resources.Theme = context.theme
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

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
    val strM: String
    val strD: String
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
    strM = if (m <= 9) "0$m" else m.toString()
    strD = if (d <= 9) "0$d" else d.toString()
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
    val nh: String
    val nm: String
    nh = sh.toString() + ""
    nm = sm.toString() + ""
    return "$nh:$nm"
}


fun snackMessage(view: View?, msg: String) {
    if (view != null) {
        val snack = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
        (snack.view
            .findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView).typeface =
            getAppFont(view.context)
        snack.view.setBackgroundColor(getColorFromAttr(view.context, R.attr.colorSnack))
        snack.show()
    }
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

fun updateAthanInPref(context: Context, name: String) {
    var alarms = context.appPrefs.getString(PREF_ATHAN_ALARM, "")
    val existAlarms = alarms!!.splitIgnoreEmpty(",")

    if (existAlarms.contains(name)) {//delete name
        alarms = ""
        var temp = ""
        for (s in existAlarms)
            if (s != name)
                temp += "$s,"
        for (i in 0 until temp.length - 1)
            alarms += temp[i]
    } else {// add name
        alarms += if (alarms.isNotEmpty())
            ",$name"
        else {
            name
        }
    }
    context.appPrefs.edit {
        putString(PREF_ATHAN_ALARM, alarms)
        apply()
    }
}

fun isExistAthanInPrefs(context: Context, name: String): Boolean {
    return when {
        context.appPrefs.getString(PREF_ATHAN_ALARM, "") == null -> false
        else -> context.appPrefs.getString(PREF_ATHAN_ALARM, "")!!.contains(name)
    }
}

fun getFileNameFromLink(link: String): String =
    link.substring(link.lastIndexOf('/') + 1, link.length)

@SuppressLint("SdCardPath")
fun getDatabasesDirectory(applicationContext: Context): String =
    "/data/data/${applicationContext.packageName}/databases/"

@SuppressLint("SdCardPath")
fun copyCityDB(applicationContext: Context) {
    try {
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
    } catch (ex: Exception) {

    }
}

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
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (ex: Exception) {
        false
    }
}
