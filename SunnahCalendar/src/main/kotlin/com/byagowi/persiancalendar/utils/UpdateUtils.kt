package com.byagowi.persiancalendar.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.CheckResult
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.collection.IntIntPair
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.withClip
import androidx.core.net.toUri
import androidx.core.text.buildSpannedString
import androidx.core.text.layoutDirection
import androidx.core.text.scale
import androidx.core.view.drawToBitmap
import com.byagowi.persiancalendar.ADD_EVENT
import com.byagowi.persiancalendar.AgeWidget
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.MONTH_NEXT_COMMAND
import com.byagowi.persiancalendar.MONTH_PREV_COMMAND
import com.byagowi.persiancalendar.MONTH_RESET_COMMAND
import com.byagowi.persiancalendar.NON_HOLIDAYS_EVENTS_KEY
import com.byagowi.persiancalendar.NWidget
import com.byagowi.persiancalendar.OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.OWGHAT_KEY
import com.byagowi.persiancalendar.OWGHAT_LOCATION_KEY
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_SELECTED_DATE_AGE_WIDGET
import com.byagowi.persiancalendar.PREF_SELECTED_DATE_AGE_WIDGET_START
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_TITLE_AGE_WIDGET
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.Widget1x1
import com.byagowi.persiancalendar.Widget2x2
import com.byagowi.persiancalendar.Widget4x1
import com.byagowi.persiancalendar.Widget4x2
import com.byagowi.persiancalendar.WidgetMap
import com.byagowi.persiancalendar.WidgetMonth
import com.byagowi.persiancalendar.WidgetMonthView
import com.byagowi.persiancalendar.WidgetMoon
import com.byagowi.persiancalendar.WidgetSchedule
import com.byagowi.persiancalendar.WidgetSunView
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.entities.PrayTime.Companion.get
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.clockIn24
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isCenterAlignWidgets
import com.byagowi.persiancalendar.global.isForcedIranTimeEnabled
import com.byagowi.persiancalendar.global.isHighTextContrastEnabled
import com.byagowi.persiancalendar.global.isNotifyDate
import com.byagowi.persiancalendar.global.isNotifyDateOnLockScreen
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.isWidgetClock
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.loadLanguageResources
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.mainCalendarDigits
import com.byagowi.persiancalendar.global.preferredDigits
import com.byagowi.persiancalendar.global.prefersWidgetsDynamicColorsFlow
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.global.whatToShowOnWidgets
import com.byagowi.persiancalendar.global.widgetTransparency
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.service.ScheduleWidgetService
import com.byagowi.persiancalendar.service.widgetWidthCellKey
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.astronomy.AstronomyState
import com.byagowi.persiancalendar.ui.calendar.calendarpager.DayPainter
import com.byagowi.persiancalendar.ui.calendar.calendarpager.MonthColors
import com.byagowi.persiancalendar.ui.calendar.calendarpager.renderMonthWidget
import com.byagowi.persiancalendar.ui.calendar.eventTextColor
import com.byagowi.persiancalendar.ui.calendar.sortEvents
import com.byagowi.persiancalendar.ui.calendar.times.SunView
import com.byagowi.persiancalendar.ui.calendar.times.SunViewColors
import com.byagowi.persiancalendar.ui.common.SolarDraw
import com.byagowi.persiancalendar.ui.map.MapDraw
import com.byagowi.persiancalendar.ui.map.MapType
import com.byagowi.persiancalendar.ui.resumeToken
import com.byagowi.persiancalendar.ui.settings.agewidget.AgeWidgetConfigureActivity
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.isLandscape
import com.byagowi.persiancalendar.ui.utils.isRtl
import com.byagowi.persiancalendar.ui.utils.isSystemInDarkTheme
import com.byagowi.persiancalendar.variants.debugLog
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.koin.getKoinInstance
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import java.util.Date
import java.util.GregorianCalendar
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


private val useDefaultPriority
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && isNotifyDateOnLockScreen.value
private const val NOTIFICATION_ID_DEFAULT_PRIORITY = 1003
private const val NOTIFICATION_ID_LOW_PRIORITY = 1001
private var pastLanguage: Language? = null
private var pastDate: AbstractDate? = null
private var deviceCalendarEvents: DeviceCalendarEventsStore = EventsStore.empty()

@ColorInt
private var selectedWidgetTextColor = DEFAULT_SELECTED_WIDGET_TEXT_COLOR

@ColorInt
private var selectedWidgetBackgroundColor = DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR

@ColorInt
private var selectedWidgetNextAthanTextColor = DEFAULT_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
val prefersWidgetsDynamicColors: Boolean get() = prefersWidgetsDynamicColorsFlow.value

// Is called from MainActivity to make sure is updated, probably should be removed however
fun readAndStoreDeviceCalendarEventsOfTheDay(context: Context) {
    runCatching { deviceCalendarEvents = context.readDayDeviceEvents(Jdn.today()) }.onFailure(
        logException
    )
}

private var latestFiredUpdate = 0L
private var latestAnyWidgetUpdate = 0L

fun hasAnyWidgetUpdateRecently(): Boolean =
    (System.currentTimeMillis() - latestAnyWidgetUpdate).milliseconds < 15.minutes

// https://developer.android.com/about/versions/12/features/widgets#ensure-compatibility
// Apply a round corner which is the default in Android 12
// 16dp on pre-12, but Android 12 is more, is a bit ugly to have it as a global variable
private var roundPixelSize = 0f

fun update(context: Context, updateDate: Boolean) {
    val now = System.currentTimeMillis()
    if (!updateDate && (now - latestFiredUpdate).milliseconds < .5.seconds) {
        debugLog("UpdateUtils: skip update")
        return
    }
    latestFiredUpdate = now

    debugLog("UpdateUtils: update")
    applyAppLanguage(context)
    if (pastLanguage != language.value) {
        loadLanguageResources(context.resources)
        pastLanguage = language.value
    }

    val jdn = Jdn.today()
    val date = jdn on mainCalendar

    if (pastDate != date || updateDate) {
        debugLog("UpdateUtils: date has changed")
        scheduleAlarms(context)
        pastDate = date
        readAndStoreDeviceCalendarEventsOfTheDay(context)
    }

    val shiftWorkTitle = getShiftWorkTitle(jdn)
    val title =
        dayTitleSummary(jdn, date) + if (shiftWorkTitle == null) "" else " ($shiftWorkTitle)"
    val widgetTitle = dayTitleSummary(
        jdn, date, calendarNameInLinear = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    ) + if (shiftWorkTitle == null) "" else " ($shiftWorkTitle)"
    val subtitle = dateStringOfOtherCalendars(jdn, spacedComma)

    val preferences = context.preferences

    // region upcoming pray time text
    val clock = Clock(Date().toGregorianCalendar(forceLocalTime = true))
    var prayTimes = coordinates.value?.calculatePrayTimes() ?: return
    val prayTimeProvider: PrayTimeProvider = getKoinInstance()
    prayTimes = prayTimeProvider.replace(prayTimes, jdn)

    val owghat = prayTimes.getNextPrayTime(clock).let {
        buildString {
            append(context.getString(it.stringRes))
            append(": ")
            append(prayTimes[it].toFormattedString())
            if (OWGHAT_LOCATION_KEY in whatToShowOnWidgets) cityName.value?.also { append(" ($it)") }
        }
    }
    // endregion

    selectedWidgetTextColor = getWidgetTextColor(preferences)
    selectedWidgetBackgroundColor = getWidgetBackgroundColor(preferences)
    selectedWidgetNextAthanTextColor = getWidgetNextAthanTextColor(preferences)

    roundPixelSize = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) 16 * context.resources.dp
    else context.resources.getDimensionPixelSize(
        android.R.dimen.system_app_widget_background_radius
    ).toFloat()

    // Widgets
    AppWidgetManager.getInstance(context).run {
        updateFromRemoteViews<AgeWidget>(context, now) { width, height, _, widgetId ->
            createAgeRemoteViews(context, width, height, widgetId, jdn)
        }
        updateFromRemoteViews<Widget1x1>(context, now) { width, height, _, _ ->
            create1x1RemoteViews(context, width, height, date)
        }
        updateFromRemoteViews<Widget4x1>(context, now) { width, height, _, _ ->
            create4x1RemoteViews(context, width, height, jdn, date, widgetTitle, subtitle)
        }
        updateFromRemoteViews<Widget2x2>(context, now) { width, height, _, _ ->
            create2x2RemoteViews(context, width, height, jdn, date, widgetTitle, subtitle, owghat)
        }
        updateFromRemoteViews<Widget4x2>(context, now) { width, height, _, _ ->
            create4x2RemoteViews(context, width, height, jdn, date, clock, prayTimes)
        }
        updateFromRemoteViews<WidgetSunView>(context, now) { width, height, _, _ ->
            createSunViewRemoteViews(context, width, height, prayTimes)
        }
        updateFromRemoteViews<WidgetMonthView>(context, now) { width, height, hasSize, _ ->
            createMonthViewRemoteViews(context, width, height, hasSize, jdn)
        }
        updateFromRemoteViews<WidgetMonth>(context, now) { _, height, hasSize, widgetId ->
            createMonthRemoteViews(context, height.takeIf { hasSize }, widgetId)
        }
        updateFromRemoteViews<WidgetMap>(context, now) { width, height, _, _ ->
            createMapRemoteViews(context, width, height, now)
        }
        updateFromRemoteViews<WidgetMoon>(context, now) { width, height, _, _ ->
            createMoonRemoteViews(context, width, height)
        }
        updateFromRemoteViews<WidgetSchedule>(context, now) { width, _, hasSize, widgetId ->
            createScheduleRemoteViews(context, width.takeIf { hasSize }, widgetId)
        }
        updateFromRemoteViews<NWidget>(context, now) { width, height, _, _ ->
            createNRemoteViews(context, width, height, clock, prayTimes)
        }
    }

    // Notification
    updateNotification(context, title, owghat, jdn, date, owghat, prayTimes, clock)
}

