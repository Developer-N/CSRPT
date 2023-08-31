package ir.namoo.quran.download

import android.app.DownloadManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import java.io.File

class QuranDownloader(context: Context) : QuranDownloaderInterface {
    private val downloadManager = context.getSystemService<DownloadManager>()

    override fun downloadFile(
        url: String, destination: File, chapterName: String, qari: String
    ): Long {
        downloadManager ?: return -1L
        val request = DownloadManager.Request(url.toUri())
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setTitle("$qari - $chapterName")
            .setDestinationUri(destination.toUri())
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            request.setRequiresCharging(false)

        return downloadManager.enqueue(request)
    }

    override fun cancelDownload(downloadID: Long) {
        downloadManager?.remove(downloadID)
    }

    override fun getDownloadManager(): DownloadManager? {
        return downloadManager
    }
}
