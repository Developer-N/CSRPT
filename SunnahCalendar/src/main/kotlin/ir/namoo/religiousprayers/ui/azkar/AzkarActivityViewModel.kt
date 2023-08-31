package ir.namoo.religiousprayers.ui.azkar

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.entities.Language
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import ir.namoo.commons.DEFAULT_AZKAR_LANG
import ir.namoo.commons.PREF_AZKAR_LANG
import ir.namoo.commons.appLink
import ir.namoo.commons.downloader.DownloadResult
import ir.namoo.commons.downloader.downloadFile
import ir.namoo.religiousprayers.ui.azkar.data.AzkarChapter
import ir.namoo.religiousprayers.ui.azkar.data.AzkarItem
import ir.namoo.religiousprayers.ui.azkar.data.AzkarReference
import ir.namoo.religiousprayers.ui.azkar.data.AzkarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class AzkarActivityViewModel constructor(
    private val azkarRepository: AzkarRepository,
    private val prefs: SharedPreferences
) :
    ViewModel() {

    var description = "\uD83E\uDD32\uD83C\uDFFB"

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow("")
    val error = _error.asStateFlow()

    private val _azkarItems = MutableStateFlow(emptyList<AzkarItem>())
    val azkarItems = _azkarItems.asStateFlow()

    private val _azkarReferences = MutableStateFlow(emptyList<AzkarReference>())
    val azkarReferences = _azkarReferences.asStateFlow()

    private val _chapter = MutableStateFlow<AzkarChapter?>(null)
    val chapter = _chapter.asStateFlow()

    private val _azkarLang = MutableStateFlow(DEFAULT_AZKAR_LANG)
    val azkarLang = _azkarLang.asStateFlow()

    private val _itemsState = MutableStateFlow(emptyList<AzkarItemState>())
    val itemsState = _itemsState.asStateFlow()

    private val _lastPlay = MutableStateFlow(-1)
    val lastPlay = _lastPlay.asStateFlow()

    fun loadItems(chapterID: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _azkarLang.value =
                prefs.getString(PREF_AZKAR_LANG, DEFAULT_AZKAR_LANG) ?: DEFAULT_AZKAR_LANG
            _azkarItems.value = azkarRepository.getAzkarItem(chapterID)
            _azkarReferences.value = azkarRepository.getAzkarReferences(chapterID)
            _chapter.value = azkarRepository.getAzkarChapter(chapterID)
            setDescription()
            _itemsState.value = mutableListOf<AzkarItemState>().apply {
                for (z in azkarItems.value)
                    add(AzkarItemState())
            }
            _isLoading.value = false
        }
    }

    private fun setDescription() {
        chapter.value?.let {
            description += "${
                when (azkarLang.value) {
                    Language.FA.code -> it.persian
                    Language.CKB.code -> it.kurdish
                    else -> it.arabic
                }
            }\n\n"
        }
        for (zkr in azkarItems.value) description += "${zkr.arabic}\n\n${
            when (azkarLang.value) {
                Language.FA.code -> zkr.persian + "\n\n"
                Language.CKB.code -> zkr.kurdish + "\n\n"
                else -> ""
            }
        }" + "${
            when (azkarLang.value) {
                Language.CKB.code -> azkarReferences.value[azkarItems.value.indexOf(zkr)].kurdish
                Language.FA.code -> azkarReferences.value[azkarItems.value.indexOf(zkr)].persian
                else -> azkarReferences.value[azkarItems.value.indexOf(zkr)].arabic
            }
        }" + "\n------------------\n"
        description += appLink
    }

    fun play(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val index = azkarItems.value.indexOfFirst { it.id == id }
            if (lastPlay.value != -1) {
                _itemsState.value[lastPlay.value].isPlaying = false
            }
            _itemsState.value[index].isPlaying = true
            _lastPlay.value = index
            _isLoading.value = false
        }
    }

    fun stop(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val index = azkarItems.value.indexOfFirst { it.id == id }
            _itemsState.value[index].isPlaying = false
            _lastPlay.value = -1
            _isLoading.value = false
        }
    }

    fun downloadSound(soundName: String, file: File, id: Int) {
        viewModelScope.launch {
            val index = azkarItems.value.indexOfFirst { it.id == id }
            val url = "https://archive.org/download/azkar_n/$soundName.MP3"
            val ktor = HttpClient(Android)
            itemsState.value[index].isDownloading = true

            ktor.downloadFile(file, url).collect { downloadResult ->
                when (downloadResult) {
                    is DownloadResult.Error -> {
                        _itemsState.value[index].isDownloading = false
                        _itemsState.value[index].downloadError = downloadResult.message
                    }

                    is DownloadResult.Progress -> _itemsState.value[index].progress =
                        downloadResult.progress.toFloat()

                    DownloadResult.Success -> _itemsState.value[index].isDownloading = false
                    is DownloadResult.TotalSize -> {}
                }
            }
        }
    }
}
