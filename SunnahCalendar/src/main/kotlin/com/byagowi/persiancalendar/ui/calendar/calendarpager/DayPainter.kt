package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.toArgb
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ZWJ
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.isHighTextContrastEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendarDigits
import com.byagowi.persiancalendar.global.secondaryCalendarDigits
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.isMoonInScorpio
import kotlin.math.min

class DayPainter(
    resources: Resources,
    val width: Float,
    val height: Float,
    private val isRtl: Boolean,
    colors: MonthColors,
    isWidget: Boolean = false,
    isYearView: Boolean = false,
    selectedDayColor: Int? = null,
) {
    private val paints =
        Paints(resources, min(width, height), colors, isWidget, isYearView, selectedDayColor)
    private var text = ""
    private var today = false
    private var dayIsSelected = false
    private var indicators = emptyList<Paint>()
    private var holiday = false
    var jdn: Jdn? = null
        private set
    private var isWeekNumber = false
    private var header = ""

    fun drawDay(canvas: Canvas) {
        drawCircle(canvas) // background circle, if is needed
        drawText(canvas) // can be a day number, week day name abbr or week number of year
        drawIndicators(canvas) // whether a day has event or appointment
        drawHeader(canvas) // shift work header
    }

    private fun drawCircle(canvas: Canvas) {
        if (dayIsSelected) paints.selectedDayPaint?.let { selectedDayPaint ->
            canvas.drawCircle(
                width / 2,
                height / 2,
                min(width, height) / 2 - paints.circlePadding,
                selectedDayPaint
            )
        }
        if (today) canvas.drawCircle(
            width / 2,
            height / 2,
            min(width, height) / 2 - paints.circlePadding,
            paints.todayPaint,
        )
    }

    private val textBounds = Rect()
    private fun drawText(canvas: Canvas) {
        val textPaint = when {
            jdn != null -> when {
                holiday -> paints.dayOfMonthNumberTextHolidayPaint
                dayIsSelected -> paints.dayOfMonthNumberTextSelectedPaint
                else /*!dayIsSelected*/ -> paints.dayOfMonthNumberTextPaint
            }

            isWeekNumber -> paints.weekNumberTextPaint
            else -> paints.weekDayInitialsTextPaint
        }
        // Measure a sample text to find height for vertical center aligning of the text to draw
        val sample = if (jdn != null) text else if (paints.isArabicScript) "س" else "Yy"
        textPaint.getTextBounds(sample, 0, sample.length, textBounds)
        val yPos = (height + textBounds.height()) / 2f
        // Draw day number/label
        canvas.drawText(text, width / 2f, yPos + paints.dayOffsetY, textPaint)
    }

    private fun drawIndicators(canvas: Canvas) {
        val offsetDirection = if (isRtl) -1 else 1
        indicators.forEachIndexed { i, paint ->
            val xOffset = paints.eventIndicatorsCentersDistance *
                    (i - (indicators.size - 1) / 2f) * offsetDirection
            canvas.drawCircle(
                width / 2f + xOffset, height / 2 + paints.eventYOffset,
                paints.eventIndicatorRadius, when {
                    dayIsSelected -> paints.headerTextSelectedPaint
                    // use textPaint for holiday event when a11y's high contrast is enabled
                    isHighTextContrastEnabled && holiday && paint == paints.eventIndicatorPaint ->
                        paints.dayOfMonthNumberTextHolidayPaint

                    else -> paint
                }
            )
        }
    }

    private fun drawHeader(canvas: Canvas) {
        if (header.isEmpty()) return
        canvas.drawText(
            header, width / 2f, height / 2 + paints.headerYOffset,
            if (dayIsSelected) paints.headerTextSelectedPaint else paints.headerTextPaint
        )
    }

    private fun setAll(
        text: String, isToday: Boolean = false, isSelected: Boolean = false,
        hasEvent: Boolean = false, hasAppointment: Boolean = false, isHoliday: Boolean = false,
        jdn: Jdn? = null, header: String? = null, isWeekNumber: Boolean = false,
        secondaryCalendar: Calendar? = null,
    ) {
        this.text = text
        this.today = isToday
        this.dayIsSelected = isSelected
        this.holiday = isHoliday
        this.jdn = jdn
        this.isWeekNumber = isWeekNumber
        this.header = listOfNotNull(
            if (isAstronomicalExtraFeaturesEnabled && jdn != null && isMoonInScorpio(jdn))
                paints.scorpioSign else null,
            if (secondaryCalendar == null || jdn == null) null else
                formatNumber(jdn.on(secondaryCalendar).dayOfMonth, secondaryCalendarDigits),
            header,
        ).joinToString(" ")
        this.indicators = listOf(
            hasAppointment to paints.appointmentIndicatorPaint,
            (hasEvent || (isHighTextContrastEnabled && holiday)) to paints.eventIndicatorPaint
        ).mapNotNull { (condition, paint) -> paint.takeIf { condition } }
    }

    fun setDayOfMonthItem(
        isToday: Boolean, isSelected: Boolean,
        hasEvent: Boolean, hasAppointment: Boolean, isHoliday: Boolean,
        jdn: Jdn, dayOfMonth: String, header: String?, secondaryCalendar: Calendar?,
    ) = setAll(
        text = dayOfMonth, isToday = isToday,
        isSelected = isSelected, hasEvent = hasEvent, hasAppointment = hasAppointment, jdn = jdn,
        header = header, isHoliday = isHoliday,
        secondaryCalendar = secondaryCalendar,
    )

    fun setInitialOfWeekDay(text: String) = setAll(text)
    fun setWeekNumber(text: String) = setAll(text, isWeekNumber = true)
}

