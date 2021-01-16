package ir.namoo.religiousprayers.utils

import android.content.Context
import android.media.AudioManager
import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.content.getSystemService
import com.google.android.material.snackbar.Snackbar
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.religiousprayers.*
import ir.namoo.religiousprayers.entities.*
import ir.namoo.religiousprayers.praytimes.PrayTimeProvider
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*


const val TAG = "NAMOO"
const val CHANGE_DATE_TAG = "changeDate"
const val UPDATE_TAG = "update"
const val TWO_SECONDS_IN_MILLIS = 2000L
const val HALF_SECOND_IN_MILLIS = 500L
const val DAY_IN_SECOND = 86400L
const val DAY_IN_MILLIS = 86400000L
val monthNameEmptyList = (1..12).map { "" }.toList()
var persianMonths = monthNameEmptyList
    private set
var islamicMonths = monthNameEmptyList
    private set
var gregorianMonths = monthNameEmptyList
    private set
val weekDaysEmptyList = (1..7).map { "" }.toList()
var weekDays = weekDaysEmptyList
    private set
var weekDaysInitials = weekDaysEmptyList
    private set
var preferredDigits = PERSIAN_DIGITS
    private set
var clockIn24 = DEFAULT_WIDGET_IN_24
    private set
var isForcedIranTimeEnabled = DEFAULT_IRAN_TIME
    private set
var isNotifyDateOnLockScreen = DEFAULT_NOTIFY_DATE_LOCK_SCREEN
    private set
var isWidgetClock = DEFAULT_WIDGET_CLOCK
    private set
var isNotifyDate = DEFAULT_NOTIFY_DATE
    private set
var selectedWidgetTextColor = DEFAULT_SELECTED_WIDGET_TEXT_COLOR
    private set
var selectedWidgetNextAthanTextColor = DEFAULT_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR
    private set
var selectedWidgetBackgroundColor = DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
    private set
var calculationMethod = CalculationMethod.valueOf(DEFAULT_PRAY_TIME_METHOD)
    private set
var asrMethod = CalculationMethod.AsrJuristics.Standard
var language = DEFAULT_APP_LANGUAGE
    private set
    get() = if (field.isEmpty()) DEFAULT_APP_LANGUAGE else field
var coordinate: Coordinate? = null
    private set
var mainCalendar = CalendarType.SHAMSI
    private set
var otherCalendars = listOf(CalendarType.GREGORIAN, CalendarType.ISLAMIC)
    private set
var spacedComma = "، "
    private set
var isShowWeekOfYearEnabled = false
    private set
var isCenterAlignWidgets = false
    private set
var weekStartOffset = 0
    private set
var weekEnds = BooleanArray(7)
    private set
var isShowDeviceCalendarEvents = false
    private set
var whatToShowOnWidgets = emptySet<String>()
    private set
var isAstronomicalFeaturesEnabled = false
    private set

@StyleRes
var appTheme = R.style.LightTheme
    private set
var isTalkBackEnabled = false
    private set
var isHighTextContrastEnabled = false
    private set
var prayTimes: PrayTimes? = null
    private set
var cachedCityKey = ""
    private set
var cachedCity: CityItem? = null
    private set
var shiftWorkTitles = emptyMap<String, String>()
    private set
var shiftWorkStartingJdn = -1L
    private set
var shiftWorkRecurs = true
    private set
var shiftWorks = emptyList<ShiftWorkRecord>()
    private set
var shiftWorkPeriod = 0
    private set
var isIranHolidaysEnabled = true
    private set
var amString = DEFAULT_AM
    private set
var pmString = DEFAULT_PM
    private set
var latestToastShowTime: Long = -1
    private set
var numericalDatePreferred = false
    private set
var calendarTypesTitleAbbr = emptyList<String>()
    private set
var allEnabledEvents: List<CalendarEvent<*>> = emptyList()
    private set
var persianCalendarEvents: PersianCalendarEventsStore = emptyEventsStore()
    private set
var islamicCalendarEvents: IslamicCalendarEventsStore = emptyEventsStore()
    private set
var gregorianCalendarEvents: GregorianCalendarEventsStore = emptyEventsStore()
    private set

