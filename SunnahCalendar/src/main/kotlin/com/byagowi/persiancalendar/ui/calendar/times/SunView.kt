package com.byagowi.persiancalendar.ui.calendar.times

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.core.graphics.withClip
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.common.SolarDraw
import com.byagowi.persiancalendar.ui.utils.dp
import io.github.cosinekitty.astronomy.Ecliptic
import io.github.cosinekitty.astronomy.Spherical
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.sunPosition
import io.github.persiancalendar.praytimes.PrayTimes
import java.util.GregorianCalendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

data class SunViewColors(
    @get:ColorInt val nightColor: Int,
    @get:ColorInt val dayColor: Int,
    @get:ColorInt val middayColor: Int,
    @get:ColorInt val sunriseTextColor: Int,
    @get:ColorInt val middayTextColor: Int,
    @get:ColorInt val sunsetTextColor: Int,
    @get:ColorInt val textColorSecondary: Int,
    @get:ColorInt val linesColor: Int,
)

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class SunView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dayPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).also { it.style = Paint.Style.FILL_AND_STROKE }

    internal var width: Int = 0
    internal var height: Int = 0
    private val curvePath = Path()
    private val nightPath = Path()
    private var current = 0f
    private var dayLengthString = ""
    private var remainingString = ""
    private val sunriseString = context.getString(R.string.sunrise_sun_view)
    private val middayString = context.getString(R.string.midday_sun_view)
    private val sunsetString = context.getString(R.string.sunset_sun_view)
    private var segmentByPixel = .0
    var prayTimes: PrayTimes? = null
        set(value) {
            field = value
            invalidate()
        }
    private var sun: Ecliptic? = null
    private var moon: Spherical? = null
    private val fontSize = (if (language.value.isArabicScript) 14f else 11.5f) * resources.dp

    fun setTime(date: Long) {
        val time = Time.fromMillisecondsSince1970(date)
        sun = sunPosition(time)
        moon = eclipticGeoMoon(time)
        invalidate()
    }

    var colors: SunViewColors? = null

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        val colors = colors ?: return

        width = w
        height = h - 18

        dayPaint.shader = LinearGradient(
            width * .17f, 0f, width / 2f, 0f, colors.dayColor, colors.middayColor,
            Shader.TileMode.MIRROR
        )

        if (width != 0) segmentByPixel = 2 * PI / width

        curvePath.also {
            it.rewind()
            it.moveTo(0f, height.toFloat())
            (0..width).forEach { x ->
                it.lineTo(x.toFloat(), getY(x, segmentByPixel, (height * .9f).toInt()))
            }
        }

        nightPath.also {
            it.rewind()
            it.addPath(curvePath)
            it.setLastPoint(width.toFloat(), height.toFloat())
            it.lineTo(width.toFloat(), 0f)
            it.lineTo(0f, 0f)
            it.close()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (clippingPath.isEmpty) mainDraw(canvas) // no change if there is path is empty
        else canvas.withClip(clippingPath) { mainDraw(canvas) }
    }

    // A home-screen widget with background has some roundness that is taken care by a passed path
    var clippingPath = Path()

    private fun mainDraw(canvas: Canvas) {
        val colors = colors ?: return
        val animatedFraction = if (animator.isRunning) animator.animatedFraction else 1f
        val value = animatedFraction * current
        val width = width
        val height = height
        val isRtl = layoutDirection == LAYOUT_DIRECTION_RTL
        canvas.withScale(x = if (isRtl) -1f else 1f, pivotX = width / 2f) {
            // draw fill of night
            withClip(0f, height * .75f, width * value, height.toFloat()) {
                paint.also {
                    it.style = Paint.Style.FILL
                    it.color = colors.nightColor
                }
                drawPath(nightPath, paint)
            }

            // draw fill of day
            withClip(0f, 0f, width * value, height * .75f) {
                drawPath(curvePath, dayPaint)
            }

            // draw time curve
            paint.also {
                it.strokeWidth = 3f
                it.style = Paint.Style.STROKE
                it.color = colors.linesColor
            }
            drawPath(curvePath, paint)
            // draw horizon line
            drawLine(0f, height * .75f, width.toFloat(), height * .75f, paint)
            // draw sunset and sunrise tag line indicator
            paint.strokeWidth = 2f
            drawLine(width * .17f, height * .3f, width * .17f, height * .7f, paint)
            drawLine(width * .83f, height * .3f, width * .83f, height * .7f, paint)
            drawLine(width / 2f, height * .7f, width / 2f, height * .8f, paint)

            // draw sun
            val radius = sqrt(width * height * .002f)
            val cx = width * value
            val cy = getY((width * value).toInt(), segmentByPixel, (height * .9f).toInt())
            if (value in .17f..0.83f) withRotation(animatedFraction * 900f, cx, cy) {
                solarDraw.sun(canvas, cx, cy, radius, solarDraw.sunColor(value))
            } else canvas.withScale(x = if (isRtl) -1f else 1f, pivotX = cx) { // cancel parent flip
                run {
                    solarDraw.moon(
                        canvas, sun ?: return@run, moon ?: return@run, cx, cy, radius
                    )
                }
            }
        }

        // draw text
        paint.also {
            it.textAlign = Paint.Align.CENTER
            it.textSize = fontSize
            it.strokeWidth = 0f
            it.style = Paint.Style.FILL
            it.color = colors.sunriseTextColor
        }
        canvas.drawText(
            sunriseString, width * if (isRtl) .83f else .17f, height * .2f, paint
        )
        paint.color = colors.middayTextColor
        canvas.drawText(middayString, width / 2f, height * .94f, paint)
        paint.color = colors.sunsetTextColor
        canvas.drawText(
            sunsetString, width * if (isRtl) .17f else .83f, height * .2f, paint
        )

        // draw remaining time
        paint.also {
            it.textAlign = Paint.Align.CENTER
            it.strokeWidth = 0f
            it.style = Paint.Style.FILL
            it.color = colors.textColorSecondary
        }
        canvas.drawText(
            dayLengthString, width * if (isRtl) .70f else .30f, height * .94f, paint
        )
        canvas.drawText(
            remainingString, width * if (isRtl) .30f else .70f, height * .94f, paint
        )
    }

    private val solarDraw = SolarDraw(resources)

    private fun getY(x: Int, segment: Double, height: Int): Float =
        height - height * ((cos(-PI + x * segment) + 1f) / 2f).toFloat() + height * .1f

    fun initiate() {
        val prayTimes = prayTimes ?: return

        val sunset = prayTimes.sunset
        val sunrise = prayTimes.sunrise
        val fajr = prayTimes.fajr
        val maghrib = prayTimes.maghrib
        val now = Clock(GregorianCalendar()).value

        fun Double.safeDiv(other: Double): Float = if (other == .0) 0f else (this / other).toFloat()
        current = when {
            now <= sunrise -> now.safeDiv(sunrise) * .17f
            now <= sunset -> (now - sunrise).safeDiv(sunset - sunrise) * .66f + .17f
            else -> (now - sunset).safeDiv(24 - sunset) * .17f + .17f + .66f
        }

        val dayLength = Clock(maghrib - fajr)
        dayLengthString = context.getString(R.string.length_of_day) + spacedColon +
                dayLength.asRemainingTime(resources, short = true)

        val remaining = if (now > sunset || now < sunrise) null else Clock(sunset - now)
        remainingString = if (remaining == null) "" else
            context.getString(R.string.remaining_daylight) + spacedColon +
                    remaining.asRemainingTime(resources, short = true)
        // a11y
        contentDescription = context.getString(R.string.length_of_day) + spacedColon +
                dayLength.asRemainingTime(resources) + if (remaining == null) "" else
            ("\n\n" + context.getString(R.string.remaining_daylight) + spacedColon +
                    remaining.asRemainingTime(resources))

        invalidate()
    }

    private val animator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = resources.getInteger(android.R.integer.config_longAnimTime) * 3L
        it.interpolator = DecelerateInterpolator()
        it.addUpdateListener { invalidate() }
    }

    fun startAnimate() {
        initiate()
        animator.start()
    }
}
