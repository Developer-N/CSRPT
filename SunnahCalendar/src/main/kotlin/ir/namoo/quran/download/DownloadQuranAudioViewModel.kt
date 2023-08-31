package ir.namoo.quran.download

import android.app.DownloadManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.namoo.quran.chapters.data.ChapterEntity
import ir.namoo.quran.chapters.data.ChapterRepository
import ir.namoo.quran.db.FileDownloadEntity
import ir.namoo.quran.db.FileDownloadRepository
import ir.namoo.quran.qari.QariEntity
import ir.namoo.quran.qari.QariRepository
import ir.namoo.quran.utils.getAyaFileName
import ir.namoo.quran.utils.getQuranDirectoryInInternal
import ir.namoo.quran.utils.getQuranDirectoryInSD
import ir.namoo.quran.utils.getSelectedQuranDirectoryPath
import ir.namoo.quran.utils.getSuraFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Timer
import kotlin.concurrent.timer

class DownloadQuranAudioViewModel(
    private val qariRepository: QariRepository,
    private val chapterRepository: ChapterRepository,
    private val quranDownloader: QuranDownloader,
    private val downloadRepository: FileDownloadRepository
) : ViewModel() {

    private val _qariList = MutableStateFlow(emptyList<QariEntity>())
    val qariList = _qariList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _selectedQari = MutableStateFlow("")
    val selectedQari = _selectedQari.asStateFlow()

    private val _chapters = MutableStateFlow(emptyList<ChapterEntity>())
    val chapters = _chapters.asStateFlow()

    private val _selectedStateList = MutableStateFlow(listOf<QuranDownloadItemState>())
    val selectedStateList = _selectedStateList.asStateFlow()

    private val _inProgressDownload = MutableStateFlow(mutableListOf<FileDownloadEntity>())

    private var timer: Timer? = null

    init {
        viewModelScope.launch {
            runCatching {
                timer = timer(period = 500) {
                    _inProgressDownload.value.forEach {
                        if (it.folderPath.contains(_selectedQari.value))
                            updateProgress(it.sura)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
        timer = null
    }

    fun loadData(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _chapters.value = chapterRepository.getAllChapters()
            _qariList.value = qariRepository.getQariList()
            _qariList.value.first().let {
                val qList = mutableListOf<QuranDownloadItemState>()
                _chapters.value.forEach { chapter ->
                    qList.add(QuranDownloadItemState(chapter.sura).apply { isChecking = true })
                }
                _selectedStateList.value = qList
            }
            _selectedQari.value = _qariList.value.first().folderName
            _inProgressDownload.value = downloadRepository.findDownloadByFileId().toMutableList()
            for (sura in 1..114)
                checkFiles(context, _selectedQari.value, sura)
            _isLoading.value = false
        }
    }

    fun updateSelectedQari(context: Context, qari: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedQari.value = qari

            val qList = mutableListOf<QuranDownloadItemState>()
            _chapters.value.forEach { chapter ->
                qList.add(QuranDownloadItemState(chapter.sura).apply { isChecking = true })
            }
            _selectedStateList.value = qList
            for (sura in 1..114)
                checkFiles(context, _selectedQari.value, sura)
            _isLoading.value = false
        }
    }

    fun downloadFiles(context: Context, sura: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val qari =
                _qariList.value.find { it.folderName == _selectedQari.value } ?: return@launch
            val destFile =
                getSelectedQuranDirectoryPath(context) + File.separator +
                        qari.folderName + File.separator + getSuraFileName(sura)

            runCatching { if (File(destFile).exists()) File(destFile).delete() }
            val url = qari.suraZipsBaseLink + getSuraFileName(sura)
            val id = quranDownloader.downloadFile(
                url = url,
                destination = File(destFile),
                chapterName = _chapters.value.find { it.sura == sura }?.nameArabic ?: "-",
                qari = _qariList.value.find { it.folderName == _selectedQari.value }?.name ?: "-"
            )
            val downloadEntity = FileDownloadEntity(
                downloadRequest = id,
                downloadFile = url,
                folderPath = destFile,
                sura = sura
            )
            downloadRepository.insert(downloadEntity)
            _inProgressDownload.value.add(downloadEntity)
            _isLoading.value = false
        }
    }

    fun cancelDownload(sura: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val downloadInfo = _inProgressDownload.value.find { it.sura == sura } ?: return@launch
            quranDownloader.cancelDownload(downloadInfo.downloadRequest)
            downloadRepository.delete(downloadInfo.id)
            _inProgressDownload.value.remove(downloadInfo)
            _selectedStateList.value.find { it.sura == sura }?.apply {
                isDownloading = false
                progress = 0f
                totalSize = 0
            }
            _isLoading.value = false
        }
    }

    fun deleteFiles(context: Context, sura: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val ayaCount = _chapters.value.find { it.sura == sura }?.ayaCount ?: return@launch
            val folderName = _selectedQari.value
            for (aya in 1..ayaCount) {
                val internalFile = File(
                    getQuranDirectoryInInternal(context) + File.separator + folderName + File.separator + getAyaFileName(
                        sura, aya
                    )
                )
                val externalFile = File(
                    getQuranDirectoryInSD(context) + File.separator + folderName + File.separator + getAyaFileName(
                        sura, aya
                    )
                )
                if (internalFile.exists())
                    internalFile.delete()
                if (externalFile.exists())
                    externalFile.delete()
            }
            _selectedStateList.value.find { it.sura == sura }?.apply {
                isDownloaded = false
            }
            _isLoading.value = false
        }
    }

    private fun updateProgress(sura: Int) {
        viewModelScope.launch {
            val downloadID =
                _inProgressDownload.value.find { it.sura == sura }?.downloadRequest ?: return@launch
            val query = DownloadManager.Query().setFilterById(downloadID)
            quranDownloader.getDownloadManager()?.query(query)?.use { cursor ->
                if (cursor.moveToNext()) {
                    val totalSizeIndex =
                        cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val bytesDownloadedIndex =
                        cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)

                    val totalBytes = cursor.getLong(totalSizeIndex)
                    val downloadedBytes = cursor.getLong(bytesDownloadedIndex)
                    _selectedStateList.value.find { it.sura == sura }?.let {
                        if (downloadedBytes == totalBytes && totalBytes > 0) {
                            it.isDownloaded = true
                            it.isDownloading = false
                            it.totalSize = 0
                            it.progress = 0f
                        } else {
                            it.isDownloading = true
                            it.totalSize = totalBytes
                            it.progress = downloadedBytes.toFloat() / totalBytes
                        }
                    }
                }
            }
        }
    }

    private fun checkFiles(context: Context, folderName: String, sura: Int) {
        viewModelScope.launch {
            runCatching {
                val chapter = _chapters.value.find { it.sura == sura } ?: return@launch
                withContext(Dispatchers.IO) {
                    for (aya in 1..chapter.ayaCount) {
                        val internalFile = File(
                            getQuranDirectoryInInternal(context) + File.separator + folderName + File.separator + getAyaFileName(
                                sura, aya
                            )
                        )
                        val externalFile = File(
                            getQuranDirectoryInSD(context) + File.separator + folderName + File.separator + getAyaFileName(
                                sura, aya
                            )
                        )
                        if (!internalFile.exists() && !externalFile.exists()) {
                            withContext(Dispatchers.Default) {
                                _selectedStateList.value.find { it.sura == sura }?.apply {
                                    isChecking = false
                                    isDownloaded = false
                                }
                            }
                            return@withContext
                        }
                    }
                    withContext(Dispatchers.Default) {
                        _selectedStateList.value.find { it.sura == sura }?.apply {
                            isChecking = false
                            isDownloaded = true
                        }
                    }
                }
            }
        }
    }//end of checkFiles
}
