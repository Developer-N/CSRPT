package ir.namoo.religiousprayers.ui.azkar

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.namoo.commons.DEFAULT_AZKAR_LANG
import ir.namoo.commons.PREF_AZKAR_LANG
import ir.namoo.religiousprayers.ui.azkar.data.AzkarChapter
import ir.namoo.religiousprayers.ui.azkar.data.AzkarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AzkarViewModel(
    private val azkarRepository: AzkarRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _isSearchBoxIsOpen = MutableStateFlow(false)
    val isSearchBoxIsOpen = _isSearchBoxIsOpen.asStateFlow()

    private val _chapters = mutableStateListOf<AzkarChapter>()
    val chapters = _chapters

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    private val _azkarLang = MutableStateFlow(DEFAULT_AZKAR_LANG)
    val azkarLang = _azkarLang.asStateFlow()


    private val _isFavShowing = MutableStateFlow(false)
    val isFavShowing = _isFavShowing.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _chapters.clear()
            _chapters.addAll(azkarRepository.getAzkarChapters())
            _azkarLang.value = prefs.getString(PREF_AZKAR_LANG, DEFAULT_AZKAR_LANG)
                ?: DEFAULT_AZKAR_LANG
            _isLoading.value = false
        }
    }

    fun showBookmarks(show: Boolean) {
        _isFavShowing.value = show
    }

    fun updateAzkarChapter(azkarChapter: AzkarChapter) {
        viewModelScope.launch {
            _isLoading.value = true
            val index = chapters.indexOf(azkarChapter)
            _chapters[index] =
                _chapters[index].copy(fav = if (azkarChapter.fav == 1) 0 else 1)
            azkarRepository.updateAzkarChapter(azkarChapter.copy(fav = if (azkarChapter.fav == 1) 0 else 1))
        }
        _isLoading.value = false
    }

    fun setLang(lang: String) {
        _azkarLang.value = lang
    }

    fun openSearch() {
        _isSearchBoxIsOpen.value = true
    }

    fun closeSearch() {
        _isSearchBoxIsOpen.value = false
    }
}//end of class
