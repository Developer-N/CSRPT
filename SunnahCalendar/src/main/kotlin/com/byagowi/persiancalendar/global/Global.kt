package com.byagowi.persiancalendar.global

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.view.accessibility.AccessibilityManager
import androidx.collection.LongSet
import androidx.collection.emptyLongSet
import androidx.collection.longSetOf
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.DEFAULT_AM
import com.byagowi.persiancalendar.DEFAULT_ASCENDING_ATHAN_VOLUME
import com.byagowi.persiancalendar.DEFAULT_ATHAN_VIBRATION
import com.byagowi.persiancalendar.DEFAULT_AZERI_ALTERNATIVE_PERSIAN_MONTHS
import com.byagowi.persiancalendar.DEFAULT_CITY
import com.byagowi.persiancalendar.DEFAULT_DREAM_NOISE
import com.byagowi.persiancalendar.DEFAULT_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.DEFAULT_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.DEFAULT_HIGH_LATITUDES_METHOD
import com.byagowi.persiancalendar.DEFAULT_HOLIDAY
import com.byagowi.persiancalendar.DEFAULT_IRAN_TIME
import com.byagowi.persiancalendar.DEFAULT_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.DEFAULT_LOCAL_DIGITS
import com.byagowi.persiancalendar.DEFAULT_NOTIFY_DATE
import com.byagowi.persiancalendar.DEFAULT_NOTIFY_DATE_LOCK_SCREEN
import com.byagowi.persiancalendar.DEFAULT_PM
import com.byagowi.persiancalendar.DEFAULT_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.DEFAULT_RED_HOLIDAYS
import com.byagowi.persiancalendar.DEFAULT_SECONDARY_CALENDAR_IN_TABLE
import com.byagowi.persiancalendar.DEFAULT_THEME_GRADIENT
import com.byagowi.persiancalendar.DEFAULT_VAZIR_ENABLED
import com.byagowi.persiancalendar.DEFAULT_WALLPAPER_AUTOMATIC
import com.byagowi.persiancalendar.DEFAULT_WALLPAPER_DARK
import com.byagowi.persiancalendar.DEFAULT_WIDGET_CLOCK
import com.byagowi.persiancalendar.DEFAULT_WIDGET_CUSTOMIZATIONS
import com.byagowi.persiancalendar.DEFAULT_WIDGET_IN_24
import com.byagowi.persiancalendar.DEFAULT_WIDGET_TRANSPARENCY
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_ASCENDING_ATHAN_VOLUME
import com.byagowi.persiancalendar.PREF_ASR_HANAFI_JURISTIC
import com.byagowi.persiancalendar.PREF_ASTRONOMICAL_FEATURES
import com.byagowi.persiancalendar.PREF_ATHAN_NAME
import com.byagowi.persiancalendar.PREF_ATHAN_VIBRATION
import com.byagowi.persiancalendar.PREF_AZERI_ALTERNATIVE_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_CALENDARS_IDS_AS_HOLIDAY
import com.byagowi.persiancalendar.PREF_CALENDARS_IDS_TO_EXCLUDE
import com.byagowi.persiancalendar.PREF_CENTER_ALIGN_WIDGETS
import com.byagowi.persiancalendar.PREF_DREAM_NOISE
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_HIGH_LATITUDES_METHOD
import com.byagowi.persiancalendar.PREF_IRAN_TIME
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LOCAL_DIGITS
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_MIDNIGHT_METHOD
import com.byagowi.persiancalendar.PREF_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE_LOCK_SCREEN
import com.byagowi.persiancalendar.PREF_NUMERICAL_DATE_PREFERRED
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.PREF_RED_HOLIDAYS
import com.byagowi.persiancalendar.PREF_SECONDARY_CALENDAR_IN_TABLE
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_RECURS
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_SETTING
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_STARTING_JDN
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.PREF_SHOW_WEEK_OF_YEAR_NUMBER
import com.byagowi.persiancalendar.PREF_SWIPE_DOWN_ACTION
import com.byagowi.persiancalendar.PREF_SWIPE_UP_ACTION
import com.byagowi.persiancalendar.PREF_SYSTEM_DARK_THEME
import com.byagowi.persiancalendar.PREF_SYSTEM_LIGHT_THEME
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.PREF_THEME_GRADIENT
import com.byagowi.persiancalendar.PREF_VAZIR_ENABLED
import com.byagowi.persiancalendar.PREF_WALLPAPER_AUTOMATIC
import com.byagowi.persiancalendar.PREF_WALLPAPER_DARK
import com.byagowi.persiancalendar.PREF_WEEK_ENDS
import com.byagowi.persiancalendar.PREF_WEEK_START
import com.byagowi.persiancalendar.PREF_WHAT_TO_SHOW_WIDGETS
import com.byagowi.persiancalendar.PREF_WIDGETS_PREFER_SYSTEM_COLORS
import com.byagowi.persiancalendar.PREF_WIDGET_CLOCK
import com.byagowi.persiancalendar.PREF_WIDGET_IN_24
import com.byagowi.persiancalendar.PREF_WIDGET_TRANSPARENCY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.ui.calendar.SwipeDownAction
import com.byagowi.persiancalendar.ui.calendar.SwipeUpAction
import com.byagowi.persiancalendar.ui.theme.Theme
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.enableHighLatitudesConfiguration
import com.byagowi.persiancalendar.utils.getJdnOrNull
import com.byagowi.persiancalendar.utils.isIslamicOffsetExpired
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.scheduleAlarms
import com.byagowi.persiancalendar.utils.splitFilterNotEmpty
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.byagowi.persiancalendar.variants.debugLog
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Coordinates
import io.github.persiancalendar.praytimes.HighLatitudesMethod
import io.github.persiancalendar.praytimes.MidnightMethod
import ir.namoo.commons.PREF_AZKAR_REINDER
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.commons.utils.createAthansSettingDB
import ir.namoo.religiousprayers.ui.azkar.scheduleAzkars
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.TimeZone

