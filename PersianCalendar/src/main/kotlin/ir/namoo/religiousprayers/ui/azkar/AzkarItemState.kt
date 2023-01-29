package ir.namoo.religiousprayers.ui.azkar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AzkarItemState {
    var downloadError by mutableStateOf("")
    var progress by mutableStateOf(0f)
    var isDownloading by mutableStateOf(false)
    var isPlaying by mutableStateOf(false)
}