fun loadEvents(context: Context) {
    val enabledTypes =
        context.appPrefs.getStringSet(PREF_HOLIDAY_TYPES, null) ?: setOf("iran_holidays")

    val afghanistanHolidays = "afghanistan_holidays" in enabledTypes
    val afghanistanOthers = "afghanistan_others" in enabledTypes
    val iranHolidays = "iran_holidays" in enabledTypes
    val iranIslamic = "iran_islamic" in enabledTypes
    val iranAncient = "iran_ancient" in enabledTypes
    val iranOthers = "iran_others" in enabledTypes
    val international = "international" in enabledTypes

    isIranHolidaysEnabled = iranHolidays

    IslamicDate.useUmmAlQura = false
    if (!iranHolidays) {
        if (afghanistanHolidays) {
            IslamicDate.useUmmAlQura = true
        }
        when (language) {
            LANG_FA_AF, LANG_PS, LANG_UR, LANG_AR, LANG_CKB, LANG_EN_US, LANG_JA ->
                IslamicDate.useUmmAlQura = true
        }
    }

    // Now that we are configuring converter's algorithm above, lets set the offset also

    IslamicDate.islamicOffset = context.appPrefs
        .getString(PREF_ISLAMIC_OFFSET, null)?.toIntOrNull() ?: 0

    try {
        val allEnabledEventsBuilder = ArrayList<CalendarEvent<*>>()

        val allTheEvents = JSONObject(readRawResource(context, R.raw.events))

        // https://stackoverflow.com/a/36188796
        fun JSONObject.getArray(key: String): Sequence<JSONObject> =
            getJSONArray(key).run { (0 until length()).asSequence().map { get(it) as JSONObject } }

        persianCalendarEvents = allTheEvents.getArray("Persian Calendar").mapNotNull {
            val month = it.getInt("month")
            val day = it.getInt("day")
            val year = if (it.has("year")) it.getInt("year") else -1
            var title = it.getString("title")
            var holiday = it.getBoolean("holiday")

            var addOrNot = false
            val type = it.getString("type")

            if (holiday && iranHolidays &&
                (type == "Islamic Iran" || type == "Iran" || type == "Ancient Iran")
            ) addOrNot = true

            if (!iranHolidays && type == "Islamic Iran") holiday = false
            if (iranIslamic && type == "Islamic Iran") addOrNot = true
            if (iranAncient && type == "Ancient Iran") addOrNot = true
            if (iranOthers && type == "Iran") addOrNot = true
            if (afghanistanHolidays && type == "Afghanistan" && holiday) addOrNot = true
            if (!afghanistanHolidays && type == "Afghanistan") holiday = false
            if (afghanistanOthers && type == "Afghanistan") addOrNot = true

            if (addOrNot) {
                title += " ("
                if (holiday && afghanistanHolidays && iranHolidays) {
                    if (type == "Islamic Iran" || type == "Iran")
                        title += "ایران، "
                    else if (type == "Afghanistan")
                        title += "افغانستان، "
                }
                title += formatDayAndMonth(day, persianMonths[month - 1]) + ")"
                PersianCalendarEvent(PersianDate(year, month, day), title, holiday)
            } else null
        }.toList().also { allEnabledEventsBuilder.addAll(it) }.toEventsStore()

        islamicCalendarEvents = allTheEvents.getArray("Hijri Calendar").mapNotNull {
            val month = it.getInt("month")
            val day = it.getInt("day")
            var title = it.getString("title")
            var holiday = it.getBoolean("holiday")

            var addOrNot = false
            val type = it.getString("type")

            if (afghanistanHolidays && holiday && type == "Islamic Afghanistan") addOrNot = true
            if (!afghanistanHolidays && type == "Islamic Afghanistan") holiday = false
            if (afghanistanOthers && type == "Islamic Afghanistan") addOrNot = true
            if (iranHolidays && holiday && type == "Islamic Iran") addOrNot = true
            if (!iranHolidays && type == "Islamic Iran") holiday = false
            if (iranIslamic && type == "Islamic Iran") addOrNot = true
            if (iranOthers && type == "Islamic Iran") addOrNot = true

            if (addOrNot) {
                title += " ("
                if (holiday && afghanistanHolidays && iranHolidays) {
                    if (type == "Islamic Iran")
                        title += "ایران، "
                    else if (type == "Islamic Afghanistan")
                        title += "افغانستان، "
                }
                title += formatDayAndMonth(day, islamicMonths[month - 1]) + ")"

                IslamicCalendarEvent(IslamicDate(-1, month, day), title, holiday)
            } else null
        }.toList().also { allEnabledEventsBuilder.addAll(it) }.toEventsStore()

        gregorianCalendarEvents = allTheEvents.getArray("Gregorian Calendar").mapNotNull {
            val month = it.getInt("month")
            val day = it.getInt("day")
            val title = it.getString("title")

            val isOfficialInIran = it.has("type") && it.getString("type") == "Iran"
            val isOfficialInAfghanistan = it.has("type") && it.getString("type") == "Afghanistan"
            val isOthers = !isOfficialInIran && !isOfficialInAfghanistan

            if (
                (isOthers && international) ||
                (isOfficialInIran && (iranOthers || international)) ||
                (isOfficialInAfghanistan && afghanistanOthers)
            ) {
                GregorianCalendarEvent(
                    CivilDate(-1, month, day),
                    title + " (" + formatDayAndMonth(day, gregorianMonths[month - 1]) + ")",
                    false
                )
            } else null
        }.toList().also { allEnabledEventsBuilder.addAll(it) }.toEventsStore()

        allEnabledEvents = allEnabledEventsBuilder
    } catch (e: JSONException) {
        e.printStackTrace()
    }
}

