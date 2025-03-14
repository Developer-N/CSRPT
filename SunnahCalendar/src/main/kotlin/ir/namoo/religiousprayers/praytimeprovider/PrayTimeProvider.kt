package ir.namoo.religiousprayers.praytimeprovider

import android.content.Context
import com.byagowi.persiancalendar.DEFAULT_CITY
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.asrMethod
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.DEFAULT_SUMMER_TIME
import ir.namoo.commons.PREF_ENABLE_EDIT
import ir.namoo.commons.PREF_SUMMER_TIME
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.utils.getDayNum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

// 0=calculated 1=exact 2=edited 3=just asr calculated
private val prayTimesFrom_ = MutableStateFlow(0)
val prayTimesFrom = prayTimesFrom_.asStateFlow()


class PrayTimeProvider(
    private val context: Context,
    private val prayTimesRepository: PrayTimeRepository,
    private val locationsDB: LocationsDB
) {

    fun replace(prayTimes: PrayTimes, jdn: Jdn): PrayTimes {
        return runCatching {
            val persianCalendar = jdn.toPersianDate()
            val dayNumber = getDayNum(persianCalendar.month, persianCalendar.dayOfMonth)
            val cityName =
                context.preferences.getString(PREF_GEOCODED_CITYNAME, DEFAULT_CITY) ?: DEFAULT_CITY
            val result = when {
                context.isExistAndEnabledEdit() -> {
                    prayTimesFrom_.value = 2
                    replaceWithEdited(prayTimes, dayNumber)
                }

                isExistExactTimes(cityName) -> {
                    prayTimesFrom_.value = 1
                    replaceWithExact(prayTimes, cityName, dayNumber)
                }

                else -> {
                    prayTimesFrom_.value = 0
                    prayTimes
                }
            }
            if (prayTimesFrom.value == 0 && context.preferences.getBoolean(
                    PREF_SUMMER_TIME, DEFAULT_SUMMER_TIME
                ) && dayNumber in 2..185
            ) result.addSummerTime()
            else if (prayTimesFrom.value == 0 ||
                context.preferences.getBoolean(PREF_SUMMER_TIME, DEFAULT_SUMMER_TIME)
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

        imsak.set(this, Clock(this.imsak).minus(Clock(1.0)).value)
        fajr.set(this, Clock(this.fajr).minus(Clock(1.0)).value)
        sunrise.set(this, Clock(this.sunrise).minus(Clock(1.0)).value)
        dhuhr.set(this, Clock(this.dhuhr).minus(Clock(1.0)).value)
        asr.set(this, Clock(this.asr).minus(Clock(1.0)).value)
        sunset.set(this, Clock(this.sunset).minus(Clock(1.0)).value)
        maghrib.set(this, Clock(this.maghrib).minus(Clock(1.0)).value)
        isha.set(this, (Clock(this.isha).minus(Clock(1.0)).value))
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

        imsak.set(this, Clock(this.imsak).plus(Clock(1.0)).value)
        fajr.set(this, Clock(this.fajr).plus(Clock(1.0)).value)
        sunrise.set(this, Clock(this.sunrise).plus(Clock(1.0)).value)
        dhuhr.set(this, Clock(this.dhuhr).plus(Clock(1.0)).value)
        asr.set(this, Clock(this.asr).plus(Clock(1.0)).value)
        sunset.set(this, Clock(this.sunset).plus(Clock(1.0)).value)
        maghrib.set(this, Clock(this.maghrib).plus(Clock(1.0)).value)
        isha.set(this, Clock(this.isha).plus(Clock(1.0)).value)
        return this
    }

    private fun replaceWithExact(
        prayTimes: PrayTimes, cityName: String, dayNumber: Int
    ): PrayTimes {

        val exactTimes = runBlocking(Dispatchers.IO) {
            val city = locationsDB.cityDAO().getCity(cityName)
            prayTimesRepository.getDownloadedTimesForCity(city!!.id, dayNumber)
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
        if (asrMethod.value == AsrMethod.Standard && exactTimes.asr != "00:00:00") asr.set(
            prayTimes, exactTimes.toDouble(exactTimes.asr)
        )
        else if (asrMethod.value == AsrMethod.Hanafi && exactTimes.asrHanafi != "00:00:00") asr.set(
            prayTimes, exactTimes.toDouble(exactTimes.asrHanafi)
        )
        else prayTimesFrom_.value = 3
        sunset.set(prayTimes, exactTimes.toDouble(exactTimes.maghrib))
        maghrib.set(prayTimes, exactTimes.toDouble(exactTimes.maghrib))
        isha.set(prayTimes, exactTimes.toDouble(exactTimes.isha))

        return prayTimes
    }

    private fun replaceWithEdited(
        prayTimes: PrayTimes, dayNumber: Int
    ): PrayTimes {
        val editedTimes =
            runBlocking(Dispatchers.IO) { prayTimesRepository.getEditedTime(dayNumber) }
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
            prayTimesRepository.getDownloadedTimesForCity(city.id)
        }
        return !dTimes.isNullOrEmpty() && dTimes.size == 366
    }

    private fun Context.isExistAndEnabledEdit(): Boolean =
        runBlocking { prayTimesRepository.getAllEditedTimes() }.size == 366 && preferences.getBoolean(
            PREF_ENABLE_EDIT, false
        )

}//end of class PrayTimeProvider

