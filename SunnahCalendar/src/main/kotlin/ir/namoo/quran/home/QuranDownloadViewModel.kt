package ir.namoo.quran.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.logException
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import ir.namoo.commons.downloader.DownloadResult
import ir.namoo.commons.downloader.downloadFile
import ir.namoo.quran.qari.QariRepository
import ir.namoo.quran.utils.DB_LINK
import ir.namoo.quran.utils.getQuranDBDownloadFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import java.io.File

class QuranDownloadViewModel(private val qariRepository: QariRepository) : ViewModel() {

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    private val _isUnzipping = MutableStateFlow(false)
    val isUnzipping = _isUnzipping.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private val _message = MutableStateFlow("")
    val message = _message.asStateFlow()

    private val _totalSize = MutableStateFlow(0L)
    val totalSize = _totalSize.asStateFlow()

    @SuppressLint("SdCardPath")
    fun download(context: Context, checkFile: () -> Unit) {
        viewModelScope.launch {
            _isDownloading.value = true

            qariRepository.getQariList()//first download qari list

            File("/data/data/${context.packageName}/databases/quran_v3.zip").apply {
                if (exists()) delete()
            }
            val downloadedFile = File(getQuranDBDownloadFolder(context), "quran_v3.zip")
            val ktor = HttpClient(Android)
            ktor.downloadFile(downloadedFile, DB_LINK).collect {
                when (it) {
                    is DownloadResult.Success -> {
                        _message.value = context.getString(R.string.download_completed_unzip)
                        _isDownloading.value = false
                        _isUnzipping.value = true
                        if (unzip(context)) {
                            _message.value = context.getString(R.string.downloaded)
                            checkFile()
                        } else _message.value =
                            context.getString(R.string.download_failed_tray_again)
                        _isUnzipping.value = false
                        _progress.value = 0f
                    }

                    is DownloadResult.Error -> {
                        _message.value = context.getString(R.string.download_failed_tray_again)
                        _progress.value = 0f
                        _isDownloading.value = false
                    }

                    is DownloadResult.Progress -> {
                        _progress.value = it.progress / 100f
                        _message.update { context.getString(R.string.downloading) }
                    }

                    is DownloadResult.TotalSize -> {
                        _totalSize.value = it.totalSize
                    }
                }
            }
        }
    }

    @SuppressLint("SdCardPath")
    private suspend fun unzip(context: Context): Boolean = runCatching {
        withContext(Dispatchers.IO) {
            File(getQuranDBDownloadFolder(context), "quran_v3.zip").copyTo(
                File("/data/data/${context.packageName}/databases/quran_v3.zip")
            )
            ZipFile(
                "/data/data/${context.packageName}/databases/quran_v3.zip", ("quran").toCharArray()
            ).extractAll("/data/data/${context.packageName}/databases/")
            File("/data/data/${context.packageName}/databases/quran_v3.zip").delete()
        }
        true
    }.onFailure(logException).getOrDefault(false)

    fun clearErrorMessage() {
        _message.value = ""
    }

}
