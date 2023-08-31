package ir.namoo.quran.download

import android.app.DownloadManager
import java.io.File

interface QuranDownloaderInterface {
    fun downloadFile(url: String, destination: File, chapterName: String, qari: String): Long
    fun cancelDownload(downloadID: Long)
    fun getDownloadManager(): DownloadManager?
}
