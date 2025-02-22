package com.byagowi.persiancalendar.ui.athan

import android.app.KeyguardManager
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.DEFAULT_ATHAN_VOLUME
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.global.ascendingAthan
import com.byagowi.persiancalendar.service.AthanNotification
import com.byagowi.persiancalendar.ui.theme.SystemTheme
import com.byagowi.persiancalendar.ui.utils.isSystemInDarkTheme
import com.byagowi.persiancalendar.utils.FIVE_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.TEN_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.THIRTY_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.athanVolume
import com.byagowi.persiancalendar.utils.getAthanUri
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import java.util.concurrent.TimeUnit

class AthanActivity : ComponentActivity() {
    private val ascendingVolumeStep = 6
    private var currentVolumeSteps = 1
    private val handler = Handler(Looper.getMainLooper())
    private var ringtone: Ringtone? = null
    private var alreadyStopped = false
    private var spentSeconds = 0
    private var originalVolume = -1
    private val preventPhoneCallIntervention = PreventPhoneCallIntervention(::stop)
    private val stopTask = object : Runnable {
        override fun run() {
            runCatching {
                spentSeconds += 5
                if (ringtone == null || ringtone?.isPlaying == false || spentSeconds > 360 ||
                    (stopAtHalfMinute && spentSeconds > 30)
                ) finish() else handler.postDelayed(this, FIVE_SECONDS_IN_MILLIS)
            }.onFailure(logException).onFailure { finish() }
        }
    }
    private var stopAtHalfMinute = false

    private val ascendVolume = object : Runnable {
        override fun run() {
            currentVolumeSteps++
            getSystemService<AudioManager>()
                ?.setStreamVolume(AudioManager.STREAM_ALARM, currentVolumeSteps, 0)
            handler.postDelayed(this, TimeUnit.SECONDS.toMillis(ascendingVolumeStep.toLong()))
            if (currentVolumeSteps == 10) handler.removeCallbacks(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (originalVolume != -1) getSystemService<AudioManager>()
            ?.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            SystemBarStyle.dark(Color.TRANSPARENT),
            if (isSystemInDarkTheme(resources.configuration))
                SystemBarStyle.dark(Color.TRANSPARENT)
            else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)

        if (intent?.action == CANCEL_ATHAN_NOTIFICATION) {
            runCatching {
                stopService(Intent(this, AthanNotification::class.java))
            }.onFailure(logException)
            finish()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }

        val prayTime = PrayTime.fromName(
            intent?.getStringExtra(KEY_EXTRA_PRAYER)
        ).debugAssertNotNull ?: PrayTime.FAJR
        var goMute = false

        getSystemService<AudioManager>()?.let { audioManager ->
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            val athanVolume = preferences.athanVolume
            if (athanVolume != DEFAULT_ATHAN_VOLUME) // Don't change alarm volume if isn't set in-app
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, athanVolume, 0)
            // Mute if system alarm is set to lowest, ringer mode is silent/vibration and it isn't Fajr
            if (originalVolume == 1 && !prayTime.isBypassDnd &&
                audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL
            ) goMute = true
        }

        if (!goMute) runCatching {
            val athanUri = getAthanUri(this)
            runCatching {
                MediaPlayer.create(this, athanUri).duration // is in milliseconds
            }.onFailure(logException).onSuccess {
                // if the URIs duration is less than half a minute, it is probably a looping one
                // so stop on half a minute regardless
                if (it < THIRTY_SECONDS_IN_MILLIS) stopAtHalfMinute = true
            }
            ringtone = RingtoneManager.getRingtone(this, athanUri).also {
                it.audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                volumeControlStream = AudioManager.STREAM_ALARM
                it.play()
            }
        }.onFailure(logException)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            getSystemService<KeyguardManager>()?.requestDismissKeyguard(this, null)
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContent {
            BackHandler { stop() }
            SystemTheme { AthanActivityContent(prayTime, ::stop) }
        }

        handler.postDelayed(stopTask, TEN_SECONDS_IN_MILLIS)

        if (ascendingAthan.value) handler.post(ascendVolume)

        preventPhoneCallIntervention.startListener(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) stop()
    }

    private fun stop() {
        if (alreadyStopped) return
        alreadyStopped = true
        preventPhoneCallIntervention.stopListener()

        runCatching { ringtone?.stop() }.onFailure(logException)

        handler.removeCallbacks(stopTask)
        if (ascendingAthan.value) handler.removeCallbacks(ascendVolume)

        finish()
    }

    companion object {
        const val CANCEL_ATHAN_NOTIFICATION = "CANCEL_ATHAN_NOTIFICATION"
    }
}