// Using global variable isn't really the best idea.
// Somehow it's a legacy thing for this now aged project.
// We have limited most of global variable to this package and
// are avoiding storing complicated things on it.

private val monthNameEmptyList = List(12) { "" }
var persianMonths = monthNameEmptyList
    private set
var islamicMonths = monthNameEmptyList
    private set
var gregorianMonths = monthNameEmptyList
    private set
var nepaliMonths = monthNameEmptyList
    private set
private val weekDaysEmptyList = List(7) { "" }
var weekDays = weekDaysEmptyList
    private set
var weekDaysInitials = weekDaysEmptyList
    private set
var preferredDigits = Language.PERSIAN_DIGITS
    private set
var clockIn24 = DEFAULT_WIDGET_IN_24
    private set

private val isForcedIranTimeEnabled_ = MutableStateFlow(DEFAULT_IRAN_TIME)
val isForcedIranTimeEnabled: StateFlow<Boolean> get() = isForcedIranTimeEnabled_

private val isNotifyDateOnLockScreen_ = MutableStateFlow(DEFAULT_NOTIFY_DATE_LOCK_SCREEN)
val isNotifyDateOnLockScreen: StateFlow<Boolean> get() = isNotifyDateOnLockScreen_

private val prefersWidgetsDynamicColors_ = MutableStateFlow(false)
val prefersWidgetsDynamicColorsFlow: StateFlow<Boolean> get() = prefersWidgetsDynamicColors_

var isWidgetClock = DEFAULT_WIDGET_CLOCK
    private set

private val isNotifyDate_ = MutableStateFlow(DEFAULT_NOTIFY_DATE)
val isNotifyDate: StateFlow<Boolean> get() = isNotifyDate_

private val notificationAthan_ = MutableStateFlow(isNotifyDate.value)
val notificationAthan: StateFlow<Boolean> get() = notificationAthan_
private val athanVibration_ = MutableStateFlow(DEFAULT_ATHAN_VIBRATION)
val athanVibration: StateFlow<Boolean> get() = athanVibration_
private val ascendingAthan_ = MutableStateFlow(DEFAULT_ASCENDING_ATHAN_VOLUME)
val ascendingAthan: StateFlow<Boolean> get() = ascendingAthan_

