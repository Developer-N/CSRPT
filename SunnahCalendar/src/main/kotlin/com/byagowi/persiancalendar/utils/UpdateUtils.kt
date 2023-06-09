package com.byagowi.persiancalendar.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withClip
import androidx.core.view.drawToBitmap
import com.byagowi.persiancalendar.AgeWidget
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.NON_HOLIDAYS_EVENTS_KEY
import com.byagowi.persiancalendar.NWidget
import com.byagowi.persiancalendar.OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.OWGHAT_KEY
import com.byagowi.persiancalendar.OWGHAT_LOCATION_KEY
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_SELECTED_DATE_AGE_WIDGET
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_TITLE_AGE_WIDGET
import com.byagowi.persiancalendar.PREF_WIDGETS_PREFER_SYSTEM_COLORS
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.RLM
import com.byagowi.persiancalendar.Widget1x1
import com.byagowi.persiancalendar.Widget2x2
import com.byagowi.persiancalendar.Widget4x1
import com.byagowi.persiancalendar.Widget4x2
import com.byagowi.persiancalendar.WidgetMap
import com.byagowi.persiancalendar.WidgetMonthView
import com.byagowi.persiancalendar.WidgetMoon
import com.byagowi.persiancalendar.WidgetSunView
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.clockIn24
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isCenterAlignWidgets
import com.byagowi.persiancalendar.global.isForcedIranTimeEnabled
import com.byagowi.persiancalendar.global.isHighTextContrastEnabled
import com.byagowi.persiancalendar.global.isNotifyDate
import com.byagowi.persiancalendar.global.isNotifyDateOnLockScreen
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.isWidgetClock
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.global.whatToShowOnWidgets
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.astronomy.AstronomyState
import com.byagowi.persiancalendar.ui.calendar.calendarpager.MonthView
import com.byagowi.persiancalendar.ui.calendar.times.SunView
import com.byagowi.persiancalendar.ui.common.SolarDraw
import com.byagowi.persiancalendar.ui.map.MapDraw
import com.byagowi.persiancalendar.ui.map.MapType
import com.byagowi.persiancalendar.ui.settings.agewidget.AgeWidgetConfigureActivity
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.prepareViewForRendering
import com.byagowi.persiancalendar.variants.debugLog
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearancePathProvider
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import java.util.*
import kotlin.math.min


private const val NOTIFICATION_ID = 1001
private var pastDate: AbstractDate? = null
private var deviceCalendarEvents: DeviceCalendarEventsStore = EventsStore.empty()

@ColorInt
private var selectedWidgetTextColor = DEFAULT_SELECTED_WIDGET_TEXT_COLOR

@ColorInt
private var selectedWidgetBackgroundColor = DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
private var prefersWidgetsDynamicColors = false

// Is called from MainActivity to make sure is updated, probably should be removed however
fun readAndStoreDeviceCalendarEventsOfTheDay(context: Context) {
    runCatching { deviceCalendarEvents = context.readDayDeviceEvents(Jdn.today()) }
        .onFailure(logException)
}

private var latestFiredUpdate = 0L

// https://developer.android.com/about/versions/12/features/widgets#ensure-compatibility
// Apply a round corner which is the default in Android 12
// 16dp on pre-12, but Android 12 is more, is a bit ugly to have it as a global variable
private var roundPixelSize = 0f

