package ir.namoo.religiousprayers.ui.edit

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.toCivilDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.DEFAULT_SUMMER_TIME
import ir.namoo.commons.PREF_ENABLE_EDIT
import ir.namoo.commons.PREF_SUMMER_TIME
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.utils.fixSummerTimes
import ir.namoo.commons.utils.fixTime
import ir.namoo.commons.utils.getDayMonthForDayOfYear
import ir.namoo.commons.utils.getDayNum
import ir.namoo.religiousprayers.praytimeprovider.EditedPrayTimesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditPrayTimeViewModel(private val prayTimeRepository: PrayTimeRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled = _isEnabled.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private val _originalTimes = MutableStateFlow(listOf<EditedPrayTimesEntity>())
    val originalTimes = _originalTimes.asStateFlow()

    private val _editedTimes = MutableStateFlow(listOf<EditTimesState>())
    val editedTimes = _editedTimes.asStateFlow()

    private val _month = MutableStateFlow(1)
    val month = _month.asStateFlow()

    private val _day = MutableStateFlow(1)
    val day = _day.asStateFlow()

    private val _dayNumber = MutableStateFlow(1)
    val dayNumber = _dayNumber.asStateFlow()

    fun loadData(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _isEnabled.value = context.preferences.getBoolean(PREF_ENABLE_EDIT, false)
            _originalTimes.value = prayTimeRepository.getAllEditedTimes()
            val temp = mutableListOf<EditTimesState>()
            originalTimes.value.forEach { edited ->
                temp.add(edited.toEditTimeState())
            }
            _editedTimes.value = temp
            _isLoading.value = false
        }
    }

    fun updateMonth(month: Int) {
        viewModelScope.launch {
            _month.value = month
            updateDayNumber()
        }
    }

    fun updateDay(day: Int) {
        viewModelScope.launch {
            _day.value = day
            updateDayNumber()
        }
    }

    private fun updateDayNumber() {
        viewModelScope.launch {
            _dayNumber.value = getDayNum(month.value, day.value)
        }
    }

    fun updateFajr(fajr: String) {
        viewModelScope.launch {
            _editedTimes.value.find { it.dayNumber == dayNumber.value }?.fajr = fajr
        }
    }

    fun updateSunrise(sunrise: String) {
        viewModelScope.launch {
            _editedTimes.value.find { it.dayNumber == dayNumber.value }?.sunrise =
                sunrise
        }
    }

    fun updateDhuhr(dhuhr: String) {
        viewModelScope.launch {
            _editedTimes.value.find { it.dayNumber == dayNumber.value }?.dhuhr =
                dhuhr
        }
    }

    fun updateAsr(asr: String) {
        viewModelScope.launch {
            _editedTimes.value.find { it.dayNumber == dayNumber.value }?.asr = asr
        }
    }

    fun updateMaghrib(maghrib: String) {
        viewModelScope.launch {
            _editedTimes.value.find { it.dayNumber == dayNumber.value }?.maghrib =
                maghrib
        }
    }

    fun updateIsha(isha: String) {
        viewModelScope.launch {
            _editedTimes.value.find { it.dayNumber == dayNumber.value }?.isha = isha
        }
    }

    fun updateIsEnabled(context: Context) {
        viewModelScope.launch {
            _isEnabled.value = !isEnabled.value
            context.preferences.edit { putBoolean(PREF_ENABLE_EDIT, isEnabled.value) }
            if (isEnabled.value && originalTimes.value.isEmpty()) {
                val newTimes = calculateAllTimes(context)
                if (newTimes.isEmpty()) return@launch
                val newEditTimes = mutableListOf<EditedPrayTimesEntity>()
                var id = 1
                for (t in newTimes) {
                    if (t != null) {
                        val temp = EditedPrayTimesEntity(
                            id,
                            id,
                            Clock(t.fajr).toFormattedString(),
                            Clock(t.sunrise).toFormattedString(),
                            Clock(t.dhuhr).toFormattedString(),
                            Clock(t.asr).toFormattedString(),
                            Clock(t.maghrib).toFormattedString(),
                            Clock(t.isha).toFormattedString()
                        )
                        id++
                        newEditTimes.add(temp)
                    }
                }
                prayTimeRepository.insertEdit(newEditTimes)
                _originalTimes.value = newEditTimes
                val temp = mutableListOf<EditTimesState>()
                originalTimes.value.forEach { edited ->
                    temp.add(edited.toEditTimeState())
                }
                _editedTimes.value = temp
            }
        }
    }

    private suspend fun calculateAllTimes(context: Context): List<PrayTimes?> {
        val res = arrayListOf<PrayTimes?>()
        val civilDate = Jdn.today().toGregorianCalendar()
        for (i in 1..366) {
            val str = getDayMonthForDayOfYear(i)
            val month = str.split("/")[0].toInt()
            val day = str.split("/")[1].toInt()
            val persianDate = PersianDate(PersianDate(civilDate.toCivilDate()).year, month, day)
            val date = CivilDate(persianDate.toJdn())
            var time = coordinates.value?.calculatePrayTimes(Jdn(date).toGregorianCalendar())
            if (!context.preferences.getBoolean(
                    PREF_SUMMER_TIME, DEFAULT_SUMMER_TIME
                ) && i in 2..185
            ) time = fixSummerTimes(time, true)
            res.add(time)
            withContext(Dispatchers.Default) {
                _progress.value = ((i * 100) / 366) / 100f
            }
        }
        return res
    }

    fun saveToDB() {
        viewModelScope.launch {
            _isLoading.value = true
            val times = mutableListOf<EditedPrayTimesEntity>()
            for (t in editedTimes.value)
                times.add(t.toEditedPraTimeEntity())
            prayTimeRepository.updateEditedTimes(times)
            _originalTimes.value = prayTimeRepository.getAllEditedTimes()
            _isLoading.value = false
        }
    }

    fun clearDB(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            prayTimeRepository.clearEditTimes()
            context.preferences.edit {
                putBoolean(PREF_ENABLE_EDIT, false)
            }
            _isEnabled.value = false
            _editedTimes.value = emptyList()
            _originalTimes.value = emptyList()
            updateDay(1)
            updateMonth(1)
            _isLoading.value = false
        }
    }

    fun groupEdit(
        athan: Int,
        fromMonth: Int,
        toMonth: Int,
        fromDay: Int,
        toDay: Int,
        minute: Int,
        isForward: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            var min = minute
            if (!isForward) min *= -1
            val fd = getDayNum(fromMonth, fromDay)
            val td = getDayNum(toMonth, toDay)
            for (day in fd..td) {
                val time = editedTimes.value.find { it.dayNumber == day } ?: return@launch
                when (athan) {
                    0 -> {
                        editedTimes.value[editedTimes.value.indexOf(time)].fajr =
                            fixTime(time.fajr, min)
                    }

                    1 -> {
                        editedTimes.value[editedTimes.value.indexOf(time)].sunrise =
                            fixTime(time.sunrise, min)
                    }

                    2 -> {
                        editedTimes.value[editedTimes.value.indexOf(time)].dhuhr =
                            fixTime(time.dhuhr, min)
                    }

                    3 -> {
                        editedTimes.value[editedTimes.value.indexOf(time)].asr =
                            fixTime(time.asr, min)
                    }

                    4 -> {
                        editedTimes.value[editedTimes.value.indexOf(time)].maghrib =
                            fixTime(time.maghrib, min)
                    }

                    5 -> {
                        editedTimes.value[editedTimes.value.indexOf(time)].isha =
                            fixTime(time.isha, min)
                    }
                }
            }
            _isLoading.value = false
        }
    }
}
