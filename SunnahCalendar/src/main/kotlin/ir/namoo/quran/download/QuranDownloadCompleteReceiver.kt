package ir.namoo.quran.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import ir.namoo.quran.db.FileDownloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.lingala.zip4j.ZipFile
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File

class QuranDownloadCompleteReceiver : BroadcastReceiver(), KoinComponent {
    private val quranDownloadRepository: FileDownloadRepository = get()

    override fun onReceive(context: Context?, intent: Intent?) {
        runCatching {
            if (intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (id != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {

                        val fileDownload = quranDownloadRepository.findDownloadByFileId()
                            .find { it.downloadRequest == id } ?: return@launch
                        File(fileDownload.folderPath).let {
                            if (it.exists() && it.extension.toLowerCase(Locale("en")) == "zip") {
                                ZipFile(it).extractAll(it.parent)
                                it.delete()
                            }
                        }
                        quranDownloadRepository.delete(fileDownload.id)
                    }
                }
            }
        }.onFailure { }
    }
}
