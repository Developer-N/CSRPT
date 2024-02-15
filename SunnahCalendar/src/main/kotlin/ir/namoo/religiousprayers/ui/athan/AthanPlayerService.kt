package ir.namoo.religiousprayers.ui.athan

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.byagowi.persiancalendar.utils.applyAppLanguage

class AthanPlayerService : MediaSessionService(), MediaSession.Callback, Player.Listener {

    private var session: MediaSession? = null
    override fun onCreate() {
        applyAppLanguage(this)
        super.onCreate()
        val player = ExoPlayer.Builder(this).setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_ALARM)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), false
        ).setHandleAudioBecomingNoisy(true).setWakeMode(C.WAKE_MODE_LOCAL).build()

        player.addListener(this)

        session = MediaSession.Builder(this, player).setId("AthanPlayer").build()
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
