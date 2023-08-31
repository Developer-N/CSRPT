package ir.namoo.religiousprayers.ui.azkar

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.entities.Language
import ir.namoo.commons.DEFAULT_AZKAR_LANG
import ir.namoo.commons.PREF_AZKAR_LANG
import ir.namoo.religiousprayers.ui.azkar.data.AzkarChapter
import ir.namoo.religiousprayers.ui.azkar.data.AzkarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class AzkarViewModel constructor(
    private val azkarRepository: AzkarRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _chapters = MutableStateFlow(emptyList<AzkarChapter>())
    val chapters = query.combine(_chapters) { text, chapters ->
        when {
            text.isBlank() -> chapters
            text.startsWith("fav") -> chapters.filter { it.fav == 1 }
            else -> chapters.filter {
                when (azkarLang.value) {
                    Language.FA.code -> it.persian?.contains(text) == true
                    Language.CKB.code -> it.kurdish?.contains(text) == true
                    else -> it.arabic?.contains(text) == true
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _chapters.value)

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    private val _azkarLang = MutableStateFlow(DEFAULT_AZKAR_LANG)
    val azkarLang = _azkarLang.asStateFlow()


    private val _isFavShowing = MutableStateFlow(false)
    val isFavShowing = _isFavShowing.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _chapters.value = azkarRepository.getAzkarChapters()
            _azkarLang.value = prefs.getString(PREF_AZKAR_LANG, DEFAULT_AZKAR_LANG)
                ?: DEFAULT_AZKAR_LANG
            _isLoading.value = false
        }
    }

    fun showBookmarks() {
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

    fun search(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _query.value = query
            _isLoading.value = false
        }
    }

    fun updateAzkarChapter(azkarChapter: AzkarChapter) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                azkarChapter.fav = if (azkarChapter.fav == 1) 0 else 1
                _chapters.value.find { it.id == azkarChapter.id }?.apply {
                    fav = azkarChapter.fav
                }
                azkarRepository.updateAzkarChapter(azkarChapter)
            }
            if (isFavShowing.value) {
                _query.update { "fav-${Random(1000)}" }
            }
            _isLoading.value = false
        }
    }

    fun setLang(lang: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _azkarLang.value = lang
            _isLoading.value = false
        }
    }
}//end of class
