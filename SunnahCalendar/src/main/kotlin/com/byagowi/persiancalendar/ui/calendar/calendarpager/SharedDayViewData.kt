package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.graphics.ColorUtils
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ZWJ
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendarDigits
import com.byagowi.persiancalendar.global.secondaryCalendarDigits
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.resolveResourceIdFromTheme
import com.byagowi.persiancalendar.ui.utils.sp
import com.byagowi.persiancalendar.utils.appPrefs
import ir.namoo.commons.PREF_APP_FONT
import ir.namoo.commons.SYSTEM_DEFAULT_FONT
import ir.namoo.commons.utils.getAppFont

class SharedDayViewData(
    context: Context, height: Float, diameter: Float = height,
    @ColorInt private val widgetTextColor: Int? = null
) {
    private val dp = context.resources.dp
    val isArabicScript = language.isArabicScript
    val circlesPadding = 1 * dp
    val eventYOffset = diameter * 12 / 40
    val eventIndicatorRadius = diameter * 2 / 40
    private val eventIndicatorsGap = diameter * 2 / 40
    val eventIndicatorsCentersDistance = 2 * eventIndicatorRadius + eventIndicatorsGap
    val scorpioSign = context.getString(R.string.scorpio).first() + if (isArabicScript) ZWJ else ""

    val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height.toInt())

    private fun addShadowIfNeeded(paint: Paint) {
        if (widgetTextColor == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return
        paint.setShadowLayer(1f, 1f, 1f, Color.BLACK)
    }

    @DrawableRes
    val selectableItemBackground = if (widgetTextColor == null) context.resolveResourceIdFromTheme(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            android.R.attr.selectableItemBackgroundBorderless
        else android.R.attr.selectableItemBackground
    ) else 0

    val appointmentIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = context.resolveColor(R.attr.colorAppointment)
    }
    val eventIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = widgetTextColor ?: context.resolveColor(R.attr.colorEventIndicator)
    }

    val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = context.resolveColor(R.attr.colorSelectedDay)
    }

    val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = 1 * dp
        it.color = widgetTextColor ?: context.resolveColor(R.attr.colorCurrentDay)
    }

    private val mainCalendarDigitsIsArabic = mainCalendarDigits === Language.ARABIC_DIGITS

    //    private val textSize = diameter * (if (mainCalendarDigitsIsArabic) 18 else 25) / 40
    private val textSize = diameter * (when {
        mainCalendarDigitsIsArabic -> 18
        context.appPrefs.getString(
            PREF_APP_FONT, SYSTEM_DEFAULT_FONT
        )?.contains("Vazir") ?: false -> 20
        else -> 25
    }) / 40
    val dayOffset = if (mainCalendarDigitsIsArabic) 0f else context.resources.sp(3f)

    private val secondaryCalendarDigitsIsArabic = secondaryCalendarDigits === Language.ARABIC_DIGITS
    private val headerTextSize = diameter / 40 * (if (secondaryCalendarDigitsIsArabic) 11 else 15)
    val headerYOffset = -diameter * (if (secondaryCalendarDigitsIsArabic) 10 else 7) / 60

    val dayOfMonthNumberTextHolidayPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = textSize
        it.color = context.resolveColor(R.attr.colorHoliday)
        it.typeface = getAppFont(context)
        addShadowIfNeeded(it)
    }

    private val colorTextDay = widgetTextColor ?: context.resolveColor(R.attr.colorOnAppBar)
    val dayOfMonthNumberTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = textSize
        it.color = colorTextDay
        it.typeface = getAppFont(context)
        addShadowIfNeeded(it)
    }

    private val colorTextDaySelected =
        widgetTextColor ?: context.resolveColor(R.attr.colorTextDaySelected)
    val dayOfMonthNumberTextSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = textSize
        it.color = colorTextDaySelected
        it.typeface = getAppFont(context)
        addShadowIfNeeded(it)
    }
    val headerTextSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = headerTextSize
        it.color = colorTextDaySelected
        addShadowIfNeeded(it)
    }

    private val colorTextDayName = ColorUtils.setAlphaComponent(
        widgetTextColor ?: context.resolveColor(R.attr.colorOnAppBar),
        0xCC
    )
    val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = headerTextSize
        it.color = colorTextDayName
        addShadowIfNeeded(it)
    }
    val weekNumberTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = headerTextSize
        it.color = colorTextDayName
        it.typeface = getAppFont(context)
        addShadowIfNeeded(it)
    }
    val weekDayInitialsTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = diameter * 20 / 40
        it.color = colorTextDayName
        it.typeface = getAppFont(context)
        addShadowIfNeeded(it)
    }

    val widgetFooterTextPaint = widgetTextColor?.let { widgetTextColor ->
        Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.textAlign = Paint.Align.CENTER
            it.textSize = diameter * 20 / 40
            it.color = widgetTextColor
            it.alpha = 90
        }
    }
}