fun PrayTimes.getNextPrayTime(clock: Clock): PrayTime {
    val isJafari = calculationMethod.value.isJafari
    val times = if (isJafari) PrayTime.jafariImportantTimes else PrayTime.nonJafariImportantTimes
    return times.firstOrNull { this[it] > clock } ?: PrayTime.FAJR
}

fun AppWidgetManager.getWidgetSize(resources: Resources, widgetId: Int): IntIntPair? {
    // https://stackoverflow.com/a/69080699
    val isLandscape = resources.isLandscape
    val (width, height) = listOf(
        if (isLandscape) AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH
        else AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
        if (isLandscape) AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT
        else AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT
    ).map { (getAppWidgetOptions(widgetId).getInt(it, 0) * resources.dp).toInt() }
    // Crashes terribly if is below zero, let's make sure that won't happen till we understand it better
    return if (width > 10 && height > 10) IntIntPair(width, height) else null
}

private inline fun <reified T> AppWidgetManager.updateFromRemoteViews(
    context: Context,
    now: Long,
    widgetUpdateAction: (width: Int, height: Int, hasSize: Boolean, widgetId: Int) -> RemoteViews
) {
    runCatching {
        getAppWidgetIds(ComponentName(context, T::class.java))?.forEach { widgetId ->
            latestAnyWidgetUpdate = now
            val size = getWidgetSize(context.resources, widgetId)
            val (width, height) = size ?: IntIntPair(250, 250)
            updateAppWidget(widgetId, widgetUpdateAction(width, height, size != null, widgetId))
        }
    }.onFailure(logException).onFailure {
        if (BuildConfig.DEVELOPMENT) {
            Toast.makeText(
                context,
                "An error has happened, see the in-app log and post it to me",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

private fun createRoundPath(width: Int, height: Int, roundSize: Float): Path {
    val roundPath = Path()
    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
    roundPath.addRoundRect(rect, roundSize, roundSize, Path.Direction.CW)
    return roundPath
}

private fun createRoundedBitmap(
    width: Int, height: Int, @ColorInt color: Int, roundSize: Float
): Bitmap {
    val bitmap = createBitmap(width, height)
    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.color = color }
    Canvas(bitmap).drawRoundRect(rect, roundSize, roundSize, paint)
    return bitmap
}

private fun getWidgetBackgroundColor(
    preferences: SharedPreferences, key: String = PREF_SELECTED_WIDGET_BACKGROUND_COLOR
) = preferences.getString(key, null)?.let(Color::parseColor)
    ?: DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR

private fun getWidgetTextColor(
    preferences: SharedPreferences, key: String = PREF_SELECTED_WIDGET_TEXT_COLOR
) = preferences.getString(key, null)?.let(Color::parseColor) ?: DEFAULT_SELECTED_WIDGET_TEXT_COLOR

private fun getWidgetNextAthanTextColor(
    preferences: SharedPreferences, key: String = PREF_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR
) = preferences.getString(key, null)?.let(Color::parseColor)
    ?: DEFAULT_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR

fun createAgeRemoteViews(
    context: Context, width: Int, height: Int, widgetId: Int, today: Jdn,
): RemoteViews {
    val preferences = context.preferences
    val primary = preferences.getJdnOrNull(PREF_SELECTED_DATE_AGE_WIDGET + widgetId) ?: today
    val title = preferences.getString(PREF_TITLE_AGE_WIDGET + widgetId, null) ?: ""
    val subtitle = calculateDaysDifference(context.resources, primary, today, isInWidget = true)
    val textColor = getWidgetTextColor(preferences, PREF_SELECTED_WIDGET_TEXT_COLOR + widgetId)
    val backgroundColor = getWidgetBackgroundColor(
        preferences, PREF_SELECTED_WIDGET_BACKGROUND_COLOR + widgetId
    )
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_age)
    remoteViews.setRoundBackground(R.id.age_widget_background, width, height, backgroundColor)
    remoteViews.setDirection(R.id.age_widget_root, context.resources)
    remoteViews.setTextViewTextOrHideIfEmpty(R.id.textview_age_widget_title, title)
    remoteViews.setTextViewText(R.id.textview_age_widget, subtitle)
    listOf(R.id.textview_age_widget_title, R.id.textview_age_widget).forEach {
        if (prefersWidgetsDynamicColors) remoteViews.setDynamicTextColor(it)
        else remoteViews.setTextColor(it, textColor)
    }
    val secondary = preferences.getJdnOrNull(PREF_SELECTED_DATE_AGE_WIDGET_START + widgetId)
    if (secondary != null && primary > secondary && primary > today && today >= secondary) {
        remoteViews.setViewVisibility(R.id.progress, View.VISIBLE)
        remoteViews.setProgressBar(R.id.progress, primary - secondary, today - secondary, false)
    } else remoteViews.setViewVisibility(R.id.progress, View.GONE)
    remoteViews.setOnClickPendingIntent(
        R.id.age_widget_root, context.launchAgeWidgetConfigurationAppPendingIntent(widgetId)
    )
    return remoteViews
}

private fun createSunViewRemoteViews(
    context: Context, width: Int, height: Int, prayTimes: PrayTimes?
): RemoteViews {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_sun_view)
    val color = when {
        prefersWidgetsDynamicColors -> if (isSystemInDarkTheme(context.resources.configuration)) Color.WHITE else Color.BLACK

        else -> selectedWidgetTextColor
    }
    val sunView = SunView(context)
    sunView.colors = SunViewColors(
        nightColor = ContextCompat.getColor(
            context, if (prefersWidgetsDynamicColors) R.color.sun_view_dynamic_night_color
            else R.color.sun_view_night_color
        ),
        dayColor = ContextCompat.getColor(
            context, if (prefersWidgetsDynamicColors) R.color.sun_view_dynamic_day_color
            else R.color.sun_view_day_color
        ),
        middayColor = ContextCompat.getColor(
            context, if (prefersWidgetsDynamicColors) R.color.sun_view_dynamic_midday_color
            else R.color.sun_view_midday_color
        ),
        sunriseTextColor = color,
        middayTextColor = color,
        sunsetTextColor = color,
        textColorSecondary = color,
        linesColor = ColorUtils.setAlphaComponent(color, 0x60)
    )
    remoteViews.setRoundBackground(R.id.image_background, width, height)
    sunView.layoutDirection = context.resources.configuration.layoutDirection
    // https://stackoverflow.com/a/69080742
    sunView.measure(
        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST),
        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST)
    )
    sunView.layout(0, 0, width, height)
    sunView.prayTimes = prayTimes
    sunView.setTime(System.currentTimeMillis())
    sunView.initiate()
    if (prefersWidgetsDynamicColors || // dynamic colors for widget need this round clipping anyway
        selectedWidgetBackgroundColor != DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
    ) sunView.clippingPath = createRoundPath(width, height, roundPixelSize)
    remoteViews.setTextColor(R.id.message, color)
    remoteViews.setTextViewTextOrHideIfEmpty(
        R.id.message,
        if (coordinates.value == null) context.getString(R.string.ask_user_to_set_location) else ""
    )

    // These are used to generate preview,
    // view.setBackgroundColor(Color.parseColor("#80A0A0A0"))
    // val outStream = ByteArrayOutputStream()
    // view.drawToBitmap().compress(Bitmap.CompressFormat.PNG, 100, outStream)
    // copyToClipboard(Base64.encodeToString(outStream.toByteArray(), Base64.DEFAULT)) {}
    // $ convert -scale 50% a.png b.png
    // $ cwebp b.png -o c.webp
    remoteViews.setImageViewBitmap(R.id.image, sunView.drawToBitmap())
    remoteViews.setContentDescription(R.id.image, sunView.contentDescription)
    remoteViews.setOnClickPendingIntent(
        R.id.widget_layout_sun_view, context.launchAppPendingIntent()
    )
    return remoteViews
}

