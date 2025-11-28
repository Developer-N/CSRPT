package ir.namoo.quran.mushaf

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.edit
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.byagowi.persiancalendar.R
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.commons.utils.digitsOf
import ir.namoo.quran.chapters.data.ChapterEntity
import ir.namoo.quran.chapters.data.ChapterRepository
import ir.namoo.quran.db.FileDownloadEntity
import ir.namoo.quran.db.FileDownloadRepository
import ir.namoo.quran.db.LastVisitedRepository
import ir.namoo.quran.db.QCFChapters
import ir.namoo.quran.download.QuranDownloader
import ir.namoo.quran.player.QuranPlayerService
import ir.namoo.quran.player.getPlayList
import ir.namoo.quran.player.isQuranDownloaded
import ir.namoo.quran.player.isTranslateDownloaded
import ir.namoo.quran.qari.QariEntity
import ir.namoo.quran.qari.QariRepository
import ir.namoo.quran.settings.data.QuranSettingRepository
import ir.namoo.quran.sura.data.QuranRepository
import ir.namoo.quran.sura.data.TafsirEntity
import ir.namoo.quran.sura.data.TranslateItem
import ir.namoo.quran.sura.data.TranslateType
import ir.namoo.quran.utils.DEFAULT_MUSHAF_TEXT_SIZE
import ir.namoo.quran.utils.DEFAULT_PLAY_NEXT_SURA
import ir.namoo.quran.utils.DEFAULT_SELECTED_QARI
import ir.namoo.quran.utils.DEFAULT_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.PREF_BOOKMARK_VERSE
import ir.namoo.quran.utils.PREF_FARSI_FULL_TRANSLATE
import ir.namoo.quran.utils.PREF_HIDE_TOOLBAR_ON_SCROLL
import ir.namoo.quran.utils.PREF_IS_SURA_VIEW_IS_OPEN
import ir.namoo.quran.utils.PREF_MUSHAF_TEXT_SIZE
import ir.namoo.quran.utils.PREF_PLAY_NEXT_SURA
import ir.namoo.quran.utils.PREF_SELECTED_QARI
import ir.namoo.quran.utils.PREF_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.getAyaFileName
import ir.namoo.quran.utils.getMushafFolder
import ir.namoo.quran.utils.getQuranDirectoryInInternal
import ir.namoo.quran.utils.getQuranDirectoryInSD
import ir.namoo.quran.utils.getSelectedQuranDirectoryPath
import ir.namoo.quran.utils.getSuraFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MushafViewModel(
    private val chapterRepository: ChapterRepository,
    private val quranRepository: QuranRepository,
    private val quranSettingRepository: QuranSettingRepository,
    private val qariRepository: QariRepository,
    private val prefs: SharedPreferences,
    private val quranDownloader: QuranDownloader,
    private val downloadRepository: FileDownloadRepository,
    private val lastVisitedRepository: LastVisitedRepository
) : ViewModel() {

    val pagesList = 1..604
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _hideTopBar = MutableStateFlow(false)
    val hideTopBar = _hideTopBar.asStateFlow()


    private val _currentPage = MutableStateFlow(1)
    val currentPage = _currentPage.asStateFlow()

    private val _error = MutableStateFlow("")
    val error = _error.asStateFlow()

    private val _suraInfo = MutableStateFlow(1 to "")
    val suraInfo = _suraInfo.asStateFlow()

    private val _pages = mutableStateListOf<PageState>()
    val pages = _pages

    private val chapters = mutableStateListOf<QCFChapters>()
    private val normalChapters = mutableStateListOf<ChapterEntity>()


    private val _isPaginatingUp = MutableStateFlow(false)
    val isPaginatingUp = _isPaginatingUp.asStateFlow()

    private val _isPaginatingDown = MutableStateFlow(false)
    val isPaginatingDown = _isPaginatingDown.asStateFlow()

    private val _fontSize = MutableStateFlow(DEFAULT_MUSHAF_TEXT_SIZE)
    val fontSize = _fontSize.asStateFlow()

    private val _bookmarkedVerse = MutableStateFlow(prefs.getInt(PREF_BOOKMARK_VERSE, -1))
    val bookmarkedVerse = _bookmarkedVerse.asStateFlow()

    private var saveSettingsJob: Job? = null

    private val _isFontsDownloaded = MutableStateFlow(false)
    val isFontsDownloaded = _isFontsDownloaded.asStateFlow()

    private var lastVisitedPage = 0

    fun init(context: Context) {
        viewModelScope.launch {
            val fontsFolder = File(getMushafFolder(context) + "/QCF2BSMLfonts")
            _isFontsDownloaded.value = fontsFolder.exists() && fontsFolder.listFiles()?.size == 607
            _fontSize.value = prefs.getFloat(PREF_MUSHAF_TEXT_SIZE, DEFAULT_MUSHAF_TEXT_SIZE)
            chapters.clear()
            chapters.addAll(chapterRepository.getQCFChapters())
            normalChapters.clear()
            normalChapters.addAll(chapterRepository.getAllChapters())
        }
    }


    fun loadPage(sura: Int, aya: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _hideTopBar.value = prefs.getBoolean(PREF_HIDE_TOOLBAR_ON_SCROLL, false)
            val quran = quranRepository.getQuran(sura, aya) ?: return@launch
            val chapter = chapters.find { it.id == quran.surahID }
            _suraInfo.value = (quran.surahID) to (chapter?.text ?: "")
            _pages.clear()
            val currentPage = quran.page
            if (currentPage in pagesList) {
                val page = getPageState(currentPage)
                if (!_pages.contains(page))
                    _pages.add(page)
            }
            _currentPage.value = currentPage

            if (currentPage > 1) {
                val page = getPageState(currentPage - 1)
                if (!_pages.contains(page))
                    _pages.add(0, page)
            }
            if (currentPage < 604) {
                val page = getPageState(currentPage + 1)
                if (!_pages.contains(page))
                    _pages.add(page)
            }

            _isLoading.value = false
        }
    }

    fun loadNextPages() {
        if (_isPaginatingDown.value) return
        viewModelScope.launch {
            _isPaginatingDown.value = true
            val lastPage = _pages.lastOrNull()?.page ?: run {
                _isPaginatingDown.value = false
                return@launch
            }
            if (lastPage < pagesList.last) {
                val nextPage = lastPage + 1
                for (page in nextPage..nextPage + 2) {
                    if (page in pagesList && _pages.none { it.page == page }) {
                        val p = getPageState(page)
                        if (!_pages.contains(p))
                            _pages.add(p)
                    }
                }
            }
            _isPaginatingDown.value = false
        }
    }

    fun loadPreviousPages() {
        if (_isPaginatingUp.value) return
        viewModelScope.launch {
            _isPaginatingUp.value = true
            val firstPage = _pages.firstOrNull()?.page ?: run {
                _isPaginatingUp.value = false
                return@launch
            }
            if (firstPage > pagesList.first) {
                val pagesToLoad = mutableListOf<PageState>()
                val startPage = (firstPage - 1)
                val endPage = (firstPage - 3).coerceAtLeast(pagesList.first)

                for (page in startPage downTo endPage) {
                    if (page in pagesList && _pages.none { it.page == page }) {
                        pagesToLoad.add(getPageState(page))
                    }
                }
                if (pagesToLoad.isNotEmpty()) {
                    pagesToLoad.reversed().forEach { p ->
                        if (!_pages.contains(p))
                            _pages.add(0, p)
                    }
                }
            }
            _isPaginatingUp.value = false
        }
    }

    private suspend fun getPageState(page: Int): PageState {
        val verses = mutableListOf<Verse>()
        val pageVerses = quranRepository.getQuranPage(page)
        val pageQuran = quranRepository.getQuranPage(page)
        val activeTranslates = quranSettingRepository.getActiveTranslatesSettings()
        val tafsirs = quranRepository.getTafsirPage(page)
        pageVerses.zip(pageQuran).forEachIndexed { index, (qcfVerse, normalVerse) ->
            val words = quranRepository.getQPCVerses(qcfVerse.surahID, qcfVerse.verseID)
            val verseText = StringBuilder()
            words.forEach { word -> verseText.append("${word.text} ") }
            val translates = mutableListOf<TranslateItem>()
            for (translate in activeTranslates) {
                translates.add(
                    TranslateItem(
                        normalVerse.verseID,
                        translate.name,
                        getTranslate(tafsirs[index], translate.name),
                        when (translate.name) {
                            "فارسی نور(خرم دل)" -> TranslateType.FARSI
                            "صحیح - انگلیسی" -> TranslateType.ENGLISH
                            else -> TranslateType.KURDISH
                        }
                    )
                )
            }
            verses.add(
                Verse(
                    id = qcfVerse.id,
                    sura = qcfVerse.surahID,
                    verseNumber = qcfVerse.verseID,
                    verseQCFText = verseText.toString(),
                    verseNormalText = normalVerse.quranArabic,
                    verseCleanText = normalVerse.quranClean,
                    note = normalVerse.note ?: "",
                    fav = normalVerse.fav,
                    translates = translates
                )
            )
        }
        return PageState(
            page = page, verses = verses
        )
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

    fun updateSuraName(sura: Int) {
        if (_suraInfo.value.first != sura) {
            val name = chapters.find { it.id == sura }?.text ?: "-"
            _suraInfo.value = sura to name
        }
    }

    fun getSuraName(sura: Int): String {
        return chapters.find { it.id == sura }?.text ?: "-"
    }

    fun getNormalSuraName(sura: Int): String {
        return normalChapters.find { it.sura == sura }?.nameArabic ?: "-"
    }

    fun changeFontSize(zoomChange: Float) {
        val current = _fontSize.value
        val newSize = (current * zoomChange).coerceIn(14f, 60f)
        _fontSize.value = newSize
        saveSettingsJob?.cancel()
        saveSettingsJob = viewModelScope.launch {
            delay(1000)
            prefs.edit { putFloat(PREF_MUSHAF_TEXT_SIZE, newSize) }
        }
    }

    fun addFontSize() {
        val current = _fontSize.value
        val newSize = (current + 1).coerceIn(14f, 60f)
        _fontSize.value = newSize
        saveSettingsJob?.cancel()
        saveSettingsJob = viewModelScope.launch {
            delay(1000)
            prefs.edit { putFloat(PREF_MUSHAF_TEXT_SIZE, newSize) }
        }
    }

    fun minusFontSize() {
        val current = _fontSize.value
        val newSize = (current - 1).coerceIn(14f, 60f)
        _fontSize.value = newSize
        saveSettingsJob?.cancel()
        saveSettingsJob = viewModelScope.launch {
            delay(1000)
            prefs.edit { putFloat(PREF_MUSHAF_TEXT_SIZE, newSize) }
        }
    }

    fun toggleBookmark(verse: Verse) {
        viewModelScope.launch {
            val quranEntity = quranRepository.getVerse(verse.id) ?: return@launch
            quranRepository.updateQuran(quranEntity.copy(fav = if (quranEntity.fav == 1) 0 else 1))
            val index =
                _pages.indexOf(_pages.find { it.verses.find { v -> v.id == verse.id } != null })
            if (index != -1) {
                val verseList = mutableListOf<Verse>()
                for (v in _pages[index].verses)
                    if (v.id == verse.id)
                        verseList.add(v.copy(fav = if (v.fav == 1) 0 else 1))
                    else verseList.add(v)
                _pages[index] = _pages[index].copy(verses = verseList)
            }
        }
    }

    fun updateVerseNote(verse: Verse, note: String) {
        viewModelScope.launch {
            val quranEntity = quranRepository.getVerse(verse.id) ?: return@launch
            quranRepository.updateQuran(quranEntity.copy(note = note))
            val index =
                _pages.indexOf(_pages.find { it.verses.find { v -> v.id == verse.id } != null })
            if (index != -1) {
                val verseList = mutableListOf<Verse>()
                for (v in _pages[index].verses)
                    if (v.id == verse.id)
                        verseList.add(v.copy(note = note))
                    else verseList.add(v)
                _pages[index] = _pages[index].copy(verses = verseList)
            }
        }
    }

    fun updateBookmarkVerse(verseID: Int) {
        prefs.edit { putInt(PREF_BOOKMARK_VERSE, verseID) }
        _bookmarkedVerse.value = verseID
    }

    fun updateLastVisitedPage(page: Int) {
        lastVisitedPage = page
    }

    fun saveLastVisitedPage() {
        viewModelScope.launch(Dispatchers.IO) {
            if (lastVisitedPage > 0) {
                val lastVisitedPages = lastVisitedRepository.getAllVisitedPages().sortedBy { it.id }
                if (lastVisitedPages.find { it.page == lastVisitedPage } == null) {
                    lastVisitedRepository.insertPage(lastVisitedPage)
                    if (lastVisitedPages.size >= 20) lastVisitedRepository.delete(lastVisitedPages.first())
                }
            }
        }
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
                                    normalChapters.find { chapter -> chapter.sura == sura }?.nameArabic
                                        ?: ""
                                _isPlayerStoped.value = false
                            }
                        }
                        startTimer()
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
                    normalChapters.find { it.sura == sura })
            )
            controller.prepare()
            controller.play()
            startTimer()
            _playingSura.value = sura
            _playingSuraName.value = normalChapters.find { it.sura == sura }?.nameArabic ?: ""
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
                        normalChapters.find { chapter -> chapter.sura == sura }?.nameArabic ?: ""
                    _isPlayerStoped.value = false
                    //scroll to playing page
                    viewModelScope.launch {
                        quranRepository.getQuran(sura, aya)?.let { verse ->
                            val page = verse.page
                            if (pages.none { p -> p.page == page }) {
                                loadPage(sura, aya)
                            } else {
                                if (_currentPage.value != page) {
                                    _currentPage.value = page
                                }
                            }
                        }
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                viewModelScope.launch {
                    if (playbackState == Player.STATE_ENDED) {
                        _playingAya.value = 0
                        if (prefs.getBoolean(PREF_PLAY_NEXT_SURA, DEFAULT_PLAY_NEXT_SURA)) {
                            val nextSura =
                                if (_playingSura.value == 114) 1 else (_playingSura.value + 1)
                            if (!isQuranDownloaded(context, nextSura)) {
                                controller.stop()
                                _isPlaying.value = false
                                _isPlayerStoped.value = true
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.audio_files_error),
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (!isTranslateDownloaded(context, nextSura)) {
                                controller.stop()
                                _isPlaying.value = false
                                _isPlayerStoped.value = true
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.audio_translate_files_error),
                                    Toast.LENGTH_LONG
                                ).show()
                            } else withContext(Dispatchers.IO) {
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

    fun downloadQuranFiles(context: Context, sura: Int) {
        viewModelScope.launch {
            val qari = qariList.find { folderName.contains(it.folderName) } ?: return@launch
            downloadAudioFiles(context, qari, sura)
        }
    }

    fun downloadTranslateFiles(context: Context, sura: Int) {
        viewModelScope.launch {
            val qari =
                qariList.find { translateFolderName.contains(it.folderName) } ?: return@launch
            downloadAudioFiles(context, qari, sura)
        }
    }

    private fun downloadAudioFiles(context: Context, qari: QariEntity, sura: Int) {
        viewModelScope.launch(Dispatchers.IO) {
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
                chapterName = normalChapters.find { it.sura == sura }?.nameArabic ?: "-",
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

    fun updateSureViewSate(isOpen: Boolean) {
        prefs.edit {
            putBoolean(PREF_IS_SURA_VIEW_IS_OPEN, isOpen)
        }
    }

}
