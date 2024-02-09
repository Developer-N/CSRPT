package ir.namoo.religiousprayers.ui.athan


import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.ContentResolver
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.byagowi.persiancalendar.ASR_KEY
import com.byagowi.persiancalendar.DEFAULT_ATHAN_VOLUME
import com.byagowi.persiancalendar.DHUHR_KEY
import com.byagowi.persiancalendar.FAJR_KEY
import com.byagowi.persiancalendar.ISHA_KEY
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.MAGHRIB_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SUNRISE_KEY
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.athan.PreventPhoneCallIntervention
import com.byagowi.persiancalendar.utils.FIVE_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.TEN_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.THIRTY_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getPrayTimeName
import com.byagowi.persiancalendar.utils.logException
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import ir.namoo.commons.ATHAN_NOTIFICATION_ID
import ir.namoo.commons.model.AthanSetting
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.utils.getAthanUri
import ir.namoo.commons.utils.getDefaultDOAUri
import ir.namoo.commons.utils.turnScreenOnAndKeyguardOff
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import org.koin.android.ext.android.get
import java.util.*
import java.util.concurrent.TimeUnit

class NAthanActivity : ComponentActivity() {
    private val prayTimeProvider: PrayTimeProvider = get()
    private val athanSettingsDB: AthanSettingsDB = get()
    private lateinit var setting: AthanSetting
    private val ascendingVolumeStep = 6
    private var currentVolumeSteps = 1
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mediaController: ListenableFuture<MediaController>
    private lateinit var controller: MediaController
    private var alreadyStopped = false
    private var spentSeconds = 0
    private var originalVolume = -1
    private val preventPhoneCallIntervention = PreventPhoneCallIntervention(::stop)
    private var playDua = false
    private lateinit var prayTimeKey: String
    private var bFajrCount = 0
    private lateinit var startDate: Date
    private val stopTask = object : Runnable {
        override fun run() = runCatching {
            spentSeconds += 5
            if (!controller.isPlaying || spentSeconds > 360 || (stopAtHalfMinute && spentSeconds > 30)) {
                if (prayTimeKey == "BFAJR" && bFajrCount < 5 && !controller.isPlaying) {
                    controller.play()
                    bFajrCount++
                    handler.postDelayed(this, FIVE_SECONDS_IN_MILLIS)
                } else finish()
            } else handler.postDelayed(
                this, FIVE_SECONDS_IN_MILLIS
            )
        }.onFailure(logException).onFailure { finish() }.let {}
    }
    private var stopAtHalfMinute = false

    private val ascendVolume = object : Runnable {
        override fun run() {
            currentVolumeSteps++
            getSystemService<AudioManager>()?.setStreamVolume(
                AudioManager.STREAM_ALARM,
                currentVolumeSteps,
                0
            )
            handler.postDelayed(this, TimeUnit.SECONDS.toMillis(ascendingVolumeStep.toLong()))
            if (currentVolumeSteps == 10) handler.removeCallbacks(this)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (originalVolume != -1) getSystemService<AudioManager>()?.setStreamVolume(
            AudioManager.STREAM_ALARM,
            originalVolume,
            0
        )
        runCatching {
            controller.stop()
            MediaController.releaseFuture(mediaController)
        }
        runCatching {
            getSystemService<NotificationManager>()?.cancel(ATHAN_NOTIFICATION_ID)
        }.onFailure(logException)
    }

    private val onBackPressedCloseCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = stop()
    }


