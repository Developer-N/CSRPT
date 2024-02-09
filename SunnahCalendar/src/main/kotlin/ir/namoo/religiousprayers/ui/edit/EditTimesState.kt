package ir.namoo.religiousprayers.ui.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ir.namoo.religiousprayers.praytimeprovider.EditedPrayTimesEntity

class EditTimesState {
    var dayNumber by mutableIntStateOf(1)
    var fajr by mutableStateOf("")
    var sunrise by mutableStateOf("")
    var dhuhr by mutableStateOf("")
    var asr by mutableStateOf("")
    var maghrib by mutableStateOf("")
    var isha by mutableStateOf("")
}

fun EditedPrayTimesEntity.toEditTimeState(): EditTimesState = EditTimesState().apply {
    dayNumber = this@toEditTimeState.dayNumber
    fajr = this@toEditTimeState.fajr
    sunrise = this@toEditTimeState.sunrise
    dhuhr = this@toEditTimeState.dhuhr
    asr = this@toEditTimeState.asr
    maghrib = this@toEditTimeState.maghrib
    isha = this@toEditTimeState.isha
}

fun EditTimesState.toEditedPraTimeEntity(): EditedPrayTimesEntity =
    EditedPrayTimesEntity(dayNumber, dayNumber, fajr, sunrise, dhuhr, asr, maghrib, isha)
