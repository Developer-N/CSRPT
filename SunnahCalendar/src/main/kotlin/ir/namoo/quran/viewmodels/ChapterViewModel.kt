package ir.namoo.quran.viewmodels

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.namoo.quran.db.ChapterEntity
import ir.namoo.quran.db.HizbEntity
import ir.namoo.quran.db.JuzEntity
import ir.namoo.quran.db.PageEntity
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.utils.PREF_QURAN_DB_DOWNLOAD_REQ_ID
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChapterViewModel @Inject constructor(
    private val db: QuranDB,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val chapterList: MutableLiveData<MutableList<ChapterEntity>> by lazy {
        MutableLiveData<MutableList<ChapterEntity>>().also {
            it.value = db.chaptersDao().getAllChapters()
        }
    }

    fun getChapters(): MutableLiveData<MutableList<ChapterEntity>> {
        return chapterList
    }

    fun getPage(page: Int): LiveData<List<PageEntity>> {
        return db.pjhDao().getAllPage(page)
    }

    fun getAllHezb(): LiveData<List<HizbEntity>> {
        return db.pjhDao().getAllHezb()
    }

    fun getAllJuz(): LiveData<List<JuzEntity>> {
        return db.pjhDao().getAllJuz()
    }

    private val _downloadId = MutableLiveData<Long?>()
    val downloadId: LiveData<Long?> get() = _downloadId

    fun checkDownload() {
        viewModelScope.launch {
            _downloadId.value = prefs.getLong(PREF_QURAN_DB_DOWNLOAD_REQ_ID, -1)
        }
    }

    fun addDownload(reqID: Long) {
        viewModelScope.launch {
            prefs.edit { putLong(PREF_QURAN_DB_DOWNLOAD_REQ_ID, reqID) }
        }
    }

    fun removeDownload() {
        viewModelScope.launch {
            prefs.edit { putLong(PREF_QURAN_DB_DOWNLOAD_REQ_ID, -1) }
        }
    }

}//end of class ChapterViewModel
