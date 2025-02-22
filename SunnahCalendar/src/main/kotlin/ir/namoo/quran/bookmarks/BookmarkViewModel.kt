package ir.namoo.quran.bookmarks

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.namoo.quran.chapters.data.ChapterEntity
import ir.namoo.quran.chapters.data.ChapterRepository
import ir.namoo.quran.sura.data.QuranEntity
import ir.namoo.quran.sura.data.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookmarkViewModel(
    private val quranRepository: QuranRepository, private val chapterRepository: ChapterRepository
) : ViewModel() {

    private val _bookmarks = mutableStateListOf<QuranEntity>()
    val bookmarks = _bookmarks

    private val _chapters = mutableStateListOf<ChapterEntity>()
    val chapters = _chapters

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _chapters.clear()
            _chapters.addAll(chapterRepository.getAllChapters())
            _bookmarks.clear()
            _bookmarks.addAll(quranRepository.getBookmarks().toMutableList())
            _isLoading.value = false
        }
    }

    fun removeBookmark(quranEntity: QuranEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            _bookmarks.remove(quranEntity)
            quranRepository.updateQuran(quranEntity.copy(fav = 0))
            _isLoading.value = false
        }
    }

}