private val calculationMethod_ =
    MutableStateFlow(CalculationMethod.valueOf(DEFAULT_PRAY_TIME_METHOD))
val calculationMethod: StateFlow<CalculationMethod> get() = calculationMethod_

private val athanSoundName_ = MutableStateFlow<String?>(null)
val athanSoundName: StateFlow<String?> get() = athanSoundName_

var midnightMethod = calculationMethod.value.defaultMidnight
    private set

private val asrMethod_ = MutableStateFlow(AsrMethod.Standard)
val asrMethod: StateFlow<AsrMethod> get() = asrMethod_

var highLatitudesMethod = HighLatitudesMethod.NightMiddle
    private set

private val language_ = MutableStateFlow(Language.FA)
val language: StateFlow<Language> get() = language_

private val userSetTheme_ = MutableStateFlow(Theme.SYSTEM_DEFAULT)

// Don't use this just to detect dark mode as it's invalid in system default
val userSetTheme: StateFlow<Theme> get() = userSetTheme_

private val systemDarkTheme_ = MutableStateFlow(Theme.SYSTEM_DEFAULT)
val systemDarkTheme: StateFlow<Theme> get() = systemDarkTheme_

private val systemLightTheme_ = MutableStateFlow(Theme.SYSTEM_DEFAULT)
val systemLightTheme: StateFlow<Theme> get() = systemLightTheme_

private val isGradient_ = MutableStateFlow(DEFAULT_THEME_GRADIENT)
val isGradient: StateFlow<Boolean> get() = isGradient_


private val isRedHolidays_ = MutableStateFlow(DEFAULT_RED_HOLIDAYS)
val isRedHolidays: StateFlow<Boolean> get() = isRedHolidays_


private val isVazirEnabled_ = MutableStateFlow(DEFAULT_VAZIR_ENABLED)
val isVazirEnabled: StateFlow<Boolean> get() = isVazirEnabled_

private var alternativeGregorianMonths = false
private var alternativePersianMonthsInAzeri = false

private val coordinates_ = MutableStateFlow<Coordinates?>(null)
val coordinates: StateFlow<Coordinates?> get() = coordinates_

private val cityName_ = MutableStateFlow<String?>(null)
val cityName: StateFlow<String?> get() = cityName_

private val widgetTransparency_ = MutableStateFlow(.0f)
val widgetTransparency: StateFlow<Float> get() = widgetTransparency_

var enabledCalendars = listOf(Calendar.SHAMSI, Calendar.GREGORIAN, Calendar.ISLAMIC)
    private set
val mainCalendar inline get() = enabledCalendars.getOrNull(0) ?: Calendar.SHAMSI
val mainCalendarDigits
    get() = when {
        secondaryCalendar == null -> preferredDigits
        preferredDigits === Language.ARABIC_DIGITS -> Language.ARABIC_DIGITS
        !language.value.canHaveLocalDigits -> Language.ARABIC_DIGITS
        else -> mainCalendar.preferredDigits
    }
val secondaryCalendar
    get() = if (secondaryCalendarEnabled) enabledCalendars.getOrNull(1) else null
val secondaryCalendarDigits
    get() = when {
        !language.value.canHaveLocalDigits -> Language.ARABIC_DIGITS
        preferredDigits === Language.ARABIC_DIGITS -> Language.ARABIC_DIGITS
        else -> secondaryCalendar?.preferredDigits ?: Language.ARABIC_DIGITS
    }
var isCenterAlignWidgets = true
    private set
var weekStartOffset = 0
    private set
var weekEnds = BooleanArray(7)
    private set

private val isShowWeekOfYearEnabled_ = MutableStateFlow(false)
val isShowWeekOfYearEnabled: StateFlow<Boolean> get() = isShowWeekOfYearEnabled_

private val dreamNoise_ = MutableStateFlow(DEFAULT_DREAM_NOISE)
val dreamNoise: StateFlow<Boolean> get() = dreamNoise_

private val wallpaperDark_ = MutableStateFlow(DEFAULT_WALLPAPER_DARK)
val wallpaperDark: StateFlow<Boolean> get() = wallpaperDark_