fun update(context: Context, updateDate: Boolean) {
    val now = System.currentTimeMillis()
    if (!updateDate && now - latestFiredUpdate < HALF_SECOND_IN_MILLIS) {
        debugLog("UpdateUtils: skip update")
        return
    }
    latestFiredUpdate = now

    debugLog("UpdateUtils: update")
    applyAppLanguage(context)

    val jdn = Jdn.today()
    val date = jdn.toCalendar(mainCalendar)

    if (pastDate == null || pastDate != date || updateDate) {
        debugLog("UpdateUtils: date has changed")
        scheduleAlarms(context)
        pastDate = date
        readAndStoreDeviceCalendarEventsOfTheDay(context)
    }

    val shiftWorkTitle = getShiftWorkTitle(jdn, false)
    val title = dayTitleSummary(jdn, date) +
            if (shiftWorkTitle.isEmpty()) "" else " ($shiftWorkTitle)"
    val widgetTitle = dayTitleSummary(
        jdn, date, calendarNameInLinear = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    ) + if (shiftWorkTitle.isEmpty()) "" else " ($shiftWorkTitle)"
    val subtitle = dateStringOfOtherCalendars(jdn, spacedComma)

    val prefs = context.appPrefs

    // region owghat calculations
    val nowClock = Clock(Date().toGregorianCalendar(forceLocalTime = true))
    var prayTimes = coordinates.value?.calculatePrayTimes()
    prayTimes = PrayTimeProvider(context).nReplace(prayTimes, jdn)
    prayTimes ?: return
    @StringRes
    val nextOwghatId = prayTimes.getNextOwghatTimeId(nowClock)
    val owghat = buildString {
        append(context.getString(nextOwghatId))
        append(": ")
        append(prayTimes.getFromStringId(nextOwghatId).toFormattedString())
        if (OWGHAT_LOCATION_KEY in whatToShowOnWidgets) prefs.cityName?.also { append(" ($it)") }
    }
    // endregion

    selectedWidgetTextColor = getWidgetTextColor(prefs)
    selectedWidgetBackgroundColor = getWidgetBackgroundColor(prefs)
    prefersWidgetsDynamicColors = Theme.isDynamicColor(prefs) &&
            prefs.getBoolean(PREF_WIDGETS_PREFER_SYSTEM_COLORS, true)

    roundPixelSize =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) 16 * context.resources.dp
        else context.resources.getDimensionPixelSize(
            android.R.dimen.system_app_widget_background_radius
        ).toFloat()

    // Widgets
    AppWidgetManager.getInstance(context).run {
        updateFromRemoteViews<AgeWidget>(context) { width, height, widgetId ->
            createAgeRemoteViews(context, width, height, widgetId)
        }
        updateFromRemoteViews<Widget1x1>(context) { width, height, _ ->
            create1x1RemoteViews(context, width, height, date)
        }
        updateFromRemoteViews<Widget4x1>(context) { width, height, _ ->
            create4x1RemoteViews(context, width, height, jdn, date, widgetTitle, subtitle)
        }
        updateFromRemoteViews<Widget2x2>(context) { width, height, _ ->
            create2x2RemoteViews(context, width, height, jdn, date, widgetTitle, subtitle, owghat)
        }
        updateFromRemoteViews<Widget4x2>(context) { width, height, _ ->
            create4x2RemoteViews(context, width, height, jdn, date, nowClock, prayTimes)
        }
        updateFromRemoteViews<WidgetSunView>(context) { width, height, _ ->
            createSunViewRemoteViews(context, width, height, jdn, prayTimes)
        }
        updateFromRemoteViews<WidgetMonthView>(context) { width, height, _ ->
            createMonthViewRemoteViews(context, width, height, date)
        }
        updateFromRemoteViews<WidgetMap>(context) { width, height, _ ->
            createMapRemoteViews(context, width, height, now)
        }
        updateFromRemoteViews<WidgetMoon>(context) { width, height, _ ->
            createMoonRemoteViews(context, width, height)
        }
        updateFromRemoteViews<NWidget>(context) { width, height, _ ->
            createNRemoteViews(context, width, height, nowClock, prayTimes)
        }
    }
//    val next = prayTimes.getNextOwghatTimeId(nowClock)
//    val timeClock = prayTimes.getFromStringId(next)
//    val difference = timeClock.toMinutes() - nowClock.toMinutes()
//    val to = String.format(
//        context.getString(R.string.till),
//        Clock.fromMinutesCount(if (difference > 0) difference else difference + 60 * 24)
//            .asRemainingTime(context.resources)
//    )

    // Notification
    updateNotification(
        context, context.getString(R.string.today) + " " + title, subtitle, jdn, date, owghat, now
    )
}

@StringRes
public fun PrayTimes.getNextOwghatTimeId(current: Clock): Int {
    val clock = current.toHoursFraction()
    val isJafari = calculationMethod.isJafari
    return when {
        fajr > clock -> R.string.fajr
        sunrise > clock -> R.string.sunrise
        dhuhr > clock -> R.string.dhuhr
        // No need to show Asr for Jafari calculation methods
        !isJafari && asr > clock -> R.string.asr
        // Sunset and Maghrib are different only in Jafari, skip if isn't Jafari
        isJafari && sunset > clock -> R.string.sunset
        maghrib > clock -> R.string.maghrib
        // No need to show Isha for Jafari calculation methods
        !isJafari && isha > clock -> R.string.isha
        midnight > clock -> R.string.midnight
        // TODO: this is today's, not tomorrow
        else -> R.string.fajr
    }
}

fun AppWidgetManager.getWidgetSize(context: Context, widgetId: Int): Pair<Int, Int> {
    // https://stackoverflow.com/a/69080699
    val isPortrait = context.resources.configuration.orientation == ORIENTATION_PORTRAIT
    val (width, height) = listOf(
        if (isPortrait) AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH
        else AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,
        if (isPortrait) AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT
        else AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT
    ).map { (getAppWidgetOptions(widgetId).getInt(it, 0) * context.resources.dp).toInt() }
    // Crashes terribly if is below zero, let's make sure that won't happen till we understand it better
    return if (width > 10 && height > 10) width to height else 250 to 250
}

