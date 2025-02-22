package ir.namoo.quran.download

data class QuranDownloadItemState(
    val sura: Int,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val isChecking: Boolean = false,
    val progress: Float = 0f,
    val totalSize: Long = 0L
)
