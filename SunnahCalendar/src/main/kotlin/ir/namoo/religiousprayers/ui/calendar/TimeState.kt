package ir.namoo.religiousprayers.ui.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TimeState {
    var name by mutableStateOf("")
    var time by mutableStateOf("")
    var remaining by mutableStateOf("")
    var state by mutableStateOf(false)
}
