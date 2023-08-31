package ir.namoo.quran.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.namoo.quran.qari.QariEntity
import ir.namoo.quran.qari.QariRepository
import ir.namoo.quran.settings.data.QuranSettingRepository
import ir.namoo.quran.settings.data.TranslateSetting
import ir.namoo.quran.utils.DEFAULT_ENGLISH_FONT
import ir.namoo.quran.utils.DEFAULT_ENGLISH_FONT_SIZE
import ir.namoo.quran.utils.DEFAULT_FARSI_FONT
import ir.namoo.quran.utils.DEFAULT_FARSI_FONT_SIZE
import ir.namoo.quran.utils.DEFAULT_KURDISH_FONT
import ir.namoo.quran.utils.DEFAULT_KURDISH_FONT_SIZE
import ir.namoo.quran.utils.DEFAULT_PLAY_TYPE
import ir.namoo.quran.utils.DEFAULT_QURAN_FONT
import ir.namoo.quran.utils.DEFAULT_QURAN_FONT_SIZE
import ir.namoo.quran.utils.DEFAULT_SELECTED_QARI
import ir.namoo.quran.utils.DEFAULT_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.PREF_ENGLISH_FONT
import ir.namoo.quran.utils.PREF_ENGLISH_FONT_SIZE
import ir.namoo.quran.utils.PREF_FARSI_FONT
import ir.namoo.quran.utils.PREF_FARSI_FONT_SIZE
import ir.namoo.quran.utils.PREF_FARSI_FULL_TRANSLATE
import ir.namoo.quran.utils.PREF_KURDISH_FONT
import ir.namoo.quran.utils.PREF_KURDISH_FONT_SIZE
import ir.namoo.quran.utils.PREF_PLAY_TYPE
import ir.namoo.quran.utils.PREF_QURAN_FONT
import ir.namoo.quran.utils.PREF_QURAN_FONT_SIZE
import ir.namoo.quran.utils.PREF_SELECTED_QARI
import ir.namoo.quran.utils.PREF_STORAGE_PATH
import ir.namoo.quran.utils.PREF_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.getRootDirs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingViewModel(
    private val quranSettingRepository: QuranSettingRepository,
    private val prefs: SharedPreferences,
    private val qariRepository: QariRepository
) : ViewModel() {

    // ------------------------------------------------------------- Qari
    private val _qariList = MutableStateFlow(listOf<QariEntity>())
    val qariList = _qariList.asStateFlow()

    private val _translatePlayList = MutableStateFlow(listOf<QariEntity>())
    val translatePlayList = _translatePlayList.asStateFlow()

    private val _selectedQari = MutableStateFlow("")
    val selectedQari = _selectedQari.asStateFlow()

    private val _selectedTranslateToPlay = MutableStateFlow("")
    val selectedTranslateToPlay = _selectedTranslateToPlay.asStateFlow()

    private val _isQariLoading = MutableStateFlow(false)
    val isQariLoading = _isQariLoading.asStateFlow()

    // ------------------------------------------------------------- Translates
    private val _translates = MutableStateFlow(listOf<TranslateSetting>())
    val translates = _translates.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isKurdishEnabled = MutableStateFlow(false)
    val isKurdishEnabled = _isKurdishEnabled.asStateFlow()

    private val _isFullFarsiEnabled = MutableStateFlow(false)
    val isFullFarsiEnabled = _isFullFarsiEnabled.asStateFlow()

    // ------------------------------------------------------------- Qaraat
    //// 1:Quran and Translate 2:Quran 3:Translate
    private val _playType = MutableStateFlow(1)
    val playType = _playType.asStateFlow()

    private val _rootPaths = MutableStateFlow(mutableListOf<String>())
    val rootPaths = _rootPaths.asStateFlow()

    private val _selectedPath = MutableStateFlow("")
    val selectedPath = _selectedPath.asStateFlow()

    // --------------------------------------------------------------- Fonts
    private val _quranFontName = MutableStateFlow("")
    val quranFontName = _quranFontName.asStateFlow()

    private val _quranFontSize = MutableStateFlow(14f)
    val quranFontSize = _quranFontSize.asStateFlow()

    private val _kurdishFontName = MutableStateFlow("")
    val kurdishFontName = _kurdishFontName.asStateFlow()

    private val _kurdishFontSize = MutableStateFlow(14f)
    val kurdishFontSize = _kurdishFontSize.asStateFlow()

    private val _farsiFontName = MutableStateFlow("")
    val farsiFontName = _farsiFontName.asStateFlow()

    private val _farsiFontSize = MutableStateFlow(14f)
    val farsiFontSize = _farsiFontSize.asStateFlow()

    private val _englishFontName = MutableStateFlow("")
    val englishFontName = _englishFontName.asStateFlow()

    private val _englishFontSize = MutableStateFlow(14f)
    val englishFontSize = _englishFontSize.asStateFlow()

    // ---------------------------------------------------------------
    fun loadPaths(context: Context) {
        viewModelScope.launch {
            _rootPaths.value.clear()
            getRootDirs(context).toList().forEach { _rootPaths.value.add(it?.absolutePath ?: "-") }
            _selectedPath.value = prefs.getString(
                PREF_STORAGE_PATH, _rootPaths.value[0]
            ) ?: ""
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _isQariLoading.value = true
            _isLoading.value = true
            _isFullFarsiEnabled.value = prefs.getBoolean(PREF_FARSI_FULL_TRANSLATE, false)
            _translates.value = quranSettingRepository.getTranslatesSettings()
            for (t in translates.value.filter { it.id > 2 }) {
                if (t.isActive) {
                    _isKurdishEnabled.value = true
                    break
                }
                _isKurdishEnabled.value = false
            }

            _playType.value = prefs.getInt(PREF_PLAY_TYPE, DEFAULT_PLAY_TYPE)

            _quranFontName.value =
                prefs.getString(PREF_QURAN_FONT, DEFAULT_QURAN_FONT) ?: DEFAULT_QURAN_FONT
            _quranFontSize.value =
                prefs.getFloat(PREF_QURAN_FONT_SIZE, DEFAULT_QURAN_FONT_SIZE)

            _kurdishFontName.value =
                prefs.getString(PREF_KURDISH_FONT, DEFAULT_KURDISH_FONT) ?: DEFAULT_KURDISH_FONT
            _kurdishFontSize.value =
                prefs.getFloat(PREF_KURDISH_FONT_SIZE, DEFAULT_KURDISH_FONT_SIZE)

            _farsiFontName.value =
                prefs.getString(PREF_FARSI_FONT, DEFAULT_FARSI_FONT) ?: DEFAULT_FARSI_FONT
            _farsiFontSize.value = prefs.getFloat(PREF_FARSI_FONT_SIZE, DEFAULT_FARSI_FONT_SIZE)

            _englishFontName.value =
                prefs.getString(PREF_ENGLISH_FONT, DEFAULT_ENGLISH_FONT) ?: DEFAULT_ENGLISH_FONT
            _englishFontSize.value =
                prefs.getFloat(PREF_ENGLISH_FONT_SIZE, DEFAULT_ENGLISH_FONT_SIZE)

            withContext(Dispatchers.IO) {
                val qList = qariRepository.getQariList()
                _qariList.value = qList.filterNot { it.name.startsWith("تفسیر") }
                _translatePlayList.value = qList.filter { it.name.startsWith("تفسیر") }
            }

            _selectedQari.value =
                prefs.getString(PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI) ?: DEFAULT_SELECTED_QARI
            _selectedTranslateToPlay.value =
                prefs.getString(PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY)
                    ?: DEFAULT_TRANSLATE_TO_PLAY

            _isQariLoading.value = false
            _isLoading.value = false
        }
    }

    fun updateSetting(translateSetting: TranslateSetting) {
        viewModelScope.launch {
            _isLoading.value = true
            quranSettingRepository.updateTranslateSetting(translateSetting)
            _translates.value = quranSettingRepository.getTranslatesSettings()
            _isLoading.value = false
        }
    }

    fun updateFullFarsi(enable: Boolean) {
        viewModelScope.launch {
            prefs.edit { putBoolean(PREF_FARSI_FULL_TRANSLATE, enable) }
            _isFullFarsiEnabled.value = enable
        }
    }

    fun updateKurdishEnable(enable: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _isKurdishEnabled.value = enable
            if (!enable) {
                _translates.value.filter { it.id > 2 }.forEach {
                    it.isActive = false
                    quranSettingRepository.updateTranslateSetting(it)
                }
                _translates.value = quranSettingRepository.getTranslatesSettings()
            }
            _isLoading.value = false
        }
    }

    fun updatePlayType(type: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            prefs.edit { putInt(PREF_PLAY_TYPE, type) }
            _playType.value = type
            _isLoading.value = false
        }
    }

    fun updateSelectedPath(path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            prefs.edit { putString(PREF_STORAGE_PATH, path) }
            _selectedPath.value = path
            _isLoading.value = false
        }
    }

    fun updateQuranFontName(font: String) {
        viewModelScope.launch {
            _isLoading.value = true
            prefs.edit { putString(PREF_QURAN_FONT, font) }
            _quranFontName.value = font
            _isLoading.value = false
        }
    }

    fun updateQuranFontSize(size: Float) {
        viewModelScope.launch {
            _isLoading.value = true
            prefs.edit { putFloat(PREF_QURAN_FONT_SIZE, size) }
            _quranFontSize.value = size
            _isLoading.value = false
        }
    }

    fun updateKurdishFontName(font: String) {
        viewModelScope.launch {
            _isLoading.value = true
            prefs.edit { putString(PREF_KURDISH_FONT, font) }
            _kurdishFontName.value = font
            _isLoading.value = false
        }
    }

    fun updateKurdishFontSize(size: Float) {
        viewModelScope.launch {
            _isLoading.value = true
            prefs.edit { putFloat(PREF_KURDISH_FONT_SIZE, size) }
            _kurdishFontSize.value = size
            _isLoading.value = false
        }
    }

    fun updateFarsiFontName(font: String) {
        viewModelScope.launch {
            _isLoading.value = true
            prefs.edit { putString(PREF_FARSI_FONT, font) }
            _farsiFontName.value = font
            _isLoading.value = false
        }
    }

    fun updateFarsiFontSize(size: Float) {
        viewModelScope.launch {
            _isLoading.value = true
            prefs.edit { putFloat(PREF_FARSI_FONT_SIZE, size) }
            _farsiFontSize.value = size
            _isLoading.value = false
        }
    }

    fun updateEnglishFontName(font: String) {
        viewModelScope.launch {
            _isLoading.value = true
            prefs.edit { putString(PREF_ENGLISH_FONT, font) }
            _englishFontName.value = font
            _isLoading.value = false
        }
    }

    fun updateEnglishFontSize(size: Float) {
        viewModelScope.launch {
            _isLoading.value = true
            prefs.edit { putFloat(PREF_ENGLISH_FONT_SIZE, size) }
            _englishFontSize.value = size
            _isLoading.value = false
        }
    }

    fun updateSelectedQari(qari: String) {
        viewModelScope.launch {
            _isQariLoading.value = true
            prefs.edit { putString(PREF_SELECTED_QARI, qari) }
            _selectedQari.value = qari
            _isQariLoading.value = false
        }
    }

    fun updateSelectedTranslateToPlay(qari: String) {
        viewModelScope.launch {
            _isQariLoading.value = true
            prefs.edit { putString(PREF_TRANSLATE_TO_PLAY, qari) }
            _selectedTranslateToPlay.value = qari
            _isQariLoading.value = false
        }
    }

}//end of class SettingViewModel
