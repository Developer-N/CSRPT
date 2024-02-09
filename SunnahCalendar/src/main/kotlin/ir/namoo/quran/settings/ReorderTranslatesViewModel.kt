package ir.namoo.quran.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.namoo.quran.settings.data.QuranSettingRepository
import ir.namoo.quran.settings.data.TranslateSetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReorderTranslatesViewModel(private val quranSettingRepository: QuranSettingRepository) :
    ViewModel() {

    private val _settings = MutableStateFlow(emptyList<TranslateSetting>())
    val settings = _settings.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _settings.value = quranSettingRepository.getTranslatesSettings()
        }
    }

    fun moveUp(setting: TranslateSetting) {
        viewModelScope.launch {
            val priority = setting.priority
            val otherSetting = settings.value.find { it.priority == priority - 1 } ?: return@launch
            setting.priority = priority - 1
            quranSettingRepository.updateTranslateSetting(setting)
            otherSetting.priority = priority
            quranSettingRepository.updateTranslateSetting(otherSetting)
            _settings.value = quranSettingRepository.getTranslatesSettings()
        }
    }

    fun moveDown(setting: TranslateSetting) {
        viewModelScope.launch {
            val priority = setting.priority
            val otherSetting = settings.value.find { it.priority == priority + 1 } ?: return@launch
            setting.priority = (priority + 1)
            quranSettingRepository.updateTranslateSetting(setting)
            otherSetting.priority = priority
            quranSettingRepository.updateTranslateSetting(otherSetting)
            _settings.value = quranSettingRepository.getTranslatesSettings()
        }
    }

}