class StoredMonthOffset(val offset: Int) {
    private val lastInteraction = System.currentTimeMillis()

    // Not expired by time
    val isExpired: Boolean
        get() = (System.currentTimeMillis() - lastInteraction).milliseconds > 15.minutes
}

private val monthWidgetOffsets = mutableMapOf<Int, StoredMonthOffset>()

fun updateMonthWidget(context: Context, widgetId: Int, command: Int) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val size = appWidgetManager.getWidgetSize(context.resources, widgetId)
    monthWidgetOffsets[widgetId] = StoredMonthOffset(
        if (command == 0) 0 else ((monthWidgetOffsets[widgetId]?.offset ?: 0) + command)
    )
    val views = createMonthRemoteViews(context, size?.second, widgetId)
    appWidgetManager.updateAppWidget(widgetId, views)
}

private fun createMonthRemoteViews(context: Context, height: Int?, widgetId: Int): RemoteViews {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_month)
    remoteViews.setDirection(R.id.widget_month, context.resources)
    val today = Jdn.today()
    val offset = monthWidgetOffsets[widgetId]?.let {
        if (it.isExpired) {
            monthWidgetOffsets.remove(widgetId)
            0
        } else it.offset
    } ?: 0
    val mainCalendar = mainCalendar
    val secondaryCalendar = secondaryCalendar
    val monthStartDate = mainCalendar.getMonthStartFromMonthsDistance(today, offset)
    remoteViews.setTextViewText(R.id.month_name, buildSpannedString {
        val firstLine =
            if (monthStartDate.year == (today on mainCalendar).year) monthStartDate.monthName
            else language.value.my.format(
                monthStartDate.monthName, formatNumber(monthStartDate.year)
            )
        secondaryCalendar?.let {
            scale(.9f) {
                append(firstLine + "\n")
                scale(.625f) { append(monthFormatForSecondaryCalendar(monthStartDate, it, true)) }
            }
        } ?: append(firstLine)
    })

    // Round the background better
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        remoteViews.setViewOutlinePreferredRadiusDimen(
            R.id.widget_month, android.R.dimen.system_app_widget_background_radius
        )
        remoteViews.setBoolean(R.id.widget_month, "setClipToOutline", true)
    }

    val monthStartJdn = Jdn(monthStartDate)
    val startingWeekDay = applyWeekStartOffsetToWeekDay(monthStartJdn.weekDay)
    val monthLength = mainCalendar.getMonthLength(monthStartDate.year, monthStartDate.month)
    val daysRowsCount = ceil((monthLength + startingWeekDay) / 7f).toInt()
    remoteViews.setViewVisibility(
        R.id.week6, if (daysRowsCount > 5) View.VISIBLE else View.GONE
    )
    remoteViews.setInt(
        R.id.week5,
        "setBackgroundResource",
        if (daysRowsCount > 5) R.drawable.widget_month_bottom_border else 0
    )
    val eventsCountToShow = height?.let {
        val bottomSpace = it / context.resources.dp - 72 - 20
        ((bottomSpace / daysRowsCount - 14) / 14).toInt()
    } ?: 3

    val deviceEvents = if (isShowDeviceCalendarEvents.value) {
        context.readDaysDeviceEvents(monthStartJdn - startingWeekDay, (daysRowsCount * 7).days)
    } else EventsStore.empty()

    monthWidgetCells.forEachIndexed { i, id ->
        if (i < 7) {
            val position = revertWeekStartOffsetFromWeekDay(i)
            remoteViews.setTextViewText(id, getInitialOfWeekDay(position))
            val contentDescription =
                context.getString(R.string.week_days_name_column, getWeekDayName(position))
            remoteViews.setContentDescription(id, contentDescription)
            return@forEachIndexed
        }
        if (i >= (daysRowsCount + 1) * 7) return@forEachIndexed
        remoteViews.removeAllViews(id)
        val day = monthStartJdn + i - 7 - startingWeekDay
        val events = sortEvents(eventsRepository?.getEvents(day, deviceEvents) ?: emptyList())
        val date = day on mainCalendar
        run {
            val viewId = when {
                date.month != monthStartDate.month -> R.layout.widget_month_other_month_day
                day == today -> R.layout.widget_month_today
                else -> R.layout.widget_month_day
            }
            val dayView = RemoteViews(context.packageName, viewId)
            if (day.isWeekEnd || events.any { it.isHoliday }) dayView.setInt(
                R.id.day_root,
                "setBackgroundResource",
                R.drawable.widget_month_holiday,
            )
            dayView.setTextViewText(R.id.day, formatNumber(date.dayOfMonth))
            secondaryCalendar?.let {
                dayView.setTextViewTextSize(
                    R.id.day,
                    TypedValue.COMPLEX_UNIT_SP,
                    if (preferredDigits === Language.ARABIC_DIGITS) 9f else 11f
                )
                dayView.setTextViewTextSize(R.id.secondary_day, TypedValue.COMPLEX_UNIT_SP, 9f)
                val text = formatNumber((day on it).dayOfMonth, it.preferredDigits)
                dayView.setTextViewText(R.id.secondary_day, "($text)")
            } ?: run {
                dayView.setTextViewTextSize(
                    R.id.day,
                    TypedValue.COMPLEX_UNIT_SP,
                    if (preferredDigits === Language.ARABIC_DIGITS) 12f else 14f
                )
                dayView.setViewVisibility(R.id.secondary_day, View.GONE)
            }
            // TODO: Consider use of addStableView
            remoteViews.addView(id, dayView)
        }
        val shiftWork = getShiftWorkTitle(day)
        shiftWork?.let {
            val shiftWorkView = RemoteViews(context.packageName, R.layout.widget_month_shift_work)
            shiftWorkView.setTextViewText(R.id.title, it)
            remoteViews.addView(id, shiftWorkView)
        }
        val dayEventsCountToShow =
            (eventsCountToShow - if (shiftWork == null) 0 else 1).coerceAtLeast(1)
        val overflows = events.size > dayEventsCountToShow
        events.take(dayEventsCountToShow - if (overflows) 1 else 0).forEach {
            val eventView = RemoteViews(context.packageName, R.layout.widget_month_event)
            eventView.setTextViewText(R.id.title, it.title)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                eventView.setViewOutlinePreferredRadius(
                    R.id.title, 3f, TypedValue.COMPLEX_UNIT_DIP
                )
                eventView.setBoolean(R.id.title, "setClipToOutline", true)
            }
            when {
                it is CalendarEvent.DeviceCalendarEvent -> {
                    val background =
                        if (it.color.isEmpty()) Color.GRAY else it.color.toLong().toInt()
                    eventView.setInt(R.id.title, "setBackgroundColor", background)
                    eventView.setInt(R.id.title, "setTextColor", eventTextColor(background))
                }

                it.isHoliday -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        eventView.setColorAttr(
                            R.id.title,
                            "setBackgroundColor",
                            android.R.attr.colorAccent,
                        )
                        eventView.setColorInt(
                            R.id.title, "setTextColor", Color.WHITE, Color.WHITE
                        )
                    } else {
                        eventView.setInt(R.id.title, "setBackgroundColor", 0xFFFF8A65.toInt())
                        eventView.setInt(R.id.title, "setTextColor", Color.WHITE)
                    }
                }

                else -> {
                    eventView.setInt(R.id.title, "setBackgroundColor", 0xFF8D95AD.toInt())
                    eventView.setInt(R.id.title, "setTextColor", Color.WHITE)
                }
            }

            remoteViews.addView(id, eventView)
        }
        if (overflows) {
            val moreIconView = RemoteViews(context.packageName, R.layout.widget_month_more_icon)
            remoteViews.addView(id, moreIconView)
        }
        val action = jdnActionKey + day.value
        remoteViews.setOnClickPendingIntent(id, context.launchAppPendingIntent(action))
    }

    run {
        val startOfYearJdn = Jdn(mainCalendar, monthStartDate.year, 1, 1)
        val weekOfYearStart = monthStartJdn.getWeekOfYear(startOfYearJdn)
        val isShowWeekOfYearEnabled = isShowWeekOfYearEnabled.value
        if (isShowWeekOfYearEnabled) monthWidgetWeeks.drop(1).forEachIndexed { i, id ->
            val weekNumber = formatNumber(weekOfYearStart + i)
            remoteViews.setTextViewText(id, weekNumber)
            val contentDescription = context.getString(R.string.nth_week_of_year, weekNumber)
            remoteViews.setContentDescription(id, contentDescription)
        }
        val visibility = if (isShowWeekOfYearEnabled) View.VISIBLE else View.GONE
        monthWidgetWeeksParents.forEach { remoteViews.setViewVisibility(it, visibility) }
    }

    run {
        val addEventPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(
                context, BroadcastReceivers::class.java
            ).setAction(if (offset == 0) ADD_EVENT else (MONTH_RESET_COMMAND + widgetId)),
            PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.add_event, addEventPendingIntent)
        remoteViews.setContentDescription(
            R.id.add_event,
            context.getString(if (offset == 0) R.string.add_event else R.string.return_to_today)
        )
        remoteViews.setImageViewResource(
            R.id.add_event,
            if (offset == 0) R.drawable.widget_add_event_icon else R.drawable.ic_restore
        )
    }
    run {
        val previousPendingIntent = PendingIntent.getBroadcast(
            context, 0, Intent(
                context, BroadcastReceivers::class.java
            ).setAction(MONTH_PREV_COMMAND + widgetId), PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.previous_month, previousPendingIntent)
        remoteViews.setContentDescription(
            R.id.previous_month,
            context.getString(R.string.previous_x, context.getString(R.string.month))
        )
        val nextPendingIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent(
                context, BroadcastReceivers::class.java
            ).setAction(MONTH_NEXT_COMMAND + widgetId),
            PendingIntent.FLAG_IMMUTABLE,
        )
        remoteViews.setOnClickPendingIntent(R.id.next_month, nextPendingIntent)
        remoteViews.setContentDescription(
            R.id.next_month,
            context.getString(R.string.next_x, context.getString(R.string.month)),
        )
        val action = jdnActionKey + monthStartJdn.value
        remoteViews.setOnClickPendingIntent(
            R.id.month_name_wrapper,
            context.launchAppPendingIntent(action),
        )
    }

    return remoteViews
}

