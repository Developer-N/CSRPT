package ir.namoo.quran.download

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class QuranDownloadItemState(s: Int) {
    val sura by mutableIntStateOf(s)
    var isDownloaded by mutableStateOf(false)
    var isDownloading by mutableStateOf(false)
    var isChecking by mutableStateOf(false)
    var progress by mutableFloatStateOf(0f)
    var totalSize by mutableLongStateOf(0L)
}
