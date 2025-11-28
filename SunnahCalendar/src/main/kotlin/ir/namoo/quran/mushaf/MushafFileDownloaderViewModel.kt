package ir.namoo.quran.mushaf

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.utils.logException
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import ir.namoo.commons.downloader.DownloadResult
import ir.namoo.commons.downloader.downloadFile
import ir.namoo.quran.utils.getMushafFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import java.io.File
import kotlin.math.round

class MushafFileDownloaderViewModel : ViewModel() {

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    private val _isDownloaded = MutableStateFlow(false)
    val isDownloaded = _isDownloaded.asStateFlow()

    private val _error = MutableStateFlow("")
    val error = _error.asStateFlow()

    private val _totalSize = MutableStateFlow(0L)
    val totalSize = _totalSize.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private val _downloadedBytes = MutableStateFlow(0f)
    val downloadedBytes = _downloadedBytes.asStateFlow()

    private val _isUnzipping = MutableStateFlow(false)
    val isUnzipping = _isUnzipping.asStateFlow()


    fun init(context: Context) {
        viewModelScope.launch {
            val fontsFolder = File(getMushafFolder(context) + "/QCF2BSMLfonts")
            _isDownloaded.value = fontsFolder.exists() && fontsFolder.listFiles()?.size == 607
        }
    }


    fun download(context: Context) {
        viewModelScope.launch {
            _error.value = ""
            _isDownloading.value = true
            val ktor = HttpClient(Android)
            val file = File(getMushafFolder(context), "2013.zip")
            if (file.exists()) file.delete()
            val fontsUrl =
                "https://media.githubusercontent.com/media/Developer-N/QuranQCFFonts/refs/heads/main/2013.zip"
            ktor.downloadFile(file, fontsUrl).collect { downloadState ->
                when (downloadState) {
                    is DownloadResult.Error -> {
                        _error.value = downloadState.message
                        _isDownloading.value = false
                        if (file.exists()) file.delete()
                    }

                    is DownloadResult.Progress -> _progress.value = downloadState.progress / 100f

                    DownloadResult.Success -> {
                        _isDownloading.value = false
                        _isDownloaded.value = unzip(context)
                    }

                    is DownloadResult.TotalSize -> _totalSize.value = downloadState.totalSize
                    is DownloadResult.DownloadedByte ->
                        _downloadedBytes.value =
                            round((downloadState.downloadedBytes / 1024f / 1024f) * 100) / 100f

                }
            }
        }
    }

    private suspend fun unzip(context: Context): Boolean = runCatching {
        withContext(Dispatchers.IO) {
            _isUnzipping.value = true
            val file = File(getMushafFolder(context), "2013.zip")
            ZipFile(file).extractAll(getMushafFolder(context))
            file.delete()
            _isUnzipping.value = false
        }
        true
    }.onFailure {
        _error.value = "Error while unzipping"
        _isUnzipping.value = false
        logException(it)
    }.getOrElse { false }

}
