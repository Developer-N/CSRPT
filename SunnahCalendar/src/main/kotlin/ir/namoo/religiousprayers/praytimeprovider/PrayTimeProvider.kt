package ir.namoo.religiousprayers.praytimeprovider

import android.content.Context
import com.byagowi.persiancalendar.DEFAULT_CITY
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.asrMethod
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.logException
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.DEFAULT_SUMMER_TIME
import ir.namoo.commons.PREF_ENABLE_EDIT
import ir.namoo.commons.PREF_SUMMER_TIME
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.utils.getDayNum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class PrayTimeProvider constructor(private val context: Context) {
    companion object {
        var ptFrom = 0 // 0=calculated 1=exact 2=edited 3=just asr calculated
    }

    private var prayTimesDAO: DownloadedPrayTimesDAO =
        DownloadedPrayTimesDB.getInstance(context.applicationContext).downloadedPrayTimes()
    private var editedPrayTimesDAO: PrayTimesDAO =
        PrayTimesDB.getInstance(context.applicationContext).prayTimes()
    private var locationsDB = LocationsDB.getInstance(context.applicationContext)

    fun nReplace(prayTimes: PrayTimes?, jdn: Jdn): PrayTimes? {
        prayTimes ?: return null
        return runCatching {
            val persianCalendar = jdn.toPersianCalendar()
            val dayNumber = getDayNum(persianCalendar.month, persianCalendar.dayOfMonth)
            val cityName =
                context.appPrefs.getString(PREF_GEOCODED_CITYNAME, DEFAULT_CITY) ?: DEFAULT_CITY
            val result = when {
                context.isExistAndEnabledEdit() -> {
                    ptFrom = 2
                    replaceWithEdited(prayTimes, dayNumber)
                }
                isExistExactTimes(cityName) -> {
                    ptFrom = 1
                    replaceWithExact(prayTimes, cityName, dayNumber)
                }
                else -> {
                    ptFrom = 0
                    prayTimes
                }
            }
            if (ptFrom == 0 && context.appPrefs.getBoolean(
                    PREF_SUMMER_TIME, DEFAULT_SUMMER_TIME
                ) && dayNumber in 2..185
            ) result.addSummerTime()
            else if (ptFrom == 0 ||
                context.appPrefs.getBoolean(PREF_SUMMER_TIME, DEFAULT_SUMMER_TIME)
                || dayNumber !in 2..185
            ) result
            else result.deleteSummerTime()
        }.onFailure(logException).getOrElse { prayTimes }
    }

    private fun PrayTimes.deleteSummerTime(): PrayTimes {
        val imsak = this.javaClass.getDeclaredField("imsak").apply { isAccessible = true }
        val fajr = this.javaClass.getDeclaredField("fajr").apply { isAccessible = true }
        val sunrise = this.javaClass.getDeclaredField("sunrise").apply { isAccessible = true }
        val dhuhr = this.javaClass.getDeclaredField("dhuhr").apply { isAccessible = true }
        val asr = this.javaClass.getDeclaredField("asr").apply { isAccessible = true }
        val sunset = this.javaClass.getDeclaredField("sunset").apply { isAccessible = true }
        val maghrib = this.javaClass.getDeclaredField("maghrib").apply { isAccessible = true }
        val isha = this.javaClass.getDeclaredField("isha").apply { isAccessible = true }

        imsak.set(
            this, (Clock.fromHoursFraction(this.imsak).apply {
                hours -= 1
            }).toHoursFraction()
        )
        fajr.set(this, (Clock.fromHoursFraction(this.fajr).apply {
            hours -= 1
        }).toHoursFraction())
        sunrise.set(this, (Clock.fromHoursFraction(this.sunrise).apply {
            hours -= 1
        }).toHoursFraction())
        dhuhr.set(this, (Clock.fromHoursFraction(this.dhuhr).apply {
            hours -= 1
        }).toHoursFraction())
        asr.set(this, (Clock.fromHoursFraction(this.asr).apply {
            hours -= 1
        }).toHoursFraction())
        sunset.set(this, (Clock.fromHoursFraction(this.sunset).apply {
            hours -= 1
        }).toHoursFraction())
        maghrib.set(this, (Clock.fromHoursFraction(this.maghrib).apply {
            hours -= 1
        }).toHoursFraction())
        isha.set(this, (Clock.fromHoursFraction(this.isha).apply {
            hours -= 1
        }).toHoursFraction())
        return this
    }

    private fun PrayTimes.addSummerTime(): PrayTimes {
        val imsak = this.javaClass.getDeclaredField("imsak").apply { isAccessible = true }
        val fajr = this.javaClass.getDeclaredField("fajr").apply { isAccessible = true }
        val sunrise = this.javaClass.getDeclaredField("sunrise").apply { isAccessible = true }
        val dhuhr = this.javaClass.getDeclaredField("dhuhr").apply { isAccessible = true }
        val asr = this.javaClass.getDeclaredField("asr").apply { isAccessible = true }
        val sunset = this.javaClass.getDeclaredField("sunset").apply { isAccessible = true }
        val maghrib = this.javaClass.getDeclaredField("maghrib").apply { isAccessible = true }
        val isha = this.javaClass.getDeclaredField("isha").apply { isAccessible = true }

        imsak.set(
            this, (Clock.fromHoursFraction(this.imsak).apply {
                hours += 1
            }).toHoursFraction()
        )
        fajr.set(this, (Clock.fromHoursFraction(this.fajr).apply {
            hours += 1
        }).toHoursFraction())
        sunrise.set(this, (Clock.fromHoursFraction(this.sunrise).apply {
            hours += 1
        }).toHoursFraction())
        dhuhr.set(this, (Clock.fromHoursFraction(this.dhuhr).apply {
            hours += 1
        }).toHoursFraction())
        asr.set(this, (Clock.fromHoursFraction(this.asr).apply {
            hours += 1
        }).toHoursFraction())
        sunset.set(this, (Clock.fromHoursFraction(this.sunset).apply {
            hours += 1
        }).toHoursFraction())
        maghrib.set(this, (Clock.fromHoursFraction(this.maghrib).apply {
            hours += 1
        }).toHoursFraction())
        isha.set(this, (Clock.fromHoursFraction(this.isha).apply {
            hours += 1
        }).toHoursFraction())
        return this
    }

    // TODO check this for correctly run
    private fun replaceWithExact(
        prayTimes: PrayTimes, cityName: String, dayNumber: Int
    ): PrayTimes {

        val exactTimes = runBlocking(Dispatchers.IO) {
            val city = locationsDB.cityDAO().getCity(cityName)
            prayTimesDAO.getDownloadFor(city!!.id, dayNumber)
        }
        exactTimes ?: return prayTimes

        val imsak = prayTimes.javaClass.getDeclaredField("imsak").apply { isAccessible = true }
        val fajr = prayTimes.javaClass.getDeclaredField("fajr").apply { isAccessible = true }
        val sunrise = prayTimes.javaClass.getDeclaredField("sunrise").apply { isAccessible = true }
        val dhuhr = prayTimes.javaClass.getDeclaredField("dhuhr").apply { isAccessible = true }
        val asr = prayTimes.javaClass.getDeclaredField("asr").apply { isAccessible = true }
        val sunset = prayTimes.javaClass.getDeclaredField("sunset").apply { isAccessible = true }
        val maghrib = prayTimes.javaClass.getDeclaredField("maghrib").apply { isAccessible = true }
        val isha = prayTimes.javaClass.getDeclaredField("isha").apply { isAccessible = true }

        imsak.set(prayTimes, exactTimes.toDoubleAndFix(exactTimes.fajr, -10))
        fajr.set(prayTimes, exactTimes.toDouble(exactTimes.fajr))
        sunrise.set(prayTimes, exactTimes.toDouble(exactTimes.sunrise))
        dhuhr.set(prayTimes, exactTimes.toDouble(exactTimes.dhuhr))
        if (asrMethod == AsrMethod.Standard && exactTimes.asr != "00:00:00") asr.set(
            prayTimes, exactTimes.toDouble(exactTimes.asr)
        )
        else if (asrMethod == AsrMethod.Hanafi && exactTimes.asrHanafi != "00:00:00") asr.set(
            prayTimes, exactTimes.toDouble(exactTimes.asrHanafi)
        )
        else ptFrom = 3
        sunset.set(prayTimes, exactTimes.toDouble(exactTimes.maghrib))
        maghrib.set(prayTimes, exactTimes.toDouble(exactTimes.maghrib))
        isha.set(prayTimes, exactTimes.toDouble(exactTimes.isha))

        return prayTimes
    }

    // TODO check this for correctly run
    private fun replaceWithEdited(
        prayTimes: PrayTimes, dayNumber: Int
    ): PrayTimes {
        val editedTimes = runBlocking(Dispatchers.IO) { editedPrayTimesDAO.getEdited(dayNumber) }
        editedTimes ?: return prayTimes

        val imsak = prayTimes.javaClass.getDeclaredField("imsak").apply { isAccessible = true }
        val fajr = prayTimes.javaClass.getDeclaredField("fajr").apply { isAccessible = true }
        val sunrise = prayTimes.javaClass.getDeclaredField("sunrise").apply { isAccessible = true }
        val dhuhr = prayTimes.javaClass.getDeclaredField("dhuhr").apply { isAccessible = true }
        val asr = prayTimes.javaClass.getDeclaredField("asr").apply { isAccessible = true }
        val sunset = prayTimes.javaClass.getDeclaredField("sunset").apply { isAccessible = true }
        val maghrib = prayTimes.javaClass.getDeclaredField("maghrib").apply { isAccessible = true }
        val isha = prayTimes.javaClass.getDeclaredField("isha").apply { isAccessible = true }

        imsak.set(prayTimes, editedTimes.toDoubleAndFix(editedTimes.fajr, -10))
        fajr.set(prayTimes, editedTimes.toDouble(editedTimes.fajr))
        sunrise.set(prayTimes, editedTimes.toDouble(editedTimes.sunrise))
        dhuhr.set(prayTimes, editedTimes.toDouble(editedTimes.dhuhr))
        asr.set(prayTimes, editedTimes.toDouble(editedTimes.asr))
        sunset.set(prayTimes, editedTimes.toDouble(editedTimes.maghrib))
        maghrib.set(prayTimes, editedTimes.toDouble(editedTimes.maghrib))
        isha.set(prayTimes, editedTimes.toDouble(editedTimes.isha))

        return prayTimes
    }


    private fun isExistExactTimes(cityName: String): Boolean {
        val dTimes = runBlocking {
            val city = locationsDB.cityDAO().getCity(cityName) ?: return@runBlocking null
            prayTimesDAO.getDownloadFor(city.id)
        }
        return !dTimes.isNullOrEmpty() && dTimes.size == 366
    }

    private fun Context.isExistAndEnabledEdit(): Boolean =
        runBlocking { editedPrayTimesDAO.getAllEdited() }?.size == 366 && appPrefs.getBoolean(
            PREF_ENABLE_EDIT, false
        )

}//end of class PrayTimeProvider

