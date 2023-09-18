package ir.namoo.quran.sura

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.commons.utils.digitsOf
import ir.namoo.quran.chapters.SearchBarState
import ir.namoo.quran.chapters.data.ChapterEntity
import ir.namoo.quran.db.FileDownloadEntity
import ir.namoo.quran.db.FileDownloadRepository
import ir.namoo.quran.db.LastVisitedRepository
import ir.namoo.quran.download.QuranDownloader
import ir.namoo.quran.player.PlayerService
import ir.namoo.quran.qari.QariEntity
import ir.namoo.quran.qari.QariRepository
import ir.namoo.quran.qari.getQariLocalPhotoFile
import ir.namoo.quran.settings.data.QuranSettingRepository
import ir.namoo.quran.sura.data.QuranEntity
import ir.namoo.quran.sura.data.QuranRepository
import ir.namoo.quran.sura.data.TafsirEntity
import ir.namoo.quran.sura.data.TranslateItem
import ir.namoo.quran.sura.data.TranslateType
import ir.namoo.quran.utils.DEFAULT_PLAY_TYPE
import ir.namoo.quran.utils.DEFAULT_SELECTED_QARI
import ir.namoo.quran.utils.DEFAULT_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.PREF_FARSI_FULL_TRANSLATE
import ir.namoo.quran.utils.PREF_PLAY_TYPE
import ir.namoo.quran.utils.PREF_SELECTED_QARI
import ir.namoo.quran.utils.PREF_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.getAyaFileName
import ir.namoo.quran.utils.getQuranDirectoryInInternal
import ir.namoo.quran.utils.getQuranDirectoryInSD
import ir.namoo.quran.utils.getSelectedQuranDirectoryPath
import ir.namoo.quran.utils.getSuraFileName
import ir.namoo.quran.utils.getWordsForSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import java.io.File

