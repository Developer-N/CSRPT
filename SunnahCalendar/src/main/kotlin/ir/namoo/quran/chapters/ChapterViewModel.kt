package ir.namoo.quran.chapters

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import ir.namoo.quran.chapters.data.ChapterEntity
import ir.namoo.quran.chapters.data.ChapterRepository
import ir.namoo.quran.db.LastVisitedEntity
import ir.namoo.quran.db.LastVisitedPageEntity
import ir.namoo.quran.db.LastVisitedRepository
import ir.namoo.quran.db.QCFChapters
import ir.namoo.quran.sura.data.QuranEntity
import ir.namoo.quran.utils.DEFAULT_PAGE_TYPE
import ir.namoo.quran.utils.PREF_BOOKMARK_VERSE
import ir.namoo.quran.utils.PREF_PAGE_TYPE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@SuppressLint("SdCardPath")
class ChapterViewModel(
    private val chapterRepository: ChapterRepository,
    private val lastVisitedRepository: LastVisitedRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isFavShowing = MutableStateFlow(false)
    val isFavShowing = _isFavShowing.asStateFlow()

    private val _lastVisitedList = mutableStateListOf<LastVisitedEntity>()
    val lastVisitedList = _lastVisitedList

    private val _lastVisitedPages = mutableStateListOf<LastVisitedPageEntity>()
    val lastVisitedPages = _lastVisitedPages

    private val _pageType = MutableStateFlow(DEFAULT_PAGE_TYPE)
    val pageType = _pageType.asStateFlow()

    private val _chapterList = mutableStateListOf<ChapterEntity>()
    val chapterList = _chapterList

    private val _qcfChapters = mutableStateListOf<QCFChapters>()
    val qcfChapters = _qcfChapters


    private val _bookmarkedVerse = MutableStateFlow<QuranEntity?>(null)
    val bookmarkedVerse = _bookmarkedVerse.asStateFlow()

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _pageType.value = prefs.getInt(PREF_PAGE_TYPE, DEFAULT_PAGE_TYPE)
            prefs.getInt(PREF_BOOKMARK_VERSE, -1).let { id ->
                if (id > 0)
                    _bookmarkedVerse.value = chapterRepository.getVerse(id)
            }
            _chapterList.clear()
            _chapterList.addAll(chapterRepository.getAllChapters())
            _qcfChapters.clear()
            _qcfChapters.addAll(chapterRepository.getQCFChapters())
            _lastVisitedList.clear()
            _lastVisitedList.addAll(lastVisitedRepository.getAllLastVisited().reversed())
            _lastVisitedPages.clear()
            _lastVisitedPages.addAll(lastVisitedRepository.getAllVisitedPages().reversed())
            _isLoading.value = false
        }
    }

    fun updateFav(chapter: ChapterEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            val index = _chapterList.indexOf(chapter)
            _chapterList[index] = _chapterList[index].copy(fav = if (chapter.fav == 1) 0 else 1)
            chapterRepository.updateChapter(chapter.copy(fav = if (chapter.fav == 1) 0 else 1))
            _isLoading.value = false
        }
    }

    fun showFav(show: Boolean) {
        _isLoading.value = true
        _isFavShowing.value = show
        _isLoading.value = false
    }

    fun navigateToPage(page: Int, navController: NavHostController) {
        viewModelScope.launch {
            val pageEntity = chapterRepository.getPage(page)
            navController.navigate("sura/${pageEntity.sura}/${pageEntity.aya}")
        }
    }

    fun navigateToJuz(juz: Int, navController: NavHostController) {
        viewModelScope.launch {
            val juzEntity = chapterRepository.getJuz(juz)
            navController.navigate("sura/${juzEntity.sura}/${juzEntity.aya}")
        }
    }

    fun navigateToHizb(hizb: Int, navController: NavHostController) {
        viewModelScope.launch {
            val hizbEntity = chapterRepository.getHizb(hizb)
            navController.navigate("sura/${hizbEntity.sura}/${hizbEntity.aya}")
        }
    }
}
