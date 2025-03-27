package ir.namoo.religiousprayers.ui.settings.athan

data class AthanState(
    val id: Int,
    val name: String,
    val fileTitle: String,
    val githubLink: String,
    val isLoading: Boolean = false,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val progress: Float = 0f,
    val totalSize: Long = 0L
)
