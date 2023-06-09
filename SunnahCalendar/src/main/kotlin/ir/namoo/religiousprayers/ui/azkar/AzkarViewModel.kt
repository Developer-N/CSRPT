package ir.namoo.religiousprayers.ui.azkar

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.entities.Language
import ir.namoo.commons.DEFAULT_AZKAR_LANG
import ir.namoo.commons.repository.DataState
import ir.namoo.commons.repository.asDataState
import kotlinx.coroutines.launch

class AzkarViewModel constructor(private val azkarRepository: AzkarRepository) : ViewModel() {

    private var azkarChapters by mutableStateOf(listOf<AzkarChapter>())
    var filteredAzkarChapters by mutableStateOf(mutableListOf<AzkarChapter>())
        private set
    var error by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(true)
        private set
    var isBookmarkShowed by mutableStateOf(false)
        private set
    var searchQuery by mutableStateOf("")
        private set
    var azkarLang by mutableStateOf(DEFAULT_AZKAR_LANG)
        private set

    fun getChapters() {
        viewModelScope.launch {
            isLoading = true
            azkarRepository.getAzkarChapters().collect {
                when (it.asDataState()) {
                    is DataState.Error -> {
                        isLoading = false
                        error = (it.asDataState() as DataState.Error).message
                    }

                    DataState.Loading -> {
                        isLoading = true
                    }

                    is DataState.Success -> {
                        azkarChapters =
                            (it.asDataState() as DataState.Success<List<AzkarChapter>>).data
                        filteredAzkarChapters.clear()
                        filteredAzkarChapters.addAll(azkarChapters)
                        isLoading = false
                    }
                }
            }
        }
    }

    fun showBookmarks() {
        isLoading = true
        isBookmarkShowed = !isBookmarkShowed
        filteredAzkarChapters.clear()
        if (isBookmarkShowed) filteredAzkarChapters.addAll(azkarChapters.filter { it.fav == 1 })
        else filteredAzkarChapters.addAll(azkarChapters)
        isLoading = false
    }

    fun search(query: String) {
        isLoading = true
        searchQuery = query
        filteredAzkarChapters.clear()
        filteredAzkarChapters.addAll(azkarChapters.filter {
            when (azkarLang) {
                Language.FA.code -> it.persian?.contains(query) == true
                Language.CKB.code -> it.kurdish?.contains(query) == true
                else -> it.arabic?.contains(query) == true
            }
        })
        isLoading = false
    }

    fun updateAzkarChapter(azkarChapter: AzkarChapter) {
        viewModelScope.launch {
            azkarChapter.fav = if (azkarChapter.fav == 1) 0 else 1
            azkarRepository.updateAzkarChapter(azkarChapter)
            if (isBookmarkShowed) {
                isLoading = true
                filteredAzkarChapters.remove(azkarChapter)
                isLoading = false
            }
        }
    }

    fun setLang(lang: String) {
        isLoading = true
        azkarLang = lang
        isLoading = false
    }

}//end of class
