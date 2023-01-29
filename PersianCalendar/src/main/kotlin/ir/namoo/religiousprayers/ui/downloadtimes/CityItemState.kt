package ir.namoo.religiousprayers.ui.downloadtimes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CityItemState {
    var isDownloading by mutableStateOf(false)
    var isDownloaded by mutableStateOf(false)
    var isSelected by mutableStateOf(false)
}
