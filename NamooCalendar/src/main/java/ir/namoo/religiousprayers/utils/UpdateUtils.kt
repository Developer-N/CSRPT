package ir.namoo.religiousprayers.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Clock
import ir.namoo.religiousprayers.AgeWidget
import ir.namoo.religiousprayers.BuildConfig
import ir.namoo.religiousprayers.DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
import ir.namoo.religiousprayers.DEFAULT_SELECTED_WIDGET_TEXT_COLOR
import ir.namoo.religiousprayers.NWidget
import ir.namoo.religiousprayers.PREF_SELECTED_DATE_AGE_WIDGET
import ir.namoo.religiousprayers.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import ir.namoo.religiousprayers.PREF_SELECTED_WIDGET_TEXT_COLOR
import ir.namoo.religiousprayers.PREF_TITLE_AGE_WIDGET
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.RLM
import ir.namoo.religiousprayers.Widget1x1
import ir.namoo.religiousprayers.Widget2x2
import ir.namoo.religiousprayers.Widget4x1
import ir.namoo.religiousprayers.Widget4x2
import ir.namoo.religiousprayers.praytimes.PrayTimeProvider
import ir.namoo.religiousprayers.service.ApplicationService
import ir.namoo.religiousprayers.ui.SplashActivity
import ir.namoo.religiousprayers.ui.widget.NWidgetView
import java.util.*
import java.util.concurrent.TimeUnit.MINUTES

private const val NOTIFICATION_ID = 6236
private var pastDate: AbstractDate? = null
private var deviceCalendarEvents: DeviceCalendarEventsStore = emptyEventsStore()

fun setDeviceCalendarEvents(context: Context) = runCatching {
    deviceCalendarEvents = Jdn.today.readDayDeviceEvents(context)
}.onFailure(logException).let {}

var latestFiredUpdate = 0L

