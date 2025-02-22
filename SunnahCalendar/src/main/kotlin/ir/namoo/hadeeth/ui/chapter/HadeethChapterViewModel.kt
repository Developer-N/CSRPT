package ir.namoo.hadeeth.ui.chapter

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.namoo.commons.repository.DataState
import ir.namoo.hadeeth.repository.CategoryEntity
import ir.namoo.hadeeth.repository.HadeethList
import ir.namoo.hadeeth.repository.HadeethRepository
import ir.namoo.hadeeth.repository.LanguageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HadeethChapterViewModel(private val hadeethRepository: HadeethRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _languages = mutableStateListOf<LanguageEntity>()
    val languages = _languages

    private val _selectedLanguage = MutableStateFlow("fa")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    private val _categories = mutableStateListOf<CategoryEntity>()
    val categories = _categories

    private val _selectedParent = mutableStateListOf<String>()
    val selectedParent = _selectedParent

    private val _chapters = MutableStateFlow<HadeethList?>(null)
    val chapters = _chapters.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage = _currentPage.asStateFlow()

    private val _perPage = MutableStateFlow(20)
    val perPage = _perPage.asStateFlow()

    private val _lastPage = MutableStateFlow(1)
    val lastPage = _lastPage.asStateFlow()

    private val _totalItems = MutableStateFlow(0)
    val totalItems = _totalItems.asStateFlow()

    private val _route = MutableStateFlow("")
    val route = _route.asStateFlow()

    private val _isChapterLoading = MutableStateFlow(false)
    val isChapterLoading = _isChapterLoading.asStateFlow()

    private val _showRetry = MutableStateFlow(false)
    val showRetry = _showRetry.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            hadeethRepository.getLanguages().collectLatest { state ->
                when (state) {
                    is DataState.Error -> {
                        _showRetry.value = true
                        _isLoading.value = false
                    }

                    DataState.Loading -> {
                        _showRetry.value = false
                        _isLoading.value = true
                    }

                    is DataState.Success -> {
                        _isLoading.value = false
                        _languages.clear()
                        _languages.addAll(state.data)
                        val settings = hadeethRepository.getSettings()
                        _selectedLanguage.value = settings.language
                        loadRootCategories(settings.language)
                    }
                }
            }
        }
    }

    private suspend fun loadRootCategories(language: String) {
        hadeethRepository.getCategories(language).collectLatest { status ->
            when (status) {
                is DataState.Error -> {
                    _isLoading.value = false
                }

                DataState.Loading -> {
                    _isLoading.value = true
                }

                is DataState.Success -> {
                    _isLoading.value = false
                    _categories.clear()
                    _categories.addAll(status.data)
                    loadChapters()
                }
            }
        }
    }

    fun onLanguageSelected(selectedLanguage: String) {
        viewModelScope.launch {
            _selectedLanguage.value = selectedLanguage
            hadeethRepository.updateLocalLanguage(selectedLanguage)
            loadData()
            loadChapters()
        }
    }

    fun addSelectedPatent(selectedParent: String) {
        viewModelScope.launch {
            _selectedParent.add(selectedParent)
            updateRoute()
            loadChapters()
        }
    }

    fun removeSelectedPatent() {
        viewModelScope.launch {
            _selectedParent.removeAt(_selectedParent.size - 1)
            updateRoute()
            loadChapters()
        }
    }

    private fun updateRoute() {
        _route.value = ""
        _perPage.value = 20
        _currentPage.value = 1
        _lastPage.value = 1
        _totalItems.value = 0
        selectedParent.forEach { parent ->
            _route.value += " / ${categories.find { it.categoryID == parent }?.title ?: "-"}"
        }
    }

    fun loadNextPage() {
        viewModelScope.launch {
            if (_currentPage.value < _lastPage.value) {
                _currentPage.value++
                loadChapters()
            }
        }
    }

    fun loadPreviousPage() {
        viewModelScope.launch {
            if (_currentPage.value > 1) {
                _currentPage.value--
                loadChapters()
            }
        }
    }

    fun updateCurrentPage(page: Int) {
        viewModelScope.launch {
            _currentPage.value = page
            loadChapters()
        }
    }

    private suspend fun loadChapters() {
        if (_selectedParent.isEmpty()) {
            _chapters.value = null
            return
        }
        hadeethRepository.getHadeethList(
            _selectedLanguage.value,
            _selectedParent.last().toInt(),
            currentPage.value,
            perPage.value
        ).collectLatest { state ->
            when (state) {
                is DataState.Error -> {
                    println("================================================")
                    _showRetry.value = true
                    _isChapterLoading.value = false
                }

                DataState.Loading -> {
                    _showRetry.value = false
                    _isChapterLoading.value = true
                }

                is DataState.Success -> {
                    _isChapterLoading.value = false
                    _chapters.value = state.data
                    _currentPage.value = state.data.meta.currentPage
                    _perPage.value = state.data.meta.perPage
                    _lastPage.value = state.data.meta.lastPage
                    _totalItems.value = state.data.meta.totalItems
                }
            }
        }
    }

    fun clearCachedData() {
        viewModelScope.launch {
            _isLoading.value = true
            hadeethRepository.clearCachedData()
            _categories.clear()
            _languages.clear()
            _selectedParent.clear()
            _chapters.value = null
            _currentPage.value = 1
            _perPage.value = 20
            _lastPage.value = 1
            _totalItems.value = 0
            _route.value = ""
            loadData()
            _isLoading.value = false
        }
    }
}