private fun createScheduleRemoteViews(context: Context, width: Int?, widgetId: Int): RemoteViews {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_schedule)
    remoteViews.setDirection(R.id.widget_schedule, context.resources)

    // An estimation, https://developer.android.com/guide/practices/ui_guidelines/widget_design.html
    val widthCells = width?.let {
        val widthDp = it / context.resources.dp
        when {
            widthDp < 180 -> 2
            widthDp < 310 -> 3
            else -> 4
        }
    } ?: 3

    // Initiate the list view
    val adapterIntent = Intent(context, ScheduleWidgetService::class.java)
    adapterIntent.putExtra(widgetWidthCellKey, widthCells)
    // Update conditions
    adapterIntent.putExtra(
        "updateToken",
        (System.currentTimeMillis().milliseconds / 1.hours).roundToInt(),
    )
    adapterIntent.putExtra("appOpenCount", resumeToken.value)
    adapterIntent.putExtra("deviceEvents", deviceCalendarEvents.hashCode())
    adapterIntent.putExtra("events", eventsRepository?.hashCode() ?: 0)
    adapterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
    adapterIntent.setData(adapterIntent.toUri(Intent.URI_INTENT_SCHEME).toUri())
    @Suppress("DEPRECATION") remoteViews.setRemoteAdapter(
        R.id.widget_schedule_content,
        adapterIntent,
    )
    remoteViews.setPendingIntentTemplate(
        R.id.widget_schedule_content,
        context.launchAppPendingIntent("CALENDAR", true),
    )

    // Add event button
    val addEventPendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, BroadcastReceivers::class.java).setAction(ADD_EVENT)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId),
        PendingIntent.FLAG_IMMUTABLE
    )
    remoteViews.setOnClickPendingIntent(R.id.add_event, addEventPendingIntent)

    // Round the background better
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        remoteViews.setViewOutlinePreferredRadiusDimen(
            R.id.widget_schedule, android.R.dimen.system_app_widget_background_radius
        )
        remoteViews.setBoolean(R.id.widget_schedule, "setClipToOutline", true)
    }

    val topPadding = ((if (widthCells > 2) 16 else 12) * context.resources.dp).roundToInt()
    val endPadding = (when {
        widthCells > 3 -> 16
        widthCells == 3 -> 8
        else -> 12
    } * context.resources.dp).roundToInt()
    remoteViews.setViewPadding(R.id.add_event_parent, endPadding, topPadding, endPadding, 0)

    return remoteViews
}

private fun createMonthViewRemoteViews(
    context: Context,
    width: Int,
    height: Int,
    hasSize: Boolean,
    today: Jdn,
): RemoteViews {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_month_view)
    remoteViews.setRoundBackground(R.id.image_background, width, height)

    val contentColor = androidx.compose.ui.graphics.Color(
        when {
            prefersWidgetsDynamicColors -> if (isSystemInDarkTheme(context.resources.configuration)) Color.WHITE else Color.BLACK

            else -> selectedWidgetTextColor
        }
    )
    val colors = MonthColors(
        contentColor = contentColor,
        appointments = androidx.compose.ui.graphics.Color(
            if (prefersWidgetsDynamicColors) context.getColor(android.R.color.system_accent1_300)
            else 0xFF376E9F.toInt()
        ),
        holidays = androidx.compose.ui.graphics.Color(
            if (prefersWidgetsDynamicColors) context.getColor(android.R.color.system_accent1_300)
            else 0xFFE51C23.toInt()
        ),
        eventIndicator = contentColor,
        currentDay = contentColor,
        textDaySelected = contentColor,
        indicator = androidx.compose.ui.graphics.Color.Transparent,
    )
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    val baseDate = mainCalendar.getMonthStartFromMonthsDistance(today, 0)
    val monthDeviceEvents: DeviceCalendarEventsStore =
        if (isShowDeviceCalendarEvents.value) context.readMonthDeviceEvents(Jdn(baseDate))
        else EventsStore.empty()
    val isRtl =
        language.value.isLessKnownRtl || language.value.asSystemLocale().layoutDirection == View.LAYOUT_DIRECTION_RTL
    val isShowWeekOfYearEnabled = isShowWeekOfYearEnabled.value
    val cellWidth = width / if (isShowWeekOfYearEnabled) 8f else 7f
    val cellHeight = height.toFloat() / 7f
    val cellRadius = min(cellWidth, cellHeight) / 2
    val mainCalendarDigitsIsArabic = mainCalendarDigits === Language.ARABIC_DIGITS
    val contentDescription = renderMonthWidget(
        dayPainter = DayPainter(
            resources = context.resources,
            width = cellWidth,
            height = cellHeight,
            isRtl = isRtl,
            colors = colors,
            isWidget = true
        ),
        width = width.toFloat(),
        canvas = canvas,
        baseDate = baseDate,
        today = today,
        deviceEvents = monthDeviceEvents,
        isRtl = isRtl,
        isShowWeekOfYearEnabled = isShowWeekOfYearEnabled,
        selectedDay = null,
        setWeekNumberText = if (hasSize && prefersWidgetsDynamicColors) { i, text ->
            val id = monthWidgetWeeks[i]
            remoteViews.setTextViewText(id, text)
            val contentDescription = context.getString(R.string.nth_week_of_year, text)
            remoteViews.setContentDescription(id, contentDescription)
            cellRadius.let {
                it * if (mainCalendarDigitsIsArabic) .8f else 1f
            }.let { remoteViews.setTextViewTextSize(id, TypedValue.COMPLEX_UNIT_PX, it * .8f) }
            remoteViews.setAlpha(id, .5f)
            remoteViews.setDynamicTextColor(id, android.R.attr.colorForeground)
        } else null,
        setText = if (hasSize && prefersWidgetsDynamicColors) { i, text, isHoliday ->
            val id = monthWidgetCells[i]
            remoteViews.setTextViewText(id, text)
            cellRadius.let {
                it * if (isShowWeekOfYearEnabled) 1.2f else 1.1f
            }.let {
                it * if (mainCalendarDigitsIsArabic) .8f else 1f
            }.let { remoteViews.setTextViewTextSize(id, TypedValue.COMPLEX_UNIT_PX, it) }
            when {
                isHoliday -> android.R.attr.colorAccent
                else -> android.R.attr.colorForeground
            }.also { remoteViews.setDynamicTextColor(id, it) }
            when {
                i < 7 -> .5f
                isHoliday -> .5f
                else -> 1f
            }.also { remoteViews.setAlpha(id, it) }
        } else null,
    )
    val footerSize = min(width, height) / 7f * 20 / 40
    if (hasSize && prefersWidgetsDynamicColors) {
        remoteViews.setTextViewText(R.id.month_year, contentDescription)
        remoteViews.setTextViewTextSize(R.id.month_year, TypedValue.COMPLEX_UNIT_PX, footerSize)
        remoteViews.setAlpha(R.id.month_year, 0.5f)
        remoteViews.setDynamicTextColor(R.id.month_year, android.R.attr.colorForeground)
        remoteViews.setViewPadding(R.id.month_year, 0, 0, 0, (height * .02f).roundToInt())
    } else {
        remoteViews.setTextViewText(R.id.month_year, "")
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { paint ->
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = footerSize
            paint.color = colors.contentColor.toArgb()
            paint.alpha = 90
        }
        canvas.drawText(contentDescription, width / 2f, height * .95f, footerPaint)
    }
    remoteViews.setImageViewBitmap(R.id.image, bitmap)
    remoteViews.setContentDescription(R.id.image, contentDescription)
    remoteViews.setViewVisibility(
        R.id.week_number_column, if (isShowWeekOfYearEnabled) View.VISIBLE else View.GONE
    )
    remoteViews.setDirection(R.id.month_grid_parent, context.resources)
    // remoteViews.setOnClickPendingIntent(R.id.image, context.launchAppPendingIntent())

    val monthStart = Jdn(baseDate)
    val weekStart = applyWeekStartOffsetToWeekDay(Jdn(baseDate).weekDay)
    val monthLength = baseDate.calendar.getMonthLength(baseDate.year, baseDate.month)
    monthWidgetCells.forEachIndexed { i, id ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !prefersWidgetsDynamicColors) {
            remoteViews.setTextViewText(id, "")
        }
        if (i !in weekStart + 7..<weekStart + 7 + monthLength) {
            if (i >= 7) {
                remoteViews.setTextViewText(id, "")
                remoteViews.setOnClickPendingIntent(id, null)
            }
            return@forEachIndexed
        }
        val jdn = monthStart + i - weekStart - 7
        val action = jdnActionKey + jdn.value
        remoteViews.setOnClickPendingIntent(id, context.launchAppPendingIntent(action))
        remoteViews.setInt(id, "setBackgroundResource", R.drawable.widget_month_day_ripple)
        if (isTalkBackEnabled) {
            val daySummary = getA11yDaySummary(
                context.resources, jdn, jdn == today, monthDeviceEvents,
                withZodiac = false, withOtherCalendars = false, withTitle = true,
            )
            remoteViews.setContentDescription(id, daySummary)
        }
    }
    return remoteViews
}