fun update(context: Context, updateDate: Boolean) {
    val now = System.currentTimeMillis()
    if (!updateDate && now - latestFiredUpdate < HALF_SECOND_IN_MILLIS) {
        logDebug("UpdateUtils", "skip update")
        return
    }
    latestFiredUpdate = now

    logDebug("UpdateUtils", "update")
    applyAppLanguage(context)
    val jdn = Jdn.today
    val date = jdn.toCalendar(mainCalendar)

    val launchAppPendingIntent = PendingIntent.getActivity(
        context, 0,
        Intent(context, SplashActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )

    // RemoteViews helpers
    fun RemoteViews.setBackgroundColor(
        @IdRes layoutId: Int, color: String = selectedWidgetBackgroundColor
    ) = this.setInt(layoutId, "setBackgroundColor", Color.parseColor(color))

    fun RemoteViews.setTextViewTextOrHideIfEmpty(viewId: Int, text: CharSequence) =
        if (text.isBlank()) setViewVisibility(viewId, View.GONE)
        else setTextViewText(viewId, text.trim())

    //
    // Widgets
    //
    //
    val manager = AppWidgetManager.getInstance(context) ?: return
    val color = Color.parseColor(selectedWidgetTextColor)
    val nextAthanColor = Color.parseColor(selectedWidgetNextAthanTextColor)
    val packageName = context.packageName

    // en-US is our only real LTR language for now
    val isRTL = isLocaleRTL()

    val ageWidget = ComponentName(context, AgeWidget::class.java)
    val widget1x1 = ComponentName(context, Widget1x1::class.java)
    val widget4x1 = ComponentName(context, Widget4x1::class.java)
    val widget4x2 = ComponentName(context, Widget4x2::class.java)
    val widget2x2 = ComponentName(context, Widget2x2::class.java)
    // val widget4x1dateOnly = ComponentName(context, Widget4x1dateOnly::class.java)
    val nWidget = ComponentName(context, NWidget::class.java)

    //region Age Widgets
    manager.getAppWidgetIds(ageWidget)?.forEach { widgetId ->
        val prefs = context.appPrefs
        val baseJdn = prefs.getJdnOrNull(PREF_SELECTED_DATE_AGE_WIDGET + widgetId) ?: Jdn.today
        val title = prefs.getString(PREF_TITLE_AGE_WIDGET + widgetId, "")
        val subtitleFormatPattern = context.getString(R.string.age_widget_placeholder)
        val subtitle = calculateDaysDifference(baseJdn, subtitleFormatPattern)
        val textColor = prefs.getString(PREF_SELECTED_WIDGET_TEXT_COLOR + widgetId, null)
            ?: DEFAULT_SELECTED_WIDGET_TEXT_COLOR
        val bgColor = prefs.getString(PREF_SELECTED_WIDGET_BACKGROUND_COLOR + widgetId, null)
            ?: DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
        manager.updateAppWidget(widgetId, RemoteViews(packageName, R.layout.widget_age).also {
            it.setBackgroundColor(R.id.age_widget_root, bgColor)
            it.setTextViewTextOrHideIfEmpty(R.id.textview_age_widget_title, title ?: "")
            it.setTextColor(R.id.textview_age_widget_title, Color.parseColor(textColor))
            it.setTextViewText(R.id.textview_age_widget, subtitle)
            it.setTextColor(R.id.textview_age_widget, Color.parseColor(textColor))
        })
    }
    //endregion

    //region Widget 1x1
    if (manager.getAppWidgetIds(widget1x1)?.isNotEmpty() == true) {
        manager.updateAppWidget(widget1x1, RemoteViews(packageName, R.layout.widget1x1).also {
            it.setBackgroundColor(R.id.widget_layout1x1)
            it.setTextColor(R.id.textPlaceholder1_1x1, color)
            it.setTextColor(R.id.textPlaceholder2_1x1, color)
            it.setTextViewText(R.id.textPlaceholder1_1x1, formatNumber(date.dayOfMonth))
            it.setTextViewText(R.id.textPlaceholder2_1x1, date.monthName)
            it.setOnClickPendingIntent(R.id.widget_layout1x1, launchAppPendingIntent)
        })
    }
    //endregion

    //region NWidget
    runCatching {
        if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            if (manager.getAppWidgetIds(nWidget)?.isNotEmpty() == true)
                RemoteViews(context.packageName, R.layout.n_widget).apply {
                    val view = NWidgetView(context)
                    setImageViewBitmap(R.id.widget_image, createBitmapFromView(view))
                    setOnClickPendingIntent(R.id.widget_image, launchAppPendingIntent)
                    manager.updateAppWidget(nWidget, this)
                }
    }.onFailure(logException)
    //endregion

    var dateHasChanged = false
    if (pastDate == null || pastDate != date || updateDate) {
        logDebug("UpdateUtils", "date has changed")

        loadAlarms(context)
        pastDate = date
        dateHasChanged = true
        setDeviceCalendarEvents(context)
    }

    val showOtherCalendars = "other_calendars" in whatToShowOnWidgets

    val weekDayName = jdn.dayOfWeekName
    var title = context.getString(R.string.today) + " " + dayTitleSummary(jdn, date)
    var widgetTitle = dayTitleSummary(jdn, date, calendarNameInLinear = showOtherCalendars)
    val shiftWorkTitle = getShiftWorkTitle(jdn, false)
    if (shiftWorkTitle.isNotEmpty()) {
        title += " ($shiftWorkTitle)"
        widgetTitle += " ($shiftWorkTitle)"
    }
    var subtitle = dateStringOfOtherCalendars(jdn, spacedComma)

    val owghatClock = Clock(Date().toJavaCalendar(forceLocalTime = true))
    var owghat = ""

//    @StringRes
    val nextOwghatId = getNextOwghatTimeId(owghatClock, dateHasChanged, context)
    var prayTimes = PrayTimeProvider.calculate(
        calculationMethod,
        Jdn.today,
        getCoordinate(context)!!,
        context
    )
    val athanNames = context.resources.getStringArray(R.array.prayerTimeNames)
    owghat = when (nextOwghatId) {
        1 -> athanNames[1] + " : " + prayTimes.sunriseClock.toFormattedString()
        6 -> {
            val cal = Jdn.today.toJavaCalendar()
            cal.add(Calendar.DAY_OF_MONTH, 1)
            prayTimes = PrayTimeProvider.calculate(
                calculationMethod,
                Jdn(cal.time.time),
                getCoordinate(context)!!,
                context
            )
            context.getString(R.string.next_athan) + " " + athanNames[0] + " : " + prayTimes.fajrClock.toFormattedString()
        }
        0 -> context.getString(R.string.next_athan) + " " + athanNames[0] + " : " + prayTimes.fajrClock.toFormattedString()
        2 -> context.getString(R.string.next_athan) + " " + athanNames[2] + " : " + prayTimes.dhuhrClock.toFormattedString()
        3 -> context.getString(R.string.next_athan) + " " + athanNames[3] + " : " + prayTimes.asrClock.toFormattedString()
        4 -> context.getString(R.string.next_athan) + " " + athanNames[4] + " : " + prayTimes.maghribClock.toFormattedString()
        //5
        else -> context.getString(R.string.next_athan) + " " + athanNames[5] + " : " + prayTimes.ishaClock.toFormattedString()
    }
    if ("owghat_location" in whatToShowOnWidgets) {
        val cityName = getCityName(context, false)
        if (cityName.isNotEmpty()) {
            owghat = "$owghat ($cityName)"
        }
    }

    val events = jdn.getEvents(deviceCalendarEvents)

    val enableClock = isWidgetClock && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
    val isCenterAligned = isCenterAlignWidgets

    //region Widget 4x1 and 2x2
    if (manager.getAppWidgetIds(widget4x1)?.isNotEmpty() == true || manager.getAppWidgetIds(
            widget2x2
        )?.isNotEmpty() == true
    ) {
        val remoteViews4: RemoteViews
        val remoteViews2: RemoteViews
        if (enableClock) {
            if (isForcedIranTimeEnabled) {
                remoteViews4 = RemoteViews(
                    packageName,
                    if (isCenterAligned) R.layout.widget4x1_clock_iran_center else R.layout.widget4x1_clock_iran
                )
                remoteViews2 = RemoteViews(
                    packageName,
                    if (isCenterAligned) R.layout.widget2x2_clock_iran_center else R.layout.widget2x2_clock_iran
                )
            } else {
                remoteViews4 = RemoteViews(
                    packageName,
                    if (isCenterAligned) R.layout.widget4x1_clock_center else R.layout.widget4x1_clock
                )
                remoteViews2 = RemoteViews(
                    packageName,
                    if (isCenterAligned) R.layout.widget2x2_clock_center else R.layout.widget2x2_clock
                )
            }
        } else {
            remoteViews4 = RemoteViews(
                packageName, if (isCenterAligned) R.layout.widget4x1_center else R.layout.widget4x1
            )
            remoteViews2 = RemoteViews(
                packageName, if (isCenterAligned) R.layout.widget2x2_center else R.layout.widget2x2
            )
        }

        val mainDateString = formatDate(date, calendarNameInLinear = showOtherCalendars)

        // Widget 4x1
        manager.updateAppWidget(widget4x1, remoteViews4.also {
            it.setBackgroundColor(R.id.widget_layout4x1)
            it.setTextColor(R.id.textPlaceholder1_4x1, color)
            it.setTextColor(R.id.textPlaceholder2_4x1, color)
            it.setTextColor(R.id.textPlaceholder3_4x1, color)

            var text2: String
            var text3 = ""

            if (enableClock) {
                text2 = widgetTitle
                if (isForcedIranTimeEnabled) text3 =
                    "(" + context.getString(R.string.iran_time) + ")"
            } else {
                remoteViews4.setTextViewText(R.id.textPlaceholder1_4x1, weekDayName)
                text2 = mainDateString
            }
            if (showOtherCalendars) {
                text2 += spacedComma + subtitle
            }

            it.setTextViewText(R.id.textPlaceholder2_4x1, text2)
            it.setTextViewText(R.id.textPlaceholder3_4x1, text3)
            it.setOnClickPendingIntent(R.id.widget_layout4x1, launchAppPendingIntent)
        })

        // Widget 2x2
        manager.updateAppWidget(widget2x2, remoteViews2.also {
            var text2: String
            it.setBackgroundColor(R.id.widget_layout2x2)
            it.setTextColor(R.id.time_2x2, color)
            it.setTextColor(R.id.date_2x2, color)
            it.setTextColor(R.id.event_2x2, color)
            it.setTextColor(R.id.owghat_2x2, color)

            text2 = if (enableClock) {
                widgetTitle
            } else {
                it.setTextViewText(R.id.time_2x2, weekDayName)
                mainDateString
            }

            val holidays = getEventsTitle(
                events, holiday = true, compact = true, showDeviceCalendarEvents = true,
                insertRLM = isRTL, addIsHoliday = isHighTextContrastEnabled
            )
            if (holidays.isNotEmpty()) {
                it.setTextViewText(R.id.holiday_2x2, holidays)
                if (isTalkBackEnabled) it.setContentDescription(
                    R.id.holiday_2x2, context.getString(R.string.holiday_reason) + " " + holidays
                )
                it.setViewVisibility(R.id.holiday_2x2, View.VISIBLE)
            } else {
                it.setViewVisibility(R.id.holiday_2x2, View.GONE)
            }

            val nonHolidays = getEventsTitle(
                events, holiday = false, compact = true, showDeviceCalendarEvents = true,
                insertRLM = isRTL, addIsHoliday = false
            )
            if ("non_holiday_events" in whatToShowOnWidgets && nonHolidays.isNotEmpty()) {
                it.setTextViewText(R.id.event_2x2, nonHolidays)
                it.setViewVisibility(R.id.event_2x2, View.VISIBLE)
            } else {
                it.setViewVisibility(R.id.event_2x2, View.GONE)
            }

            if ("owghat" in whatToShowOnWidgets && owghat.isNotEmpty()) {
                it.setTextViewText(R.id.owghat_2x2, owghat)
                it.setViewVisibility(R.id.owghat_2x2, View.VISIBLE)
            } else {
                it.setViewVisibility(R.id.owghat_2x2, View.GONE)
            }

            if (showOtherCalendars) {
                text2 = text2 + "\n" + subtitle + "\n" +
                        getZodiacInfo(context, jdn, withEmoji = true, short = true)
            }
            it.setTextViewText(R.id.date_2x2, text2)

            it.setOnClickPendingIntent(R.id.widget_layout2x2, launchAppPendingIntent)
        })
    }
    //endregion

    //region Widget 4x2
    if (manager.getAppWidgetIds(widget4x2)?.isNotEmpty() == true) {
        val remoteViews4x2 = RemoteViews(
            packageName, if (enableClock) {
                if (isForcedIranTimeEnabled) R.layout.widget4x2_clock_iran else R.layout.widget4x2_clock
            } else R.layout.widget4x2
        )

        manager.updateAppWidget(widget4x2, remoteViews4x2.also {
            it.setBackgroundColor(R.id.widget_layout4x2)

            it.setTextColor(R.id.textPlaceholder0_4x2, color)
            it.setTextColor(R.id.textPlaceholder1_4x2, color)
            it.setTextColor(R.id.textPlaceholder2_4x2, color)
            it.setTextColor(R.id.textPlaceholder4owghat_3_4x2, color)
            it.setTextColor(R.id.textPlaceholder4owghat_1_4x2, color)
            it.setTextColor(R.id.textPlaceholder4owghat_4_4x2, color)
            it.setTextColor(R.id.textPlaceholder4owghat_2_4x2, color)
            it.setTextColor(R.id.textPlaceholder4owghat_5_4x2, color)
            it.setTextColor(R.id.textPlaceholder4owghat_6_4x2, color)

            var text2 = formatDate(date, calendarNameInLinear = showOtherCalendars)
            if (enableClock)
                text2 = jdn.dayOfWeekName + "\n" + text2
            else
                it.setTextViewText(R.id.textPlaceholder0_4x2, weekDayName)

            if (showOtherCalendars)
                text2 = text2 + "\n" + dateStringOfOtherCalendars(jdn, "\n")

            it.setTextViewText(R.id.textPlaceholder1_4x2, text2)

            if (nextOwghatId >= 0) {
                // Set text of owghats
                listOf(
                    R.id.textPlaceholder4owghat_1_4x2, R.id.textPlaceholder4owghat_2_4x2,
                    R.id.textPlaceholder4owghat_3_4x2, R.id.textPlaceholder4owghat_4_4x2,
                    R.id.textPlaceholder4owghat_5_4x2, R.id.textPlaceholder4owghat_6_4x2
                ).zip(
                    when (calculationMethod) {
                        CalculationMethod.Tehran, CalculationMethod.Jafari -> listOf(
                            R.string.fajr, R.string.sunrise, R.string.dhuhr,
                            R.string.sunset, R.string.maghrib,
                            R.string.midnight
                        )
                        else -> listOf(
                            R.string.fajr, R.string.sunrise, R.string.dhuhr,
                            R.string.asr, R.string.maghrib,
                            R.string.isha
                        )
                    }
                ) { textHolderViewId, owghatStringId ->
                    it.setTextViewText(
                        textHolderViewId,
                        "${context.getString(owghatStringId)}\n${
                            getClockFromStringId(
                                owghatStringId, context
                            ).toFormattedString()
                        }"
                    )
                    it.setTextColor(
                        textHolderViewId,
                        if (owghatStringId == getClockStringFromId(nextOwghatId))
                            nextAthanColor
                        else
                            color
                    )
                }

                var difference = getClockFromStringId(
                    getClockStringFromId(nextOwghatId), context
                ).toInt() - owghatClock.toInt()
                if (difference < 0) difference += 60 * 24

                val hrs = (MINUTES.toHours(difference.toLong()) % 24).toInt()
                val min = (MINUTES.toMinutes(difference.toLong()) % 60).toInt()

                val remainingTime = when {
                    hrs == 0 -> context.getString(R.string.n_minutes).format(formatNumber(min))
                    min == 0 -> context.getString(R.string.n_hours).format(formatNumber(hrs))
                    else -> context.getString(R.string.n_minutes_and_hours)
                        .format(formatNumber(hrs), formatNumber(min))
                }

                it.setTextViewText(
                    R.id.textPlaceholder2_4x2,
                    context.getString(R.string.n_till)
                        .format(remainingTime, owghat)
                )
                it.setTextColor(R.id.textPlaceholder2_4x2, color)
            } else {
                it.setTextViewText(
                    R.id.textPlaceholder2_4x2, context.getString(R.string.ask_user_to_set_location)
                )
                it.setTextColor(R.id.textPlaceholder2_4x2, color)
            }

            it.setOnClickPendingIntent(R.id.widget_layout4x2, launchAppPendingIntent)
        })
    }
    //endregion


    //
    // Permanent Notification Bar Update
    //
    //

    // Prepend a right-to-left mark character to Android with sane text rendering stack
    // to resolve a bug seems some Samsung devices have with characters with weak direction,
    // digits being at the first of string on
    if (isRTL && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        title = RLM + title
        if (subtitle.isNotEmpty()) {
            subtitle = RLM + subtitle
        }
    }

    if (isNotifyDate) {
        subtitle = owghat
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
        // Don't remove this condition checking ever
        if (isTalkBackEnabled) {
            // Don't use isToday, per a feedback
            subtitle = owghat
            subtitle += spacedComma
            subtitle += getA11yDaySummary(
                context, jdn, true,
                deviceCalendarEvents,
                withZodiac = true, withOtherCalendars = true, withTitle = false
            )
            if (owghat.isNotEmpty()) {
                subtitle += spacedComma
                subtitle += owghat
            }
        }

        val builder = NotificationCompat.Builder(context, NOTIFICATION_ID.toString())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setWhen(0)
            .setContentIntent(launchAppPendingIntent)
            .setVisibility(
                if (isNotifyDateOnLockScreen)
                    NotificationCompat.VISIBILITY_PUBLIC
                else
                    NotificationCompat.VISIBILITY_SECRET
            )
            .setColor(0xFF607D8B.toInt())
            .setColorized(true)
            .setContentTitle(title)
            .setContentText(subtitle)

        // Dynamic small icon generator, disabled as it needs API 23 and we need to have the other path anyway
        if ((false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val icon = IconCompat.createWithBitmap(createStatusIcon(context, date.dayOfMonth))
                builder.setSmallIcon(icon)
            }
        } else {
            builder.setSmallIcon(getDayIconResource(date.dayOfMonth))
        }

        // Night mode doesn't like our custom notification in Samsung and HTC One UI
        val shouldDisableCustomNotification = when (Build.BRAND) {
            "samsung", "htc" -> isNightModeEnabled(context)
            else -> false
        }

        if (!isTalkBackEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val holidays = getEventsTitle(
                events, holiday = true,
                compact = true, showDeviceCalendarEvents = true, insertRLM = isRTL,
                addIsHoliday = shouldDisableCustomNotification || isHighTextContrastEnabled
            )

            val nonHolidays = if ("non_holiday_events" in whatToShowOnWidgets) getEventsTitle(
                events, holiday = false,
                compact = true, showDeviceCalendarEvents = true, insertRLM = isRTL,
                addIsHoliday = false
            ) else ""

            val notificationOwghat = if ("owghat" in whatToShowOnWidgets) owghat else ""

            if (shouldDisableCustomNotification) {
                subtitle = dateStringOfOtherCalendars(jdn, spacedComma)
                if (isRTL && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    if (subtitle.isNotEmpty()) {
                        subtitle = RLM + subtitle
                    }
                }
                val content = listOf(subtitle, holidays.trim(), nonHolidays, notificationOwghat)
                    .filter { it.isNotBlank() }.joinToString("\n")
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(content))
            } else {
                builder.setCustomContentView(RemoteViews(
                    packageName,
                    if (isRTL) R.layout.custom_notification else R.layout.custom_notification_ltr
                ).also {
                    it.setTextViewText(R.id.title, title)
                    it.setTextViewText(R.id.body, subtitle)
                })

                subtitle = dateStringOfOtherCalendars(jdn, spacedComma)
                if (isRTL && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    if (subtitle.isNotEmpty()) {
                        subtitle = RLM + subtitle
                    }
                }

                if (listOf(holidays, nonHolidays, notificationOwghat).any { it.isNotBlank() })
                    builder.setCustomBigContentView(RemoteViews(
                        packageName,
                        if (isRTL) R.layout.custom_notification_big else R.layout.custom_notification_big_ltr
                    ).also {
                        it.setTextViewText(R.id.title, title)
                        it.setTextViewTextOrHideIfEmpty(R.id.body, subtitle)
                        it.setTextViewTextOrHideIfEmpty(R.id.holidays, holidays)
                        it.setTextViewTextOrHideIfEmpty(R.id.nonholidays, nonHolidays)
                        it.setTextViewTextOrHideIfEmpty(R.id.owghat, notificationOwghat)
                    })

                builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
            }
        }

        if (BuildConfig.DEBUG) builder.setWhen(Calendar.getInstance().timeInMillis)

        if (goForWorker()) notificationManager?.notify(NOTIFICATION_ID, builder.build())
        else runCatching {
            ApplicationService.getInstance()?.startForeground(NOTIFICATION_ID, builder.build())
        }.onFailure(logException)
    } else if (goForWorker()) {
        context.getSystemService<NotificationManager>()?.cancel(NOTIFICATION_ID)
    }
}
