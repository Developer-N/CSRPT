package ir.namoo.commons.downloader

sealed class DownloadResult {
    data object Success : DownloadResult()
    data class Error(val message: String, val cause: Exception? = null) : DownloadResult()
    data class Progress(val progress: Long) : DownloadResult()
    data class TotalSize(val totalSize: Long) : DownloadResult()
    data class DownloadedByte(val downloadedBytes: Long) : DownloadResult()
}
