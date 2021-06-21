package ir.namoo.religiousprayers.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.widget.LinearLayoutCompat
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Clock
import ir.namoo.religiousprayers.PREF_GEOCODED_CITYNAME
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.utils.Jdn
import ir.namoo.religiousprayers.utils.appPrefs
import ir.namoo.religiousprayers.utils.appTheme
import ir.namoo.religiousprayers.utils.dayTitleSummary
import ir.namoo.religiousprayers.utils.getAppFont
import ir.namoo.religiousprayers.utils.getNextOwghatTimeId
import ir.namoo.religiousprayers.utils.mainCalendar
import ir.namoo.religiousprayers.utils.prayTimes
import ir.namoo.religiousprayers.utils.selectedWidgetBackgroundColor
import ir.namoo.religiousprayers.utils.selectedWidgetNextAthanTextColor
import ir.namoo.religiousprayers.utils.selectedWidgetTextColor
import ir.namoo.religiousprayers.utils.toFormattedString
import ir.namoo.religiousprayers.utils.toJavaCalendar

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

    private val athanNames = resources.getStringArray(R.array.prayerTimeNames)
    private val nextTimeIndex: Int

    @ColorInt
    private val textColorNext: Int
@ColorInt
    private val textColor: Int

    private val pDate: PersianDate

    init {
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
        context.setTheme(appTheme)
        textColor = Color.parseColor(selectedWidgetTextColor)
        textColorNext = Color.parseColor(selectedWidgetNextAthanTextColor)
        val bgColor = Color.parseColor(selectedWidgetBackgroundColor)

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
        prayTimePaint.textSize = displayMetrics.density * 15
        prayTimePaint.typeface = getAppFont(context)
        prayTimePaint.color = textColor
        prayTimePaint.textAlign = Paint.Align.LEFT

        val civilDate = Jdn.today.toGregorianCalendar()
        pDate = PersianDate(civilDate.toJdn())
        val now = Clock(Jdn.today.toJavaCalendar())
        nextTimeIndex = getNextOwghatTimeId(now, false, context)

    }//end of init

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawBackground(canvas)
        drawDateStr(canvas)
        drawMorning(canvas)
        drawSunrise(canvas)
        drawNoon(canvas)
        drawEvening(canvas)
        drawSunset(canvas)
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

    private fun drawMorning(canvas: Canvas?) {
        prayTimes?.let {
            if (nextTimeIndex == 0) {
                prayNamePaint.color = textColorNext
                prayTimePaint.color = textColorNext
            }else{
                prayNamePaint.color = textColor
                prayTimePaint.color = textColor
            }
            val time = it.fajrClock.toFormattedString()
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
            if (nextTimeIndex == 1) {
                prayNamePaint.color = textColorNext
                prayTimePaint.color = textColorNext
            }else{
                prayNamePaint.color = textColor
                prayTimePaint.color = textColor
            }
            val time = it.sunriseClock.toFormattedString()
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

    private fun drawNoon(canvas: Canvas?) {
        prayTimes?.let {
            if (nextTimeIndex == 2) {
                prayNamePaint.color = textColorNext
                prayTimePaint.color = textColorNext
            }else{
                prayNamePaint.color = textColor
                prayTimePaint.color = textColor
            }
            val time = it.dhuhrClock.toFormattedString()
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

    private fun drawEvening(canvas: Canvas?) {
        prayTimes?.let {
            if (nextTimeIndex == 3) {
                prayNamePaint.color = textColorNext
                prayTimePaint.color = textColorNext
            }else{
                prayNamePaint.color = textColor
                prayTimePaint.color = textColor
            }
            var x = (width - width / 6 * 3).toFloat()
            val bounds = Rect()
            val athanName = athanNames[3]
            val time: String = it.asrClock.toFormattedString()
            prayNamePaint.getTextBounds(athanName, 0, athanName.length, bounds)
            var y =  /*bounds.height() +*/paddingTop + height / 2.toFloat()
            canvas!!.drawText(athanName, x - (width / 12 - bounds.width() / 2), y, prayNamePaint)

            y += bounds.height() + paddingTop * 2.toFloat()
            prayTimePaint.getTextBounds(time, 0, time.length, bounds)
            x = width - width / 6 * 4.toFloat()
            canvas.drawText(time, x + bounds.width() / 2, y, prayTimePaint)
        }
    }//end of drawEvening

    private fun drawSunset(canvas: Canvas?) {
        prayTimes?.let {
            if (nextTimeIndex == 4) {
                prayNamePaint.color = textColorNext
                prayTimePaint.color = textColorNext
            }else{
                prayNamePaint.color = textColor
                prayTimePaint.color = textColor
            }
            val time = it.maghribClock.toFormattedString()
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
            if (nextTimeIndex == 5) {
                prayNamePaint.color = textColorNext
                prayTimePaint.color = textColorNext
            }else{
                prayNamePaint.color = textColor
                prayTimePaint.color = textColor
            }
            val time = it.ishaClock.toFormattedString()
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
                    " ${dayTitleSummary(Jdn.today, Jdn.today.toCalendar(mainCalendar))}" +
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
