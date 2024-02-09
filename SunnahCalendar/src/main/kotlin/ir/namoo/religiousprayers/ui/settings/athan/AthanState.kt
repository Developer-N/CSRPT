package ir.namoo.religiousprayers.ui.settings.athan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AthanState {
    var isLoading by mutableStateOf(false)
    var isDownloaded by mutableStateOf(false)
    var isDownloading by mutableStateOf(false)
    var progress by mutableFloatStateOf(0f)
    var totalSize by mutableLongStateOf(0L)
}
