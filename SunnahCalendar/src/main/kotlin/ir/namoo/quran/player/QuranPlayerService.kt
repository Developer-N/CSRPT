package ir.namoo.quran.player

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.applyAppLanguage
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.commons.utils.digitsOf
import ir.namoo.quran.QuranActivity
import ir.namoo.quran.qari.QariRepository
import ir.namoo.quran.sura.data.QuranRepository
import ir.namoo.quran.utils.DEFAULT_PLAY_NEXT_SURA
import ir.namoo.quran.utils.DEFAULT_SELECTED_QARI
import ir.namoo.quran.utils.DEFAULT_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.EXTRA_AYA
import ir.namoo.quran.utils.EXTRA_SURA
import ir.namoo.quran.utils.PREF_IS_SURA_VIEW_IS_OPEN
import ir.namoo.quran.utils.PREF_PLAY_NEXT_SURA
import ir.namoo.quran.utils.PREF_SELECTED_QARI
import ir.namoo.quran.utils.PREF_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.getAyaFileName
import ir.namoo.quran.utils.getQuranDirectoryInInternal
import ir.namoo.quran.utils.getQuranDirectoryInSD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File

class QuranPlayerService : MediaSessionService(), MediaSession.Callback, Player.Listener {
    //end of class PlayerService
    private var session: MediaSession? = null
    private var currentSura = -1
    private var currentAya = -1

    private val qariRepository: QariRepository by inject()
    private val quranRepository: QuranRepository by inject()

    override fun onCreate() {
        applyAppLanguage(this)
        super.onCreate()
        val player = ExoPlayer.Builder(this).setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true).setWakeMode(C.WAKE_MODE_LOCAL).build()

        player.addListener(this)

        session = MediaSession.Builder(this, player).setId("QuranPlayer").build()
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        mediaItem?.let {
            currentSura = it.mediaMetadata.title.toString().split("|")[0].digitsOf().toInt()
            currentAya = it.mediaMetadata.title.toString().split("|")[1].digitsOf().toInt()
            session?.setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, QuranActivity::class.java).apply {
                        putExtra(EXTRA_SURA, currentSura)
                        putExtra(EXTRA_AYA, currentAya)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                )
            )
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        val isSuraViewOpen = appPrefsLite.getBoolean(PREF_IS_SURA_VIEW_IS_OPEN, false)
        if (isSuraViewOpen) return
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == Player.STATE_ENDED) {
            if (appPrefsLite.getBoolean(
                    PREF_PLAY_NEXT_SURA, DEFAULT_PLAY_NEXT_SURA
                )
            ) {//PlayNext Sura
                val sura = if (currentSura == 114) 1 else currentSura + 1
                if (!isQuranDownloaded(this, sura)) {
                    Toast.makeText(this, R.string.audio_files_error, Toast.LENGTH_SHORT).show()
                    return
                }
                if (!isTranslateDownloaded(this, sura)) {
                    Toast.makeText(this, R.string.audio_translate_files_error, Toast.LENGTH_SHORT)
                        .show()
                    return
                }
                CoroutineScope(Dispatchers.Main).launch {
                    val fileName = getAyaFileName(sura, 1)
                    val folderName = if (File(
                            getQuranDirectoryInInternal(this@QuranPlayerService) + "/" + appPrefsLite.getString(
                                PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
                            ) + if (fileName.isNotEmpty()) "/$fileName" else ""
                        ).exists()
                    ) getQuranDirectoryInInternal(this@QuranPlayerService) + "/" + appPrefsLite.getString(
                        PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
                    )
                    else getQuranDirectoryInSD(this@QuranPlayerService) + "/" + appPrefsLite.getString(
                        PREF_SELECTED_QARI, DEFAULT_SELECTED_QARI
                    )

                    val translateFolderName = if (File(
                            getQuranDirectoryInInternal(this@QuranPlayerService) + "/" + appPrefsLite.getString(
                                PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
                            ) + if (fileName.isNotEmpty()) "/$fileName" else ""
                        ).exists()
                    ) getQuranDirectoryInInternal(this@QuranPlayerService) + "/" + appPrefsLite.getString(
                        PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
                    )
                    else getQuranDirectoryInSD(this@QuranPlayerService) + "/" + appPrefsLite.getString(
                        PREF_TRANSLATE_TO_PLAY, DEFAULT_TRANSLATE_TO_PLAY
                    )

                    session?.player?.clearMediaItems()
                    session?.player?.addMediaItems(
                        getPlayList(
                            this@QuranPlayerService,
                            sura,
                            1,
                            folderName,
                            translateFolderName,
                            qariRepository.getQariList(),
                            quranRepository.getChapter(sura)
                        )
                    )
                    session?.player?.prepare()
                    session?.player?.play()
                }
            } else session?.player?.stop()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        session?.run {
            player.release()
            release()
            session = null
        }
        super.onDestroy()
    }
}