private inline fun <reified T> AppWidgetManager.updateFromRemoteViews(
    context: Context, widgetUpdateAction: (width: Int, height: Int, widgetId: Int) -> RemoteViews
) {
    runCatching {
        getAppWidgetIds(ComponentName(context, T::class.java))?.forEach { widgetId ->
            val (width, height) = getWidgetSize(context, widgetId)
            updateAppWidget(widgetId, widgetUpdateAction(width, height, widgetId))
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
    val appearanceModel = ShapeAppearanceModel().withCornerSize(roundSize)
    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
    ShapeAppearancePathProvider().calculatePath(appearanceModel, 1f, rect, roundPath)
    return roundPath
}

private fun createRoundDrawable(@ColorInt color: Int, roundSize: Float): Drawable {
    val shapeDrawable = MaterialShapeDrawable()
    shapeDrawable.fillColor = ColorStateList.valueOf(color)
    shapeDrawable.shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(roundSize)
    return shapeDrawable
}

private fun getWidgetBackgroundColor(
    prefs: SharedPreferences, key: String = PREF_SELECTED_WIDGET_BACKGROUND_COLOR
) = prefs.getString(key, null)?.let(Color::parseColor)
    ?: DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR

private fun getWidgetTextColor(
    prefs: SharedPreferences, key: String = PREF_SELECTED_WIDGET_TEXT_COLOR
) = prefs.getString(key, null)?.let(Color::parseColor)
    ?: DEFAULT_SELECTED_WIDGET_TEXT_COLOR

fun createAgeRemoteViews(context: Context, width: Int, height: Int, widgetId: Int): RemoteViews {
    val appPrefs = context.appPrefs
    val baseJdn = appPrefs.getJdnOrNull(PREF_SELECTED_DATE_AGE_WIDGET + widgetId) ?: Jdn.today()
    val title = appPrefs.getString(PREF_TITLE_AGE_WIDGET + widgetId, null) ?: ""
    val subtitle = calculateDaysDifference(context.resources, baseJdn)
    val textColor = getWidgetTextColor(appPrefs, PREF_SELECTED_WIDGET_TEXT_COLOR + widgetId)
    val backgroundColor = getWidgetBackgroundColor(
        appPrefs, PREF_SELECTED_WIDGET_BACKGROUND_COLOR + widgetId
    )
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_age)
    remoteViews.setRoundBackground(R.id.age_widget_background, width, height, backgroundColor)
    remoteViews.setDirection(R.id.age_widget_root, context)
    remoteViews.setTextViewTextOrHideIfEmpty(R.id.textview_age_widget_title, title)
    remoteViews.setTextViewText(R.id.textview_age_widget, subtitle)
    listOf(R.id.textview_age_widget_title, R.id.textview_age_widget).forEach {
        if (prefersWidgetsDynamicColors) remoteViews.setDynamicTextColor(it)
        else remoteViews.setTextColor(it, textColor)
    }
    remoteViews.setOnClickPendingIntent(
        R.id.age_widget_root,
        context.launchAgeWidgetConfigurationAppPendingIntent(widgetId)
    )
    return remoteViews
}

private fun createSunViewRemoteViews(
    context: Context, width: Int, height: Int, jdn: Jdn, prayTimes: PrayTimes?
): RemoteViews {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_sun_view)
    val color = when {
        prefersWidgetsDynamicColors -> if (Theme.isNightMode(context)) Color.WHITE else Color.BLACK
        else -> selectedWidgetTextColor
    }
    val sunView = SunView(context, textColor = color)
    remoteViews.setRoundBackground(R.id.image_background, width, height)
    prepareViewForRendering(sunView, width, height)
    sunView.prayTimes = prayTimes
    sunView.setTime(jdn.toGregorianCalendar())
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
    // $ zopflipng --iterations=15 --filters=01234mepb --lossy_8bit --lossy_transparent b.png c.png
    remoteViews.setImageViewBitmap(R.id.image, sunView.drawToBitmap())
    remoteViews.setContentDescription(R.id.image, sunView.contentDescription)
    remoteViews.setOnClickPendingIntent(
        R.id.widget_layout_sun_view,
        context.launchAppPendingIntent()
    )
    return remoteViews
}

private fun createMonthViewRemoteViews(
    context: Context, width: Int, height: Int, date: AbstractDate
): RemoteViews {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_month_view)
    val widgetTheme = Theme.getWidgetSuitableStyle(context, prefersWidgetsDynamicColors)
    val monthView = MonthView(ContextThemeWrapper(context, widgetTheme))
    val color = when {
        prefersWidgetsDynamicColors -> if (Theme.isNightMode(context)) Color.WHITE else Color.BLACK
        else -> selectedWidgetTextColor
    }
    monthView.initializeForRendering(color, width, height, date)
    remoteViews.setRoundBackground(R.id.image_background, width, height)
    prepareViewForRendering(monthView, width, height)
    remoteViews.setImageViewBitmap(R.id.image, monthView.drawToBitmap())
    remoteViews.setContentDescription(R.id.image, monthView.contentDescription)
    remoteViews.setOnClickPendingIntent(R.id.image, context.launchAppPendingIntent())
    return remoteViews
}