private val wallpaperAutomatic_ = MutableStateFlow(DEFAULT_WALLPAPER_AUTOMATIC)
val wallpaperAutomatic: StateFlow<Boolean> get() = wallpaperAutomatic_

private val preferredSwipeUpAction_ = MutableStateFlow(SwipeUpAction.entries[0])
val preferredSwipeUpAction: StateFlow<SwipeUpAction> get() = preferredSwipeUpAction_

private val preferredSwipeDownAction_ = MutableStateFlow(SwipeDownAction.entries[0])
val preferredSwipeDownAction: StateFlow<SwipeDownAction> get() = preferredSwipeDownAction_

private val isShowDeviceCalendarEvents_ = MutableStateFlow(false)
val isShowDeviceCalendarEvents: StateFlow<Boolean> get() = isShowDeviceCalendarEvents_

private val eventCalendarsIdsToExclude_ = MutableStateFlow(emptyLongSet())
val eventCalendarsIdsToExclude: StateFlow<LongSet> get() = eventCalendarsIdsToExclude_

private val eventCalendarsIdsAsHoliday_ = MutableStateFlow(emptyLongSet())
val eventCalendarsIdsAsHoliday: StateFlow<LongSet> get() = eventCalendarsIdsAsHoliday_

var whatToShowOnWidgets = emptySet<String>()
    private set
var isAstronomicalExtraFeaturesEnabled = false
    private set
var isTalkBackEnabled = false
    private set
var isHighTextContrastEnabled = false
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
var spacedAndInDates = " و "
    private set
var spacedOr = " و "
    private set
var spacedColon = ": "
    private set
var spacedComma = "، "
    private set

// Otherwise WidgetService might use untranslated messages
var prayTimesTitles: Map<PrayTime, String> = emptyMap()
    private set
var nothingScheduledString = ""
    private set
var holidayString = DEFAULT_HOLIDAY
    private set
var numericalDatePreferred = false
    private set
var calendarsTitlesAbbr = emptyList<String>()
    private set
var eventsRepository: EventsRepository? = null
    private set

private var secondaryCalendarEnabled = false

// This should be called before any use of Utils on the activity and services
fun initGlobal(context: Context) {
    debugLog("Utils: initGlobal is called")
    updateStoredPreference(context)
    applyAppLanguage(context)
    loadLanguageResources(context.resources)
    scheduleAlarms(context)
    configureCalendarsAndLoadEvents(context)
    createAthansSettingDB(context)
}

fun configureCalendarsAndLoadEvents(context: Context) {
    debugLog("Utils: configureCalendarsAndLoadEvents is called")
    val preferences = context.preferences

    IslamicDate.islamicOffset = if (preferences.isIslamicOffsetExpired) 0
    else preferences.getString(PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET)?.toIntOrNull() ?: 0

    eventsRepository = EventsRepository(preferences, language.value)
    isIranHolidaysEnabled = eventsRepository?.iranHolidays == true
}

fun loadLanguageResources(resources: Resources) {
    debugLog("Utils: loadLanguageResources is called")
    val language = language.value
    persianMonths = language.getPersianMonths(resources, alternativePersianMonthsInAzeri)
    islamicMonths = language.getIslamicMonths(resources)
    gregorianMonths = language.getGregorianMonths(resources, alternativeGregorianMonths)
    nepaliMonths = language.getNepaliMonths()
    weekDays = language.getWeekDays(resources)
    weekDaysInitials = language.getWeekDaysInitials(resources)
    shiftWorkTitles = mapOf(
        "d" to resources.getString(R.string.shift_work_morning), // d -> day work, legacy key
        "r" to resources.getString(R.string.shift_work_off), // r -> rest, legacy key
        "e" to resources.getString(R.string.shift_work_evening),
        "n" to resources.getString(R.string.shift_work_night)
    )
    calendarsTitlesAbbr = Calendar.entries.map { resources.getString(it.shortTitle) }
    when {
        // This is mostly pointless except we want to make sure even on broken language resources state
        // which might happen in widgets updates we don't have wrong values for these important two
        language.isPersian -> {
            amString = DEFAULT_AM
            pmString = DEFAULT_PM
        }

        else -> {
            amString = resources.getString(R.string.am)
            pmString = resources.getString(R.string.pm)
        }
    }
    holidayString = when {
        language.isPersian -> DEFAULT_HOLIDAY
        language.isDari -> "رخصتی"
        else -> resources.getString(R.string.holiday)
    }
    nothingScheduledString = resources.getString(R.string.nothing_scheduled)
    prayTimesTitles = PrayTime.entries.associateWith { resources.getString(it.stringRes) }
    spacedOr = resources.getString(R.string.spaced_or)
    spacedAndInDates = if (language.languagePrefersHalfSpaceAndInDates) " "
    else resources.getString(R.string.spaced_and)
    spacedColon = resources.getString(R.string.spaced_colon)
    spacedComma = resources.getString(R.string.spaced_comma)
}

