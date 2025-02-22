package ir.namoo.religiousprayers.ui.azkar

data class AzkarItemState(
    val downloadError: String = "",
    val progress: Float = 0f,
    val totalSize: Long = 0L,
    val isDownloading: Boolean = false,
    val isFileExist: Boolean = false,
    val readCount: Int = 0
)