private fun createMapRemoteViews(
    context: Context, width: Int, height: Int, time: Long
): RemoteViews {
    val size = min(width / 2, height)
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_map)
    val isNightMode = Theme.isNightMode(context)
    val backgroundColor =
        if (prefersWidgetsDynamicColors) context.getColor(
            if (isNightMode) android.R.color.system_accent2_800
            else android.R.color.system_accent2_50
        )
        else null
    val foregroundColor =
        if (prefersWidgetsDynamicColors) context.getColor(
            if (isNightMode) android.R.color.system_accent1_50
            else android.R.color.system_accent1_600
        )
        else null
    val mapDraw = MapDraw(context, backgroundColor, foregroundColor)
    mapDraw.markersScale = .75f
    mapDraw.updateMap(time, MapType.DayNight)
    val matrix = Matrix()
    matrix.setScale(size * 2f / mapDraw.mapWidth, size.toFloat() / mapDraw.mapHeight)
    val bitmap = createBitmap(size * 2, size).applyCanvas {
        withClip(createRoundPath(size * 2, size, roundPixelSize)) {
            mapDraw.draw(this, matrix, true, null, false)
        }
    }
    remoteViews.setImageViewBitmap(R.id.image, bitmap)
    remoteViews.setContentDescription(R.id.image, context.getString(R.string.map))
    remoteViews.setOnClickPendingIntent(R.id.image, context.launchAppPendingIntent("MAP"))
    return remoteViews
}

