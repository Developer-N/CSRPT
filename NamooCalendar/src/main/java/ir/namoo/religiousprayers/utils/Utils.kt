package ir.namoo.religiousprayers.utils

import android.content.Context
import android.media.AudioManager
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
import ir.namoo.religiousprayers.AppLocalesData
import ir.namoo.religiousprayers.DEFAULT_AM
import ir.namoo.religiousprayers.DEFAULT_APP_LANGUAGE
import ir.namoo.religiousprayers.DEFAULT_ASR_JURISTICS
import ir.namoo.religiousprayers.DEFAULT_CITY
import ir.namoo.religiousprayers.DEFAULT_IRAN_TIME
import ir.namoo.religiousprayers.DEFAULT_ISLAMIC_OFFSET
import ir.namoo.religiousprayers.DEFAULT_NOTIFY_DATE
import ir.namoo.religiousprayers.DEFAULT_NOTIFY_DATE_LOCK_SCREEN
import ir.namoo.religiousprayers.DEFAULT_PERSIAN_DIGITS
import ir.namoo.religiousprayers.DEFAULT_PM
import ir.namoo.religiousprayers.DEFAULT_PRAY_TIME_METHOD
import ir.namoo.religiousprayers.DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
import ir.namoo.religiousprayers.DEFAULT_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR
import ir.namoo.religiousprayers.DEFAULT_SELECTED_WIDGET_TEXT_COLOR
import ir.namoo.religiousprayers.DEFAULT_WEEK_ENDS
import ir.namoo.religiousprayers.DEFAULT_WEEK_START
import ir.namoo.religiousprayers.DEFAULT_WIDGET_CLOCK
import ir.namoo.religiousprayers.DEFAULT_WIDGET_IN_24
import ir.namoo.religiousprayers.LANG_AR
import ir.namoo.religiousprayers.LANG_AZB
import ir.namoo.religiousprayers.LANG_CKB
import ir.namoo.religiousprayers.LANG_EN_IR
import ir.namoo.religiousprayers.LANG_EN_US
import ir.namoo.religiousprayers.LANG_ES
import ir.namoo.religiousprayers.LANG_FA
import ir.namoo.religiousprayers.LANG_FA_AF
import ir.namoo.religiousprayers.LANG_FR
import ir.namoo.religiousprayers.LANG_GLK
import ir.namoo.religiousprayers.LANG_JA
import ir.namoo.religiousprayers.LANG_PS
import ir.namoo.religiousprayers.LANG_UR
import ir.namoo.religiousprayers.PREF_APP_LANGUAGE
import ir.namoo.religiousprayers.PREF_ASR_JURISTICS
import ir.namoo.religiousprayers.PREF_ASTRONOMICAL_FEATURES
import ir.namoo.religiousprayers.PREF_CENTER_ALIGN_WIDGETS
import ir.namoo.religiousprayers.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import ir.namoo.religiousprayers.PREF_HOLIDAY_TYPES
import ir.namoo.religiousprayers.PREF_IRAN_TIME
import ir.namoo.religiousprayers.PREF_ISLAMIC_OFFSET
import ir.namoo.religiousprayers.PREF_MAIN_CALENDAR_KEY
import ir.namoo.religiousprayers.PREF_NOTIFY_DATE
import ir.namoo.religiousprayers.PREF_NOTIFY_DATE_LOCK_SCREEN
import ir.namoo.religiousprayers.PREF_NUMERICAL_DATE_PREFERRED
import ir.namoo.religiousprayers.PREF_OTHER_CALENDARS_KEY
import ir.namoo.religiousprayers.PREF_PERSIAN_DIGITS
import ir.namoo.religiousprayers.PREF_PRAY_TIME_METHOD
import ir.namoo.religiousprayers.PREF_SELECTED_LOCATION
import ir.namoo.religiousprayers.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import ir.namoo.religiousprayers.PREF_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR
import ir.namoo.religiousprayers.PREF_SELECTED_WIDGET_TEXT_COLOR
import ir.namoo.religiousprayers.PREF_SHIFT_WORK_RECURS
import ir.namoo.religiousprayers.PREF_SHIFT_WORK_SETTING
import ir.namoo.religiousprayers.PREF_SHIFT_WORK_STARTING_JDN
import ir.namoo.religiousprayers.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import ir.namoo.religiousprayers.PREF_SHOW_WEEK_OF_YEAR_NUMBER
import ir.namoo.religiousprayers.PREF_WEEK_ENDS
import ir.namoo.religiousprayers.PREF_WEEK_START
import ir.namoo.religiousprayers.PREF_WHAT_TO_SHOW_WIDGETS
import ir.namoo.religiousprayers.PREF_WIDGET_CLOCK
import ir.namoo.religiousprayers.PREF_WIDGET_IN_24
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.entities.CalendarEvent
import ir.namoo.religiousprayers.entities.CityItem
import ir.namoo.religiousprayers.entities.ShiftWorkRecord
import ir.namoo.religiousprayers.generated.EventType
import ir.namoo.religiousprayers.generated.citiesStore
import ir.namoo.religiousprayers.generated.gregorianEvents
import ir.namoo.religiousprayers.generated.irregularRecurringEvents
import ir.namoo.religiousprayers.generated.islamicEvents
import ir.namoo.religiousprayers.generated.persianEvents
import ir.namoo.religiousprayers.praytimes.PrayTimeProvider
import java.util.*

