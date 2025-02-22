package ir.namoo.hadeeth.ui.hadeeth

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.R
import ir.namoo.commons.APP_LINK
import ir.namoo.commons.repository.DataState
import ir.namoo.hadeeth.repository.Hadeeth
import ir.namoo.hadeeth.repository.HadeethRepository
import ir.namoo.hadeeth.repository.LanguageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HadeethViewModel(private val hadeethRepository: HadeethRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _hadeeth = MutableStateFlow<Hadeeth?>(null)
    val hadeeth = _hadeeth.asStateFlow()

    private val _languages = mutableStateListOf<LanguageEntity>()
    val languages = _languages

    private val _selectedLanguage = MutableStateFlow("fa")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()

    private val _showRetry = MutableStateFlow(false)
    val showRetry = _showRetry.asStateFlow()

    fun loadData(context: Context, hadeethID: String) {
        viewModelScope.launch {
            hadeethRepository.getLanguages().collectLatest { state ->
                when (state) {
                    is DataState.Error -> {
                        _isLoading.value = false
                        _showRetry.value = true
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
                        loadHadeeth(context, settings.language, hadeethID)
                    }
                }
            }
        }
    }

    fun onLanguageSelected(context: Context, selectedLanguage: String, hadeethID: String) {
        viewModelScope.launch {
            _selectedLanguage.value = selectedLanguage
            hadeethRepository.updateLocalLanguage(selectedLanguage)
            _hadeeth.value = null
            _content.value = ""
            loadHadeeth(context, selectedLanguage, hadeethID)
        }
    }

    private fun loadHadeeth(context: Context, language: String, hadeethID: String) {
        viewModelScope.launch {
            _content.value = ""
            hadeethRepository.getHadeeth(language, hadeethID.toInt()).collectLatest { state ->
                when (state) {
                    is DataState.Error -> {
                        _isLoading.value = false
                        _showRetry.value = true
                    }

                    DataState.Loading -> {
                        _showRetry.value = false
                        _isLoading.value = true
                    }

                    is DataState.Success -> {
                        _isLoading.value = false
                        _hadeeth.value = state.data

                        state.data.let { hadeeth ->
                            _content.value += "📑 "
                            _content.value += context.getString(
                                R.string.hadeeth_number, hadeethID
                            )
                            _content.value += " 📑"
                            _content.value += "\n\n"
                            _content.value += hadeeth.title
                            _content.value += "\n\n"
                            _content.value += hadeeth.hadeeth
                            _content.value += "\n\n"
                            _content.value += "${context.getString(R.string.sharh)}: ${hadeeth.explanation}\n\n"
                            if (hadeeth.hints.isNotEmpty()) _content.value += "${
                                context.getString(
                                    R.string.benefits
                                )
                            }:\n ${
                                hadeeth.hints.joinToString("\n ")
                            }\n\n"
                            hadeeth.wordsMeanings?.let { wordsMeanings ->
                                if (wordsMeanings.isNotEmpty())
                                    _content.value += "${context.getString(R.string.words_meaning)}:\n ${
                                        wordsMeanings.joinToString("\n ")
                                    }\n\n"
                            }
                            hadeeth.reference?.let { reference ->
                                _content.value += "${context.getString(R.string.reference)}: $reference \n\n"
                            }
                            _content.value += APP_LINK
                        }
                    }
                }
            }
        }
    }
}
