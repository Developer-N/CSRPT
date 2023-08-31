package ir.namoo.quran.bookmarks

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

    private val _bookmarks = MutableStateFlow(mutableListOf<QuranEntity>())
    val bookmarks = _bookmarks.asStateFlow()

    private val _chapters = MutableStateFlow(listOf<ChapterEntity>())
    val chapters = _chapters.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _chapters.value = chapterRepository.getAllChapters()
            _bookmarks.value = quranRepository.getBookmarks().toMutableList()
            _isLoading.value = false
        }
    }

    fun removeBookmark(quranEntity: QuranEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            _bookmarks.value.remove(quranEntity)
            quranEntity.fav = 0
            quranRepository.updateQuran(quranEntity)
            _isLoading.value = false
        }
    }

}