fun updateStoredPreference(context: Context) {
    debugLog("Utils: updateStoredPreference is called")
    val preferences = context.preferences
    val language = language.value

    language_.value = preferences.getString(PREF_APP_LANGUAGE, null)
        ?.let(Language::valueOfLanguageCode) ?: Language.getPreferredDefaultLanguage(context)
    userSetTheme_.value = run {
        val key = preferences.getString(PREF_THEME, null)
        Theme.entries.find { it.key == key }
    } ?: Theme.SYSTEM_DEFAULT
    systemDarkTheme_.value = run {
        val key = preferences.getString(PREF_SYSTEM_DARK_THEME, null)
        Theme.entries.find { it.key == key }.takeIf { it != Theme.SYSTEM_DEFAULT }
    } ?: Theme.DARK
    systemLightTheme_.value = run {
        val key = preferences.getString(PREF_SYSTEM_LIGHT_THEME, null)
        Theme.entries.find { it.key == key }.takeIf { it != Theme.SYSTEM_DEFAULT }
    } ?: Theme.LIGHT
    isGradient_.value = preferences.getBoolean(PREF_THEME_GRADIENT, DEFAULT_THEME_GRADIENT)
    isRedHolidays_.value = preferences.getBoolean(PREF_RED_HOLIDAYS, DEFAULT_RED_HOLIDAYS)
    isVazirEnabled_.value = preferences.getBoolean(PREF_VAZIR_ENABLED, DEFAULT_VAZIR_ENABLED)
    alternativeGregorianMonths = when {
        language.isPersian -> preferences.getBoolean(
            PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS, DEFAULT_ENGLISH_GREGORIAN_PERSIAN_MONTHS
        )

        language.isArabic -> preferences.getBoolean(
            PREF_EASTERN_GREGORIAN_ARABIC_MONTHS, DEFAULT_EASTERN_GREGORIAN_ARABIC_MONTHS
        )

        else -> false
    }
    alternativePersianMonthsInAzeri = language == Language.AZB && preferences.getBoolean(
        PREF_AZERI_ALTERNATIVE_PERSIAN_MONTHS, DEFAULT_AZERI_ALTERNATIVE_PERSIAN_MONTHS
    )

    prefersWidgetsDynamicColors_.value =
        userSetTheme.value.isDynamicColors && preferences.getBoolean(
            PREF_WIDGETS_PREFER_SYSTEM_COLORS,
            true
        )

    preferredDigits = if (!preferences.getBoolean(
            PREF_LOCAL_DIGITS, DEFAULT_LOCAL_DIGITS
        ) || !language.canHaveLocalDigits
    ) Language.ARABIC_DIGITS
    else language.preferredDigits

    clockIn24 = preferences.getBoolean(PREF_WIDGET_IN_24, DEFAULT_WIDGET_IN_24)
    isForcedIranTimeEnabled_.value = language.showIranTimeOption && preferences.getBoolean(
        PREF_IRAN_TIME, DEFAULT_IRAN_TIME
    ) && TimeZone.getDefault().id != IRAN_TIMEZONE_ID
    isNotifyDateOnLockScreen_.value = preferences.getBoolean(
        PREF_NOTIFY_DATE_LOCK_SCREEN, DEFAULT_NOTIFY_DATE_LOCK_SCREEN
    )
    isWidgetClock = preferences.getBoolean(PREF_WIDGET_CLOCK, DEFAULT_WIDGET_CLOCK)
    isNotifyDate_.value = preferences.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)
    notificationAthan_.value = preferences.getBoolean(PREF_NOTIFICATION_ATHAN, isNotifyDate.value)
    athanVibration_.value = preferences.getBoolean(PREF_ATHAN_VIBRATION, DEFAULT_ATHAN_VIBRATION)
    ascendingAthan_.value =
        preferences.getBoolean(PREF_ASCENDING_ATHAN_VOLUME, DEFAULT_ASCENDING_ATHAN_VOLUME)
    isCenterAlignWidgets = preferences.getBoolean(PREF_CENTER_ALIGN_WIDGETS, true)

    // We were using "Jafari" method but later found out Tehran is nearer to time.ir and others
    // so switched to "Tehran" method as default calculation algorithm
    calculationMethod_.value = CalculationMethod.valueOf(
        preferences.getString(PREF_PRAY_TIME_METHOD, null) ?: DEFAULT_PRAY_TIME_METHOD
    )
    asrMethod_.value = if (calculationMethod.value.isJafari || !preferences.getBoolean(
            PREF_ASR_HANAFI_JURISTIC, language.isHanafiMajority
        )
    ) AsrMethod.Standard else AsrMethod.Hanafi
    midnightMethod = preferences.getString(PREF_MIDNIGHT_METHOD, null)?.let(MidnightMethod::valueOf)
        ?.takeIf { !it.isJafariOnly || calculationMethod.value.isJafari }
        ?: calculationMethod.value.defaultMidnight
    highLatitudesMethod = HighLatitudesMethod.valueOf(
        if (coordinates.value?.enableHighLatitudesConfiguration != true) DEFAULT_HIGH_LATITUDES_METHOD
        else preferences.getString(PREF_HIGH_LATITUDES_METHOD, null)
            ?: DEFAULT_HIGH_LATITUDES_METHOD
    )
    athanSoundName_.value = preferences.getString(PREF_ATHAN_NAME, null)

    dreamNoise_.value = preferences.getBoolean(PREF_DREAM_NOISE, DEFAULT_DREAM_NOISE)
    wallpaperDark_.value = preferences.getBoolean(PREF_WALLPAPER_DARK, DEFAULT_WALLPAPER_DARK)
    wallpaperAutomatic_.value =
        preferences.getBoolean(PREF_WALLPAPER_AUTOMATIC, DEFAULT_WALLPAPER_AUTOMATIC)

    preferredSwipeUpAction_.value = SwipeUpAction.entries.firstOrNull {
        it.name == preferences.getString(PREF_SWIPE_UP_ACTION, null)
    } ?: SwipeUpAction.entries[0]
    preferredSwipeDownAction_.value = SwipeDownAction.entries.firstOrNull {
        it.name == preferences.getString(PREF_SWIPE_DOWN_ACTION, null)
    } ?: SwipeDownAction.entries[0]

    val storedCity = preferences.getString(PREF_SELECTED_LOCATION, null)
        ?.takeIf { it.isNotEmpty() && it != DEFAULT_CITY }?.let { citiesStore[it] }
    coordinates_.value = storedCity?.coordinates ?: run {
        listOf(PREF_LATITUDE, PREF_LONGITUDE, PREF_ALTITUDE).map {
            preferences.getString(it, null)?.toDoubleOrNull() ?: .0
        }.takeIf { coords -> coords.any { it != .0 } } // if all were zero preference isn't set yet
            ?.let { (lat, lng, alt) -> Coordinates(lat, lng, alt) }
    }
    cityName_.value = storedCity?.let(language::getCityName) ?: preferences.getString(
        PREF_GEOCODED_CITYNAME,
        null
    )?.takeIf { it.isNotEmpty() }

    widgetTransparency_.value =
        preferences.getFloat(PREF_WIDGET_TRANSPARENCY, DEFAULT_WIDGET_TRANSPARENCY)

    runCatching {
        val mainCalendar = Calendar.valueOf(
            preferences.getString(PREF_MAIN_CALENDAR_KEY, null) ?: language.defaultMainCalendar
        )
        val otherCalendars = (preferences.getString(PREF_OTHER_CALENDARS_KEY, null)
            ?: language.defaultOtherCalendars).splitFilterNotEmpty(",").map(Calendar::valueOf)
        enabledCalendars = (listOf(mainCalendar) + otherCalendars).distinct()
        secondaryCalendarEnabled = preferences.getBoolean(
            PREF_SECONDARY_CALENDAR_IN_TABLE, DEFAULT_SECONDARY_CALENDAR_IN_TABLE
        )
    }.onFailure(logException).onFailure {
        // This really shouldn't happen, just in case
        enabledCalendars = listOf(Calendar.SHAMSI, Calendar.GREGORIAN, Calendar.ISLAMIC)
        secondaryCalendarEnabled = false
    }.getOrNull().debugAssertNotNull

    isShowWeekOfYearEnabled_.value = preferences.getBoolean(PREF_SHOW_WEEK_OF_YEAR_NUMBER, false)
    weekStartOffset =
        (preferences.getString(PREF_WEEK_START, null) ?: language.defaultWeekStart).toIntOrNull()
            ?: 0

    weekEnds = BooleanArray(7)
    (preferences.getStringSet(PREF_WEEK_ENDS, null)
        ?: language.defaultWeekEnds).mapNotNull(String::toIntOrNull).forEach { weekEnds[it] = true }

    isShowDeviceCalendarEvents_.value =
        preferences.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false)
    eventCalendarsIdsToExclude_.value = if (isShowDeviceCalendarEvents_.value) longSetOf(
        *(preferences.getString(PREF_CALENDARS_IDS_TO_EXCLUDE, null) ?: "").splitFilterNotEmpty(",")
            .mapNotNull { it.toLongOrNull() }.toLongArray()
    ) else emptyLongSet()
    eventCalendarsIdsAsHoliday_.value = if (isShowDeviceCalendarEvents_.value) longSetOf(
        *(preferences.getString(PREF_CALENDARS_IDS_AS_HOLIDAY, null) ?: "").splitFilterNotEmpty(",")
            .mapNotNull { it.toLongOrNull() }.toLongArray()
    ) else emptyLongSet()

    whatToShowOnWidgets =
        preferences.getStringSet(PREF_WHAT_TO_SHOW_WIDGETS, null) ?: DEFAULT_WIDGET_CUSTOMIZATIONS

    isAstronomicalExtraFeaturesEnabled = preferences.getBoolean(PREF_ASTRONOMICAL_FEATURES, false)
    numericalDatePreferred = preferences.getBoolean(PREF_NUMERICAL_DATE_PREFERRED, false)

    // TODO: probably can be done in applyAppLanguage itself?
    if (language.language != context.getString(R.string.code)) applyAppLanguage(context)

    shiftWorks =
        (preferences.getString(PREF_SHIFT_WORK_SETTING, null) ?: "").splitFilterNotEmpty(",")
            .map { it.splitFilterNotEmpty("=") }.filter { it.size == 2 }
            .map { ShiftWorkRecord(it[0], it[1].toIntOrNull() ?: 1) }
    shiftWorkPeriod = shiftWorks.sumOf { it.length }
    shiftWorkStartingJdn = preferences.getJdnOrNull(PREF_SHIFT_WORK_STARTING_JDN)
    shiftWorkRecurs = preferences.getBoolean(PREF_SHIFT_WORK_RECURS, true)

    isTalkBackEnabled = context.getSystemService<AccessibilityManager>()?.let {
        it.isEnabled && it.isTouchExplorationEnabled
    } == true

    // https://stackoverflow.com/a/61599809
    isHighTextContrastEnabled = runCatching {
        context.getSystemService<AccessibilityManager>()?.let {
            if (Build.VERSION.SDK_INT >= 36) {
                it.isHighContrastTextEnabled
            } else it.javaClass.getMethod("isHighTextContrastEnabled").invoke(it) as? Boolean
        }
    }.onFailure(logException).getOrNull() == true
}

// A very special case to trig coordinates mechanism in saveLocation
fun overrideCoordinatesGlobalVariable(coordinates: Coordinates) {
    coordinates_.value = coordinates
}