class SuraViewModel(
    private val quranRepository: QuranRepository,
    private val quranSettingRepository: QuranSettingRepository,
    private val qariRepository: QariRepository,
    private val prefs: SharedPreferences,
    private val quranDownloader: QuranDownloader,
    private val downloadRepository: FileDownloadRepository,
    private val lastVisitedRepository: LastVisitedRepository
) : ViewModel() {
    var searchBarState by mutableStateOf(SearchBarState.CLOSED)

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()
    private val _resultIDs = MutableStateFlow(emptyList<Int>())

    private val _chapter = MutableStateFlow<ChapterEntity?>(null)
    val chapter = _chapter.asStateFlow()

    private val _quranList = MutableStateFlow(listOf<QuranEntity>())
    val quranList = _resultIDs.combine(_quranList) { idList, quranList ->
        quranList.filter { idList.contains(it.id) }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), _quranList.value
    )

    private val _enabledTranslates = MutableStateFlow(listOf<List<TranslateItem>>())
    val enabledTranslates = _enabledTranslates.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()
    private val _tafsirs = MutableStateFlow(listOf<TafsirEntity>())

    private val _lastVisitedAya = MutableStateFlow(0)
    private val lastVisitedAya = _lastVisitedAya.asStateFlow()

    fun loadDate(sura: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _chapter.value = quranRepository.getChapter(sura)
            _quranList.value = quranRepository.getQuran(sura)
            _tafsirs.value = quranRepository.getTafsir(sura)

            val enabledTranslates =
                quranSettingRepository.getTranslatesSettings().filter { it.isActive }

            val translates = mutableListOf<List<TranslateItem>>()
            for (i in 0..<quranList.value.size) {
                val tmpList = mutableListOf<TranslateItem>()
                for (e in enabledTranslates) tmpList.add(
                    TranslateItem(
                        _tafsirs.value[i].verseID,
                        e.name,
                        getTranslate(_tafsirs.value[i], e.name),
                        when (e.name) {
                            "فارسی نور(خرم دل)" -> TranslateType.FARSI
                            "صحیح - انگلیسی" -> TranslateType.ENGLISH
                            else -> TranslateType.KURDISH
                        }
                    )
                )

                translates.add(tmpList)
            }

            val tmp = mutableListOf<Int>()
            _quranList.value.forEach {
                if (!tmp.contains(it.id)) tmp.add(it.id)
            }
            _resultIDs.value = tmp

            _enabledTranslates.value = translates
            _isLoading.value = false
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
            quran.fav = if (quran.fav == 1) 0 else 1
            quranRepository.updateQuran(quran)
            _quranList.value.find { it.id == quran.id }?.apply { fav = quran.fav }
            _isLoading.value = false
        }
    }

    fun updateNote(quran: QuranEntity, newNote: String) {
        viewModelScope.launch {
            _isLoading.value = true
            quran.note = newNote
            quranRepository.updateQuran(quran)
            _quranList.value.find { it.id == quran.id }?.apply { note = newNote }
            _isLoading.value = false
        }
    }

    fun onQuery(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _query.update { query }
            if (query.isDigitsOnly()) {
                _isLoading.value = false
                return@launch
            }
            withContext(Dispatchers.IO) {
                val tmp = mutableListOf<Int>()
                if (query.getWordsForSearch().isEmpty()) _quranList.value.forEach {
                    if (!tmp.contains(it.id)) tmp.add(it.id)
                }
                else query.getWordsForSearch().forEach { word ->
                    _quranList.value.forEach {
                        if (!tmp.contains(it.id) && it.quranClean.contains(word)) tmp.add(it.id)
                    }
                    _enabledTranslates.value.forEach { tList ->
                        tList.forEach { translate ->
                            if (translate.text.contains(word)) {
                                val id =
                                    quranList.value.find { it.verseID == translate.ayaID }?.id ?: -1
                                if (id > 0 && !tmp.contains(id)) tmp.add(id)
                            }
                        }
                    }
                }
                _resultIDs.value = tmp
            }
            _isLoading.value = false
        }
    }

    // ====================================== Player
    private lateinit var mediaController: ListenableFuture<MediaController>
    private lateinit var controller: MediaController
    private lateinit var folderName: String
    private lateinit var translateFolderName: String
    private val _qariList = MutableStateFlow(emptyList<QariEntity>())


    private val _playingAya = MutableStateFlow(0)
    val playingAya = _playingAya.asStateFlow()

    private val _playingSura = MutableStateFlow(0)


    fun setupPlayer(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                val sessionToken =
                    SessionToken(context, ComponentName(context, PlayerService::class.java))
                mediaController = MediaController.Builder(context, sessionToken).buildAsync()
                mediaController.addListener({
                    controller = mediaController.get()
                    initController()
                }, MoreExecutors.directExecutor())

                loadFolders(context, getAyaFileName(1, 1))
                _qariList.value = qariRepository.getQariList()
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

    fun updatePlayingAya(aya: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            if (searchBarState == SearchBarState.CLOSED) _playingAya.value = aya
            else _playingAya.value = 0
            _isLoading.value = false
        }
    }

    fun onPlay(context: Context, sura: Int, aya: Int) {
        viewModelScope.launch {
            loadFolders(context, getAyaFileName(sura, aya))
            if (playingAya.value == aya && _playingSura.value == sura) {
                controller.stop()
                updatePlayingAya(0)
                return@launch
            }
            if (controller.isPlaying) controller.stop()
            if (controller.mediaItemCount > 0) controller.clearMediaItems()
            controller.addMediaItems(getPlayList(context, sura, aya))
            controller.prepare()
            controller.play()
        }
    }

    private fun initController() {
        controller.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                mediaItem?.let {
                    val sura = it.mediaMetadata.title.toString().split("|")[0].digitsOf().toInt()
                    val aya = it.mediaMetadata.title.toString().split("|")[1].digitsOf().toInt()
                    if (chapter.value?.sura != sura && _playingAya.value > 0) updatePlayingAya(0)
                    else if (chapter.value?.sura == sura && _playingAya.value != aya) updatePlayingAya(
                        aya
                    )
                    _playingSura.value = sura
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) {
                    updatePlayingAya(0)
                    controller.stop()
                }
            }
        })
    }

    private fun getPlayList(context: Context, sura: Int, aya: Int): List<MediaItem> {
        val result = mutableListOf<MediaItem>()
        val playType = prefs.getInt(PREF_PLAY_TYPE, DEFAULT_PLAY_TYPE)
        val q = _qariList.value.find { folderName.contains(it.folderName) }
        val t = _qariList.value.find { translateFolderName.contains(it.folderName) }
        if ((sura != 1 && sura != 9) && aya == 1 && prefs.getInt(
                PREF_PLAY_TYPE,
                DEFAULT_PLAY_TYPE
            ) != 3
        ) {
            result.add(
                MediaItem.Builder().setMediaMetadata(
                    MediaMetadata.Builder().setTitle(
                        " 📖 " + context.getString(R.string.str_bismillah) + " 📖 " + formatNumber(
                            sura
                        ) + "|" + formatNumber(1)
                    ).setArtist(q?.name)
                        .setArtworkUri(context.getQariLocalPhotoFile(q?.photoLink?.trim())?.toUri())
                        .build()
                ).setUri(
                    when {
                        File(
                            "$folderName/" + getAyaFileName(
                                sura,
                                0
                            )
                        ).exists() -> ("$folderName/" + getAyaFileName(sura, 0)).toUri()

                        File(
                            "$folderName/" + getAyaFileName(
                                1,
                                1
                            )
                        ).exists() -> ("$folderName/" + getAyaFileName(1, 1)).toUri()

                        else -> (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.resources.getResourcePackageName(
                            R.raw.bismillah
                        ) + "/" + context.resources.getResourceTypeName(R.raw.bismillah) + "/" + context.resources.getResourceEntryName(
                            R.raw.bismillah
                        )).toUri()
                    }
                ).build()
            )
        }

        for (i in aya..(chapter.value?.ayaCount ?: aya)) {
            val title =
                " 📖 " + chapter.value?.nameArabic + " 📖 " + formatNumber(sura) + " | " + formatNumber(
                    i
                )
            when (playType) {
                1 -> {
                    result.add(
                        MediaItem.Builder().setMediaMetadata(
                            MediaMetadata.Builder().setTitle(title).setArtist(q?.name)
                                .setArtworkUri(
                                    context.getQariLocalPhotoFile(q?.photoLink?.trim())?.toUri()
                                ).build()
                        ).setUri(("$folderName/" + getAyaFileName(sura, i)).toUri()).build()
                    )
                    result.add(
                        MediaItem.Builder().setMediaMetadata(
                            MediaMetadata.Builder().setTitle(title).setArtist(t?.name)
                                .setArtworkUri(
                                    context.getQariLocalPhotoFile(t?.photoLink?.trim())?.toUri()
                                ).build()
                        ).setUri(("$translateFolderName/" + getAyaFileName(sura, i)).toUri())
                            .build()
                    )

                }

                2 -> {
                    result.add(
                        MediaItem.Builder().setMediaMetadata(
                            MediaMetadata.Builder().setTitle(title).setArtist(q?.name)
                                .setArtworkUri(
                                    context.getQariLocalPhotoFile(q?.photoLink?.trim())?.toUri()
                                ).build()
                        ).setUri(("$folderName/" + getAyaFileName(sura, i)).toUri()).build()
                    )

                }

                else -> {
                    result.add(
                        MediaItem.Builder().setMediaMetadata(
                            MediaMetadata.Builder().setTitle(title).setArtist(t?.name)
                                .setArtworkUri(
                                    context.getQariLocalPhotoFile(t?.photoLink?.trim())?.toUri()
                                ).build()
                        ).setUri(("$translateFolderName/" + getAyaFileName(sura, i)).toUri())
                            .build()
                    )

                }
            }
        }
        return result
    }

    fun isQuranDownloaded(context: Context, sura: Int): Boolean {
        if (prefs.getInt(PREF_PLAY_TYPE, DEFAULT_PLAY_TYPE) == 3) return true
        if (File(
                getQuranDirectoryInInternal(context) + File.separator + context.appPrefsLite.getString(
                    PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
                ) + "/" + getAyaFileName(sura, 1)
            ).exists()
        ) return true
        if (File(
                getQuranDirectoryInSD(context) + File.separator + context.appPrefsLite.getString(
                    PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
                ) + "/" + getAyaFileName(sura, 1)
            ).exists()
        ) return true
        //if zip file is exist extract it and then recheck
        val internalZip =
            getQuranDirectoryInInternal(context) + File.separator + context.appPrefsLite.getString(
                PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
            ) + File.separator + getSuraFileName(
                sura
            )
        File(internalZip).let {
            if (it.exists()) {
                ZipFile(it).extractAll(it.parent)
                it.delete()
            }
        }

        if (File(
                getQuranDirectoryInInternal(context) + File.separator + context.appPrefsLite.getString(
                    PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
                ) + "/" + getAyaFileName(sura, 1)
            ).exists()
        ) return true

        val externalZip =
            getQuranDirectoryInSD(context) + File.separator + context.appPrefsLite.getString(
                PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
            ) + File.separator + getSuraFileName(
                sura
            )
        File(externalZip).let {
            if (it.exists()) {
                ZipFile(it).extractAll(it.parent)
                it.delete()
            }
        }

        if (File(
                getQuranDirectoryInSD(context) + File.separator + context.appPrefsLite.getString(
                    PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
                ) + "/" + getAyaFileName(sura, 1)
            ).exists()
        ) return true

        return false
    }

    fun isTranslateDownloaded(context: Context, sura: Int): Boolean {
        if (prefs.getInt(PREF_PLAY_TYPE, DEFAULT_PLAY_TYPE) == 2) return true
        if (File(
                getQuranDirectoryInInternal(context) + "/" + context.appPrefsLite.getString(
                    PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
                ) + "/" + getAyaFileName(sura, 1)
            ).exists()
        ) return true

        if (File(
                getQuranDirectoryInSD(context) + "/" + context.appPrefsLite.getString(
                    PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
                ) + "/" + getAyaFileName(sura, 1)
            ).exists()
        ) return true
        //if zip file is exist extract it and then recheck
        val internalZip =
            getQuranDirectoryInInternal(context) + File.separator + context.appPrefsLite.getString(
                PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
            ) + File.separator + getSuraFileName(
                sura
            )
        File(internalZip).let {
            if (it.exists()) {
                ZipFile(it).extractAll(it.parent)
                it.delete()
            }
        }
        if (File(
                getQuranDirectoryInInternal(context) + "/" + context.appPrefsLite.getString(
                    PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
                ) + "/" + getAyaFileName(sura, 1)
            ).exists()
        ) return true

        val externalZip =
            getQuranDirectoryInSD(context) + File.separator + context.appPrefsLite.getString(
                PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
            ) + File.separator + getSuraFileName(
                sura
            )
        File(externalZip).let {
            if (it.exists()) {
                ZipFile(it).extractAll(it.parent)
                it.delete()
            }
        }
        if (File(
                getQuranDirectoryInSD(context) + "/" + context.appPrefsLite.getString(
                    PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
                ) + "/" + getAyaFileName(sura, 1)
            ).exists()
        ) return true

        return false
    }

    fun downloadQuranFiles(context: Context) {
        viewModelScope.launch {
            val qari = _qariList.value.find { folderName.contains(it.folderName) } ?: return@launch
            downloadAudioFiles(context, qari)
        }
    }

    fun downloadTranslateFiles(context: Context) {
        viewModelScope.launch {
            val qari = _qariList.value.find { translateFolderName.contains(it.folderName) }
                ?: return@launch
            downloadAudioFiles(context, qari)
        }
    }

    private fun downloadAudioFiles(context: Context, qari: QariEntity) {
        viewModelScope.launch {
            val sura = chapter.value?.sura ?: return@launch
            val oldRequest = downloadRepository.findDownloadByFileId()
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
                downloadRequest = id, downloadFile = url, folderPath = destFile, sura = sura
            )
            downloadRepository.insert(downloadEntity)
        }
    }

    override fun onCleared() {
        super.onCleared()
        MediaController.releaseFuture(mediaController)
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
                        lastVisitedAya.value,
                        chapter.sura
                    )
                }
            }
        }
    }

}//end of class SuraViewModel
