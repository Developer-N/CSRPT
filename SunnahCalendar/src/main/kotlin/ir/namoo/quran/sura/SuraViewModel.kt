package ir.namoo.quran.sura

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.edit
import androidx.core.text.HtmlCompat
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.byagowi.persiancalendar.R
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import ir.namoo.commons.PREF_AUTO_SCROLL
import ir.namoo.commons.downloader.DownloadResult
import ir.namoo.commons.downloader.downloadFile
import ir.namoo.commons.repository.DataState
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.commons.utils.digitsOf
import ir.namoo.quran.TawhidDB
import ir.namoo.quran.chapters.data.ChapterEntity
import ir.namoo.quran.chapters.data.ChapterRepository
import ir.namoo.quran.db.FileDownloadEntity
import ir.namoo.quran.db.FileDownloadRepository
import ir.namoo.quran.db.LastVisitedRepository
import ir.namoo.quran.download.QuranDownloader
import ir.namoo.quran.player.QuranPlayerService
import ir.namoo.quran.player.getPlayList
import ir.namoo.quran.player.isQuranDownloaded
import ir.namoo.quran.player.isTranslateDownloaded
import ir.namoo.quran.qari.QariEntity
import ir.namoo.quran.qari.QariRepository
import ir.namoo.quran.settings.data.QuranSettingRepository
import ir.namoo.quran.sura.data.QuranEntity
import ir.namoo.quran.sura.data.QuranRepository
import ir.namoo.quran.sura.data.TafsirEntity
import ir.namoo.quran.sura.data.TranslateItem
import ir.namoo.quran.sura.data.TranslateType
import ir.namoo.quran.utils.DEFAULT_PLAY_NEXT_SURA
import ir.namoo.quran.utils.DEFAULT_SELECTED_QARI
import ir.namoo.quran.utils.DEFAULT_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.PREF_BOOKMARK_VERSE
import ir.namoo.quran.utils.PREF_FARSI_FULL_TRANSLATE
import ir.namoo.quran.utils.PREF_IS_SURA_VIEW_IS_OPEN
import ir.namoo.quran.utils.PREF_PLAY_NEXT_SURA
import ir.namoo.quran.utils.PREF_SELECTED_QARI
import ir.namoo.quran.utils.PREF_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.getAyaFileName
import ir.namoo.quran.utils.getQuranDirectoryInInternal
import ir.namoo.quran.utils.getQuranDirectoryInSD
import ir.namoo.quran.utils.getSelectedQuranDirectoryPath
import ir.namoo.quran.utils.getSuraFileName
import ir.namoo.quran.utils.getWordsForSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Thread.sleep