const val jdnActionKey = "JDN"
const val eventKey = "EVENT"

private val monthWidgetWeeks = listOf(
    R.id.month_grid_week0, R.id.month_grid_week1, R.id.month_grid_week2, R.id.month_grid_week3,
    R.id.month_grid_week4, R.id.month_grid_week5, R.id.month_grid_week6,
)
private val monthWidgetWeeksParents = listOf(
    R.id.month_grid_week0, R.id.month_grid_week1_parent, R.id.month_grid_week2_parent,
    R.id.month_grid_week3_parent, R.id.month_grid_week4_parent, R.id.month_grid_week5_parent,
    R.id.month_grid_week6_parent,
)

private val monthWidgetCells = listOf(
    R.id.month_grid_cell0x1, R.id.month_grid_cell0x2, R.id.month_grid_cell0x3,
    R.id.month_grid_cell0x4, R.id.month_grid_cell0x5, R.id.month_grid_cell0x6,
    R.id.month_grid_cell0x7,
    R.id.month_grid_cell1x1, R.id.month_grid_cell1x2, R.id.month_grid_cell1x3,
    R.id.month_grid_cell1x4, R.id.month_grid_cell1x5, R.id.month_grid_cell1x6,
    R.id.month_grid_cell1x7,
    R.id.month_grid_cell2x1, R.id.month_grid_cell2x2, R.id.month_grid_cell2x3,
    R.id.month_grid_cell2x4, R.id.month_grid_cell2x5, R.id.month_grid_cell2x6,
    R.id.month_grid_cell2x7,
    R.id.month_grid_cell3x1, R.id.month_grid_cell3x2, R.id.month_grid_cell3x3,
    R.id.month_grid_cell3x4, R.id.month_grid_cell3x5, R.id.month_grid_cell3x6,
    R.id.month_grid_cell3x7,
    R.id.month_grid_cell4x1, R.id.month_grid_cell4x2, R.id.month_grid_cell4x3,
    R.id.month_grid_cell4x4, R.id.month_grid_cell4x5, R.id.month_grid_cell4x6,
    R.id.month_grid_cell4x7,
    R.id.month_grid_cell5x1, R.id.month_grid_cell5x2, R.id.month_grid_cell5x3,
    R.id.month_grid_cell5x4, R.id.month_grid_cell5x5, R.id.month_grid_cell5x6,
    R.id.month_grid_cell5x7,
    R.id.month_grid_cell6x1, R.id.month_grid_cell6x2, R.id.month_grid_cell6x3,
    R.id.month_grid_cell6x4, R.id.month_grid_cell6x5, R.id.month_grid_cell6x6,
    R.id.month_grid_cell6x7,
)

private fun createMapRemoteViews(
    context: Context, width: Int, height: Int, time: Long
): RemoteViews {
    val size = min(width / 2, height)
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_map)
    val isNightMode = isSystemInDarkTheme(context.resources.configuration)
    val backgroundColor = if (prefersWidgetsDynamicColors) ColorUtils.setAlphaComponent(
        context.getColor(
            if (isNightMode) android.R.color.system_accent2_800
            else android.R.color.system_accent2_50
        ),
        (255 * (1 - widgetTransparency.value)).roundToInt().coerceIn(0, 255),
    )
    else null
    val foregroundColor = if (prefersWidgetsDynamicColors) context.getColor(
        if (isNightMode) android.R.color.system_accent1_50
        else android.R.color.system_accent1_600
    )
    else null
    val mapDraw = MapDraw(context.resources, backgroundColor, foregroundColor)
    mapDraw.markersScale = .75f
    mapDraw.updateMap(time, MapType.DAY_NIGHT)
    val matrix = Matrix()
    matrix.setScale(size * 2f / mapDraw.mapWidth, size.toFloat() / mapDraw.mapHeight)
    val bitmap = createBitmap(size * 2, size).applyCanvas {
        withClip(createRoundPath(size * 2, size, roundPixelSize)) {
            mapDraw.draw(
                canvas = this,
                matrix = matrix,
                coordinates = coordinates.value,
                displayLocation = true,
                directPathDestination = null,
                displayGrid = false
            )
        }
    }
    remoteViews.setImageViewBitmap(R.id.image, bitmap)
    remoteViews.setContentDescription(R.id.image, context.getString(R.string.map))
    remoteViews.setOnClickPendingIntent(R.id.image, context.launchAppPendingIntent("MAP"))
    return remoteViews
}

private fun createMoonRemoteViews(context: Context, width: Int, height: Int): RemoteViews {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_moon)
    val solarDraw = SolarDraw(context.resources)
    val bitmap = createBitmap(width, height).applyCanvas {
        val state = AstronomyState(GregorianCalendar())
        solarDraw.moon(
            this,
            state.sun,
            state.moon,
            width / 2f,
            height / 2f,
            min(width, height) / 2f,
            state.moonTilt,
            null /* make it always fully visible */
        )
    }
    remoteViews.setImageViewBitmap(R.id.image, bitmap)
    remoteViews.setContentDescription(R.id.image, context.getString(R.string.map))
    remoteViews.setOnClickPendingIntent(R.id.image, context.launchAppPendingIntent("ASTRONOMY"))
    return remoteViews
}

fun createSampleRemoteViews(context: Context, width: Int, height: Int): RemoteViews {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_sample)
    remoteViews.setRoundBackground(R.id.widget_sample_background, width, height)
    remoteViews.setDirection(R.id.widget_sample, context.resources)
    remoteViews.setupForegroundTextColors(
        R.id.sample_text, R.id.sample_clock, R.id.sample_clock_replacement
    )
    if (prefersWidgetsDynamicColors) remoteViews.setDynamicTextColor(
        R.id.sample_clock, android.R.attr.colorAccent
    )
    if (isWidgetClock) {
        remoteViews.setViewVisibility(R.id.sample_clock, View.VISIBLE)
        remoteViews.configureClock(R.id.sample_clock)
        remoteViews.setTextViewTextOrHideIfEmpty(R.id.sample_clock_replacement, "")
    } else {
        remoteViews.setViewVisibility(R.id.sample_clock, View.GONE)
        remoteViews.setTextViewTextOrHideIfEmpty(R.id.sample_clock_replacement, getWeekDayName(0))
    }
    remoteViews.setTextViewText(R.id.sample_text, context.getString(R.string.widget_text_color))
    return remoteViews
}

private fun create1x1RemoteViews(
    context: Context, width: Int, height: Int, date: AbstractDate
): RemoteViews {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget1x1)
    remoteViews.setRoundBackground(R.id.widget_layout1x1_background, width, height)
    remoteViews.setDirection(R.id.widget_layout1x1, context.resources)
    remoteViews.setupForegroundTextColors(R.id.textPlaceholder1_1x1, R.id.textPlaceholder2_1x1)
    if (prefersWidgetsDynamicColors) remoteViews.setDynamicTextColor(
        R.id.textPlaceholder1_1x1, android.R.attr.colorAccent
    )
    remoteViews.setTextViewText(R.id.textPlaceholder1_1x1, formatNumber(date.dayOfMonth))
    remoteViews.setTextViewText(R.id.textPlaceholder2_1x1, date.monthName)
    remoteViews.setOnClickPendingIntent(R.id.widget_layout1x1, context.launchAppPendingIntent())
    return remoteViews
}