const val TAG = "NAMOO"
const val CHANGE_DATE_TAG = "changeDate"
const val UPDATE_TAG = "update"
const val TWO_SECONDS_IN_MILLIS = 2000L
const val HALF_SECOND_IN_MILLIS = 500L
const val DAY_IN_SECOND = 86400L
const val DAY_IN_MILLIS = 86400000L
val monthNameEmptyList = List(12) { "" }
var persianMonths = monthNameEmptyList
    private set
var islamicMonths = monthNameEmptyList
    private set
var gregorianMonths = monthNameEmptyList
    private set
val weekDaysEmptyList = List(7) { "" }
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
var easternGregorianArabicMonths = false
    private set
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
var shiftWorkStartingJdn: Jdn? = null
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
var latestToastShowTime = -1L
    private set
var numericalDatePreferred = false
    private set
var calendarTypesTitleAbbr = emptyList<String>()
    private set
var allEnabledEvents = emptyList<CalendarEvent<*>>()
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
    val iranAncient = "iran_ancient" in enabledTypes
    val iranOthers = "iran_others" in enabledTypes || /*legacy*/ "iran_islamic" in enabledTypes
    val international = "international" in enabledTypes

    isIranHolidaysEnabled = iranHolidays

    IslamicDate.useUmmAlQura = false
    if (!iranHolidays) {
        if (afghanistanHolidays) {
            IslamicDate.useUmmAlQura = true
        }
        when (language) {
            LANG_FA_AF, LANG_PS, LANG_UR, LANG_AR, LANG_CKB, LANG_EN_US, LANG_JA, LANG_FR, LANG_ES ->
                IslamicDate.useUmmAlQura = true
        }
    }

    // Now that we are configuring converter's algorithm above, lets set the offset also

    IslamicDate.islamicOffset = context.appPrefs
        .getString(PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET)?.toIntOrNull() ?: 0

    val allEnabledEventsBuilder = ArrayList<CalendarEvent<*>>()

    persianCalendarEvents = persianEvents.mapNotNull {
        var holiday = it.isHoliday
        var addOrNot = false

        if (holiday && iranHolidays && it.type == EventType.Iran) addOrNot = true

        if (!iranHolidays && it.type == EventType.Iran) holiday = false
        if (iranAncient && it.type == EventType.AncientIran) addOrNot = true
        if (iranOthers && it.type == EventType.Iran) addOrNot = true
        if (afghanistanHolidays && it.type == EventType.Afghanistan && holiday) addOrNot = true
        if (!afghanistanHolidays && it.type == EventType.Afghanistan) holiday = false
        if (afghanistanOthers && it.type == EventType.Afghanistan) addOrNot = true

        if (addOrNot) {
            var title = it.title + " ("
            if (holiday && afghanistanHolidays && iranHolidays) {
                if (it.type == EventType.Iran)
                    title += "ایران، "
                else if (it.type == EventType.Afghanistan)
                    title += "افغانستان، "
            }
            title += formatDayAndMonth(it.day, persianMonths[it.month - 1]) + ")"
            CalendarEvent.PersianCalendarEvent(
                date = PersianDate(-1, it.month, it.day), title = title, isHoliday = holiday
            )
        } else null
    }.also { allEnabledEventsBuilder.addAll(it) }.toEventsStore()

    islamicCalendarEvents = islamicEvents.mapNotNull {
        var holiday = it.isHoliday
        var addOrNot = false

        if (afghanistanHolidays && holiday && it.type == EventType.Afghanistan) addOrNot = true
        if (!afghanistanHolidays && it.type == EventType.Afghanistan) holiday = false
        if (afghanistanOthers && it.type == EventType.Afghanistan) addOrNot = true
        if (iranHolidays && holiday && it.type == EventType.Iran) addOrNot = true
        if (!iranHolidays && it.type == EventType.Iran) holiday = false
        if (iranOthers && it.type == EventType.Iran) addOrNot = true

        if (addOrNot) {
            var title = it.title + " ("
            if (holiday && afghanistanHolidays && iranHolidays) {
                if (it.type == EventType.Iran)
                    title += "ایران، "
                else if (it.type == EventType.Afghanistan)
                    title += "افغانستان، "
            }
            title += formatDayAndMonth(it.day, islamicMonths[it.month - 1]) + ")"

            CalendarEvent.IslamicCalendarEvent(
                date = IslamicDate(-1, it.month, it.day), title = title, isHoliday = holiday
            )
        } else null
    }.let { list ->
        list + irregularRecurringEvents.filter { event ->
            iranOthers && event["calendar"] == "Hijri" && event["type"] == "Iran"
        }.flatMap { event ->
            // This adds only this, next and previous years' events, hacky but enough for now
            if (event["rule"] != "last weekday of month") return@flatMap emptyList()
            val dayOfWeek = event["weekday"]?.toIntOrNull() ?: return@flatMap emptyList()
            val month = event["month"]?.toIntOrNull() ?: return@flatMap emptyList()
            val title = event["title"] ?: return@flatMap emptyList()
            val year = Jdn.today.toIslamicCalendar().year
            (-1..1).map { offset ->
                val day = CalendarType.ISLAMIC.getLastDayOfWeek(year + offset, month, dayOfWeek)
                CalendarEvent.IslamicCalendarEvent(
                    date = IslamicDate(year + offset, month, day), title = title, isHoliday = false
                )
            }
        }
    }.also { allEnabledEventsBuilder.addAll(it) }.toEventsStore()

    gregorianCalendarEvents = gregorianEvents.mapNotNull {
        val isOfficialInIran = it.type == EventType.Iran
        val isOfficialInAfghanistan = it.type == EventType.Afghanistan
        val isOthers = !isOfficialInIran && !isOfficialInAfghanistan

        if (
            (isOthers && international) ||
            (isOfficialInIran && (iranOthers || international)) ||
            (isOfficialInAfghanistan && afghanistanOthers)
        ) {
            CalendarEvent.GregorianCalendarEvent(
                date = CivilDate(-1, it.month, it.day),
                title = "${it.title} (${formatDayAndMonth(it.day, gregorianMonths[it.month - 1])})",
                isHoliday = false
            )
        } else null
    }.also { allEnabledEventsBuilder.addAll(it) }.toEventsStore()

    allEnabledEvents = allEnabledEventsBuilder
}

