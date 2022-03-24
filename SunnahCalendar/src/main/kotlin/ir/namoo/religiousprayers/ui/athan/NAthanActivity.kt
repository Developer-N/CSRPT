package ir.namoo.religiousprayers.ui.athan


import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.ASR_KEY
import com.byagowi.persiancalendar.DEFAULT_ATHAN_VOLUME
import com.byagowi.persiancalendar.DHUHR_KEY
import com.byagowi.persiancalendar.FAJR_KEY
import com.byagowi.persiancalendar.ISHA_KEY
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.MAGHRIB_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SUNRISE_KRY
import com.byagowi.persiancalendar.databinding.ActivityNathanBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.FIVE_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.TEN_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.THIRTY_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.cityName
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getPrayTimeName
import com.byagowi.persiancalendar.utils.logException
import dagger.hilt.android.AndroidEntryPoint
import ir.namoo.commons.model.AthanSetting
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.utils.getAthanUri
import ir.namoo.commons.utils.getDefaultDOAUri
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class NAthanActivity : AppCompatActivity() {

    @Inject
    lateinit var athanSettingsDB: AthanSettingsDB
    private lateinit var setting: AthanSetting
    private val ascendingVolumeStep = 6
    private var currentVolumeSteps = 1
    private val handler = Handler(Looper.getMainLooper())
    private var ringtone: Ringtone? = null
    private var alreadyStopped = false
    private var spentSeconds = 0
    private var originalVolume = -1
    private var isDoaPlayed = false
    private var doaPlayer: MediaPlayer? = null
    private val stopTask = object : Runnable {
        override fun run() = runCatching {
            spentSeconds += 5
            if (ringtone == null || ringtone?.isPlaying == false || spentSeconds > 360 ||
                (stopAtHalfMinute && spentSeconds > 30)
            ) {
                if (isDoaPlayed) finish()
                else playDoa()
            } else handler.postDelayed(
                this,
                FIVE_SECONDS_IN_MILLIS
            )
        }.onFailure(logException).onFailure { finish() }.let {}
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

    private var phoneStateListener: PhoneStateListener? = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                stop()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (originalVolume != -1) getSystemService<AudioManager>()
            ?.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resolveColor(android.R.attr.colorPrimaryDark)
        }
        val prayerKey = intent.getStringExtra(KEY_EXTRA_PRAYER) ?: ""
        setting = athanSettingsDB.athanSettingsDAO().getAllAthanSettings()
            .find { prayerKey.contains(it.athanKey) } ?: return
        if (prayerKey.startsWith("B") || !setting.playDoa) isDoaPlayed = true
        val isFajr = prayerKey == FAJR_KEY
        var goMute = false



        getSystemService<AudioManager>()?.let { audioManager ->
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            if (setting.athanVolume != DEFAULT_ATHAN_VOLUME) // Don't change alarm volume if isn't set in-app
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, setting.athanVolume, 0)
            // Mute if system alarm is set to lowest, ringer mode is silent/vibration and it isn't Fajr
            if (originalVolume == 1 && !isFajr &&
                audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL
            ) goMute = true
        }
        if (!goMute) runCatching {
            val athanUri = getAthanUri(setting, prayerKey, this)
            runCatching {
                MediaPlayer.create(this, athanUri).duration // is in milliseconds
            }.onFailure(logException).onSuccess {
                // if the URIs duration is less than half a minute, it is probably a looping one
                // so stop on half a minute regardless
                if (it < THIRTY_SECONDS_IN_MILLIS) stopAtHalfMinute = true
            }
            ringtone =
                RingtoneManager.getRingtone(this, getAthanUri(setting, prayerKey, this)).also {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        it.audioAttributes = AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    } else {
                        @Suppress("DEPRECATION")
                        it.streamType = AudioManager.STREAM_ALARM
                    }
                    volumeControlStream = AudioManager.STREAM_ALARM
                    it.play()
                }
        }.onFailure(logException)

        applyAppLanguage(this)

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
        var prayTimes = coordinates?.calculatePrayTimes()
        prayTimes = PrayTimeProvider(this).nReplace(prayTimes, Jdn.today())
        ActivityNathanBinding.inflate(layoutInflater).apply {
            setContentView(root)
            athanName.setText(getPrayTimeName(prayerKey))
            btnStop.setOnClickListener { stop() }
            place.text = String.format(getString(R.string.in_city_time), appPrefs.cityName) + " " +
                    when (prayerKey) {
                        FAJR_KEY -> prayTimes?.getFromStringId(R.string.fajr)?.toFormattedString()
                        SUNRISE_KRY -> prayTimes?.getFromStringId(R.string.sunrise)
                            ?.toFormattedString()
                        DHUHR_KEY -> prayTimes?.getFromStringId(R.string.dhuhr)?.toFormattedString()
                        ASR_KEY -> prayTimes?.getFromStringId(R.string.asr)?.toFormattedString()
                        MAGHRIB_KEY -> prayTimes?.getFromStringId(R.string.maghrib)
                            ?.toFormattedString()
                        ISHA_KEY -> prayTimes?.getFromStringId(R.string.isha)?.toFormattedString()
                        else -> ""
                    }
        }

        handler.postDelayed(stopTask, TEN_SECONDS_IN_MILLIS)

        if (setting.isAscending) handler.post(ascendVolume)

        runCatching {
            getSystemService<TelephonyManager>()?.listen(
                phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE
            )
        }.onFailure(logException)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) stop()
    }

    override fun onBackPressed() = stop()

    private fun stop() {
        if (alreadyStopped) return
        alreadyStopped = true

        runCatching {
            getSystemService<TelephonyManager>()?.listen(
                phoneStateListener, PhoneStateListener.LISTEN_NONE
            )
            phoneStateListener = null
        }.onFailure(logException)

        ringtone?.stop()

        runCatching {
            if (doaPlayer != null)
                if (doaPlayer!!.isPlaying) {
                    doaPlayer!!.release()
                    doaPlayer!!.stop()
                }
        }.onFailure(logException)

        handler.removeCallbacks(stopTask)
        if (setting.isAscending) handler.removeCallbacks(ascendVolume)
        finish()
    }

    private fun playDoa() {
        isDoaPlayed = true
        if (setting.playDoa)//play doa
            doaPlayer = MediaPlayer().apply {
                runCatching {
                    setDataSource(
                        this@NAthanActivity,
                        getDefaultDOAUri(this@NAthanActivity)
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        setAudioStreamType(AudioManager.STREAM_ALARM)
                    }
                    prepare()
                    setOnCompletionListener { finish() }
                }.onFailure(logException)
                start()
            }
    }

}