private fun create4x1RemoteViews(
    context: Context,
    width: Int,
    height: Int,
    jdn: Jdn,
    date: AbstractDate,
    widgetTitle: String,
    subtitle: String
): RemoteViews {
    val weekDayName = jdn.weekDayName
    val showOtherCalendars = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    val mainDateString = formatDate(date, calendarNameInLinear = showOtherCalendars)
    val remoteViews = RemoteViews(
        context.packageName, if (isWidgetClock) {
            if (isCenterAlignWidgets) R.layout.widget4x1_clock_center else R.layout.widget4x1_clock
        } else {
            if (isCenterAlignWidgets) R.layout.widget4x1_center else R.layout.widget4x1
        }
    )
    if (isWidgetClock) remoteViews.configureClock(R.id.textPlaceholder1_4x1)
    remoteViews.setRoundBackground(R.id.widget_layout4x1_background, width, height)
    remoteViews.setDirection(R.id.widget_layout4x1, context.resources)
    remoteViews.setupForegroundTextColors(
        R.id.textPlaceholder1_4x1, R.id.textPlaceholder2_4x1, R.id.textPlaceholder3_4x1
    )
    if (prefersWidgetsDynamicColors) remoteViews.setDynamicTextColor(
        R.id.textPlaceholder1_4x2, android.R.attr.colorAccent
    )

    if (!isWidgetClock) remoteViews.setTextViewText(R.id.textPlaceholder1_4x1, weekDayName)
    remoteViews.setTextViewText(R.id.textPlaceholder2_4x1, buildString {
        append(if (isWidgetClock) widgetTitle else mainDateString)
        if (showOtherCalendars) append(spacedComma + subtitle)
    })
    remoteViews.setTextViewText(
        R.id.textPlaceholder3_4x1,
        if (isWidgetClock && isForcedIranTimeEnabled.value) "(" + context.getString(R.string.iran_time) + ")" else ""
    )
    remoteViews.setOnClickPendingIntent(R.id.widget_layout4x1, context.launchAppPendingIntent())
    return remoteViews
}

private fun create2x2RemoteViews(
    context: Context,
    width: Int,
    height: Int,
    jdn: Jdn,
    date: AbstractDate,
    widgetTitle: String,
    subtitle: String,
    owghat: String
): RemoteViews {
    val weekDayName = jdn.weekDayName
    val showOtherCalendars = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    val mainDateString = formatDate(date, calendarNameInLinear = showOtherCalendars)
    val remoteViews = RemoteViews(
        context.packageName, if (isWidgetClock) {
            if (isCenterAlignWidgets) R.layout.widget2x2_clock_center else R.layout.widget2x2_clock
        } else {
            if (isCenterAlignWidgets) R.layout.widget2x2_center else R.layout.widget2x2
        }
    )
    if (isWidgetClock) remoteViews.configureClock(R.id.time_2x2)
    remoteViews.setRoundBackground(R.id.widget_layout2x2_background, width, height)
    remoteViews.setDirection(R.id.widget_layout2x2, context.resources)
    remoteViews.setupForegroundTextColors(
        R.id.time_2x2, R.id.date_2x2, R.id.event_2x2, R.id.owghat_2x2
    )
    if (prefersWidgetsDynamicColors) remoteViews.setDynamicTextColor(
        R.id.time_2x2, android.R.attr.colorAccent
    )

    setEventsInWidget(context.resources, jdn, remoteViews, R.id.holiday_2x2, R.id.event_2x2)

    if (OWGHAT_KEY in whatToShowOnWidgets && owghat.isNotEmpty()) {
        remoteViews.setTextViewText(R.id.owghat_2x2, owghat)
        remoteViews.setViewVisibility(R.id.owghat_2x2, View.VISIBLE)
    } else {
        remoteViews.setViewVisibility(R.id.owghat_2x2, View.GONE)
    }

    if (!isWidgetClock) remoteViews.setTextViewText(R.id.time_2x2, weekDayName)
    remoteViews.setTextViewText(R.id.date_2x2, buildString {
        append(if (isWidgetClock) widgetTitle else mainDateString)
        if (showOtherCalendars) appendLine().append(subtitle)
    })

    remoteViews.setOnClickPendingIntent(R.id.widget_layout2x2, context.launchAppPendingIntent())
    return remoteViews
}

@IdRes
private val widget4x2TimesViewsIds = listOf(
    R.id.textPlaceholder4owghat_1_4x2,
    R.id.textPlaceholder4owghat_15_4x2,
    R.id.textPlaceholder4owghat_2_4x2,
    R.id.textPlaceholder4owghat_3_4x2,
    R.id.textPlaceholder4owghat_4_4x2,
    R.id.textPlaceholder4owghat_5_4x2
)

@IdRes
private val widgetNTimesViewsIds = listOf(
    R.id.textPlaceholder4owghat_1_4x2,
    R.id.textPlaceholder4owghat_15_4x2,
    R.id.textPlaceholder4owghat_2_4x2,
    R.id.textPlaceholder4owghat_3_4x2,
    R.id.textPlaceholder4owghat_4_4x2,
    R.id.textPlaceholder4owghat_5_4x2
)

private fun create4x2RemoteViews(
    context: Context,
    width: Int,
    height: Int,
    jdn: Jdn,
    date: AbstractDate,
    clock: Clock,
    prayTimes: PrayTimes?
): RemoteViews {
    val weekDayName = jdn.weekDayName
    val showOtherCalendars = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    val remoteViews = RemoteViews(
        context.packageName, if (isWidgetClock) R.layout.widget4x2_clock else R.layout.widget4x2
    )

    if (isWidgetClock) remoteViews.configureClock(R.id.textPlaceholder0_4x2)
    remoteViews.setRoundBackground(R.id.widget_layout4x2_background, width, height)
    remoteViews.setDirection(R.id.widget_layout4x2, context.resources)

    remoteViews.setupForegroundTextColors(
        R.id.textPlaceholder0_4x2,
        R.id.textPlaceholder1_4x2,
        R.id.textPlaceholder2_4x2,
        R.id.textPlaceholder4owghat_3_4x2,
        R.id.textPlaceholder4owghat_1_4x2,
        R.id.textPlaceholder4owghat_4_4x2,
        R.id.textPlaceholder4owghat_2_4x2,
        R.id.textPlaceholder4owghat_5_4x2,
        R.id.event_4x2
    )
    if (prefersWidgetsDynamicColors) remoteViews.setDynamicTextColor(
        R.id.textPlaceholder0_4x2, android.R.attr.colorAccent
    )

    if (!isWidgetClock) remoteViews.setTextViewText(R.id.textPlaceholder0_4x2, weekDayName)
    remoteViews.setTextViewText(R.id.textPlaceholder1_4x2, buildString {
        if (isWidgetClock) append(jdn.weekDayName + "\n")
        append(formatDate(date, calendarNameInLinear = showOtherCalendars))
        if (showOtherCalendars) appendLine().append(dateStringOfOtherCalendars(jdn, "\n"))
    })

    if (prayTimes != null && OWGHAT_KEY in whatToShowOnWidgets) {
        // Set text of owghats
        val owghats = widget4x2TimesViewsIds.zip(
            listOf(
                PrayTime.FAJR,
                PrayTime.SUNRISE,
                PrayTime.DHUHR,
                PrayTime.ASR,
                PrayTime.MAGHRIB,
                PrayTime.ISHA
            )
        ) { textHolderViewId, prayTime ->
            val timeClock = prayTimes[prayTime]
            remoteViews.setTextViewText(
                textHolderViewId,
                context.getString(prayTime.stringRes) + "\n" + timeClock.toFormattedString(printAmPm = false)
            )
            remoteViews.setupForegroundTextColors(textHolderViewId)
            Triple(textHolderViewId, prayTime.stringRes, timeClock)
        }
        val (nextViewId, nextOwghatId, timeClock) = owghats.firstOrNull { (_, _, timeClock) ->
            timeClock > clock
        } ?: owghats[0]

        owghats.forEach { (viewId) ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) remoteViews.setAlpha(viewId, 1f)
            if (viewId != nextViewId) {
                if (prefersWidgetsDynamicColors) {
                    remoteViews.setAlpha(viewId, .6f)
                    remoteViews.setDynamicTextColor(viewId)
                } else remoteViews.setTextColor(
                    viewId, ColorUtils.setAlphaComponent(selectedWidgetTextColor, 180)
                )
            } else if (prefersWidgetsDynamicColors) remoteViews.setupForegroundTextColors(viewId)
            else remoteViews.setTextColor(viewId, selectedWidgetNextAthanTextColor)
        }

        val difference = timeClock - clock
        remoteViews.setTextViewText(
            R.id.textPlaceholder2_4x2, context.getString(
                R.string.n_till,
                (if (difference.value < .0) difference + Clock(24.0) else difference).asRemainingTime(
                    context.resources
                ),
                context.getString(nextOwghatId)
            )
        )

        remoteViews.setImageViewResource(R.id.refresh_icon, R.drawable.ic_widget_refresh)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, Widget4x2::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        remoteViews.setOnClickPendingIntent(R.id.refresh_wrapper, pendingIntent)

        remoteViews.setViewVisibility(R.id.widget4x2_owghat, View.VISIBLE)
    } else remoteViews.setViewVisibility(R.id.widget4x2_owghat, View.GONE)

    setEventsInWidget(context.resources, jdn, remoteViews, R.id.holiday_4x2, R.id.event_4x2)

    remoteViews.setOnClickPendingIntent(R.id.widget_layout4x2, context.launchAppPendingIntent())
    return remoteViews
}