private fun createMoonRemoteViews(context: Context, width: Int, height: Int): RemoteViews {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_map)
    val solarDraw = SolarDraw(context)
    val bitmap = createBitmap(width, height).applyCanvas {
        val state = AstronomyState(GregorianCalendar())
        solarDraw.moon(
            this, state.sun, state.moon, width / 2f, height / 2f, min(width, height) / 2f,
            state.moonTilt, state.moonAltitude
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
    remoteViews.setDirection(R.id.widget_sample, context)
    remoteViews.setupForegroundTextColors(
        R.id.sample_text, R.id.sample_clock, R.id.sample_clock_replacement
    )
    if (prefersWidgetsDynamicColors)
        remoteViews.setDynamicTextColor(R.id.sample_clock, android.R.attr.colorAccent)
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
    remoteViews.setDirection(R.id.widget_layout1x1, context)
    remoteViews.setupForegroundTextColors(R.id.textPlaceholder1_1x1, R.id.textPlaceholder2_1x1)
    if (prefersWidgetsDynamicColors)
        remoteViews.setDynamicTextColor(R.id.textPlaceholder1_1x1, android.R.attr.colorAccent)
    remoteViews.setTextViewText(R.id.textPlaceholder1_1x1, formatNumber(date.dayOfMonth))
    remoteViews.setTextViewText(R.id.textPlaceholder2_1x1, date.monthName)
    remoteViews.setOnClickPendingIntent(R.id.widget_layout1x1, context.launchAppPendingIntent())
    return remoteViews
}

private fun create4x1RemoteViews(
    context: Context, width: Int, height: Int, jdn: Jdn, date: AbstractDate, widgetTitle: String,
    subtitle: String
): RemoteViews {
    val weekDayName = jdn.dayOfWeekName
    val enableClock =
        isWidgetClock && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
    val showOtherCalendars = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    val mainDateString = formatDate(date, calendarNameInLinear = showOtherCalendars)
    val remoteViews = RemoteViews(
        context.packageName, if (enableClock) {
            if (isCenterAlignWidgets) R.layout.widget4x1_clock_center else R.layout.widget4x1_clock
        } else {
            if (isCenterAlignWidgets) R.layout.widget4x1_center else R.layout.widget4x1
        }
    )
    if (enableClock) remoteViews.configureClock(R.id.textPlaceholder1_4x1)
    remoteViews.setRoundBackground(R.id.widget_layout4x1_background, width, height)
    remoteViews.setDirection(R.id.widget_layout4x1, context)
    remoteViews.setupForegroundTextColors(
        R.id.textPlaceholder1_4x1, R.id.textPlaceholder2_4x1, R.id.textPlaceholder3_4x1
    )
    if (prefersWidgetsDynamicColors)
        remoteViews.setDynamicTextColor(R.id.textPlaceholder1_4x2, android.R.attr.colorAccent)

    if (!enableClock) remoteViews.setTextViewText(R.id.textPlaceholder1_4x1, weekDayName)
    remoteViews.setTextViewText(R.id.textPlaceholder2_4x1, buildString {
        append(if (enableClock) widgetTitle else mainDateString)
        if (showOtherCalendars) append(spacedComma + subtitle)
    })
    remoteViews.setTextViewText(
        R.id.textPlaceholder3_4x1,
        if (enableClock && isForcedIranTimeEnabled) "(" + context.getString(R.string.iran_time) + ")" else ""
    )
    remoteViews.setOnClickPendingIntent(R.id.widget_layout4x1, context.launchAppPendingIntent())
    return remoteViews
}

private fun create2x2RemoteViews(
    context: Context, width: Int, height: Int, jdn: Jdn, date: AbstractDate, widgetTitle: String,
    subtitle: String, owghat: String
): RemoteViews {
    val weekDayName = jdn.dayOfWeekName
    val enableClock =
        isWidgetClock && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
    val showOtherCalendars = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    val mainDateString = formatDate(date, calendarNameInLinear = showOtherCalendars)
    val remoteViews = RemoteViews(
        context.packageName, if (enableClock) {
            if (isCenterAlignWidgets) R.layout.widget2x2_clock_center else R.layout.widget2x2_clock
        } else {
            if (isCenterAlignWidgets) R.layout.widget2x2_center else R.layout.widget2x2
        }
    )
    if (enableClock) remoteViews.configureClock(R.id.time_2x2)
    remoteViews.setRoundBackground(R.id.widget_layout2x2_background, width, height)
    remoteViews.setDirection(R.id.widget_layout2x2, context)
    remoteViews.setupForegroundTextColors(
        R.id.time_2x2, R.id.date_2x2, R.id.event_2x2, R.id.owghat_2x2
    )
    if (prefersWidgetsDynamicColors)
        remoteViews.setDynamicTextColor(R.id.time_2x2, android.R.attr.colorAccent)

    setEventsInWidget(context, jdn, remoteViews, R.id.holiday_2x2, R.id.event_2x2)

    if (OWGHAT_KEY in whatToShowOnWidgets && owghat.isNotEmpty()) {
        remoteViews.setTextViewText(R.id.owghat_2x2, owghat)
        remoteViews.setViewVisibility(R.id.owghat_2x2, View.VISIBLE)
    } else {
        remoteViews.setViewVisibility(R.id.owghat_2x2, View.GONE)
    }

    if (!enableClock) remoteViews.setTextViewText(R.id.time_2x2, weekDayName)
    remoteViews.setTextViewText(R.id.date_2x2, buildString {
        append(if (enableClock) widgetTitle else mainDateString)
        if (showOtherCalendars) appendLine().append(subtitle)
    })

    remoteViews.setOnClickPendingIntent(R.id.widget_layout2x2, context.launchAppPendingIntent())
    return remoteViews
}

private fun create4x2RemoteViews(
    context: Context, width: Int, height: Int, jdn: Jdn, date: AbstractDate, nowClock: Clock,
    prayTimes: PrayTimes?
): RemoteViews {
    val weekDayName = jdn.dayOfWeekName
    val showOtherCalendars = OTHER_CALENDARS_KEY in whatToShowOnWidgets
    val enableClock =
        isWidgetClock && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
    val remoteViews = RemoteViews(
        context.packageName, if (enableClock) R.layout.widget4x2_clock else R.layout.widget4x2
    )

    if (enableClock) remoteViews.configureClock(R.id.textPlaceholder0_4x2)
    remoteViews.setRoundBackground(R.id.widget_layout4x2_background, width, height)
    remoteViews.setDirection(R.id.widget_layout4x2, context)

    remoteViews.setupForegroundTextColors(
        R.id.textPlaceholder0_4x2, R.id.textPlaceholder1_4x2, R.id.textPlaceholder2_4x2,
        R.id.textPlaceholder4owghat_3_4x2, R.id.textPlaceholder4owghat_1_4x2,
        R.id.textPlaceholder4owghat_15_4x2,
        R.id.textPlaceholder4owghat_4_4x2, R.id.textPlaceholder4owghat_2_4x2,
        R.id.textPlaceholder4owghat_5_4x2, R.id.event_4x2
    )
    if (prefersWidgetsDynamicColors)
        remoteViews.setDynamicTextColor(R.id.textPlaceholder0_4x2, android.R.attr.colorAccent)

    if (!enableClock) remoteViews.setTextViewText(R.id.textPlaceholder0_4x2, weekDayName)
    remoteViews.setTextViewText(R.id.textPlaceholder1_4x2, buildString {
        if (enableClock) append(jdn.dayOfWeekName + "\n")
        append(formatDate(date, calendarNameInLinear = showOtherCalendars))
        if (showOtherCalendars) appendLine().append(dateStringOfOtherCalendars(jdn, "\n"))
    })

    if (prayTimes != null && OWGHAT_KEY in whatToShowOnWidgets) {
        // Set text of owghats
        val owghats = listOf(
            R.id.textPlaceholder4owghat_1_4x2, R.id.textPlaceholder4owghat_15_4x2,
            R.id.textPlaceholder4owghat_2_4x2, R.id.textPlaceholder4owghat_3_4x2,
            R.id.textPlaceholder4owghat_4_4x2, R.id.textPlaceholder4owghat_5_4x2
        ).zip(
            if (calculationMethod.isJafari) listOf(
                R.string.fajr, R.string.sunrise,
                R.string.dhuhr, R.string.maghrib,
                R.string.midnight
            ) else listOf(
                R.string.fajr, R.string.sunrise, R.string.dhuhr,
                R.string.asr, R.string.maghrib,
                R.string.isha
            )
        ) { textHolderViewId, owghatStringId ->
            val timeClock = prayTimes.getFromStringId(owghatStringId)
            remoteViews.setTextViewText(
                textHolderViewId, context.getString(owghatStringId) + "\n" +
                        timeClock.toFormattedString(printAmPm = false)
            )
            remoteViews.setupForegroundTextColors(textHolderViewId)
            Triple(textHolderViewId, owghatStringId, timeClock)
        }
        val (nextViewId, nextOwghatId, timeClock) = owghats.firstOrNull { (_, _, timeClock) ->
            timeClock.toMinutes() > nowClock.toMinutes()
        } ?: owghats[0]
        if (prefersWidgetsDynamicColors) {
            remoteViews.setDynamicTextColor(nextViewId, android.R.attr.colorAccent)
        } else {
            val color = context.appPrefs.getString(PREF_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR, null)
                ?.let(Color::parseColor)
                ?: DEFAULT_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR
            remoteViews.setTextColor(nextViewId, color)
        }

        val difference = timeClock.toMinutes() - nowClock.toMinutes()
        remoteViews.setTextViewText(
            R.id.textPlaceholder2_4x2, context.getString(
                R.string.n_till,
                Clock.fromMinutesCount(if (difference > 0) difference else difference + 60 * 24)
                    .asRemainingTime(context.resources), context.getString(nextOwghatId)
            )
        )

        if (//enableWorkManager && // no need for refresh icon, they get frequent updates
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP // for vector drawables
        ) {
            remoteViews.setImageViewResource(R.id.refresh_icon, R.drawable.ic_widget_refresh)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, Intent(context, Widget4x2::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            )
            remoteViews.setOnClickPendingIntent(R.id.refresh_wrapper, pendingIntent)
        }

        remoteViews.setViewVisibility(R.id.widget4x2_owghat, View.VISIBLE)
    } else remoteViews.setViewVisibility(R.id.widget4x2_owghat, View.GONE)

    setEventsInWidget(context, jdn, remoteViews, R.id.holiday_4x2, R.id.event_4x2)

    remoteViews.setOnClickPendingIntent(R.id.widget_layout4x2, context.launchAppPendingIntent())
    return remoteViews
}

private fun createNRemoteViews(
    context: Context, width: Int, height: Int, nowClock: Clock, prayTimes: PrayTimes?
): RemoteViews {
    val title = "${context.resources.getString(R.string.today)} :" +
            " ${dayTitleSummary(Jdn.today(), Jdn.today().toCalendar(mainCalendar))}" +
            " - ${context.appPrefs.getString(PREF_GEOCODED_CITYNAME, "")}"

    val remoteViews = RemoteViews(context.packageName, R.layout.n_widget)
    remoteViews.setRoundBackground(R.id.n_widget_background, width, height)
    remoteViews.setDirection(R.id.n_widget_layout, context)

    remoteViews.setupForegroundTextColors(
        R.id.textPlaceholder4owghat_3_4x2, R.id.textPlaceholder4owghat_1_4x2,
        R.id.textPlaceholder4owghat_15_4x2, R.id.textPlaceholder4owghat_4_4x2,
        R.id.textPlaceholder4owghat_2_4x2, R.id.textPlaceholder4owghat_5_4x2,
        R.id.n_widget_title
    )

    remoteViews.setTextViewText(R.id.n_widget_title, title)

    prayTimes ?: return remoteViews

    val owghats = listOf(
        R.id.textPlaceholder4owghat_1_4x2, R.id.textPlaceholder4owghat_15_4x2,
        R.id.textPlaceholder4owghat_2_4x2, R.id.textPlaceholder4owghat_3_4x2,
        R.id.textPlaceholder4owghat_4_4x2, R.id.textPlaceholder4owghat_5_4x2
    ).zip(
        if (calculationMethod.isJafari) listOf(
            R.string.fajr, R.string.sunrise,
            R.string.dhuhr, R.string.maghrib,
            R.string.midnight
        ) else listOf(
            R.string.fajr, R.string.sunrise, R.string.dhuhr,
            R.string.asr, R.string.maghrib,
            R.string.isha
        )
    ) { textHolderViewId, owghatStringId ->
        val timeClock = prayTimes.getFromStringId(owghatStringId)
        remoteViews.setTextViewText(
            textHolderViewId, context.getString(owghatStringId) + "\n" +
                    timeClock.toFormattedString(printAmPm = false)
        )
        remoteViews.setupForegroundTextColors(textHolderViewId)
        Triple(textHolderViewId, owghatStringId, timeClock)
    }
    val (nextViewId, _, _) = owghats.firstOrNull { (_, _, timeClock) ->
        timeClock.toMinutes() > nowClock.toMinutes()
    } ?: owghats[0]
    if (prefersWidgetsDynamicColors) {
        remoteViews.setDynamicTextColor(nextViewId, android.R.attr.colorAccent)
    } else {
        val color = context.appPrefs.getString(PREF_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR, null)
            ?.let(Color::parseColor)
            ?: DEFAULT_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR
        remoteViews.setTextColor(nextViewId, color)
    }

    remoteViews.setOnClickPendingIntent(R.id.n_widget_layout, context.launchAppPendingIntent())
    return remoteViews
}

private fun setEventsInWidget(
    context: Context, jdn: Jdn, remoteViews: RemoteViews, holidaysId: Int, eventsId: Int
) {
    val events = eventsRepository?.getEvents(jdn, deviceCalendarEvents) ?: emptyList()
    val holidays = getEventsTitle(
        events, holiday = true, compact = true, showDeviceCalendarEvents = true,
        insertRLM = context.resources.isRtl, addIsHoliday = isHighTextContrastEnabled
    )
    remoteViews.setTextViewTextOrHideIfEmpty(holidaysId, holidays)
    if (isTalkBackEnabled)
        remoteViews.setContentDescription(
            holidaysId,
            context.getString(R.string.holiday_reason, holidays)
        )

    val nonHolidays = if (NON_HOLIDAYS_EVENTS_KEY in whatToShowOnWidgets) getEventsTitle(
        events, holiday = false, compact = true, showDeviceCalendarEvents = true,
        insertRLM = context.resources.isRtl, addIsHoliday = false
    ) else ""
    remoteViews.setTextViewTextOrHideIfEmpty(eventsId, nonHolidays)

    if (!prefersWidgetsDynamicColors) remoteViews.setInt(
        holidaysId, "setTextColor",
        ContextCompat.getColor(context, R.color.light_holiday)
    )
}

private fun updateNotification(
    context: Context, title: String, subtitle: String, jdn: Jdn, date: AbstractDate, owghat: String,
    time: Long
) {
    if (!isNotifyDate) {
        if (enableWorkManager)
            context.getSystemService<NotificationManager>()?.cancel(NOTIFICATION_ID)
        return
    }

    val notificationManager = context.getSystemService<NotificationManager>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(
            NOTIFICATION_ID.toString(),
            context.getString(R.string.app_name), importance
        )
        channel.setShowBadge(false)
        notificationManager?.createNotificationChannel(channel)
    }

    // Prepend a right-to-left mark character to Android with sane text rendering stack
    // to resolve a bug seems some Samsung devices have with characters with weak direction,
    // digits being at the first of string on
    val toPrepend =
        if (context.resources.isRtl && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) RLM else ""

    val builder = NotificationCompat.Builder(context, NOTIFICATION_ID.toString())
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .setWhen(0)
        .setContentIntent(context.launchAppPendingIntent())
        .setVisibility(
            if (isNotifyDateOnLockScreen)
                NotificationCompat.VISIBILITY_PUBLIC
            else
                NotificationCompat.VISIBILITY_SECRET
        )
        .setContentTitle(toPrepend + title)
        .setContentText(
            when {
                isTalkBackEnabled -> if (owghat.isEmpty()) "" else spacedComma + owghat + getA11yDaySummary(
                    context = context, jdn = jdn,
                    isToday = false, // Don't set isToday, per a feedback
                    deviceCalendarEvents = deviceCalendarEvents, withZodiac = true,
                    withOtherCalendars = true, withTitle = false
                )

                owghat.isEmpty() -> owghat
                else -> toPrepend + owghat
            }
        )

    // Dynamic small icon generator, most of the times disabled as it needs API 23 and
    // we need to have the other path anyway
    if (language.isNepali && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val icon = IconCompat.createWithBitmap(createStatusIcon(date.dayOfMonth))
        builder.setSmallIcon(icon)
    } else {
        builder.setSmallIcon(getDayIconResource(date.dayOfMonth))
    }

    // Night mode doesn't like our custom notification in Samsung and HTC One UI
    val shouldDisableCustomNotification = when (Build.BRAND) {
        "samsung", "htc" -> Theme.isNightMode(context)
        else -> false
    }

    if (!isTalkBackEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val events = eventsRepository?.getEvents(jdn, deviceCalendarEvents) ?: emptyList()
        val holidays = getEventsTitle(
            events, holiday = true,
            compact = true, showDeviceCalendarEvents = true, insertRLM = context.resources.isRtl,
            addIsHoliday = shouldDisableCustomNotification || isHighTextContrastEnabled
        )

        val nonHolidays = if (NON_HOLIDAYS_EVENTS_KEY in whatToShowOnWidgets) getEventsTitle(
            events, holiday = false,
            compact = true, showDeviceCalendarEvents = true, insertRLM = context.resources.isRtl,
            addIsHoliday = false
        ) else ""

        val notificationOwghat = if (OWGHAT_KEY in whatToShowOnWidgets) owghat else ""

        if (shouldDisableCustomNotification) {
            val content = listOf(owghat, subtitle, holidays.trim(), nonHolidays)
                .filter { it.isNotBlank() }.joinToString("\n")
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(content))
        } else {
            builder.setCustomContentView(RemoteViews(
                context.packageName, R.layout.custom_notification
            ).also {
                it.setDirection(R.id.custom_notification_root, context)
                it.setTextViewText(R.id.title, title)
                it.setTextViewText(R.id.body, owghat)
            })

            if (listOf(owghat, holidays, nonHolidays).any { it.isNotBlank() })
                builder.setCustomBigContentView(RemoteViews(
                    context.packageName, R.layout.custom_notification_big
                ).also {
                    it.setDirection(R.id.custom_notification_root, context)
                    it.setTextViewText(R.id.title, title)
                    it.setTextViewTextOrHideIfEmpty(R.id.body, subtitle)
                    it.setTextViewTextOrHideIfEmpty(R.id.holidays, holidays)
                    it.setTextViewTextOrHideIfEmpty(R.id.nonholidays, nonHolidays)
                    it.setTextViewTextOrHideIfEmpty(R.id.owghat, owghat)
                })

            builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        }
    }

    if (BuildConfig.DEVELOPMENT) builder.setWhen(time)

    if (enableWorkManager) notificationManager?.notify(NOTIFICATION_ID, builder.build())
    else context.runCatching {
        ApplicationService.getInstance()?.startForeground(NOTIFICATION_ID, builder.build())
    }.onFailure(logException)
}