fun loadLanguageResource(context: Context) = try {
    val messages = JSONObject(
        readRawResource(
            context, when (language) {
                LANG_FA_AF -> R.raw.faaf
                LANG_PS -> R.raw.ps
                LANG_GLK -> R.raw.glk
                LANG_AR -> R.raw.ar
                LANG_CKB -> R.raw.ckb
                LANG_UR -> R.raw.ur
                LANG_EN_US -> R.raw.en
                LANG_JA -> R.raw.ja
                LANG_AZB -> R.raw.azb
                LANG_EN_IR, LANG_FA -> R.raw.fa
                else -> R.raw.fa
            }
        )
    )

    fun JSONArray.toStringList() = (0 until length()).map { getString(it) }

    persianMonths = messages.getJSONArray("PersianCalendarMonths").toStringList()
    islamicMonths = messages.getJSONArray("IslamicCalendarMonths").toStringList()
    gregorianMonths = messages.getJSONArray("GregorianCalendarMonths").toStringList()
    weekDays = messages.getJSONArray("WeekDays").toStringList()
    weekDaysInitials = when (language) {
        LANG_AR, LANG_AZB -> messages.getJSONArray("WeekDaysInitials").toStringList()
        else -> weekDays.map { it.substring(0, 1) }
    }
} catch (e: JSONException) {
    e.printStackTrace()
    persianMonths = monthNameEmptyList
    islamicMonths = monthNameEmptyList
    gregorianMonths = monthNameEmptyList
    weekDays = weekDaysEmptyList
    weekDaysInitials = weekDaysEmptyList
}

@StringRes
fun getNextOwghatTimeId(current: Clock, dateHasChanged: Boolean, context: Context): Int {
    coordinate ?: return 0
    if (prayTimes == null || dateHasChanged)
        prayTimes = PrayTimeProvider.calculate(calculationMethod, Date(), coordinate!!, context)
    val now = current.toInt()
    return prayTimes?.run {
        if (now < fajrClock.toInt()) 0
        else if (now >= fajrClock.toInt() && now < sunriseClock.toInt()) 1
        else if (now >= sunriseClock.toInt() && now < dhuhrClock.toInt()) 2
        else if (now >= dhuhrClock.toInt() && now < asrClock.toInt()) 3
        else if (now >= asrClock.toInt() && now < maghribClock.toInt()) 4
        else if (now >= maghribClock.toInt() && now < ishaClock.toInt()) 5
        else 6
    } ?: 0
}

fun getClockFromStringId(@StringRes stringId: Int, context: Context): Clock {
    if (prayTimes == null && coordinate != null)
        prayTimes = PrayTimeProvider.calculate(calculationMethod, Date(), coordinate!!, context)

    return prayTimes?.run {
        when (stringId) {
            R.string.imsak -> imsakClock
            R.string.fajr -> fajrClock
            R.string.sunrise -> sunriseClock
            R.string.dhuhr -> dhuhrClock
            R.string.asr -> asrClock
            R.string.sunset -> sunsetClock
            R.string.maghrib -> maghribClock
            R.string.isha -> ishaClock
            R.string.midnight -> midnightClock
            else -> Clock.fromInt(0)
        }
    } ?: Clock.fromInt(0)
}

