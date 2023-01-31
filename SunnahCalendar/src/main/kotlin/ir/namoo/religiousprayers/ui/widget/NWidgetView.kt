package ir.namoo.religiousprayers.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.ColorInt
import androidx.appcompat.widget.LinearLayoutCompat
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR
import com.byagowi.persiancalendar.DEFAULT_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LOCAL_DIGITS
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_WIDGETS_PREFER_SYSTEM_COLORS
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getNextOwghatTimeId
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.utils.getAppFont
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
private var prefersWidgetsDynamicColors = false

class NWidgetView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    //############################################
    private val displayMetrics: DisplayMetrics = DisplayMetrics()
    private val circlePaint: Paint
    private val dayNumPaint: Paint
    private val dayStringPaint: Paint

    private val borderPaint: Paint
    private val backgroundPaint: Paint

    private val prayNamePaint: Paint
    private val prayTimePaint: Paint

    private val athanNames = arrayOf(
        resources.getString(R.string.fajr), resources.getString(R.string.sunrise),
        resources.getString(R.string.dhuhr), resources.getString(R.string.asr),
        resources.getString(R.string.maghrib), resources.getString(R.string.isha)
    )
    private val nextTimeIndex: Int?

    @ColorInt
    private val textColorNext: Int

    @ColorInt
    private val textColor: Int

    private val pDate: PersianDate
    var prayTimes: PrayTimes?

    init {
        prefersWidgetsDynamicColors = Theme.isDynamicColor(context.appPrefs) &&
                context.appPrefs.getBoolean(PREF_WIDGETS_PREFER_SYSTEM_COLORS, true)

        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay.getMetrics(displayMetrics)
        setPadding(
            (displayMetrics.density * 5).toInt(),
            (displayMetrics.density * 5).toInt(),
            (displayMetrics.density * 5).toInt(),
            (displayMetrics.density * 5).toInt()
        )
        val params = LinearLayoutCompat.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(
            (displayMetrics.density * 5).toInt(),
            (displayMetrics.density * 5).toInt(),
            (displayMetrics.density * 5).toInt(),
            (displayMetrics.density * 5).toInt()
        )
        layoutParams = params

        textColor =
            if (prefersWidgetsDynamicColors)
                context.resolveColor(android.R.attr.colorForeground)
            else
                context.appPrefs.getString(PREF_SELECTED_WIDGET_TEXT_COLOR, null)
                    ?.let(Color::parseColor)
                    ?: DEFAULT_SELECTED_WIDGET_TEXT_COLOR

        textColorNext =
            if (prefersWidgetsDynamicColors)
                context.resolveColor(android.R.attr.colorAccent)
            else
                context.appPrefs.getString(PREF_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR, null)
                    ?.let(Color::parseColor)
                    ?: DEFAULT_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR

        val bgColor = if (prefersWidgetsDynamicColors)
            context.resolveColor(android.R.attr.colorBackground)
        else context.appPrefs.getString(PREF_SELECTED_WIDGET_BACKGROUND_COLOR, null)
            ?.let(Color::parseColor)
            ?: DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR

        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint.style = Paint.Style.STROKE
        circlePaint.color = textColor
        circlePaint.strokeWidth = displayMetrics.density * 2

        dayNumPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        dayNumPaint.textSize = displayMetrics.density * 22
        dayNumPaint.typeface = getAppFont(context)
        dayNumPaint.color = textColor
        dayNumPaint.textAlign = Paint.Align.CENTER

        dayStringPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        dayStringPaint.textSize = displayMetrics.density * 15
        dayStringPaint.typeface = getAppFont(context)
        dayStringPaint.color = textColor
        dayStringPaint.textAlign = Paint.Align.RIGHT

        borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint.style = Paint.Style.STROKE
        borderPaint.color = textColor
        borderPaint.strokeWidth = displayMetrics.density * 2

        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.color = bgColor
        backgroundPaint.strokeWidth = displayMetrics.density * 2

        prayNamePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        prayNamePaint.textSize = displayMetrics.density * 14
        prayNamePaint.typeface = getAppFont(context)
        prayNamePaint.color = textColor
        prayNamePaint.textAlign = Paint.Align.RIGHT

        prayTimePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        prayTimePaint.textSize = if (context.appPrefs.getBoolean(PREF_LOCAL_DIGITS, true))
            displayMetrics.density * 13
        else displayMetrics.density * 15
        prayTimePaint.typeface = getAppFont(context)
        prayTimePaint.color = textColor
        prayTimePaint.textAlign = Paint.Align.LEFT

        val civilDate = Jdn.today()
        pDate = PersianDate(civilDate.value)
        val now = Clock(Jdn.today().toJavaCalendar())
        prayTimes = coordinates?.calculatePrayTimes()
        prayTimes = PrayTimeProvider(context).nReplace(prayTimes, civilDate)
        nextTimeIndex = prayTimes?.getNextOwghatTimeId(now)

    }//end of init

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawBackground(canvas)
        drawDateStr(canvas)
        drawFajr(canvas)
        drawSunrise(canvas)
        drawDhuhr(canvas)
        drawAsr(canvas)
        drawMaghrib(canvas)
        drawIsha(canvas)
        drawBorder(canvas)
    }//end of onDraw

    private fun drawBackground(canvas: Canvas?) {
        canvas?.drawRoundRect(
            RectF(
                paddingLeft.toFloat(),
                paddingTop.toFloat(),
                (width - paddingRight).toFloat(),
                (height - paddingBottom).toFloat()
            ),
            20f, 20f, backgroundPaint
        )
    }//end of drawBackground

    private fun drawFajr(canvas: Canvas?) {
        prayTimes?.let {
            if (nextTimeIndex == R.string.fajr) {
                prayNamePaint.color = textColorNext
                prayTimePaint.color = textColorNext
            } else {
                prayNamePaint.color = textColor
                prayTimePaint.color = textColor
            }
            val time = it.getFromStringId(R.string.fajr).toFormattedString()
            var x = width.toFloat()
            val bounds = Rect()
            prayNamePaint.getTextBounds(athanNames[0], 0, athanNames[0].length, bounds)
            var y =  /*bounds.height() + */paddingTop + height / 2.toFloat()
            canvas!!.drawText(
                athanNames[0], x - (width / 12 - bounds.width() / 2), y, prayNamePaint
            )

            y += bounds.height() + paddingTop * 2.toFloat()
            prayTimePaint.getTextBounds(time, 0, time.length, bounds)
            x = width - width / 6.toFloat()
            canvas.drawText(time, x + bounds.width() / 2, y, prayTimePaint)
        }

    }//end of drawMorning

    private fun drawSunrise(canvas: Canvas?) {
        prayTimes?.let {
            if (nextTimeIndex == R.string.sunrise) {
                prayNamePaint.color = textColorNext
                prayTimePaint.color = textColorNext
            } else {
                prayNamePaint.color = textColor
                prayTimePaint.color = textColor
            }
            val time = it.getFromStringId(R.string.sunrise).toFormattedString()
            var x = (width - width / 6).toFloat()
            val bounds = Rect()
            prayNamePaint.getTextBounds(athanNames[1], 0, athanNames[1].length, bounds)
            var y =  /*bounds.height() + */paddingTop + height / 2.toFloat()
            canvas!!.drawText(
                athanNames[1], x - (width / 12 - bounds.width() / 2), y, prayNamePaint
            )

            y += bounds.height() + paddingTop * 2.toFloat()
            prayTimePaint.getTextBounds(time, 0, time.length, bounds)
            x = width - width / 6 * 2.toFloat()
            canvas.drawText(time, x + bounds.width() / 2, y, prayTimePaint)
        }
    }//end of drawSunrise

    private fun drawDhuhr(canvas: Canvas?) {
        prayTimes?.let {
            if (nextTimeIndex == R.string.dhuhr) {
                prayNamePaint.color = textColorNext
                prayTimePaint.color = textColorNext
            } else {
                prayNamePaint.color = textColor
                prayTimePaint.color = textColor
            }
            val time = it.getFromStringId(R.string.dhuhr).toFormattedString()
            var x = (width - width / 6 * 2).toFloat()
            val bounds = Rect()
            prayNamePaint.getTextBounds(athanNames[2], 0, athanNames[2].length, bounds)
            var y =  /*bounds.height() + */paddingTop + height / 2.toFloat()
            canvas!!.drawText(
                athanNames[2], x - (width / 12 - bounds.width() / 2), y, prayNamePaint
            )

            y += bounds.height() + paddingTop * 2.toFloat()
            prayTimePaint.getTextBounds(time, 0, time.length, bounds)
            x = width - width / 6 * 3.toFloat()
            canvas.drawText(time, x + bounds.width() / 2, y, prayTimePaint)
        }
    }//end of drawNoon

    private fun drawAsr(canvas: Canvas?) {
        prayTimes?.let {
            if (nextTimeIndex == R.string.asr) {
                prayNamePaint.color = textColorNext
                prayTimePaint.color = textColorNext
            } else {
                prayNamePaint.color = textColor
                prayTimePaint.color = textColor
            }
            var x = (width - width / 6 * 3).toFloat()
            val bounds = Rect()
            val athanName = athanNames[3]
            val time: String = it.getFromStringId(R.string.asr).toFormattedString()
            prayNamePaint.getTextBounds(athanName, 0, athanName.length, bounds)
            var y =  /*bounds.height() +*/paddingTop + height / 2.toFloat()
            canvas!!.drawText(athanName, x - (width / 12 - bounds.width() / 2), y, prayNamePaint)

            y += bounds.height() + paddingTop * 2.toFloat()
            prayTimePaint.getTextBounds(time, 0, time.length, bounds)
            x = width - width / 6 * 4.toFloat()
            canvas.drawText(time, x + bounds.width() / 2, y, prayTimePaint)
        }
    }//end of drawEvening

    private fun drawMaghrib(canvas: Canvas?) {
        prayTimes?.let {
            if (nextTimeIndex == R.string.maghrib || nextTimeIndex == R.string.sunset) {
                prayNamePaint.color = textColorNext
                prayTimePaint.color = textColorNext
            } else {
                prayNamePaint.color = textColor
                prayTimePaint.color = textColor
            }
            val time = it.getFromStringId(R.string.maghrib).toFormattedString()
            var x = (width - width / 6 * 4).toFloat()
            val bounds = Rect()
            prayNamePaint.getTextBounds(athanNames[4], 0, athanNames[4].length, bounds)
            var y =  /*bounds.height() + */paddingTop + height / 2.toFloat()
            canvas!!.drawText(
                athanNames[4], x - (width / 12 - bounds.width() / 2), y, prayNamePaint
            )

            y += bounds.height() + paddingTop * 2.toFloat()
            prayTimePaint.getTextBounds(time, 0, time.length, bounds)
            x = width - width / 6 * 5.toFloat()
            canvas.drawText(time, x + bounds.width() / 2, y, prayTimePaint)
        }
    }//end of drawSunset

    private fun drawIsha(canvas: Canvas?) {
        prayTimes?.let {
            if (nextTimeIndex == R.string.isha) {
                prayNamePaint.color = textColorNext
                prayTimePaint.color = textColorNext
            } else {
                prayNamePaint.color = textColor
                prayTimePaint.color = textColor
            }
            val time = it.getFromStringId(R.string.isha).toFormattedString()
            var x = (width - width / 6 * 5).toFloat()
            val bounds = Rect()
            prayNamePaint.getTextBounds(athanNames[5], 0, athanNames[5].length, bounds)
            var y =  /*bounds.height() + */paddingTop + height / 2.toFloat()
            canvas!!.drawText(
                athanNames[5], x - (width / 12 - bounds.width() / 2), y, prayNamePaint
            )

            y += bounds.height() + paddingTop * 2.toFloat()
            prayTimePaint.getTextBounds(time, 0, time.length, bounds)
            x = width - width / 6 * 6.toFloat()
            canvas.drawText(time, x + bounds.width() / 2, y, prayTimePaint)
        }
    }//end of drawIsha

    private fun drawDateStr(canvas: Canvas?) {
        val text =
            "${resources.getString(R.string.today)} :" +
                    " ${dayTitleSummary(Jdn.today(), Jdn.today().toCalendar(mainCalendar))}" +
                    " - ${
                        context.appPrefs.getString(
                            PREF_GEOCODED_CITYNAME, ""
                        )
                    }"

        val bounds = Rect()
        dayStringPaint.getTextBounds(text, 0, text.length, bounds)
        canvas!!.drawText(
            text,
            (width / 2 + paddingRight + bounds.width() / 2).toFloat(),
            paddingTop + bounds.height().toFloat(),
            dayStringPaint
        )

        canvas.drawLine(
            paddingLeft.toFloat(),
            paddingTop * 3 + bounds.height().toFloat(),
            width - paddingRight.toFloat(),
            paddingTop * 3 + bounds.height().toFloat(),
            borderPaint
        )
    }

    private fun drawBorder(canvas: Canvas?) {
        canvas!!.drawRoundRect(
            RectF(
                paddingLeft.toFloat(),
                paddingTop.toFloat(),
                (width - paddingRight).toFloat(),
                (height - paddingBottom).toFloat()
            ),
            20f, 20f, borderPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measureWidth(), measureHeight())
    }

    private fun measureHeight(): Int = displayMetrics.heightPixels / 7

    private fun measureWidth(): Int = displayMetrics.widthPixels

}//end of class