private fun RemoteViews.setRoundBackground(
    @IdRes viewId: Int, width: Int, height: Int,
    @ColorInt color: Int = selectedWidgetBackgroundColor
) {
    when {
        prefersWidgetsDynamicColors -> setImageViewResource(viewId, R.drawable.widget_background)
        color == DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR -> setImageViewResource(viewId, 0)
        else -> {
            val roundBackground = createRoundDrawable(color, roundPixelSize).toBitmap(width, height)
            setImageViewBitmap(viewId, roundBackground)
        }
    }
}

fun RemoteViews.setDirection(@IdRes viewId: Int, context: Context) {
    val direction = when {
        // Apply RTL for Arabic script locales anyway just in case something went wrong
        language.isArabicScript -> View.LAYOUT_DIRECTION_RTL
        // Most text in en-IR are RTL
        language.isIranianEnglish -> View.LAYOUT_DIRECTION_RTL
        else -> context.resources.configuration.layoutDirection
    }
    setInt(viewId, "setLayoutDirection", direction)
}

private fun RemoteViews.configureClock(@IdRes viewId: Int) {
    if (isForcedIranTimeEnabled) setString(viewId, "setTimeZone", IRAN_TIMEZONE_ID)
    val clockFormat = if (clockIn24) "kk:mm" else "h:mm"
    setCharSequence(viewId, "setFormat12Hour", clockFormat)
    setCharSequence(viewId, "setFormat24Hour", clockFormat)
}

@RequiresApi(Build.VERSION_CODES.S)
private fun RemoteViews.setDynamicTextColor(
    @IdRes id: Int, @AttrRes attr: Int = android.R.attr.colorForeground
) {
    setColorAttr(id, "setTextColor", attr)
}

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

private fun Context.launchAppPendingIntent(action: String? = null): PendingIntent? {
    return PendingIntent.getActivity(
        this, 0,
        Intent(this, MainActivity::class.java).setAction(action)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )
}

private fun Context.launchAgeWidgetConfigurationAppPendingIntent(widgetId: Int): PendingIntent? {
    return PendingIntent.getActivity(
        this, 0,
        Intent(this, AgeWidgetConfigureActivity::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )
}