fun getClockStringFromId(stringId: Int): Int =
    when (stringId) {
        0 -> R.string.fajr
        1 -> R.string.sunrise
        2 -> R.string.dhuhr
        3 -> R.string.asr
        4 -> R.string.maghrib
        5 -> R.string.isha
        else -> R.string.fajr
    }


fun getCityFromPreference(context: Context): CityItem? {
    val key = context.appPrefs.getString(PREF_SELECTED_LOCATION, null)
        ?.takeUnless { it.isEmpty() || it == DEFAULT_CITY } ?: return null

    if (key == cachedCityKey)
        return cachedCity

    // cache last query even if no city available under the key, useful in case invalid
    // value is somehow inserted on the preference
    cachedCityKey = key
    cachedCity = getAllCities(context, false).firstOrNull { it.key == key }
    return cachedCity
}

fun a11yAnnounceAndClick(view: View, @StringRes resId: Int) {
    if (!isTalkBackEnabled) return

    val context = view.context ?: return

    val now = System.currentTimeMillis()
    if (now - latestToastShowTime > TWO_SECONDS_IN_MILLIS) {
        Snackbar.make(view, resId, Snackbar.LENGTH_SHORT).show()
        // https://stackoverflow.com/a/29423018
        context.getSystemService<AudioManager>()?.playSoundEffect(AudioManager.FX_KEY_CLICK)
        latestToastShowTime = now
    }
}

private fun getOnlyLanguage(string: String): String = string.replace("-(IR|AF|US)".toRegex(), "")