class SuraViewModel(
    private val chapterRepository: ChapterRepository,
    private val quranRepository: QuranRepository,
    private val quranSettingRepository: QuranSettingRepository,
    private val qariRepository: QariRepository,
    private val prefs: SharedPreferences,
    private val quranDownloader: QuranDownloader,
    private val downloadRepository: FileDownloadRepository,
    private val lastVisitedRepository: LastVisitedRepository,
    private val tawhidDB: TawhidDB
) : ViewModel() {

    private val _isSearchBarOpen = MutableStateFlow(false)
    val isSearchBarOpen = _isSearchBarOpen.asStateFlow()

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _resultIDs = mutableStateListOf<Int>()
    val resultIDs = _resultIDs

    private val _chapter = MutableStateFlow<ChapterEntity?>(null)
    val chapter = _chapter.asStateFlow()

    private val _quranList = mutableStateListOf<QuranEntity>()
    val quranList = _quranList

    private val _enabledTranslates = mutableStateListOf<TranslateItem>()
    val enabledTranslates = _enabledTranslates

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _tafsirs = mutableStateListOf<TafsirEntity>()

    private val _lastVisitedAya = MutableStateFlow(0)
    private val lastVisitedAya = _lastVisitedAya.asStateFlow()

    private val _currentSura = MutableStateFlow(1)
    val currentSura = _currentSura.asStateFlow()

    private val _scrollToTop = MutableStateFlow(0)
    val scrollToTop = _scrollToTop.asStateFlow()

    private val _autoScroll = MutableStateFlow(true)
    val autoScroll = _autoScroll.asStateFlow()

    private val chapters = mutableStateListOf<ChapterEntity>()

    private val _bookmarkedVerse = MutableStateFlow(prefs.getInt(PREF_BOOKMARK_VERSE, -1))
    val bookmarkedVerse = _bookmarkedVerse.asStateFlow()


    init {
        viewModelScope.launch {
            chapters.clear()
            chapters.addAll(chapterRepository.getAllChapters())
            updateTawheed()
        }
    }

    fun loadDate(sura: Int, scrollToTop: Boolean = true) {
        viewModelScope.launch {
            _isLoading.value = true
            _currentSura.value = sura
            _autoScroll.value = prefs.getBoolean(PREF_AUTO_SCROLL, true)
            quranSettingRepository.getTranslatesSettings().collectLatest { state ->
                when (state) {
                    is DataState.Error -> {
                        _isLoading.value = false
                        Log.e("SettingViewModel", "loadTranslatesSettings: ${state.message}")
                    }

                    DataState.Loading -> _isLoading.value = true
                    is DataState.Success -> {
                        _chapter.value = chapters.first { it.sura == sura }
                        _quranList.clear()
                        _quranList.addAll(quranRepository.getQuran(sura))
                        _tafsirs.clear()
                        _tafsirs.addAll(quranRepository.getTafsir(sura))

                        _resultIDs.clear()
                        _quranList.forEach {
                            if (!_resultIDs.contains(it.id)) _resultIDs.add(it.id)
                        }
                        val enabledTranslates = state.data.filter { it.isActive }
                        _enabledTranslates.clear()
                        _quranList.forEachIndexed { index, quran ->
                            for (translate in enabledTranslates) {
                                _enabledTranslates.add(
                                    TranslateItem(
                                        quran.verseID,
                                        translate.name,
                                        getTranslate(_tafsirs[index], translate.name),
                                        when (translate.name) {
                                            "فارسی نور(خرم دل)" -> TranslateType.FARSI
                                            "صحیح - انگلیسی" -> TranslateType.ENGLISH
                                            else -> TranslateType.KURDISH
                                        }
                                    )
                                )
                            }
                        }
                        if (scrollToTop) _scrollToTop.value++
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    private fun getTranslate(tafsir: TafsirEntity, name: String): String {
        return when (name) {
            "فارسی نور(خرم دل)" -> getKhorramdel(tafsir.khorramdel)
            "صحیح - انگلیسی" -> tafsir.sahihInternational
            "ئاسان" -> tafsir.asan
            "پوختە" -> tafsir.puxta
            "هەژار" -> tafsir.hazhar
            "ڕوشن" -> tafsir.roshn
            "تەوحید" -> HtmlCompat.fromHtml(tafsir.tawhid, HtmlCompat.FROM_HTML_MODE_COMPACT)
                .toString()

            "ڕێبەر" -> HtmlCompat.fromHtml(tafsir.rebar, HtmlCompat.FROM_HTML_MODE_COMPACT)
                .toString()

            "مویەسەر" -> HtmlCompat.fromHtml(tafsir.maisar, HtmlCompat.FROM_HTML_MODE_COMPACT)
                .toString()

            "ڕامان" -> tafsir.raman
            "ژیان" -> tafsir.zhian
            else -> tafsir.sanahi
        }
    }

    private fun getKhorramdel(text: String): String {
        var result = ""
        if (!prefs.getBoolean(PREF_FARSI_FULL_TRANSLATE, false)) {
            for (c in text) {
                if (c == ']' || c == '[') break
                result += c
            }
        } else result = text
        return result
    }

    //------------------------------------------------
    fun updateBookMark(quran: QuranEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            val index = _quranList.indexOf(quran)
            quranRepository.updateQuran(quran.copy(fav = if (quran.fav == 1) 0 else 1))
            _quranList[index] = _quranList[index].copy(fav = if (quran.fav == 1) 0 else 1)
            _isLoading.value = false
        }
    }

    fun updateBookmarkVerse(verseID: Int) {
        prefs.edit { putInt(PREF_BOOKMARK_VERSE, verseID) }
        _bookmarkedVerse.value = verseID
        Log.e("NAMOO", "updateBookmarkVerse: ====> id=$verseID ")
    }

    fun updateNote(quran: QuranEntity, newNote: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val index = _quranList.indexOf(quran)
            quranRepository.updateQuran(quran.copy(note = newNote))
            _quranList[index] = _quranList[index].copy(note = newNote)
            _isLoading.value = false
        }
    }

    fun updateAutoScroll() {
        _autoScroll.value = !_autoScroll.value
        prefs.edit { putBoolean(PREF_AUTO_SCROLL, _autoScroll.value) }
    }

    fun onQuery(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _query.value = query
            if (query.isNotEmpty() && query.isDigitsOnly()) {
                _isLoading.value = false
                return@launch
            }
            withContext(Dispatchers.IO) {
                _resultIDs.clear()
                if (query.getWordsForSearch().isEmpty()) _quranList.forEach {
                    if (!_resultIDs.contains(it.id)) _resultIDs.add(it.id)
                }
                else query.getWordsForSearch().forEach { word ->
                    _quranList.forEach {
                        if (!_resultIDs.contains(it.id) && it.quranClean.contains(word)) _resultIDs.add(
                            it.id
                        )
                    }
                    _enabledTranslates.forEach { translate ->
                        if (translate.text.contains(word)) {
                            val id = quranList.find { it.verseID == translate.verseID }?.id ?: -1
                            if (id > 0 && !_resultIDs.contains(id)) _resultIDs.add(id)
                        }
                    }
                }
            }
            _isLoading.value = false
        }
    }

    fun goToNextSura() {
        _currentSura.value++
        if (_currentSura.value > 114) _currentSura.value = 1
        loadDate(_currentSura.value)
    }

    fun goToPrevSura() {
        _currentSura.value--
        if (_currentSura.value < 1) _currentSura.value = 114
        loadDate(_currentSura.value)
    }

    // ====================================== Player
    private lateinit var mediaController: ListenableFuture<MediaController>
    lateinit var controller: MediaController
    private lateinit var folderName: String
    private lateinit var translateFolderName: String
    private val qariList = mutableStateListOf<QariEntity>()

    private val _playingAya = MutableStateFlow(0)
    val playingAya = _playingAya.asStateFlow()

    private val _playingSura = MutableStateFlow(0)
    val playingSura = _playingSura.asStateFlow()

    private val _playingSuraName = MutableStateFlow("")
    val playingSuraName = _playingSuraName.asStateFlow()

    // ================= for PlayerUI
    private val _isPlayerStoped = MutableStateFlow(true)
    val isPlayerStoped = _isPlayerStoped.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem = _currentMediaItem.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private var handler = Handler(Looper.getMainLooper())

    fun setupPlayer(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                val sessionToken = SessionToken(
                    context, ComponentName(context, QuranPlayerService::class.java)
                )
                mediaController = MediaController.Builder(context, sessionToken).buildAsync()
                mediaController.addListener({
                    controller = mediaController.get()
                    initController(context)
                    if (controller.isPlaying) {
                        controller.currentMediaItem.let { mediaItem ->
                            _currentMediaItem.value = mediaItem
                            mediaItem?.let {
                                val sura =
                                    it.mediaMetadata.title.toString().split("|")[0].digitsOf()
                                        .toInt()
                                val aya = it.mediaMetadata.title.toString().split("|")[1].digitsOf()
                                    .toInt()
                                _playingAya.value = aya
                                _playingSura.value = sura
                                _isPlaying.value = true
                                _playingSuraName.value =
                                    chapters.find { chapter -> chapter.sura == sura }?.nameArabic
                                        ?: ""
                                _isPlayerStoped.value = false
                            }
                        }
                    }
                }, MoreExecutors.directExecutor())

                loadFolders(context, getAyaFileName(1, 1))
                qariList.clear()
                qariList.addAll(qariRepository.getQariList())
            }.onFailure { }
            _isLoading.value = false
        }
    }

    private suspend fun loadFolders(context: Context, fileName: String) =
        withContext(Dispatchers.IO) {
            folderName = if (File(
                    getQuranDirectoryInInternal(context) + "/" + context.appPrefsLite.getString(
                        PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
                    ) + if (fileName.isNotEmpty()) "/$fileName" else ""
                ).exists()
            ) getQuranDirectoryInInternal(context) + "/" + context.appPrefsLite.getString(
                PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
            )
            else getQuranDirectoryInSD(context) + "/" + context.appPrefsLite.getString(
                PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
            )

            translateFolderName = if (File(
                    getQuranDirectoryInInternal(context) + "/" + context.appPrefsLite.getString(
                        PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
                    ) + if (fileName.isNotEmpty()) "/$fileName" else ""
                ).exists()
            ) getQuranDirectoryInInternal(context) + "/" + context.appPrefsLite.getString(
                PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
            )
            else getQuranDirectoryInSD(context) + "/" + context.appPrefsLite.getString(
                PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
            )
        }

    fun onPlay(context: Context, sura: Int, aya: Int) {
        viewModelScope.launch {
            loadFolders(context, getAyaFileName(sura, 2))
            if (playingAya.value == aya && playingSura.value == sura) {
                stop()
                return@launch
            }
            _isPlaying.value = true
            _isPlayerStoped.value = false
            if (controller.isPlaying) controller.stop()
            if (controller.mediaItemCount > 0) controller.clearMediaItems()
            controller.addMediaItems(
                getPlayList(
                    context,
                    sura,
                    aya,
                    folderName,
                    translateFolderName,
                    qariList,
                    chapters.find { it.sura == sura })
            )
            controller.prepare()
            controller.play()
            startTimer()
            _playingSura.value = sura
            _playingSuraName.value = chapters.find { it.sura == sura }?.nameArabic ?: ""
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                handler.post {
                    _currentPosition.value = controller.currentPosition
                }
                delay(100)
            }
        }
    }

    fun pause() {
        controller.pause()
        _isPlaying.value = false
    }

    fun resume() {
        controller.play()
        startTimer()
        _isPlaying.value = true
    }

    fun stop() {
        controller.stop()
        _playingAya.value = 0
        _isPlayerStoped.value = true
    }

    fun seekTo(position: Long) {
        _currentPosition.value = position
        controller.seekTo(position)
    }

    fun next() {
        controller.seekToNext()
    }

    fun previous() {
        controller.seekToPrevious()
    }

    private fun initController(context: Context) {
        controller.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                _currentMediaItem.value = mediaItem
                mediaItem?.let {
                    val sura = it.mediaMetadata.title.toString().split("|")[0].digitsOf().toInt()
                    val aya = it.mediaMetadata.title.toString().split("|")[1].digitsOf().toInt()
                    _playingAya.value = aya
                    _playingSura.value = sura
                    _isPlaying.value = true
                    _playingSuraName.value =
                        chapters.find { chapter -> chapter.sura == sura }?.nameArabic ?: ""
                    _isPlayerStoped.value = false
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                viewModelScope.launch {
                    if (playbackState == Player.STATE_ENDED) {
                        _playingAya.value = 0
                        if (prefs.getBoolean(PREF_PLAY_NEXT_SURA, DEFAULT_PLAY_NEXT_SURA)) {
                            if (_playingSura.value == currentSura.value && !_isSearchBarOpen.value) {
                                goToNextSura()
                            }
                            val nextSura =
                                if (_playingSura.value == 114) 1 else (_playingSura.value + 1)
                            if (!isQuranDownloaded(context, nextSura)) Toast.makeText(
                                context,
                                context.getString(R.string.audio_files_error),
                                Toast.LENGTH_LONG
                            ).show()
                            else if (!isTranslateDownloaded(context, nextSura)) Toast.makeText(
                                context,
                                context.getString(R.string.audio_translate_files_error),
                                Toast.LENGTH_LONG
                            ).show()
                            else withContext(Dispatchers.IO) {
                                sleep(3000)
                                onPlay(context, nextSura, 1)
                            }
                        } else {
                            controller.stop()
                            _isPlaying.value = false
                            _isPlayerStoped.value = true
                        }
                    }
                }
            }

            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                _duration.value = player.duration
            }
        })
    }

    fun downloadQuranFiles(context: Context) {
        viewModelScope.launch {
            val qari = qariList.find { folderName.contains(it.folderName) } ?: return@launch
            downloadAudioFiles(context, qari)
        }
    }

    fun downloadTranslateFiles(context: Context) {
        viewModelScope.launch {
            val qari =
                qariList.find { translateFolderName.contains(it.folderName) } ?: return@launch
            downloadAudioFiles(context, qari)
        }
    }

    private fun downloadAudioFiles(context: Context, qari: QariEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val sura = chapter.value?.sura ?: return@launch
            val oldRequest = downloadRepository.getAllDownloads()
                .filter { (it.sura == sura && it.folderPath.contains(qari.folderName)) }
            oldRequest.forEach {
                quranDownloader.cancelDownload(it.downloadRequest)
                downloadRepository.delete(it.id)
            }
            val destFile =
                getSelectedQuranDirectoryPath(context) + File.separator + qari.folderName + File.separator + getSuraFileName(
                    sura
                )

            runCatching { if (File(destFile).exists()) File(destFile).delete() }
            val url = qari.suraZipsBaseLink + getSuraFileName(sura)
            val id = quranDownloader.downloadFile(
                url = url,
                destination = File(destFile),
                chapterName = chapter.value?.nameArabic ?: "-",
                qari = qari.name
            )
            val downloadEntity = FileDownloadEntity(
                downloadRequest = id,
                downloadFile = url,
                folderPath = destFile,
                sura = sura,
                qariId = qari.id
            )
            downloadRepository.insert(downloadEntity)
        }
    }

    override fun onCleared() {
        MediaController.releaseFuture(mediaController)
        super.onCleared()
    }

    fun updateLastVisited(aya: Int) {
        viewModelScope.launch {
            _lastVisitedAya.value = aya
        }
    }

    fun saveLastVisited() {
        viewModelScope.launch(Dispatchers.IO) {
            if (lastVisitedAya.value > 0) {
                val lastVisitedList = lastVisitedRepository.getAllLastVisited().sortedBy { it.id }
                if (lastVisitedList.size >= 20) lastVisitedRepository.delete(lastVisitedList.first())
                chapter.value?.let { chapter ->
                    if (lastVisitedList.find { (it.suraID == chapter.sura && it.ayaID == lastVisitedAya.value) } == null) lastVisitedRepository.insert(
                        lastVisitedAya.value, chapter.sura
                    )
                }
            }
        }
    }

    fun updateSureViewSate(isOpen: Boolean) {
        prefs.edit {
            putBoolean(PREF_IS_SURA_VIEW_IS_OPEN, isOpen)
        }
    }

    fun openSearchBar() {
        _isSearchBarOpen.value = true
    }

    fun closeSearchBar() {
        _isSearchBarOpen.value = false
    }

    @SuppressLint("SdCardPath")
    private fun updateTawheed() {
        //Update Tawhid tafseer if need, can remove in future.
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val tawhidList = quranRepository.getTafsir(112)
                //if is fresh return
                if (tawhidList[2].tawhid == "(٣) نە كەسى لێ بووە و نە لە كەسیش بووە.") return@launch
                val packageName = "ir.namoo.religiousprayers"
                val dbFile = File("/data/data/$packageName/databases/tafseeri_tawhid.db")
                if (dbFile.exists()) dbFile.delete()
                val ktor = HttpClient(Android)
                ktor.downloadFile(
                    dbFile,
                    "https://github.com/Developer-N/QuranProject/raw/refs/heads/main/AndroidDB/tafseeri_tawhid.db"
                ).collectLatest { status ->
                    when (status) {
                        is DownloadResult.Error -> {}
                        is DownloadResult.Progress -> {}
                        DownloadResult.Success -> {
                            val newTawhid = tawhidDB.tawhidDAO().getAll()
                            quranRepository.getTafsir().forEachIndexed { index, oldItem ->
                                quranRepository.updateTafsir(
                                    oldItem.copy(
                                        tawhid = newTawhid[index].text
                                    )
                                )
                            }
                            dbFile.deleteOnExit()
                        }

                        is DownloadResult.TotalSize -> {}
                    }
                }
            }.onFailure {}
        }
    }
}//end of class SuraViewModel
