package ir.namoo.religiousprayers.praytimes

import android.content.Context
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimes
import io.github.persiancalendar.praytimes.PrayTimesCalculator
import ir.namoo.religiousprayers.DEFAULT_CITY
import ir.namoo.religiousprayers.PREF_ENABLE_EDIT
import ir.namoo.religiousprayers.PREF_GEOCODED_CITYNAME
import ir.namoo.religiousprayers.PREF_SUMMER_TIME
import ir.namoo.religiousprayers.utils.*
import java.util.*

class PrayTimeProvider {

    companion object {
        var ptFrom = 0// 0=generated 1=exact 2=edited
        fun calculate(
            method: CalculationMethod,
            date: Date,
            coordinate: Coordinate,
            context: Context
        ): PrayTimes {
            ptFrom = 0
            PrayTimesCalculator.ASR_METHOD = asrMethod
            var times = PrayTimesCalculator.calculate(method, date, coordinate)
            val civilDate = calendarToCivilDate(makeCalendarFromDate(date))
            val pDate = PersianDate(civilDate.toJdn())
            val dayOfYear = getDayNum(pDate.month, pDate.dayOfMonth)
            times = when {
                isExistAndEnabledEdit(context) -> {
                    ptFrom = 2
                    times = replaceWithEdits(times, dayOfYear, context)
                    times
                }
                isExistExactTimes(context) -> {
                    ptFrom = 1
                    times = replaceTimes(times, dayOfYear, context)
                    times
                }
                else -> times
            }
            return if (context.appPrefs.getBoolean(
                    PREF_SUMMER_TIME,
                    true
                ) || dayOfYear !in 2..185
            ) times
            else deleteSummerTimes(times)
        }

        fun calculate(
            method: CalculationMethod,
            date: Date,
            coordinate: Coordinate,
            timeZone: Double,
            dst: Boolean,
            context: Context
        ): PrayTimes {
            ptFrom = 0
            PrayTimesCalculator.ASR_METHOD = asrMethod
            var times = PrayTimesCalculator.calculate(method, date, coordinate, timeZone, dst)
            val civilDate = calendarToCivilDate(makeCalendarFromDate(date))
            val pDate = PersianDate(civilDate.toJdn())
            val dayOfYear = getDayNum(pDate.month, pDate.dayOfMonth)
            times = when {
                isExistAndEnabledEdit(context) -> {
                    ptFrom = 2
                    times = replaceWithEdits(times, dayOfYear, context)
                    times
                }
                isExistExactTimes(context) -> {
                    ptFrom = 1
                    times = replaceTimes(times, dayOfYear, context)
                    times
                }
                else -> times
            }
            return if (context.appPrefs.getBoolean(
                    PREF_SUMMER_TIME,
                    true
                ) || dayOfYear !in 2..185
            ) times
            else deleteSummerTimes(times)
        }

        private fun replaceTimes(times: PrayTimes?, dayOfYear: Int, context: Context): PrayTimes? {
            var res = times
            val ts = DPTDB.getInstance(context.applicationContext).downloadedPrayTimes().getDownloadFor(
                context.appPrefs.getString(PREF_GEOCODED_CITYNAME, DEFAULT_CITY) ?: DEFAULT_CITY
            )
            for (t in ts!!) {
                if (t.dayNumber == dayOfYear) {
                    val strImsak = fixTime(t.fajr, -10)
                    val imsak =
                        toDouble(strImsak.split(":")[0].toInt(), strImsak.split(":")[1].toInt())
                    val fajr =
                        toDouble(t.fajr.split(":")[0].toInt(), t.fajr.split(":")[1].toInt())
                    val sunrise =
                        toDouble(t.sunrise.split(":")[0].toInt(), t.sunrise.split(":")[1].toInt())
                    val dhuhr =
                        toDouble(t.dhuhr.split(":")[0].toInt(), t.dhuhr.split(":")[1].toInt())
                    val asr = toDouble(
                        t.asr.split(":")[0].toInt(),
                        t.asr.split(":")[1].toInt()
                    )
                    val sunset =
                        toDouble(t.maghrib.split(":")[0].toInt(), t.maghrib.split(":")[1].toInt())
                    val maghrib =
                        toDouble(t.maghrib.split(":")[0].toInt(), t.maghrib.split(":")[1].toInt())
                    val isha = toDouble(t.isha.split(":")[0].toInt(), t.isha.split(":")[1].toInt())
                    val midnight = toDouble(times!!.midnightClock.hour, times.midnightClock.minute)
                    res =
                        PrayTimes(imsak, fajr, sunrise, dhuhr, asr, sunset, maghrib, isha, midnight)
                    break
                }
            }
            return res
        }

        private fun replaceWithEdits(
            times: PrayTimes?,
            dayOfYear: Int,
            context: Context
        ): PrayTimes? {
            val res: PrayTimes?
            val t = PrayTimesDB.getInstance(context).prayTimes().getEdited(dayOfYear)

            val strImsak = fixTime(t.fajr, -10)
            val imsak =
                toDouble(strImsak.split(":")[0].toInt(), strImsak.split(":")[1].toInt())
            val fajr =
                toDouble(t.fajr.split(":")[0].toInt(), t.fajr.split(":")[1].toInt())
            val sunrise =
                toDouble(t.sunrise.split(":")[0].toInt(), t.sunrise.split(":")[1].toInt())
            val dhuhr =
                toDouble(t.dhuhr.split(":")[0].toInt(), t.dhuhr.split(":")[1].toInt())
            val asr = toDouble(
                t.asr.split(":")[0].toInt(),
                t.asr.split(":")[1].toInt()
            )
            val sunset =
                toDouble(t.maghrib.split(":")[0].toInt(), t.maghrib.split(":")[1].toInt())
            val maghrib =
                toDouble(t.maghrib.split(":")[0].toInt(), t.maghrib.split(":")[1].toInt())
            val isha = toDouble(t.isha.split(":")[0].toInt(), t.isha.split(":")[1].toInt())
            val midnight = toDouble(times!!.midnightClock.hour, times.midnightClock.minute)
            res =
                PrayTimes(imsak, fajr, sunrise, dhuhr, asr, sunset, maghrib, isha, midnight)
            return res
        }

        private fun isExistExactTimes(context: Context): Boolean {
            return DPTDB.getInstance(context.applicationContext).downloadedPrayTimes().getDownloadFor(
                context.appPrefs.getString(PREF_GEOCODED_CITYNAME, DEFAULT_CITY) ?: DEFAULT_CITY
            )?.size == 366
        }

        private fun isExistAndEnabledEdit(context: Context): Boolean {
            return (PrayTimesDB.getInstance(context.applicationContext).prayTimes()
                .getAllEdited()?.size == 366
                    && context.appPrefs.getBoolean(PREF_ENABLE_EDIT, false))
        }
    }
}