fun updateStoredPreference(context: Context) {
    val prefs = context.appPrefs

    language = prefs.getString(PREF_APP_LANGUAGE, null) ?: DEFAULT_APP_LANGUAGE

    preferredDigits =
        if (prefs.getBoolean(PREF_PERSIAN_DIGITS, DEFAULT_PERSIAN_DIGITS)) when (language) {
            LANG_AR, LANG_CKB -> ARABIC_INDIC_DIGITS
            LANG_JA -> CJK_DIGITS
            else -> PERSIAN_DIGITS
        }
        else ARABIC_DIGITS

    clockIn24 = prefs.getBoolean(PREF_WIDGET_IN_24, DEFAULT_WIDGET_IN_24)
    isForcedIranTimeEnabled = prefs.getBoolean(PREF_IRAN_TIME, DEFAULT_IRAN_TIME)
    isNotifyDateOnLockScreen = prefs.getBoolean(
        PREF_NOTIFY_DATE_LOCK_SCREEN,
        DEFAULT_NOTIFY_DATE_LOCK_SCREEN
    )
    isWidgetClock = prefs.getBoolean(PREF_WIDGET_CLOCK, DEFAULT_WIDGET_CLOCK)
    isNotifyDate = prefs.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)
    isCenterAlignWidgets = prefs.getBoolean("CenterAlignWidgets", false)

    selectedWidgetTextColor =
        prefs.getString(PREF_SELECTED_WIDGET_TEXT_COLOR, null)
            ?: DEFAULT_SELECTED_WIDGET_TEXT_COLOR

    selectedWidgetNextAthanTextColor =
        prefs.getString(PREF_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR, null)
            ?: DEFAULT_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR

    selectedWidgetBackgroundColor =
        prefs.getString(PREF_SELECTED_WIDGET_BACKGROUND_COLOR, null)
            ?: DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR

    // We were using "Jafari" method but later found out Tehran is nearer to time.ir and others
    // so switched to "Tehran" method as default calculation algorithm
    calculationMethod = CalculationMethod
        .valueOf(prefs.getString(PREF_PRAY_TIME_METHOD, null) ?: DEFAULT_PRAY_TIME_METHOD)
    asrMethod = if (prefs.getString(
            PREF_ASR_JURISTICS,
            DEFAULT_ASR_JURISTICS
        ) == DEFAULT_ASR_JURISTICS
    ) CalculationMethod.AsrJuristics.Standard else CalculationMethod.AsrJuristics.Hanafi
    coordinate = getCoordinate(context)
    try {
        mainCalendar =
            CalendarType.valueOf(prefs.getString(PREF_MAIN_CALENDAR_KEY, null) ?: "SHAMSI")

        otherCalendars = (prefs.getString(PREF_OTHER_CALENDARS_KEY, null) ?: "GREGORIAN,ISLAMIC")
            .splitIgnoreEmpty(",").map(CalendarType::valueOf).toList()
    } catch (e: Exception) {
        Log.e(TAG, "Fail on parsing calendar preference", e)
        mainCalendar = CalendarType.SHAMSI
        otherCalendars = listOf(CalendarType.GREGORIAN, CalendarType.ISLAMIC)
    }

    spacedComma = if (isNonArabicScriptSelected()) ", " else "، "
    isShowWeekOfYearEnabled = prefs.getBoolean("showWeekOfYearNumber", false)
    weekStartOffset =
        (prefs.getString(PREF_WEEK_START, null) ?: DEFAULT_WEEK_START).toIntOrNull() ?: 0

    weekEnds = BooleanArray(7)
    (prefs.getStringSet(PREF_WEEK_ENDS, null) ?: DEFAULT_WEEK_ENDS)
        .mapNotNull(String::toIntOrNull)
        .forEach { weekEnds[it] = true }

    isShowDeviceCalendarEvents = prefs.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false)
    val resources = context.resources
    whatToShowOnWidgets = prefs.getStringSet("what_to_show", null)
        ?: resources.getStringArray(R.array.what_to_show_default).toSet()

    isAstronomicalFeaturesEnabled = prefs.getBoolean("astronomicalFeatures", false)
    numericalDatePreferred = prefs.getBoolean("numericalDatePreferred", false)

    if (getOnlyLanguage(language) != resources.getString(R.string.code))
        applyAppLanguage(context)

    calendarTypesTitleAbbr = context.resources.getStringArray(R.array.calendar_type_abbr).toList()

    shiftWorks = (prefs.getString(PREF_SHIFT_WORK_SETTING, null) ?: "")
        .splitIgnoreEmpty(",")
        .map { it.splitIgnoreEmpty("=") }
        .filter { it.size == 2 }
        .map { ShiftWorkRecord(it[0], it[1].toIntOrNull() ?: 1) }
    shiftWorkPeriod = shiftWorks.map { it.length }.sum()
    shiftWorkStartingJdn = prefs.getLong(PREF_SHIFT_WORK_STARTING_JDN, -1)
    shiftWorkRecurs = prefs.getBoolean(PREF_SHIFT_WORK_RECURS, true)
    shiftWorkTitles = resources.getStringArray(R.array.shift_work_keys)
        .zip(resources.getStringArray(R.array.shift_work))
        .toMap()

    when (language) {
        LANG_FA, LANG_FA_AF, LANG_EN_IR -> {
            amString = DEFAULT_AM
            pmString = DEFAULT_PM
        }
        else -> {
            amString = context.getString(R.string.am)
            pmString = context.getString(R.string.pm)
        }
    }

    appTheme = try {
        getThemeFromName(getThemeFromPreference(context, prefs))
    } catch (e: Exception) {
        e.printStackTrace()
        R.style.LightTheme
    }

    isTalkBackEnabled = context.getSystemService<AccessibilityManager>()?.run {
        isEnabled && isTouchExplorationEnabled
    } ?: false

    // https://stackoverflow.com/a/61599809
    isHighTextContrastEnabled = try {
        context.getSystemService<AccessibilityManager>()?.run {
            (javaClass.getMethod("isHighTextContrastEnabled").invoke(this) as? Boolean)
        } ?: false
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

// Context preferably should be activity context not application
fun applyAppLanguage(context: Context) {
    val localeCode = getOnlyLanguage(language)
    val locale = Locale(localeCode)
    Locale.setDefault(locale)
    val resources = context.resources
    val config = resources.configuration
    config.setLocale(locale)
    config.setLayoutDirection(
        when (localeCode) {
            LANG_AZB, LANG_GLK -> Locale(LANG_FA)
            else -> locale
        }
    )
    resources.updateConfiguration(config, resources.displayMetrics)
}

//fun Context.withLocale(): Context {
//    val config = resources.configuration
//    val locale = Locale(getOnlyLanguage(language))
//    Locale.setDefault(locale)
//    config.setLocale(locale)
//    config.setLayoutDirection(when (language) {
//        LANG_AZB, LANG_GLK -> Locale(LANG_FA)
//        else -> locale
//    })
//    return createConfigurationContext(config)
//}