private fun timesToShow(clock: Clock, prayTimes: PrayTimes): List<PrayTime> {
    return if (calculationMethod.value.isJafari) {
        if (clock.value in prayTimes.dhuhr..prayTimes.isha) {
            PrayTime.timesBetweenDhuhrAndIshaForJafari
        } else PrayTime.timesNotBetweenDhuhrAndIshaForJafari
    } else PrayTime.athans
}

private fun setEventsInWidget(
    resources: Resources, jdn: Jdn, remoteViews: RemoteViews, holidaysId: Int, eventsId: Int
) {
    val events = eventsRepository?.getEvents(jdn, deviceCalendarEvents) ?: emptyList()
    val holidays = getEventsTitle(
        events,
        holiday = true,
        compact = true,
        showDeviceCalendarEvents = true,
        insertRLM = resources.isRtl,
        addIsHoliday = isHighTextContrastEnabled
    )
    remoteViews.setTextViewTextOrHideIfEmpty(holidaysId, holidays)
    if (isTalkBackEnabled) remoteViews.setContentDescription(
        holidaysId, resources.getString(R.string.holiday_reason, holidays)
    )

    val nonHolidays = if (NON_HOLIDAYS_EVENTS_KEY in whatToShowOnWidgets) getEventsTitle(
        events,
        holiday = false,
        compact = true,
        showDeviceCalendarEvents = true,
        insertRLM = resources.isRtl,
        addIsHoliday = false
    ) else ""
    remoteViews.setTextViewTextOrHideIfEmpty(eventsId, nonHolidays)

    if (!prefersWidgetsDynamicColors) remoteViews.setInt(
        holidaysId, "setTextColor", 0xFFFF8A65.toInt()
    )
}

private var latestPostedNotification: NotificationData? = null

private fun updateNotification(
    context: Context, title: String, subtitle: String, jdn: Jdn, date: AbstractDate, owghat: String,
    prayTimes: PrayTimes?, clock: Clock,
) {
    if (!isNotifyDate.value) {
        val notificationManager = context.getSystemService<NotificationManager>()
        notificationManager?.cancel(NOTIFICATION_ID_LOW_PRIORITY)
        notificationManager?.cancel(NOTIFICATION_ID_DEFAULT_PRIORITY)
        return
    }

    val timesToShow = if (prayTimes != null && OWGHAT_KEY in whatToShowOnWidgets) {
        timesToShow(clock, prayTimes)
    } else null

    val nextPrayTime =
        if (prayTimes == null || timesToShow == null) null else timesToShow.map { it to prayTimes[it] }
            .firstOrNull { (_, timeClock) -> timeClock > clock }?.first ?: timesToShow[0]

    val notificationData = NotificationData(
        title = title,
        subtitle = subtitle,
        jdn = jdn,
        date = date,
        owghat = owghat,
        prayTimes = prayTimes,
        nextPrayTime = nextPrayTime,
        timesToShow = timesToShow,
        isRtl = context.resources.isRtl,
        events = eventsRepository?.getEvents(jdn, deviceCalendarEvents) ?: emptyList(),
        isTalkBackEnabled = isTalkBackEnabled,
        isHighTextContrastEnabled = isHighTextContrastEnabled,
        isNotifyDateOnLockScreen = isNotifyDateOnLockScreen.value,
        deviceCalendarEventsList = deviceCalendarEvents.getAllEvents(),
        whatToShowOnWidgets = whatToShowOnWidgets,
        spacedComma = spacedComma,
        language = language.value,
        notificationId = if (useDefaultPriority) NOTIFICATION_ID_DEFAULT_PRIORITY else NOTIFICATION_ID_LOW_PRIORITY
    )
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || // always update as complains in 8.3.0
        latestPostedNotification != notificationData
    ) {
        if (notificationData.post(context)) latestPostedNotification = notificationData
    }
}

private val notificationTimesViewsIds = listOf(
    R.id.head1 to R.id.time1, R.id.head2 to R.id.time2, R.id.head3 to R.id.time3,
    R.id.head4 to R.id.time4, R.id.head5 to R.id.time5,
)

private data class NotificationData(
    private val title: String,
    private val subtitle: String,
    private val jdn: Jdn,
    private val date: AbstractDate,
    private val owghat: String,
    private val prayTimes: PrayTimes?,
    private val nextPrayTime: PrayTime?,
    private val timesToShow: List<PrayTime>?,
    private val isRtl: Boolean,
    private val events: List<CalendarEvent<*>>,
    private val isTalkBackEnabled: Boolean,
    private val isHighTextContrastEnabled: Boolean,
    private val isNotifyDateOnLockScreen: Boolean,
    private val deviceCalendarEventsList: List<CalendarEvent.DeviceCalendarEvent>,
    private val whatToShowOnWidgets: Set<String>,
    private val spacedComma: String,
    private val language: Language,
    private val notificationId: Int,
) {
    @CheckResult
    fun post(context: Context): Boolean {
        val notificationManager = context.getSystemService<NotificationManager>() ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationId.toString(),
                context.getString(R.string.app_name),
                if (useDefaultPriority) NotificationManager.IMPORTANCE_DEFAULT
                else NotificationManager.IMPORTANCE_LOW
            )
            if (useDefaultPriority) {
                channel.setSound(null, null)
                channel.enableVibration(false)
            }
            channel.setShowBadge(false)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, notificationId.toString())
            .setPriority(NotificationCompat.PRIORITY_LOW).setOngoing(true).setWhen(0)
            .setContentIntent(context.launchAppPendingIntent()).setVisibility(
                if (isNotifyDateOnLockScreen) NotificationCompat.VISIBILITY_PUBLIC
                else NotificationCompat.VISIBILITY_SECRET
            ).setContentTitle(title).setContentText(
                when {
                    isTalkBackEnabled -> getA11yDaySummary(
                        resources = context.resources,
                        jdn = jdn,
                        isToday = false, // Don't set isToday, per a feedback
                        deviceCalendarEvents = deviceCalendarEvents,
                        withZodiac = true,
                        withOtherCalendars = true,
                        withTitle = false
                    ) + if (owghat.isEmpty()) "" else spacedComma + owghat

                    else -> subtitle
                },
            )

        // Dynamic small icon generator, most of the times disabled as it needs API 23 and
        // we need to have the other path anyway
        if (language.isNepali && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val icon = IconCompat.createWithBitmap(createStatusIcon(date.dayOfMonth))
            builder.setSmallIcon(icon)
        } else {
            builder.setSmallIcon(getDayIconResource(date.dayOfMonth))
        }

        // Night mode doesn't like our custom notifications in Samsung and HTC One UI,
        // apparently fixed in newer version of Samsung UI
        val shouldDisableCustomNotification = when (Build.BRAND) {
            "samsung", "htc" -> isSystemInDarkTheme(context.resources.configuration)
            else -> false
        } && Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE

        if (!isTalkBackEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val holidays = getEventsTitle(
                events,
                holiday = true,
                compact = true,
                showDeviceCalendarEvents = true,
                insertRLM = isRtl,
                addIsHoliday = shouldDisableCustomNotification || isHighTextContrastEnabled
            )

            val nonHolidays = if (NON_HOLIDAYS_EVENTS_KEY in whatToShowOnWidgets) getEventsTitle(
                events,
                holiday = false,
                compact = true,
                showDeviceCalendarEvents = true,
                insertRLM = isRtl,
                addIsHoliday = false
            ) else ""

            val notificationOwghat = if (OWGHAT_KEY in whatToShowOnWidgets) owghat else ""

            if (shouldDisableCustomNotification) {
                val content = listOf(
                    dateStringOfOtherCalendars(jdn, spacedComma),
                    holidays.trim(),
                    nonHolidays,
                    notificationOwghat
                ).filter { it.isNotBlank() }.joinToString("\n")
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(content))
            } else {
                builder.setCustomContentView(
                    RemoteViews(
                        context.packageName, R.layout.custom_notification
                    ).also {
                        it.setDirection(R.id.custom_notification_root, context.resources)
                        it.setTextViewText(R.id.title, title)
                        it.setTextViewText(R.id.body, subtitle)
                    },
                )

                if (holidays.isNotBlank() || nonHolidays.isNotBlank() || timesToShow != null) builder.setCustomBigContentView(
                    RemoteViews(
                        context.packageName, R.layout.custom_notification_big
                    ).also {
                        it.setDirection(R.id.custom_notification_root, context.resources)
                        it.setTextViewText(R.id.title, title)
                        it.setTextViewTextOrHideIfEmpty(
                            R.id.body, dateStringOfOtherCalendars(jdn, spacedComma)
                        )
                        it.setTextViewTextOrHideIfEmpty(R.id.holidays, holidays)
                        it.setTextViewTextOrHideIfEmpty(R.id.nonholidays, nonHolidays)
                        it.setViewVisibility(
                            R.id.times, if (timesToShow == null) View.GONE else View.VISIBLE
                        )
                        if (timesToShow != null && prayTimes != null) notificationTimesViewsIds.zip(
                            timesToShow,
                        ) { (headViewId, timeViewId), prayTime ->
                            it.setTextViewText(
                                headViewId, context.getString(prayTime.stringRes)
                            )
                            it.setTextViewText(
                                timeViewId, prayTimes[prayTime].toFormattedString(printAmPm = false)
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val alpha = if (prayTime == nextPrayTime) 1f else .6f
                                it.setAlpha(headViewId, alpha)
                                it.setAlpha(timeViewId, alpha)
                            }
                        }
                    },
                )

                builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
            }
        }

        if (BuildConfig.DEVELOPMENT) builder.setWhen(System.currentTimeMillis())

        val notification = builder.build()

        // https://stackoverflow.com/a/40708431
        // But this hasn't fixed the issue for the user who has reported it or perhaps anyone :/
        val deviceBrand = Build.BRAND.lowercase()
        @SuppressLint("PrivateApi") if (deviceBrand == "redmi" || deviceBrand == "xiaomi") runCatching {
            val miuiNotification =
                Class.forName("android.app.MiuiNotification").getDeclaredConstructor().newInstance()
            val customizedIconField =
                miuiNotification::class.java.getDeclaredField("customizedIcon")
            customizedIconField.isAccessible = true
            customizedIconField.set(miuiNotification, true)

            val extraNotificationField = notification::class.java.getField("extraNotification")
            extraNotificationField.isAccessible = true
            extraNotificationField.set(notification, miuiNotification)
        }.onFailure(logException)

        notificationManager.notify(notificationId, notification)
        return true
    }
}

