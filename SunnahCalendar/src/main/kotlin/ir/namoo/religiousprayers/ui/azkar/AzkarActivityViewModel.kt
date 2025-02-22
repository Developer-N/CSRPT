package ir.namoo.religiousprayers.ui.azkar

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.utils.logException
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import ir.namoo.commons.APP_LINK
import ir.namoo.commons.DEFAULT_AZKAR_LANG
import ir.namoo.commons.PREF_AZKAR_LANG
import ir.namoo.commons.downloader.DownloadResult
import ir.namoo.commons.downloader.downloadFile
import ir.namoo.religiousprayers.ui.azkar.data.AzkarChapter
import ir.namoo.religiousprayers.ui.azkar.data.AzkarItem
import ir.namoo.religiousprayers.ui.azkar.data.AzkarReference
import ir.namoo.religiousprayers.ui.azkar.data.AzkarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class AzkarActivityViewModel(
    private val azkarRepository: AzkarRepository, private val prefs: SharedPreferences
) : ViewModel() {

    var description = "\uD83E\uDD32\uD83C\uDFFB"

    private var azkarDirectory: String? = null

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow("")
    val error = _error.asStateFlow()

    private val _azkarItems = mutableStateListOf<AzkarItem>()
    val azkarItems = _azkarItems

    private val _azkarReferences = mutableStateListOf<AzkarReference>()
    val azkarReferences = _azkarReferences

    private val _chapter = MutableStateFlow<AzkarChapter?>(null)
    val chapter = _chapter.asStateFlow()

    private val _azkarLang = MutableStateFlow(DEFAULT_AZKAR_LANG)
    val azkarLang = _azkarLang.asStateFlow()

    private val _itemsState = mutableStateListOf<AzkarItemState>()
    val itemsState = _itemsState

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPlayingItem = MutableStateFlow(-1)
    val currentPlayingItem = _currentPlayingItem.asStateFlow()

    private val _totalDuration = MutableStateFlow(0)
    val totalDuration = _totalDuration.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition = _currentPosition.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var handler = Handler(Looper.getMainLooper())

    fun loadItems(chapterID: Int, directory: String) {
        viewModelScope.launch {
            _isLoading.value = true
            azkarDirectory = directory
            _azkarLang.value =
                prefs.getString(PREF_AZKAR_LANG, DEFAULT_AZKAR_LANG) ?: DEFAULT_AZKAR_LANG
            _azkarItems.clear()
            _azkarItems.addAll(azkarRepository.getAzkarItem(chapterID))
            _azkarReferences.clear()
            _azkarReferences.addAll(azkarRepository.getAzkarReferences(chapterID))
            _chapter.value = azkarRepository.getAzkarChapter(chapterID)
            setDescription()
            _itemsState.clear()
            azkarItems.map { item ->
                _itemsState.add(
                    AzkarItemState().copy(
                        isFileExist = File(directory + getIdString(item.id) + ".mp3").exists()
                    )
                )
            }
        }
        _isLoading.value = false
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
        for (zkr in azkarItems) description += "${zkr.arabic}\n\n${
            when (azkarLang.value) {
                Language.FA.code -> zkr.persian + "\n\n"
                Language.CKB.code -> zkr.kurdish + "\n\n"
                else -> ""
            }
        }" + "${
            when (azkarLang.value) {
                Language.CKB.code -> azkarReferences[azkarItems.indexOf(zkr)].kurdish
                Language.FA.code -> azkarReferences[azkarItems.indexOf(zkr)].persian
                else -> azkarReferences[azkarItems.indexOf(zkr)].arabic
            }
        }" + "\n------------------\n"
        description += APP_LINK
    }

    fun play(context: Context, item: AzkarItem) {
        runCatching {

            if (mediaPlayer?.isPlaying == true && currentPlayingItem.value == item.id) {
                stop()
            } else {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.stop()
                    _isPlaying.value = false
                }
                val mp3File = File(azkarDirectory + getIdString(item.id) + ".mp3")
                if (!mp3File.exists()) return
                _currentPlayingItem.value = -1
                _currentPosition.value = 0
                mediaPlayer = MediaPlayer()
                mediaPlayer?.let { player ->
                    player.setDataSource(context, Uri.fromFile(mp3File))
                    player.prepare()
                    player.setOnPreparedListener {
                        player.start()
                        startTimer()
                        _currentPlayingItem.value = item.id
                        _isPlaying.value = true
                        _totalDuration.value = player.duration
                    }
                    player.setOnCompletionListener {
                        _isPlaying.value = false
                        _currentPlayingItem.value = -1
                        _currentPosition.value = 0
                        _totalDuration.value = 0
                        mediaPlayer?.release()
                        mediaPlayer = null
                    }
                }
            }
        }.onFailure(logException)
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (mediaPlayer?.isPlaying == true) {
                handler.post {
                    mediaPlayer?.let { player ->
                        if (player.isPlaying) _currentPosition.value = player.currentPosition
                    }
                }
                delay(100)
            }
        }
    }

    fun stop() {
        runCatching {
            _currentPlayingItem.value = -1
            _isPlaying.value = false
            if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }.onFailure(logException)
    }

    fun pause() {
        mediaPlayer?.pause()
        _isPlaying.value = false
    }

    fun resume() {
        mediaPlayer?.start()
        startTimer()
        _isPlaying.value = true
    }

    fun seekTo(position: Float) {
        mediaPlayer?.seekTo(position.toInt())
        _currentPosition.value = position.toInt()
    }

    fun addReadCount(item: AzkarItem) {
        val index = azkarItems.indexOf(item)
        _itemsState[index] = _itemsState[index].copy(readCount = _itemsState[index].readCount + 1)
    }

    fun resetReadCount(item: AzkarItem) {
        val index = azkarItems.indexOf(item)
        _itemsState[index] = _itemsState[index].copy(readCount = 0)
    }

    fun delete(item: AzkarItem) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val file = File(azkarDirectory + item.sound + ".mp3")
                file.delete()
                val index = azkarItems.indexOf(item)
                _itemsState[index] = _itemsState[index].copy(isFileExist = false)
            }
        }
    }

    fun downloadSound(item: AzkarItem) {
        viewModelScope.launch {
            val itemId = getIdString(item.id)
            val url =
                "https://raw.githubusercontent.com/Developer-N/Azkar/main/azkar-audio/$itemId.mp3"
            Log.e("AzkarActivityViewModel", "downloadSound: $url")
            val ktor = HttpClient(Android)
            val index = azkarItems.indexOf(item)
            _itemsState[index] = _itemsState[index].copy(isDownloading = true)

            ktor.downloadFile(File(azkarDirectory + getIdString(item.id) + ".mp3"), url)
                .collect { downloadResult ->
                    when (downloadResult) {
                        is DownloadResult.Error -> {
                            _itemsState[index] = _itemsState[index].copy(
                                isDownloading = false, downloadError = downloadResult.message
                            )
                        }

                        is DownloadResult.Progress -> _itemsState[index] = _itemsState[index].copy(
                            progress = downloadResult.progress.toFloat()
                        )

                        DownloadResult.Success -> {
                            _itemsState[index] =
                                _itemsState[index].copy(isDownloading = false, isFileExist = true)
                        }

                        is DownloadResult.TotalSize -> {
                            _itemsState[index] =
                                _itemsState[index].copy(totalSize = downloadResult.totalSize)
                        }
                    }
                }
        }
    }

    private fun getIdString(id: Int): String {
        return if (id < 10) "00${id}"
        else if (id < 100) "0${id}"
        else id.toString()
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}
