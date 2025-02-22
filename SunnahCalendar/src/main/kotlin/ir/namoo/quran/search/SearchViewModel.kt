package ir.namoo.quran.search

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.namoo.quran.chapters.data.ChapterEntity
import ir.namoo.quran.chapters.data.ChapterRepository
import ir.namoo.quran.sura.data.QuranEntity
import ir.namoo.quran.sura.data.QuranRepository
import ir.namoo.quran.utils.DEFAULT_SEARCH_IN_TRANSLATES
import ir.namoo.quran.utils.PREF_SEARCH_IN_TRANSLATES
import ir.namoo.quran.utils.getWordsForSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val quranRepository: QuranRepository,
    private val chapterRepository: ChapterRepository,
    private val prefs: SharedPreferences
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isInSearch = MutableStateFlow(false)
    val isInSearch = _isInSearch.asStateFlow()

    private val _quranList = mutableStateListOf<QuranEntity>()
    val quranList = _quranList

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _chapters = mutableStateListOf<ChapterEntity>()
    val chapters = _chapters

    private val _searchInTranslates = MutableStateFlow(false)
    val searchInTranslates = _searchInTranslates.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _isInSearch.value = false
            _searchInTranslates.value = prefs.getBoolean(
                PREF_SEARCH_IN_TRANSLATES,
                DEFAULT_SEARCH_IN_TRANSLATES
            )
            _chapters.clear()
            _chapters.addAll(chapterRepository.getAllChapters())
            _isLoading.value = false
        }

    }

    fun updateQuery(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _query.value = query
            if (query.isEmpty()) {
                _quranList.clear()
                _isInSearch.value = false
            }
            _isLoading.value = false
        }
    }

    fun search() {
        viewModelScope.launch {
            _isLoading.value = true
            _isInSearch.value = true
            _quranList.clear()
            query.value.getWordsForSearch().forEach { word ->
                quranRepository.searchInQuran(word).forEach { quran ->
                    if (_quranList.find { it.id == quran.id } == null)
                        _quranList.add(quran)
                }
                if (searchInTranslates.value)
                    quranRepository.searchInTafsirs(word).forEach { tafsir ->
                        if (_quranList.find { it.id == tafsir.id } == null)
                            quranRepository.getQuran(
                                tafsir.surahID, tafsir.verseID
                            )?.let { _quranList.add(it) }
                    }
            }
            _isLoading.value = false
        }
    }

    fun updateSearchInTranslate() {
        prefs.edit {
            putBoolean(PREF_SEARCH_IN_TRANSLATES, !searchInTranslates.value)
        }
        _searchInTranslates.value = !searchInTranslates.value
        if(query.value.isNotEmpty()) search()
    }
}