    @SuppressLint("SetTextI18n")
    @Suppress("Deprecation")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Just to make sure we have an initial transparent system bars
        // System bars are tweaked later with project's with real values
        applyEdgeToEdge(isBackgroundColorLight = false, isSurfaceColorLight = true)
        runCatching {
            val telephonyManager = getSystemService<TelephonyManager>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyManager?.registerTelephonyCallback(mainExecutor,
                    object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                        override fun onCallStateChanged(state: Int) {
                            when (state) {
                                TelephonyManager.CALL_STATE_IDLE -> {}
                                else -> finish()
                            }
                        }
                    })
            } else if (telephonyManager?.callState != TelephonyManager.CALL_STATE_IDLE) finish()
            else {
                //do nothing
            }
        }.onFailure(logException)
        startDate = Date(System.currentTimeMillis())
        setTheme(R.style.BaseTheme)
        applyAppLanguage(this)
        turnScreenOnAndKeyguardOff()
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, onBackPressedCloseCallback)

        prayTimeKey = intent.getStringExtra(KEY_EXTRA_PRAYER) ?: ""
        setting = athanSettingsDB.athanSettingsDAO().getAllAthanSettings()
            .find { prayTimeKey.contains(it.athanKey) } ?: return
        if (!prayTimeKey.startsWith("B") && !(prayTimeKey.startsWith("A") && prayTimeKey != ASR_KEY) && setting.playDoa)
            playDua = true
        val isFajr = prayTimeKey == FAJR_KEY
        var goMute = false

        getSystemService<AudioManager>()?.let { audioManager ->
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            if (setting.athanVolume != DEFAULT_ATHAN_VOLUME) // Don't change alarm volume if isn't set in-app
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, setting.athanVolume, 0)
            // Mute if system alarm is set to lowest, ringer mode is silent/vibration and it isn't Fajr
            if (originalVolume == 1 && !isFajr && audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) goMute =
                true
        }

        var prayTimes = coordinates.value?.calculatePrayTimes()
        prayTimes = prayTimeProvider.replace(prayTimes, Jdn.today())

        val title = getString(getPrayTimeName(prayTimeKey))
        val subtitle = String.format(
            getString(R.string.in_city_time),
            cityName.value ?: "-"
        ) + " " + when (prayTimeKey) {
            FAJR_KEY -> prayTimes?.getFromStringId(R.string.fajr)
                ?.toFormattedString()

            SUNRISE_KEY -> prayTimes?.getFromStringId(R.string.sunrise)
                ?.toFormattedString()

            DHUHR_KEY -> prayTimes?.getFromStringId(R.string.dhuhr)
                ?.toFormattedString()

            ASR_KEY -> prayTimes?.getFromStringId(R.string.asr)
                ?.toFormattedString()

            MAGHRIB_KEY -> prayTimes?.getFromStringId(R.string.maghrib)
                ?.toFormattedString()

            ISHA_KEY -> prayTimes?.getFromStringId(R.string.isha)
                ?.toFormattedString()

            else -> ""
        }

        if (!goMute) runCatching {
            val athanUri = getAthanUri(setting, prayTimeKey, this)
            runCatching {
                MediaPlayer.create(this, athanUri).duration // is in milliseconds
            }.onFailure(logException).onSuccess {
                // if the URIs duration is less than half a minute, it is probably a looping one
                // so stop on half a minute regardless
                if (it < THIRTY_SECONDS_IN_MILLIS) stopAtHalfMinute = true
            }
//            ringtone =
//                RingtoneManager.getRingtone(this, getAthanUri(setting, prayTimeKey, this)).also {
//                    it.audioAttributes =
//                        AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                            .setUsage(AudioAttributes.USAGE_ALARM).build()
//                    volumeControlStream = AudioManager.STREAM_ALARM
//                    it.play()
//                }

            val sessionToken =
                SessionToken(this, ComponentName(this, AthanPlayerService::class.java))
            mediaController = MediaController.Builder(this, sessionToken).buildAsync()
            mediaController.addListener(
                {
                    controller = mediaController.get()
                    controller.clearMediaItems()
                    controller.addMediaItem(
                        MediaItem.Builder()
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(title)
                                    .setArtworkUri(
                                        "%s://%s/%s/%s".format(
                                            ContentResolver.SCHEME_ANDROID_RESOURCE,
                                            resources.getResourcePackageName(R.drawable.adhan_background),
                                            resources.getResourceTypeName(R.drawable.adhan_background),
                                            resources.getResourceEntryName(R.drawable.adhan_background)
                                        ).toUri()
                                    )
                                    .setArtist(subtitle)
                                    .build()
                            )
                            .setUri(athanUri)
                            .build()
                    )
                    if (playDua)
                        controller.addMediaItem(
                            MediaItem.Builder()
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(title)
                                        .setArtworkUri(
                                            "%s://%s/%s/%s".format(
                                                ContentResolver.SCHEME_ANDROID_RESOURCE,
                                                resources.getResourcePackageName(R.drawable.adhan_background),
                                                resources.getResourceTypeName(R.drawable.adhan_background),
                                                resources.getResourceEntryName(R.drawable.adhan_background)
                                            ).toUri()
                                        )
                                        .setArtist(subtitle)
                                        .build()
                                )
                                .setUri(getDefaultDOAUri(this))
                                .build()
                        )
                    controller.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            super.onPlaybackStateChanged(playbackState)
                            if (playbackState == Player.STATE_ENDED) {
                                stop()
                            }
                        }
                    })
                    controller.prepare()
                    controller.play()
                },
                MoreExecutors.directExecutor()
            )

        }.onFailure(logException)

        setContent {
            NAthanActivityContent(
                title = title,
                subtitle = subtitle,
                applyEdgeToEdge = { isBackgroundColorLight, isSurfaceColorLight ->
                    applyEdgeToEdge(isBackgroundColorLight, isSurfaceColorLight)
                }, stop = { stop() })
        }

        handler.postDelayed(stopTask, TEN_SECONDS_IN_MILLIS)

        if (setting.isAscending) handler.post(ascendVolume)
        preventPhoneCallIntervention.startListener(this)
    }

    private fun applyEdgeToEdge(isBackgroundColorLight: Boolean, isSurfaceColorLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) enableEdgeToEdge(
            if (isBackgroundColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
            if (isSurfaceColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
        ) else enableEdgeToEdge( // Just don't tweak navigation bar in older Android versions
            if (isBackgroundColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT)
        )
    }

    override fun onPause() {
        super.onPause()
        if (isLockedAndPassed2Second()) stop()
    }

    private fun isLockedAndPassed2Second(): Boolean {
        val keyguardManager = getSystemService<KeyguardManager>() ?: return true
        val nowDate = Date(System.currentTimeMillis())
        val diffInSecond = TimeUnit.MILLISECONDS.toSeconds(nowDate.time - startDate.time)
        return if (!keyguardManager.isKeyguardLocked) true
        else diffInSecond > 2
    }

    private fun stop() {
        if (alreadyStopped) return
        alreadyStopped = true

        preventPhoneCallIntervention.stopListener()

        handler.removeCallbacks(stopTask)
        if (setting.isAscending) handler.removeCallbacks(ascendVolume)
        finish()
    }

}