fun loadLanguageResource() {
    val language = language
    persianMonths = AppLocalesData.getPersianCalendarMonths(language)
    islamicMonths = AppLocalesData.getIslamicCalendarMonths(language)
    gregorianMonths =
        AppLocalesData.getGregorianCalendarMonths(language, easternGregorianArabicMonths)
    weekDays = AppLocalesData.getWeekDays(language)
    weekDaysInitials = AppLocalesData.getWeekDaysInitials(language)
}

@StringRes
fun getNextOwghatTimeId(current: Clock, dateHasChanged: Boolean, context: Context): Int {
    coordinate ?: return 0

    if (prayTimes == null || dateHasChanged)
        prayTimes = PrayTimeProvider.calculate(calculationMethod, Jdn.today, coordinate!!, context)
    val now = current.toInt()
    return prayTimes?.let {
        if (now < it.fajrClock.toInt()) 0
        else if (now >= it.fajrClock.toInt() && now < it.sunriseClock.toInt()) 1
        else if (now >= it.sunriseClock.toInt() && now < it.dhuhrClock.toInt()) 2
        else if (now >= it.dhuhrClock.toInt() && now < it.asrClock.toInt()) 3
        else if (now >= it.asrClock.toInt() && now < it.maghribClock.toInt()) 4
        else if (now >= it.maghribClock.toInt() && now < it.ishaClock.toInt()) 5
        else 6
    } ?: 0
}

