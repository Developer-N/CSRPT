package ir.namoo.religiousprayers.ui.calendar

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.entities.PrayTime.Companion.get
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.utils.getNextPrayTime
import com.byagowi.persiancalendar.utils.scheduleAlarms
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.repository.DataState
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.religiousprayers.praytimeprovider.prayTimesFrom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class NTimesViewModel(
    private val settings: AthanSettingsDB,
    private val prayTimeRepository: PrayTimeRepository,
) : ViewModel() {

    private val _isTimesAvailableForDownload = MutableStateFlow(false)
    val isTimesAvailableForDownload = _isTimesAvailableForDownload

    private val _times = mutableStateListOf<TimesState>()
    val times = _times
    private val timeNames = listOf(
        PrayTime.FAJR,
        PrayTime.SUNRISE,
        PrayTime.DHUHR,
        PrayTime.ASR,
        PrayTime.MAGHRIB,
        PrayTime.ISHA
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun load(context: Context, prayTimes: PrayTimes, isToday: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            val clock = Clock(Date().toGregorianCalendar(forceLocalTime = true))
            val next = prayTimes.getNextPrayTime(clock)
            _times.clear()
            timeNames.forEach { time ->
                val position = when (time) {
                    PrayTime.FAJR -> 0
                    PrayTime.SUNRISE -> 1
                    PrayTime.DHUHR -> 2
                    PrayTime.ASR -> 3
                    PrayTime.MAGHRIB -> 4
                    PrayTime.ISHA -> 5
                    else -> 0
                }
                var rem = ""
                val tt = prayTimes[time]
                if (isToday && tt.minus(clock).value > 0) {
                    val difference = tt.minus(clock)
                    rem =
                        if (
                            (time == next || (next == PrayTime.SUNSET && time == PrayTime.MAGHRIB)) &&
                            difference.toHoursAndMinutesPair().second > 0
                        )
                            difference.asRemainingTime(context.resources, short = true)
                        else ""
                }
                val isNext = rem.isNotEmpty()
                val athanState = settings.athanSettingsDAO().getAllAthanSettings()[position].state
                val timeState = TimesState(
                    position = position,
                    icon = when (time) {
                        PrayTime.FAJR -> R.drawable.ic_fajr_isha
                        PrayTime.SUNRISE -> R.drawable.ic_sunrise
                        PrayTime.DHUHR -> R.drawable.ic_dhuhr_asr
                        PrayTime.ASR -> R.drawable.ic_dhuhr_asr
                        PrayTime.MAGHRIB -> R.drawable.ic_maghrib
                        PrayTime.ISHA -> R.drawable.ic_fajr_isha
                        else -> R.drawable.ic_fajr_isha
                    },
                    time = time,
                    remainingTime = rem,
                    isActive = athanState,
                    isNext = isNext
                )
                _times.add(timeState)
            }
            _isLoading.value = false
            withContext(Dispatchers.IO) {
                isTimeAvailableForDownload(context)
            }
        }

    }

    fun changeTimeState(context: Context, time: TimesState) {
        viewModelScope.launch {
            val index = _times.indexOf(time)
            _times[index] = _times[index].copy(isActive = !time.isActive)
            val setting = settings.athanSettingsDAO().getAllAthanSettings()[time.position]
            setting.state = !setting.state
            settings.athanSettingsDAO().update(setting)
            scheduleAlarms(context)
        }
    }


    private suspend fun isTimeAvailableForDownload(context: Context) {
        if (!isNetworkConnected(context) || prayTimesFrom.value != 0) {
            _isTimesAvailableForDownload.value = false
            return
        }
        prayTimeRepository.getAddedCity().collectLatest { state ->
            when (state) {
                is DataState.Error ->
                    _isTimesAvailableForDownload.value = false

                DataState.Loading -> {}
                is DataState.Success -> {
                    val list = state.data
                    val currentCity = cityName.value
                    if (currentCity.isNullOrEmpty()) {
                        _isTimesAvailableForDownload.value = false
                        return@collectLatest
                    }
                    _isTimesAvailableForDownload.value =
                        list.find { cityModel -> cityModel.name == currentCity } != null
                }
            }
        }
    }
}
