package ir.namoo.quran.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.namoo.quran.db.ChapterEntity
import ir.namoo.quran.db.FileDownloadEntity
import ir.namoo.quran.db.FileDownloadRepository
import ir.namoo.quran.db.QuranDB
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val fileDownloadRepository: FileDownloadRepository,
    private val db: QuranDB
) : ViewModel() {

    val chapterList: MutableLiveData<MutableList<ChapterEntity>> by lazy {
        MutableLiveData<MutableList<ChapterEntity>>().also {
            it.value = db.chaptersDao().getAllChapters()
        }
    }

    private val _reqIDs = MutableLiveData<MutableList<FileDownloadEntity>>()
    val reqIDs: LiveData<MutableList<FileDownloadEntity>> get() = _reqIDs

    fun checkDownloads() {
        viewModelScope.launch {
            _reqIDs.value =
                fileDownloadRepository.findDownloadByFileId() as MutableList<FileDownloadEntity>?
        }
    }

    fun addDownload(fileDownload: FileDownloadEntity) {
        viewModelScope.launch {
            fileDownloadRepository.insert(fileDownload)
        }
    }

    fun removeDownload(id: Int) {
        viewModelScope.launch {
            fileDownloadRepository.delete(id)
        }
    }

}