private class Paints(
    resources: Resources,
    diameter: Float,
    colors: MonthColors,
    isWidget: Boolean,
    isYearView: Boolean,
    @ColorInt selectedDayColor: Int?,
) {
    private val dp = resources.dp
    val circlePadding = .5f * dp
    val isArabicScript = language.value.isArabicScript
    val eventYOffset = diameter * 12 / 40
    val eventIndicatorRadius = diameter * 2 / 40
    private val eventIndicatorsGap = diameter * 2 / 40
    val eventIndicatorsCentersDistance = 2 * eventIndicatorRadius + eventIndicatorsGap
    val scorpioSign =
        resources.getString(R.string.scorpio).first() + if (isArabicScript) ZWJ else ""

    private fun addShadowIfNeeded(paint: Paint) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            paint.setShadowLayer(1f, 1f, 1f, Color.BLACK)
    }

    val appointmentIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = colors.appointments.toArgb()
    }
    val eventIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = colors.eventIndicator.toArgb()
    }

    val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = 1 * dp
        it.color = colors.currentDay.toArgb()
    }
    val selectedDayPaint = selectedDayColor?.let {
        Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.style = Paint.Style.FILL
            it.color = selectedDayColor
        }
    }

    private val mainCalendarDigitsIsArabic = mainCalendarDigits === Language.ARABIC_DIGITS
    private val textSize = diameter * (if (mainCalendarDigitsIsArabic) 18 else 25) / 40
    val dayOffsetY = if (mainCalendarDigitsIsArabic) 0f else diameter * 3 / 40

    private val secondaryCalendarDigitsIsArabic = secondaryCalendarDigits === Language.ARABIC_DIGITS
    private val headerTextSize = diameter / 40 * (if (secondaryCalendarDigitsIsArabic) 11 else 15)
    val headerYOffset = -diameter * (if (secondaryCalendarDigitsIsArabic) 10 else 7) / 40

    val dayOfMonthNumberTextHolidayPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = textSize
        it.color = colors.holidays.toArgb()
        if (isWidget) addShadowIfNeeded(it)
    }

    val dayOfMonthNumberTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = textSize
        it.color = colors.contentColor.toArgb()
        if (isWidget) addShadowIfNeeded(it)
    }

    val dayOfMonthNumberTextSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = textSize
        it.color = colors.textDaySelected.toArgb()
        if (isWidget) addShadowIfNeeded(it)
    }
    val headerTextSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = headerTextSize
        it.color = colors.textDaySelected.toArgb()
        if (isWidget) addShadowIfNeeded(it)
    }

    val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = headerTextSize
        it.color = colors.colorTextDayName.toArgb()
        if (isWidget) addShadowIfNeeded(it)
    }
    val weekNumberTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = if (isYearView) textSize else headerTextSize
        it.color = colors.colorTextDayName.toArgb()
        if (isWidget) addShadowIfNeeded(it)
    }
    val weekDayInitialsTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = diameter * 20 / 40
        it.color = colors.colorTextDayName.toArgb()
        if (isWidget) addShadowIfNeeded(it)
    }
}
