package ir.namoo.religiousprayers.ui.athan


import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.athan.AthanActivity.Companion.CANCEL_ATHAN_NOTIFICATION
import com.byagowi.persiancalendar.ui.athan.PreventPhoneCallIntervention
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.logException
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import ir.namoo.commons.model.AthanSetting
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.utils.getAthanUri
import ir.namoo.commons.utils.getDefaultDOAUri
import ir.namoo.commons.utils.turnScreenOnAndKeyguardOff
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import org.koin.android.ext.android.get
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
                    handler.postDelayed(this, 5.seconds.inWholeMilliseconds)
                } else finish()
            } else handler.postDelayed(
                this, 5.seconds.inWholeMilliseconds
            )
        }.onFailure(logException).onFailure { finish() }.let {}
    }
    private var stopAtHalfMinute = false

    private val ascendVolume = object : Runnable {
        override fun run() {
            currentVolumeSteps++
            getSystemService<AudioManager>()?.setStreamVolume(
                AudioManager.STREAM_ALARM, currentVolumeSteps, 0
            )
            handler.postDelayed(this, TimeUnit.SECONDS.toMillis(ascendingVolumeStep.toLong()))
            if (currentVolumeSteps == 10) handler.removeCallbacks(this)
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("Deprecation")
    override fun onCreate(savedInstanceState: Bundle?) {
        applyEdgeToEdge(isBackgroundColorLight = false, isSurfaceColorLight = true)
        runCatching {
            val telephonyManager = getSystemService<TelephonyManager>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyManager?.registerTelephonyCallback(
                    mainExecutor,
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
        applyAppLanguage(this)
        turnScreenOnAndKeyguardOff()
        super.onCreate(savedInstanceState)
        if (intent?.action == CANCEL_ATHAN_NOTIFICATION) {
            runCatching {
                stopService(Intent(this, NAthanNotification::class.java))
            }.onFailure(logException)
            finish()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
        prayTimeKey = intent.getStringExtra(KEY_EXTRA_PRAYER) ?: ""
        setting = athanSettingsDB.athanSettingsDAO().getAllAthanSettings()
            .find { prayTimeKey.contains(it.athanKey) } ?: return
        if (!prayTimeKey.startsWith("B") && !(prayTimeKey.startsWith("A") && prayTimeKey != PrayTime.ASR.name) && setting.playDoa) playDua =
            true
        var goMute = false

        getSystemService<AudioManager>()?.let { audioManager ->
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            val athanVolume = setting.athanVolume
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, athanVolume, 0)
            // Mute if system alarm is set to lowest, ringer mode is silent/vibration and it isn't Fajr
            if (originalVolume == 1 && PrayTime.fromName(prayTimeKey)?.isBypassDnd == true && audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) goMute =
                true
        }

        var prayTimes = coordinates.value?.calculatePrayTimes() ?: return
        prayTimes = prayTimeProvider.replace(prayTimes, Jdn.today())

        val title = getStringForKey(prayTimeKey)
        val subtitle = String.format(
            getString(R.string.in_city_time), cityName.value ?: "-"
        ) + " " + when (prayTimeKey) {
            PrayTime.FAJR.name -> Clock(prayTimes.fajr).toFormattedString()
            PrayTime.SUNRISE.name -> Clock(prayTimes.sunrise).toFormattedString()
            PrayTime.DHUHR.name -> Clock(prayTimes.dhuhr).toFormattedString()
            PrayTime.ASR.name -> Clock(prayTimes.asr).toFormattedString()
            PrayTime.MAGHRIB.name -> Clock(prayTimes.maghrib).toFormattedString()
            PrayTime.ISHA.name -> Clock(prayTimes.isha).toFormattedString()
            else -> ""
        }

        if (!goMute) runCatching {
            val athanUri = getAthanUri(setting, prayTimeKey, this)
            runCatching {
                MediaPlayer.create(this, athanUri).duration.milliseconds // is in milliseconds
            }.onFailure(logException).onSuccess {
                // if the URIs duration is less than half a minute, it is probably a looping one
                // so stop on half a minute regardless
                if (it < 30.seconds) stopAtHalfMinute = true
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
                        MediaItem.Builder().setMediaMetadata(
                            MediaMetadata.Builder().setTitle(title).setArtworkUri(
                                "%s://%s/%s/%s".format(
                                    ContentResolver.SCHEME_ANDROID_RESOURCE,
                                    resources.getResourcePackageName(R.drawable.adhan_background),
                                    resources.getResourceTypeName(R.drawable.adhan_background),
                                    resources.getResourceEntryName(R.drawable.adhan_background)
                                ).toUri()
                            ).setArtist(subtitle).build()
                        ).setUri(athanUri).build()
                    )
                    if (playDua) controller.addMediaItem(
                        MediaItem.Builder().setMediaMetadata(
                            MediaMetadata.Builder().setTitle(title).setArtworkUri(
                                "%s://%s/%s/%s".format(
                                    ContentResolver.SCHEME_ANDROID_RESOURCE,
                                    resources.getResourcePackageName(R.drawable.adhan_background),
                                    resources.getResourceTypeName(R.drawable.adhan_background),
                                    resources.getResourceEntryName(R.drawable.adhan_background)
                                ).toUri()
                            ).setArtist(subtitle).build()
                        ).setUri(getDefaultDOAUri(this)).build()
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
                }, MoreExecutors.directExecutor()
            )

        }.onFailure(logException)

        setContent {
            AppTheme {
                val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
                val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
                LaunchedEffect(isBackgroundColorLight, isSurfaceColorLight) {
                    applyEdgeToEdge(isBackgroundColorLight, isSurfaceColorLight)
                }
                BackHandler { stop() }

                NAthanActivityContent(
                    title = title,
                    subtitle = subtitle,
                    background = setting.backgroundUri,
                    stop = { stop() })
            }
        }

        handler.postDelayed(stopTask, 10.seconds.inWholeMilliseconds)

        if (setting.isAscending) handler.post(ascendVolume)
        preventPhoneCallIntervention.startListener(this)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                if (isLockedAndPassed2Second()) stop()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                if (originalVolume != -1) getSystemService<AudioManager>()?.setStreamVolume(
                    AudioManager.STREAM_ALARM, originalVolume, 0
                )
                runCatching {
                    controller.stop()
                    controller.release()
                    MediaController.releaseFuture(mediaController)
                    stopService(Intent(this@NAthanActivity, AthanPlayerService::class.java))
                }.onFailure(logException)
                super.onDestroy(owner)
            }
        })
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

    private fun applyEdgeToEdge(isBackgroundColorLight: Boolean, isSurfaceColorLight: Boolean) {
        val statusBarStyle =
            if (isBackgroundColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) enableEdgeToEdge(
            statusBarStyle,
            if (isSurfaceColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
        ) else enableEdgeToEdge(
            statusBarStyle,
            // Just don't tweak navigation bar in older Android versions, leave it to default
        )
    }
}
