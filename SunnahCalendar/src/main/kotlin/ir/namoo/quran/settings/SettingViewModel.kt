package ir.namoo.quran.settings

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.namoo.commons.repository.DataState
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
import ir.namoo.quran.utils.DEFAULT_PAGE_TYPE
import ir.namoo.quran.utils.DEFAULT_PLAYER_SPEED
import ir.namoo.quran.utils.DEFAULT_PLAY_NEXT_SURA
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
import ir.namoo.quran.utils.PREF_HIDE_TOOLBAR_ON_SCROLL
import ir.namoo.quran.utils.PREF_KURDISH_FONT
import ir.namoo.quran.utils.PREF_KURDISH_FONT_SIZE
import ir.namoo.quran.utils.PREF_PAGE_TYPE
import ir.namoo.quran.utils.PREF_PLAYER_SPEED
import ir.namoo.quran.utils.PREF_PLAY_NEXT_SURA
import ir.namoo.quran.utils.PREF_PLAY_TYPE
import ir.namoo.quran.utils.PREF_QURAN_FONT
import ir.namoo.quran.utils.PREF_QURAN_FONT_SIZE
import ir.namoo.quran.utils.PREF_SELECTED_QARI
import ir.namoo.quran.utils.PREF_STORAGE_PATH
import ir.namoo.quran.utils.PREF_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.getRootDirs
import ir.namoo.quran.utils.updatePlayerSpeedGlobal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingViewModel(
    private val quranSettingRepository: QuranSettingRepository,
    private val prefs: SharedPreferences,
    private val qariRepository: QariRepository
) : ViewModel() {

    // ------------------------------------------------------------- Qari
    private val _qariList = mutableStateListOf<QariEntity>()
    val qariList = _qariList

    private val _translatePlayList = mutableStateListOf<QariEntity>()
    val translatePlayList = _translatePlayList

    private val _selectedQari = MutableStateFlow("")
    val selectedQari = _selectedQari.asStateFlow()

    private val _selectedTranslateToPlay = MutableStateFlow("")
    val selectedTranslateToPlay = _selectedTranslateToPlay.asStateFlow()

    private val _isQariLoading = MutableStateFlow(false)
    val isQariLoading = _isQariLoading.asStateFlow()

    // ------------------------------------------------------------- Translates
    private val _translates = mutableStateListOf<TranslateSetting>()
    val translates = _translates

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

    private val _rootPaths = mutableStateListOf<String>()
    val rootPaths = _rootPaths

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

    private val _playNextSura = MutableStateFlow(DEFAULT_PLAY_NEXT_SURA)
    val playNextSura = _playNextSura.asStateFlow()

    private val _playerSpeed = MutableStateFlow(DEFAULT_PLAYER_SPEED)
    val playerSpeed = _playerSpeed.asStateFlow()

    //----------------------------------------------------------------
    private val _pageType = MutableStateFlow(DEFAULT_PAGE_TYPE)
    val pageType = _pageType.asStateFlow()

    private val _hideToolbarOnScroll = MutableStateFlow(false)
    val hideToolbarOnScroll = _hideToolbarOnScroll.asStateFlow()


    // ---------------------------------------------------------------
    fun loadPaths(context: Context) {
        viewModelScope.launch {
            _rootPaths.clear()
            getRootDirs(context).toList().forEach { _rootPaths.add(it?.absolutePath ?: "-") }
            _selectedPath.value = prefs.getString(
                PREF_STORAGE_PATH, _rootPaths[0]
            ) ?: ""
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _isQariLoading.value = true
            _isLoading.value = true
            _playerSpeed.value = prefs.getFloat(PREF_PLAYER_SPEED, DEFAULT_PLAYER_SPEED)
            _playNextSura.value = prefs.getBoolean(PREF_PLAY_NEXT_SURA, DEFAULT_PLAY_NEXT_SURA)
            _isFullFarsiEnabled.value = prefs.getBoolean(PREF_FARSI_FULL_TRANSLATE, false)
            _pageType.value = prefs.getInt(PREF_PAGE_TYPE, DEFAULT_PAGE_TYPE)
            _hideToolbarOnScroll.value = prefs.getBoolean(PREF_HIDE_TOOLBAR_ON_SCROLL, false)
            quranSettingRepository.getTranslatesSettings().collectLatest { state ->
                when (state) {
                    is DataState.Error -> {
                        Log.e("SettingViewModel", "loadTranslatesSettings: ${state.message}")
                        _isLoading.value = false
                    }

                    DataState.Loading -> _isLoading.value = true
                    is DataState.Success -> {
                        _translates.clear()
                        _translates.addAll(state.data)
                    }
                }
            }
            for (t in translates.filter { it.id > 2 }) {
                if (t.isActive) {
                    _isKurdishEnabled.value = true
                    break
                }
                _isKurdishEnabled.value = false
            }

            _playType.value = prefs.getInt(PREF_PLAY_TYPE, DEFAULT_PLAY_TYPE)

            _quranFontName.value =
                prefs.getString(PREF_QURAN_FONT, DEFAULT_QURAN_FONT) ?: DEFAULT_QURAN_FONT
            _quranFontSize.value = prefs.getFloat(PREF_QURAN_FONT_SIZE, DEFAULT_QURAN_FONT_SIZE)

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

            val qList = qariRepository.getQariList()
            _qariList.clear()
            _qariList.addAll(qList.filterNot { it.name.startsWith("تفسیر") })
            _translatePlayList.clear()
            _translatePlayList.addAll(qList.filter { it.name.startsWith("تفسیر") })

            _selectedQari.value =
                prefs.getString(PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI) ?: DEFAULT_SELECTED_QARI
            _selectedTranslateToPlay.value =
                prefs.getString(PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY)
                    ?: DEFAULT_TRANSLATE_TO_PLAY

            _isQariLoading.value = false
            _isLoading.value = false
        }
    }

    fun updateSetting(translateSetting: TranslateSetting, isActive: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            quranSettingRepository.updateTranslateSetting(translateSetting.copy(isActive = isActive))
            val index = _translates.indexOf(translateSetting)
            _translates[index] = _translates[index].copy(isActive = isActive)
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
                _translates.filter { it.id > 2 }.forEach {
                    quranSettingRepository.updateTranslateSetting(it.copy(isActive = false))
                }
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

    fun updatePlayNextSura(play: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _playNextSura.value = play
            prefs.edit { putBoolean(PREF_PLAY_NEXT_SURA, play) }
            _isLoading.value = false
        }
    }

    fun updatePlayerSpeed(speed: Float) {
        viewModelScope.launch {
            prefs.edit { putFloat(PREF_PLAYER_SPEED, speed) }
            _playerSpeed.value = speed
            updatePlayerSpeedGlobal(speed)
        }
    }

    fun updatePageType(type: Int, reload: () -> Unit) {
        viewModelScope.launch {
            prefs.edit { putInt(PREF_PAGE_TYPE, type) }
            _pageType.value = type
            reload()
        }
    }

    fun updateHideToolbarOnScroll(hide: Boolean) {
        viewModelScope.launch {
            prefs.edit { putBoolean(PREF_HIDE_TOOLBAR_ON_SCROLL, hide) }
            _hideToolbarOnScroll.value = hide
        }
    }

}//end of class SettingViewModel
