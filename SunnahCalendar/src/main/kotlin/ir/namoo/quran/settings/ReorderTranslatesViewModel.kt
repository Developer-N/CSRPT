package ir.namoo.quran.settings

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.namoo.commons.repository.DataState
import ir.namoo.quran.settings.data.QuranSettingRepository
import ir.namoo.quran.settings.data.TranslateSetting
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReorderTranslatesViewModel(private val quranSettingRepository: QuranSettingRepository) :
    ViewModel() {

    private val _settings = mutableStateListOf<TranslateSetting>()
    val settings = _settings

    fun loadData() {
        viewModelScope.launch {
            quranSettingRepository.getTranslatesSettings().collectLatest { state ->
                when (state) {
                    is DataState.Error -> {}
                    DataState.Loading -> {}
                    is DataState.Success -> {
                        _settings.clear()
                        _settings.addAll(state.data)
                    }
                }
            }
        }
    }

    fun moveUp(setting: TranslateSetting) {
        viewModelScope.launch {
            val index = settings.indexOf(setting)
            val priority = setting.priority
            val otherSetting = settings.find { it.priority == priority - 1 } ?: return@launch
            quranSettingRepository.updateTranslateSetting(setting.copy(priority = priority - 1))
            quranSettingRepository.updateTranslateSetting(otherSetting.copy(priority = priority))

            _settings[index] = _settings[index].copy(priority = priority - 1)
            _settings[index - 1] = _settings[index - 1].copy(priority = priority)
            _settings.sortBy { it.priority }
        }
    }

    fun moveDown(setting: TranslateSetting) {
        viewModelScope.launch {
            val index = settings.indexOf(setting)
            val priority = setting.priority
            val otherSetting = settings.find { it.priority == priority + 1 } ?: return@launch
            quranSettingRepository.updateTranslateSetting(setting.copy(priority = priority + 1))
            quranSettingRepository.updateTranslateSetting(otherSetting.copy(priority = priority))

            _settings[index] = _settings[index].copy(priority = priority + 1)
            _settings[index + 1] = _settings[index + 1].copy(priority = priority)
            _settings.sortBy { it.priority }
        }
    }
}