private fun RemoteViews.setRoundBackground(
    @IdRes viewId: Int,
    width: Int,
    height: Int,
    @ColorInt color: Int = selectedWidgetBackgroundColor
) {
    when {
        prefersWidgetsDynamicColors -> {
            setImageViewResource(viewId, R.drawable.widget_background)
            setAlpha(viewId, 1 - widgetTransparency.value)
        }

        color == DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR -> setImageViewResource(viewId, 0)
        else -> {
            val roundBackground = createRoundedBitmap(width, height, color, roundPixelSize)
            setImageViewBitmap(viewId, roundBackground)
        }
    }
}

fun RemoteViews.setDirection(@IdRes viewId: Int, resources: Resources) {
    val direction = when {
        // Apply RTL for Arabic script locales anyway just in case something went wrong
        language.value.isArabicScript -> View.LAYOUT_DIRECTION_RTL
        else -> resources.configuration.layoutDirection
    }
    setInt(viewId, "setLayoutDirection", direction)
}

private fun RemoteViews.configureClock(@IdRes viewId: Int) {
    if (isForcedIranTimeEnabled.value) setString(viewId, "setTimeZone", IRAN_TIMEZONE_ID)
    val clockFormat = if (clockIn24) "kk:mm" else "h:mm"
    setCharSequence(viewId, "setFormat12Hour", clockFormat)
    setCharSequence(viewId, "setFormat24Hour", clockFormat)
}

@RequiresApi(Build.VERSION_CODES.S)
private fun RemoteViews.setDynamicTextColor(
    @IdRes id: Int, @AttrRes attr: Int = android.R.attr.colorForeground
): Unit = setColorAttr(id, "setTextColor", attr)

@RequiresApi(Build.VERSION_CODES.S)
private fun RemoteViews.setAlpha(@IdRes viewId: Int, value: Float): Unit =
    setFloat(viewId, "setAlpha", value)

private fun RemoteViews.setupForegroundTextColors(@IdRes vararg ids: Int) {
    ids.forEach {
        if (prefersWidgetsDynamicColors) setDynamicTextColor(it)
        else setTextColor(it, selectedWidgetTextColor)
    }
}

private fun RemoteViews.setTextViewTextOrHideIfEmpty(viewId: Int, text: CharSequence) {
    if (text.isBlank()) setViewVisibility(viewId, View.GONE)
    else {
        setViewVisibility(viewId, View.VISIBLE)
        setTextViewText(viewId, text.trim())
    }
}

fun Context.launchAppPendingIntent(
    action: String? = null,
    isMutable: Boolean = false,
): PendingIntent? {
    return PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).setAction(action)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.let {
                if (action != null) it or Intent.FLAG_ACTIVITY_CLEAR_TASK else it
            }),
        PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) (if (isMutable) PendingIntent.FLAG_MUTABLE
        else PendingIntent.FLAG_IMMUTABLE)
        else 0
    )
}

private fun createNRemoteViews(
    context: Context, width: Int, height: Int, clock: Clock, prayTimes: PrayTimes?
): RemoteViews {
    val title = "${context.resources.getString(R.string.today)} :" + " ${
        dayTitleSummary(
            Jdn.today(), Jdn.today() on mainCalendar
        )
    }" + " - ${context.preferences.getString(PREF_GEOCODED_CITYNAME, "")}"

    val remoteViews = RemoteViews(context.packageName, R.layout.n_widget)
    remoteViews.setRoundBackground(R.id.n_widget_background, width, height)
    remoteViews.setDirection(R.id.n_widget_layout, context.resources)

    remoteViews.setupForegroundTextColors(
        R.id.textPlaceholder4owghat_3_4x2,
        R.id.textPlaceholder4owghat_1_4x2,
        R.id.textPlaceholder4owghat_15_4x2,
        R.id.textPlaceholder4owghat_4_4x2,
        R.id.textPlaceholder4owghat_2_4x2,
        R.id.textPlaceholder4owghat_5_4x2,
        R.id.n_widget_title
    )

    remoteViews.setTextViewText(R.id.n_widget_title, title)

    prayTimes ?: return remoteViews

    val owghats = widgetNTimesViewsIds.zip(
        listOf(
            PrayTime.FAJR,
            PrayTime.SUNRISE,
            PrayTime.DHUHR,
            PrayTime.ASR,
            PrayTime.MAGHRIB,
            PrayTime.ISHA
        )
    ) { textHolderViewId, prayTime ->
        val timeClock = prayTimes[prayTime]
        remoteViews.setTextViewText(
            textHolderViewId,
            context.getString(prayTime.stringRes) + "\n" + timeClock.toFormattedString(printAmPm = false)
        )
        remoteViews.setupForegroundTextColors(textHolderViewId)
        Triple(textHolderViewId, prayTime.stringRes, timeClock)
    }
    val (nextViewId, _, _) = owghats.firstOrNull { (_, _, timeClock) ->
        timeClock > clock
    } ?: owghats[0]
    owghats.forEach { (viewId) ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) remoteViews.setAlpha(viewId, 1f)
        if (viewId != nextViewId) {
            if (prefersWidgetsDynamicColors) {
                remoteViews.setAlpha(viewId, .6f)
                remoteViews.setDynamicTextColor(viewId)
            } else remoteViews.setTextColor(
                viewId, ColorUtils.setAlphaComponent(selectedWidgetTextColor, 180)
            )
        } else if (prefersWidgetsDynamicColors) remoteViews.setupForegroundTextColors(viewId)
        else remoteViews.setTextColor(viewId, selectedWidgetNextAthanTextColor)
    }
    remoteViews.setOnClickPendingIntent(R.id.n_widget_layout, context.launchAppPendingIntent())
    return remoteViews
}

private fun Context.launchAgeWidgetConfigurationAppPendingIntent(widgetId: Int): PendingIntent? {
    return PendingIntent.getActivity(
        this,
        0,
        Intent(
            this, AgeWidgetConfigureActivity::class.java
        ).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )
}
