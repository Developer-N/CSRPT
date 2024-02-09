package ir.namoo.quran.chapters

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.namoo.quran.chapters.data.ChapterEntity
import ir.namoo.quran.chapters.data.ChapterRepository
import ir.namoo.quran.chapters.data.HizbEntity
import ir.namoo.quran.chapters.data.JuzEntity
import ir.namoo.quran.chapters.data.PageEntity
import ir.namoo.quran.db.LastVisitedEntity
import ir.namoo.quran.db.LastVisitedRepository
import ir.namoo.quran.db.OldQuranDB
import ir.namoo.quran.sura.data.QuranRepository
import ir.namoo.quran.utils.getWordsForSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

@SuppressLint("SdCardPath")
class ChapterViewModel(
    private val chapterRepository: ChapterRepository,
    private val oldQuranDB: OldQuranDB,
    private val quranRepository: QuranRepository,
    private val lastVisitedRepository: LastVisitedRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isFavShowing = MutableStateFlow(false)
    val isFavShowing = _isFavShowing.asStateFlow()

    private val _lastVisitedList = MutableStateFlow(emptyList<LastVisitedEntity>())
    val lastVisitedList = _lastVisitedList.asStateFlow()

    private val _isSearchBarOpen = MutableStateFlow(false)
    val isSearchBarOpen = _isSearchBarOpen.asStateFlow()

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _chapterList = MutableStateFlow(listOf<ChapterEntity>())
    val chapterList = query.combine(_chapterList) { text, chapterList ->
        when {
            text.isBlank() -> chapterList
            text.startsWith("fav") -> chapterList.filter { it.fav == 1 }
            text == "default" -> chapterList.sortedBy { it.sura }
            text == "alphabet" -> chapterList.sortedBy { it.nameArabic }
            text == "ayaIncrease" -> chapterList.sortedBy { it.ayaCount }
            text == "ayaDecrease" -> chapterList.sortedByDescending { it.ayaCount }
            text == "revelation" -> chapterList.sortedBy { it.revelationOrder }
            else -> chapterList.filter { isContains(it.nameArabic, text) }
        }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), _chapterList.value
    )

    private fun isContains(name: String, text: String): Boolean {
        text.getWordsForSearch().forEach { word ->
            if (name.contains(word)) return true
        }
        return false
    }


    init {
        viewModelScope.launch(Dispatchers.IO) {
            // transfer old data (notes and bookmarks) to new db
            runCatching {
                val oldDB = File("/data/data/ir.namoo.religiousprayers/databases/quran.db")
                if (oldDB.exists()) {
                    //Get old bookmarked chapters
                    oldQuranDB.chaptersDao().getAllChapters().filter { it.fav == 1 }
                        .forEach { oldChapter ->
                            chapterRepository.getAllChapters().find { it.sura == oldChapter.sura }
                                ?.let { newChapter ->
                                    newChapter.fav = 1
                                    chapterRepository.updateChapter(newChapter)
                                }
                        }
                    //Get old bookmarked verses
                    oldQuranDB.quranDao().getAllBookmarks()?.forEach { oldQuranEntity ->
                        quranRepository.getQuran(oldQuranEntity.sura ?: 0)
                            .find { it.verseID == oldQuranEntity.aya }?.let { newQuran ->
                                newQuran.fav = 1
                                quranRepository.updateQuran(newQuran)
                            }
                    }
                    //Get old notes
                    oldQuranDB.quranDao().getAllNotes()?.forEach { oldQuranEntity ->
                        quranRepository.getQuran(oldQuranEntity.sura ?: 0)
                            .find { it.verseID == oldQuranEntity.aya }?.let { newQuran ->
                                newQuran.note = oldQuranEntity.note
                                quranRepository.updateQuran(newQuran)
                            }
                    }
                    //delete old db
                    oldDB.delete()
                }
            }.onFailure {}
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _chapterList.value = chapterRepository.getAllChapters()
            _lastVisitedList.value = lastVisitedRepository.getAllLastVisited().reversed()
            _isLoading.value = false
        }
    }

    fun onQuery(text: String) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                _query.update { text }
            }
            _isLoading.value = false
        }
    }

    fun updateFav(sura: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                val chapter = chapterList.value.find { it.sura == sura } ?: return@withContext
                chapter.fav = if (chapter.fav == 1) 0 else 1
                chapterRepository.updateChapter(chapter)
            }
            if (isFavShowing.value) _query.update { "fav-${Random(1000)}" }
            _isLoading.value = false
        }
    }

    fun showFav() {
        viewModelScope.launch {
            _isLoading.value = true
            if (_isFavShowing.value) {
                _query.update { "" }
                _isFavShowing.value = false
            } else {
                _query.update { "fav" }
                _isFavShowing.value = true
            }
            _isLoading.value = false
        }
    }

    suspend fun getPage(page: Int): PageEntity {
        return chapterRepository.getPage(page)
    }

    suspend fun getJuz(juz: Int): JuzEntity {
        return chapterRepository.getJuz(juz)
    }

    suspend fun getHizb(hizb: Int): HizbEntity {
        return chapterRepository.getHizb(hizb)
    }

    fun openSearchBar() {
        _isSearchBarOpen.value = true
    }

    fun closeSearchBar() {
        _isSearchBarOpen.value = false
    }
}//end of class ChapterViewModel