fun getClockFromStringId(@StringRes stringId: Int, context: Context): Clock {
    if (prayTimes == null && coordinate != null)
        prayTimes = PrayTimeProvider.calculate(calculationMethod, Jdn.today, coordinate!!, context)

    return prayTimes?.let {
        when (stringId) {
            R.string.imsak -> it.imsakClock
            R.string.fajr -> it.fajrClock
            R.string.sunrise -> it.sunriseClock
            R.string.dhuhr -> it.dhuhrClock
            R.string.asr -> it.asrClock
            R.string.sunset -> it.sunsetClock
            R.string.maghrib -> it.maghribClock
            R.string.isha -> it.ishaClock
            R.string.midnight -> it.midnightClock
            else -> null
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
        ?.takeIf { it.isNotEmpty() && it != DEFAULT_CITY } ?: return null

    if (key == cachedCityKey)
        return cachedCity

    // cache last query even if no city available under the key, useful in case invalid
    // value is somehow inserted on the preference
    cachedCityKey = key
    cachedCity = citiesStore[key]
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

private fun getOnlyLanguage(string: String): String = string.replace(Regex("-(IR|AF|US)"), "")

fun updateStoredPreference(context: Context) {
    val prefs = context.appPrefs

    language = prefs.getString(PREF_APP_LANGUAGE, null) ?: DEFAULT_APP_LANGUAGE
    easternGregorianArabicMonths = prefs.getBoolean(PREF_EASTERN_GREGORIAN_ARABIC_MONTHS, false)

    preferredDigits = when (language) {
        LANG_EN_US, LANG_JA, LANG_FR, LANG_ES -> ARABIC_DIGITS
        else -> when {
            prefs.getBoolean(PREF_PERSIAN_DIGITS, DEFAULT_PERSIAN_DIGITS) -> when (language) {
                LANG_AR, LANG_CKB -> ARABIC_INDIC_DIGITS
                else -> PERSIAN_DIGITS
            }
            else -> ARABIC_DIGITS
        }
    }

    clockIn24 = prefs.getBoolean(PREF_WIDGET_IN_24, DEFAULT_WIDGET_IN_24)
    isForcedIranTimeEnabled = prefs.getBoolean(PREF_IRAN_TIME, DEFAULT_IRAN_TIME)
    isNotifyDateOnLockScreen = prefs.getBoolean(
        PREF_NOTIFY_DATE_LOCK_SCREEN,
        DEFAULT_NOTIFY_DATE_LOCK_SCREEN
    )
    isWidgetClock = prefs.getBoolean(PREF_WIDGET_CLOCK, DEFAULT_WIDGET_CLOCK)
    isNotifyDate = prefs.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)
    isCenterAlignWidgets = prefs.getBoolean(PREF_CENTER_ALIGN_WIDGETS, false)

    selectedWidgetTextColor = prefs.getString(PREF_SELECTED_WIDGET_TEXT_COLOR, null)
        ?: DEFAULT_SELECTED_WIDGET_TEXT_COLOR

    selectedWidgetNextAthanTextColor =
        prefs.getString(PREF_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR, null)
            ?: DEFAULT_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR

    selectedWidgetBackgroundColor = prefs.getString(PREF_SELECTED_WIDGET_BACKGROUND_COLOR, null)
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
    runCatching {
        mainCalendar =
            CalendarType.valueOf(prefs.getString(PREF_MAIN_CALENDAR_KEY, null) ?: "SHAMSI")

        otherCalendars = (prefs.getString(PREF_OTHER_CALENDARS_KEY, null) ?: "GREGORIAN,ISLAMIC")
            .splitIgnoreEmpty(",").map(CalendarType::valueOf)
    }.onFailure(logException).onFailure {
        mainCalendar = CalendarType.SHAMSI
        otherCalendars = listOf(CalendarType.GREGORIAN, CalendarType.ISLAMIC)
    }

    spacedComma = if (isNonArabicScriptSelected()) ", " else "، "
    isShowWeekOfYearEnabled = prefs.getBoolean(PREF_SHOW_WEEK_OF_YEAR_NUMBER, false)
    weekStartOffset =
        (prefs.getString(PREF_WEEK_START, null) ?: DEFAULT_WEEK_START).toIntOrNull() ?: 0

    weekEnds = BooleanArray(7)
    (prefs.getStringSet(PREF_WEEK_ENDS, null) ?: DEFAULT_WEEK_ENDS)
        .mapNotNull(String::toIntOrNull).forEach { weekEnds[it] = true }

    isShowDeviceCalendarEvents = prefs.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false)
    val resources = context.resources
    whatToShowOnWidgets = prefs.getStringSet(PREF_WHAT_TO_SHOW_WIDGETS, null)
        ?: resources.getStringArray(R.array.what_to_show_default).toSet()

    isAstronomicalFeaturesEnabled = prefs.getBoolean(PREF_ASTRONOMICAL_FEATURES, false)
    numericalDatePreferred = prefs.getBoolean(PREF_NUMERICAL_DATE_PREFERRED, false)

    if (getOnlyLanguage(language) != resources.getString(R.string.code))
        applyAppLanguage(context)

    calendarTypesTitleAbbr = context.resources.getStringArray(R.array.calendar_type_abbr).toList()

    shiftWorks = (prefs.getString(PREF_SHIFT_WORK_SETTING, null) ?: "")
        .splitIgnoreEmpty(",")
        .map { it.splitIgnoreEmpty("=") }
        .filter { it.size == 2 }
        .map { ShiftWorkRecord(it[0], it[1].toIntOrNull() ?: 1) }
    shiftWorkPeriod = shiftWorks.sumOf { it.length }
    shiftWorkStartingJdn = prefs.getJdnOrNull(PREF_SHIFT_WORK_STARTING_JDN)
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

    appTheme = runCatching {
        getThemeFromName(getThemeFromPreference(context, prefs))
    }.onFailure(logException).getOrDefault(R.style.LightTheme)

    isTalkBackEnabled = context.getSystemService<AccessibilityManager>()?.let {
        it.isEnabled && it.isTouchExplorationEnabled
    } ?: false

    // https://stackoverflow.com/a/61599809
    isHighTextContrastEnabled = runCatching {
        context.getSystemService<AccessibilityManager>()?.let {
            (it.javaClass.getMethod("isHighTextContrastEnabled").invoke(it) as? Boolean)
        }
    }.onFailure(logException).getOrNull() ?: false
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
