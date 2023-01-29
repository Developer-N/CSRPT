package ir.namoo.religiousprayers.ui.azkar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.utils.logException
import io.ktor.client.*
import io.ktor.client.engine.android.*
import ir.namoo.commons.DEFAULT_AZKAR_LANG
import ir.namoo.commons.appLink
import ir.namoo.commons.downloader.DownloadResult
import ir.namoo.commons.downloader.downloadFile
import ir.namoo.commons.repository.DataState
import ir.namoo.commons.repository.asDataState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class AzkarActivityViewModel constructor(private val azkarRepository: AzkarRepository) :
    ViewModel() {

    var description by mutableStateOf(" \uD83E\uDD32\uD83C\uDFFB")
        private set
    var isLoading by mutableStateOf(true)
        private set
    var error by mutableStateOf("")

    var azkarItems by mutableStateOf(listOf<AzkarItem>())
        private set
    var azkarReferences by mutableStateOf(listOf<AzkarReference>())
        private set
    private var getItems: Job? = null

    var chapter by mutableStateOf<AzkarChapter?>(null)
        private set
    var azkarLang by mutableStateOf(DEFAULT_AZKAR_LANG)
        private set
    var itemsState by mutableStateOf(listOf<AzkarItemState>())
        private set

    var lastPlay by mutableStateOf(-1)
        private set

    fun loadItems(chapterID: Int) {
        runCatching {
            getItems?.cancel()
            getItems = viewModelScope.launch {
                azkarRepository.getAzkarItem(chapterID).collect { azkarItemList ->
                    when (azkarItemList.asDataState()) {
                        is DataState.Error -> {
                            isLoading = false
                            error = (azkarItemList.asDataState() as DataState.Error).message
                        }
                        DataState.Loading -> {
                            isLoading = true
                        }
                        is DataState.Success -> {
                            azkarItems =
                                (azkarItemList.asDataState() as DataState.Success<List<AzkarItem>>).data
                            azkarReferences = azkarRepository.getAzkarReferences(chapterID)
                            chapter = azkarRepository.getAzkarChapter(chapterID)
                            setDescription()
                            itemsState = mutableListOf<AzkarItemState>().apply {
                                for (z in azkarItems)
                                    add(AzkarItemState())
                            }
                            isLoading = false
                        }
                    }
                }
            }
        }.onFailure { logException }
    }

    private fun setDescription() {
        chapter?.let {
            description += "${
                when (azkarLang) {
                    Language.FA.code -> it.persian
                    Language.CKB.code -> it.kurdish
                    else -> it.arabic
                }
            }\n\n"
        }
        for (zkr in azkarItems) description += "${zkr.arabic}\n\n${
            when (azkarLang) {
                Language.FA.code -> zkr.persian + "\n\n"
                Language.CKB.code -> zkr.kurdish + "\n\n"
                else -> ""
            }
        }" + "${
            when (azkarLang) {
                Language.CKB.code -> azkarReferences[azkarItems.indexOf(zkr)].kurdish
                Language.FA.code -> azkarReferences[azkarItems.indexOf(zkr)].persian
                else -> azkarReferences[azkarItems.indexOf(zkr)].arabic
            }
        }" + "\n------------------\n"
        description += appLink
    }

    fun setLang(lang: String) {
        azkarLang = lang
    }

    fun play(id: Int) {
        val index = azkarItems.indexOfFirst { it.id == id }
        if (lastPlay != -1) {
            itemsState[lastPlay].isPlaying = false
        }
        itemsState[index].isPlaying = true
        lastPlay = index
    }

    fun stop(id: Int) {
        val index = azkarItems.indexOfFirst { it.id == id }
        itemsState[index].isPlaying = false
        lastPlay = -1
    }

    fun downloadSound(soundName: String, file: File, id: Int) {
        val index = azkarItems.indexOfFirst { it.id == id }

        val url = "https://archive.org/download/azkar_n/$soundName.MP3"
        val ktor = HttpClient(Android)
        itemsState[index].isDownloading = true
        viewModelScope.launch {
            ktor.downloadFile(file, url).collect { downloadResult ->
                when (downloadResult) {
                    is DownloadResult.Error -> {
                        itemsState[index].isDownloading = false
                        itemsState[index].downloadError = downloadResult.message
                    }
                    is DownloadResult.Progress -> itemsState[index].progress =
                        downloadResult.progress.toFloat()
                    DownloadResult.Success -> itemsState[index].isDownloading = false
                }
            }
        }
    }
}
