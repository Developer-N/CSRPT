package ir.namoo.quran.download

import android.app.DownloadManager
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import java.io.File

class DownloadQuranAudioViewModel(
    private val qariRepository: QariRepository,
    private val chapterRepository: ChapterRepository,
    private val quranDownloader: QuranDownloader,
    private val downloadRepository: FileDownloadRepository
) : ViewModel() {

    private val _qariList = mutableStateListOf<QariEntity>()
    val qariList = _qariList

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _selectedQari = MutableStateFlow("")
    val selectedQari = _selectedQari.asStateFlow()

    private val _chapters = mutableStateListOf<ChapterEntity>()
    val chapters = _chapters

    private val _selectedStateList = mutableStateListOf<QuranDownloadItemState>()
    val selectedStateList = _selectedStateList

    private val _inProgressDownload = mutableStateListOf<FileDownloadEntity>()


    init {
        viewModelScope.launch {
            runCatching {
                while (true) {
                    delay(1000)
                    _inProgressDownload.forEach { inProgress ->
                        val selectedQariId =
                            _qariList.find { it.folderName == _selectedQari.value }?.id ?: -1
                        if (inProgress.qariId == selectedQariId) updateProgress(inProgress.sura)
                    }
                }
            }
        }
    }

    fun loadData(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val dbChapters = chapterRepository.getAllChapters()
            val dbQaries = qariRepository.getQariList()
            _chapters.clear()
            _chapters.addAll(dbChapters)
            _qariList.clear()
            _qariList.addAll(dbQaries)
            val firstQari = dbQaries.first().folderName
            _selectedQari.value = firstQari
            _selectedStateList.clear()
            dbChapters.forEach { chapter ->
                _selectedStateList.add(
                    QuranDownloadItemState(
                        sura = chapter.sura, isChecking = true
                    )
                )
            }
            _inProgressDownload.clear()
            _inProgressDownload.addAll(downloadRepository.getAllDownloads())
            for (sura in 1..114) checkFiles(context, firstQari, sura)
            _isLoading.value = false
        }
    }

    fun updateSelectedQari(context: Context, qari: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedQari.value = qari
            _selectedStateList.clear()
            _chapters.forEach { chapter ->
                _selectedStateList.add(
                    QuranDownloadItemState(sura = chapter.sura, isChecking = true)
                )
            }
            for (sura in 1..114) checkFiles(context, qari, sura)
            _isLoading.value = false
        }
    }

    fun downloadFiles(context: Context, sura: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val qari = _qariList.find { it.folderName == _selectedQari.value } ?: return@launch
            downloadRepository.getAllDownloads()
                .filter { it.sura == sura && it.qariId == qari.id }.forEach {
                    downloadRepository.delete(it.id)
                    quranDownloader.cancelDownload(it.downloadRequest)
                }

            val destFile =
                getSelectedQuranDirectoryPath(context) + File.separator + qari.folderName + File.separator + getSuraFileName(
                    sura
                )

            runCatching { if (File(destFile).exists()) File(destFile).delete() }
            val url = qari.suraZipsBaseLink + getSuraFileName(sura)
            val id = quranDownloader.downloadFile(
                url = url,
                destination = File(destFile),
                chapterName = _chapters.find { it.sura == sura }?.nameArabic ?: "-",
                qari = _qariList.find { it.folderName == _selectedQari.value }?.name ?: "-"
            )
            val selectedQariId = _qariList.find { it.folderName == _selectedQari.value }?.id ?: -1
            val downloadEntity = FileDownloadEntity(
                downloadRequest = id,
                downloadFile = url,
                folderPath = destFile,
                sura = sura,
                qariId = selectedQariId
            )
            downloadRepository.insert(downloadEntity)
            downloadRepository.getAllDownloads().lastOrNull()?.let {
                _inProgressDownload.add(it)
            }
            _isLoading.value = false
        }
    }

    fun cancelDownload(sura: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val selectedQariId = _qariList.find { it.folderName == _selectedQari.value }?.id ?: -1
            val downloadInfo = downloadRepository.getAllDownloads()
                .find { it.sura == sura && it.qariId == selectedQariId } ?: return@launch
            quranDownloader.cancelDownload(downloadInfo.downloadRequest)
            withContext(Dispatchers.IO) {
                downloadRepository.delete(downloadInfo.id)
            }
            _inProgressDownload.removeAt(_inProgressDownload.indexOfFirst { it.downloadRequest == downloadInfo.downloadRequest })
            val index = _selectedStateList.indexOfFirst { it.sura == sura }
            _selectedStateList[index] = _selectedStateList[index].copy(
                isDownloading = false, progress = 0f, totalSize = 0
            )
            _isLoading.value = false
        }
    }

    fun deleteFiles(context: Context, sura: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val ayaCount = _chapters.find { it.sura == sura }?.ayaCount ?: return@launch
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
                if (internalFile.exists()) internalFile.delete()
                if (externalFile.exists()) externalFile.delete()
            }
            val index = _selectedStateList.indexOfFirst { it.sura == sura }
            _selectedStateList[index] = _selectedStateList[index].copy(isDownloaded = false)
            _isLoading.value = false
        }
    }

    private fun updateProgress(sura: Int) {
        val selectedQariId = _qariList.find { it.folderName == _selectedQari.value }?.id ?: -1
        val downloadInfo =
            _inProgressDownload.find { it.sura == sura && it.qariId == selectedQariId } ?: return
        val query = DownloadManager.Query().setFilterById(downloadInfo.downloadRequest)
        val cursor = quranDownloader.getDownloadManager()?.query(query)
        if (cursor == null) {
            val index = _selectedStateList.indexOfFirst { it.sura == sura }
            _selectedStateList[index] = _selectedStateList[index].copy(
                isDownloaded = true,
                isDownloading = false,
                isChecking = false,
                totalSize = 0,
                progress = 0f
            )
            _inProgressDownload.remove(downloadInfo)
        } else cursor.use { cursor ->
            if (cursor.moveToNext()) {
                val totalSizeIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val bytesDownloadedIndex =
                    cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val totalBytes = cursor.getLong(totalSizeIndex)
                val downloadedBytes = cursor.getLong(bytesDownloadedIndex)
                val isFinished = downloadedBytes == totalBytes && totalBytes > 0
                if (isFinished) _inProgressDownload.remove(downloadInfo)
                val index = _selectedStateList.indexOfFirst { it.sura == sura }
                _selectedStateList[index] = if (isFinished) _selectedStateList[index].copy(
                    isDownloaded = true,
                    isDownloading = false,
                    isChecking = false,
                    totalSize = 0,
                    progress = 0f
                )
                else _selectedStateList[index].copy(
                    isDownloading = true,
                    isChecking = false,
                    totalSize = totalBytes,
                    progress = downloadedBytes.toFloat() / totalBytes
                )
            }
        }
    }

    private fun checkFiles(context: Context, folderName: String, sura: Int) {
        viewModelScope.launch {
            runCatching {
                val chapter = _chapters.find { it.sura == sura } ?: return@launch
                withContext(Dispatchers.IO) {
                    val internalZip =
                        getQuranDirectoryInInternal(context) + File.separator + folderName + File.separator + getSuraFileName(
                            sura
                        )
                    File(internalZip).let {
                        if (it.exists()) {
                            ZipFile(it).extractAll(it.parent)
                            it.delete()
                        }
                    }
                    val externalZip =
                        getQuranDirectoryInSD(context) + File.separator + folderName + File.separator + getSuraFileName(
                            sura
                        )
                    File(externalZip).let {
                        if (it.exists()) {
                            ZipFile(it).extractAll(it.parent)
                            it.delete()
                        }
                    }
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
                                val index = _selectedStateList.indexOfFirst { it.sura == sura }
                                _selectedStateList[index] = _selectedStateList[index].copy(
                                    isChecking = false, isDownloaded = false
                                )
                            }
                            return@withContext
                        }
                    }
                    withContext(Dispatchers.Default) {
                        val index = _selectedStateList.indexOfFirst { it.sura == sura }
                        _selectedStateList[index] =
                            _selectedStateList[index].copy(isChecking = false, isDownloaded = true)
                    }
                }
            }
        }
    }//end of checkFiles
}
