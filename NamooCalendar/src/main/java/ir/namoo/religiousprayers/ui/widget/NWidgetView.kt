package ir.namoo.religiousprayers.ui.widget

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.religiousprayers.PREF_GEOCODED_CITYNAME
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.praytimes.PrayTimeProvider
import ir.namoo.religiousprayers.utils.*
import java.util.*

class NWidgetView : View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet?) : super(context, attributes)
    constructor(context: Context, attributes: AttributeSet?, defStyle: Int) : super(
        context,
        attributes,
        defStyle
    )

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attributes: AttributeSet?,
        defStyle: Int,
        defStyleRes: Int
    ) : super(context, attributes, defStyle, defStyleRes)

    //############################################
    private val displayMetrics: DisplayMetrics
    private val circlePaint: Paint
    private val dayNumPaint: Paint
    private val dayStringPaint: Paint

    private val borderPaint: Paint
    private val backgroundPaint: Paint

    private val prayNamePaint: Paint
    private val prayTimePaint: Paint
    private val prayNextPaint: Paint

    private val prayTimes: PrayTimes
    private val athanNames = resources.getStringArray(R.array.prayerTimeNames)
    private val nextTimeIndex: Int

    private val pDate: PersianDate

    init {
        displayMetrics = DisplayMetrics()
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
        val textColor = getColorFromAttr(context, R.attr.colorNWidgetText)
        val bgColor = getColorFromAttr(context, R.attr.colorNWidgetBackground)

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

        prayNextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        prayNextPaint.color = textColor
        prayNextPaint.style = Paint.Style.STROKE
        prayNextPaint.strokeWidth = displayMetrics.density * 2

        prayTimes = PrayTimeProvider.calculate(
            calculationMethod,
            CivilDate(getTodayJdn()).toCalendar().time,
            getCoordinate(context)!!,
            context
        )
        val civilDate = calendarToCivilDate(makeCalendarFromDate(Date()))
        pDate = PersianDate(civilDate.toJdn())
        val now = Clock(makeCalendarFromDate(Date()))
        nextTimeIndex = getNextOwghatTimeId(now, false, context)

    }//end of init

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawBackground(canvas)

        //drawCircle(canvas);
        //drawDateNum(canvas);
        //drawCircle(canvas);
        //drawDateNum(canvas);
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
        val time = prayTimes.fajrClock.toFormattedString()
        var x = width.toFloat()
        val bounds = Rect()
        prayNamePaint.getTextBounds(athanNames[0], 0, athanNames[0].length, bounds)
        var y =  /*bounds.height() + */paddingTop + height / 2.toFloat()
        canvas!!.drawText(
            athanNames[0],
            x - (width / 12 - bounds.width() / 2),
            y,
            prayNamePaint
        )

        y += bounds.height() + paddingTop * 2.toFloat()
        prayTimePaint.getTextBounds(
            time,
            0,
            time.length,
            bounds
        )
        x = width - width / 6.toFloat()
        canvas.drawText(
            time,
            x + bounds.width() / 3,
            y,
            prayTimePaint
        )

        if (nextTimeIndex == 0) { //DrawNextHere
            canvas.drawLine(
                x + bounds.width() / 3,
                y + bounds.height() / 4,
                x + bounds.width() + bounds.width() / 3,
                y + bounds.height() / 4,
                prayNextPaint
            )
        }
    }//end of drawMorning

    private fun drawSunrise(canvas: Canvas?) {
        val time = prayTimes.sunriseClock.toFormattedString()
        var x = (width - width / 6).toFloat()
        val bounds = Rect()
        prayNamePaint.getTextBounds(athanNames[1], 0, athanNames[1].length, bounds)
        var y =  /*bounds.height() + */paddingTop + height / 2.toFloat()
        canvas!!.drawText(
            athanNames[1],
            x - (width / 12 - bounds.width() / 2),
            y,
            prayNamePaint
        )

        y += bounds.height() + paddingTop * 2.toFloat()
        prayTimePaint.getTextBounds(
            time,
            0,
            time.length,
            bounds
        )
        x = width - width / 6 * 2.toFloat()
        canvas.drawText(
            time,
            x + bounds.width() / 3,
            y,
            prayTimePaint
        )

        if (nextTimeIndex == 1) { //DrawNextHere
            canvas.drawLine(
                x + bounds.width() / 3,
                y + bounds.height() / 4,
                x + bounds.width() + bounds.width() / 3,
                y + bounds.height() / 4,
                prayNextPaint
            )
        }
    }//end of drawSunrise

    private fun drawNoon(canvas: Canvas?) {
        val time = prayTimes.dhuhrClock.toFormattedString()
        var x = (width - width / 6 * 2).toFloat()
        val bounds = Rect()
        prayNamePaint.getTextBounds(athanNames[2], 0, athanNames[2].length, bounds)
        var y =  /*bounds.height() + */paddingTop + height / 2.toFloat()
        canvas!!.drawText(
            athanNames[2],
            x - (width / 12 - bounds.width() / 2),
            y,
            prayNamePaint
        )

        y += bounds.height() + paddingTop * 2.toFloat()
        prayTimePaint.getTextBounds(
            time,
            0,
            time.length,
            bounds
        )
        x = width - width / 6 * 3.toFloat()
        canvas.drawText(
            time,
            x + bounds.width() / 3,
            y,
            prayTimePaint
        )

        if (nextTimeIndex == 2) { //DrawNextHere
            canvas.drawLine(
                x + bounds.width() / 3,
                y + bounds.height() / 4,
                x + bounds.width() + bounds.width() / 3,
                y + bounds.height() / 4,
                prayNextPaint
            )
        }
    }//end of drawNoon

    private fun drawEvening(canvas: Canvas?) {
        var x = (width - width / 6 * 3).toFloat()
        val bounds = Rect()
        val athanName = athanNames[3]
        val time: String = prayTimes.asrClock.toFormattedString()
        prayNamePaint.getTextBounds(athanName, 0, athanName.length, bounds)
        var y =  /*bounds.height() +*/paddingTop + height / 2.toFloat()
        canvas!!.drawText(athanName, x - (width / 12 - bounds.width() / 2), y, prayNamePaint)

        y += bounds.height() + paddingTop * 2.toFloat()
        prayTimePaint.getTextBounds(time, 0, time.length, bounds)
        x = width - width / 6 * 4.toFloat()
        canvas.drawText(time, x + bounds.width() / 3, y, prayTimePaint)

        if (nextTimeIndex == 3) { //DrawNextHere
            canvas.drawLine(
                x + bounds.width() / 3,
                y + bounds.height() / 4,
                x + bounds.width() + bounds.width() / 3,
                y + bounds.height() / 4,
                prayNextPaint
            )
        }
    }//end of drawEvening

    private fun drawSunset(canvas: Canvas?) {
        val time = prayTimes.maghribClock.toFormattedString()
        var x = (width - width / 6 * 4).toFloat()
        val bounds = Rect()
        prayNamePaint.getTextBounds(athanNames[4], 0, athanNames[4].length, bounds)
        var y =  /*bounds.height() + */paddingTop + height / 2.toFloat()
        canvas!!.drawText(
            athanNames[4],
            x - (width / 12 - bounds.width() / 2),
            y,
            prayNamePaint
        )

        y += bounds.height() + paddingTop * 2.toFloat()
        prayTimePaint.getTextBounds(
            time,
            0,
            time.length,
            bounds
        )
        x = width - width / 6 * 5.toFloat()
        canvas.drawText(
            time,
            x + bounds.width() / 3,
            y,
            prayTimePaint
        )

        if (nextTimeIndex == 4) { //DrawNextHere
            canvas.drawLine(
                x + bounds.width() / 3,
                y + bounds.height() / 4,
                x + bounds.width() + bounds.width() / 3,
                y + bounds.height() / 4,
                prayNextPaint
            )
        }
    }//end of drawSunset

    private fun drawIsha(canvas: Canvas?) {
        val time = prayTimes.ishaClock.toFormattedString()
        var x = (width - width / 6 * 5).toFloat()
        val bounds = Rect()
        prayNamePaint.getTextBounds(athanNames[5], 0, athanNames[5].length, bounds)
        var y =  /*bounds.height() + */paddingTop + height / 2.toFloat()
        canvas!!.drawText(
            athanNames[5],
            x - (width / 12 - bounds.width() / 2),
            y,
            prayNamePaint
        )

        y += bounds.height() + paddingTop * 2.toFloat()
        prayTimePaint.getTextBounds(
            time,
            0,
            time.length,
            bounds
        )
        x = width - width / 6 * 6.toFloat()
        canvas.drawText(
            time,
            x + bounds.width() / 3,
            y,
            prayTimePaint
        )

        if (nextTimeIndex == 5) { //DrawNextHere
            canvas.drawLine(
                x + bounds.width() / 3,
                y + bounds.height() / 4,
                x + bounds.width() + bounds.width() / 3,
                y + bounds.height() / 4,
                prayNextPaint
            )
        }
    }//end of drawIsha

    private fun drawCircle(canvas: Canvas?) {
        val height = height / 2
        canvas!!.drawCircle(
            width / 2.toFloat(),
            height / 2.toFloat(),
            (height / 2 - paddingTop * 1.3).toFloat(),
            circlePaint
        )
    }

    private fun drawDateNum(canvas: Canvas?) {
        val bounds = Rect()
        val height = height / 2
        dayNumPaint.getTextBounds(
            pDate.dayOfMonth.toString(),
            0,
            pDate.dayOfMonth.toString().length,
            bounds
        )
        canvas!!.drawText(
            pDate.dayOfMonth.toString() + "",
            width / 2.toFloat(),
            height / 2 + bounds.height() / 2.toFloat(),
            dayNumPaint
        )
    }

    private fun drawDateStr(canvas: Canvas?) {
        Log.d(
            ContentValues.TAG,
            "density dpi: " + displayMetrics.densityDpi + " scaled density" + displayMetrics.scaledDensity
                    + " ~ " + displayMetrics.widthPixels + " x " + displayMetrics.heightPixels
        )

//        val text =
//            resources.getString(R.string.today) + Utils.getWeekDayName(persianDate).toString() + "، " + CalendarUtils.dateToString(
//                persianDate
//            ).toString() + "" +
//                    " - افق " + cityDBAdapter.getCity(preferences.getCityCode()).getName()
        val mainCalendar = mainCalendar
        val text =
            "${resources.getString(R.string.today)} :" +
                    " ${dayTitleSummary(getTodayOfCalendar(mainCalendar))}" +
                    " - ${context.appPrefs.getString(
                        PREF_GEOCODED_CITYNAME, ""
                    )}"

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
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
    }

    private fun measureHeight(measureSpec: Int): Int { //        int size = getPaddingTop() + getPaddingBottom();
//        size += 250;
//        return resolveSizeAndState(size, measureSpec, 0);
        return displayMetrics.heightPixels / 7
    }

    private fun measureWidth(measureSpec: Int): Int { //        int size = getPaddingLeft() + getPaddingRight();
//        size += 200 * 6;
//        return resolveSizeAndState(size, measureSpec, 0);
        return displayMetrics.widthPixels
    }
}//end of class