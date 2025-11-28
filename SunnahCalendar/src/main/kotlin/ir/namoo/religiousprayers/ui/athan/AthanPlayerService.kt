package ir.namoo.religiousprayers.ui.athan

import androidx.core.app.ServiceCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.byagowi.persiancalendar.utils.applyAppLanguage

class AthanPlayerService : MediaSessionService(), MediaSession.Callback, Player.Listener {

    private var session: MediaSession? = null
    private var player: Player? = null
    override fun onCreate() {
        applyAppLanguage(this)
        super.onCreate()
        player = ExoPlayer.Builder(this).setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_ALARM)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), false
        ).setHandleAudioBecomingNoisy(true).setWakeMode(C.WAKE_MODE_LOCAL).build()

        player?.let { player ->
            player.addListener(this)
            session = MediaSession.Builder(this, player).setId("AthanPlayer").build()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == Player.STATE_ENDED) {
            player?.let { player ->
                player.stop()
                player.clearMediaItems()
                player.release()
            }
            session?.release()
            session = null
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }
}
