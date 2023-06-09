package ir.namoo.religiousprayers.ui.athan


import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.byagowi.persiancalendar.ASR_KEY
import com.byagowi.persiancalendar.DEFAULT_ATHAN_VOLUME
import com.byagowi.persiancalendar.DHUHR_KEY
import com.byagowi.persiancalendar.FAJR_KEY
import com.byagowi.persiancalendar.ISHA_KEY
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.MAGHRIB_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SUNRISE_KEY
import com.byagowi.persiancalendar.databinding.ActivityNathanBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.athan.PreventPhoneCallIntervention
import com.byagowi.persiancalendar.ui.utils.transparentSystemBars
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
import ir.namoo.commons.ATHAN_NOTIFICATION_ID
import ir.namoo.commons.model.AthanSetting
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.utils.getAthanUri
import ir.namoo.commons.utils.getDefaultDOAUri
import ir.namoo.commons.utils.turnScreenOnAndKeyguardOff
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import org.koin.android.ext.android.inject
import java.util.*
import java.util.concurrent.TimeUnit

class NAthanActivity : AppCompatActivity() {

    private val athanSettingsDB: AthanSettingsDB by inject()
    private lateinit var setting: AthanSetting
    private val ascendingVolumeStep = 6
    private var currentVolumeSteps = 1
    private val handler = Handler(Looper.getMainLooper())
    private var ringtone: Ringtone? = null
    private var alreadyStopped = false
    private var spentSeconds = 0
    private var originalVolume = -1
    private val preventPhoneCallIntervention = PreventPhoneCallIntervention(::stop)
    private var isDoaPlayed = false
    private var doaPlayer: MediaPlayer? = null
    private lateinit var prayTimeKey: String
    private var bFajrCount = 0
    private lateinit var startDate: Date
    private val stopTask = object : Runnable {
        override fun run() = runCatching {
            spentSeconds += 5
            if (ringtone == null || ringtone?.isPlaying == false || spentSeconds > 360 ||
                (stopAtHalfMinute && spentSeconds > 30)
            ) {
                if (prayTimeKey == "BFAJR" && bFajrCount < 5 && ringtone?.isPlaying == false) {
                    ringtone?.play()
                    bFajrCount++
                    handler.postDelayed(this, FIVE_SECONDS_IN_MILLIS)
                } else if (isDoaPlayed) finish()
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


    override fun onDestroy() {
        super.onDestroy()
        if (originalVolume != -1) getSystemService<AudioManager>()
            ?.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
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
            } else if (telephonyManager?.callState != TelephonyManager.CALL_STATE_IDLE)
                finish()
            else {
                //do nothing
            }
        }.onFailure(logException)
        startDate = Date(System.currentTimeMillis())
        Theme.apply(this)
        applyAppLanguage(this)
        turnScreenOnAndKeyguardOff()
        super.onCreate(savedInstanceState)
        transparentSystemBars()
        onBackPressedDispatcher.addCallback(this, onBackPressedCloseCallback)

        prayTimeKey = intent.getStringExtra(KEY_EXTRA_PRAYER) ?: ""
        setting = athanSettingsDB.athanSettingsDAO().getAllAthanSettings()
            .find { prayTimeKey.contains(it.athanKey) } ?: return
        if (prayTimeKey.startsWith("B") || (prayTimeKey.startsWith("A") && prayTimeKey != ASR_KEY) || !setting.playDoa) isDoaPlayed =
            true
        val isFajr = prayTimeKey == FAJR_KEY
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
            val athanUri = getAthanUri(setting, prayTimeKey, this)
            runCatching {
                MediaPlayer.create(this, athanUri).duration // is in milliseconds
            }.onFailure(logException).onSuccess {
                // if the URIs duration is less than half a minute, it is probably a looping one
                // so stop on half a minute regardless
                if (it < THIRTY_SECONDS_IN_MILLIS) stopAtHalfMinute = true
            }
            ringtone =
                RingtoneManager.getRingtone(this, getAthanUri(setting, prayTimeKey, this)).also {
                    it.audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    volumeControlStream = AudioManager.STREAM_ALARM
                    it.play()
                }
        }.onFailure(logException)

        applyAppLanguage(this)

        var prayTimes = coordinates.value?.calculatePrayTimes()
        prayTimes = PrayTimeProvider(this).nReplace(prayTimes, Jdn.today())
        val binding = ActivityNathanBinding.inflate(layoutInflater).apply {
            setContentView(root)
            athanName.setText(getPrayTimeName(prayTimeKey))
            btnStop.setOnClickListener { stop() }
            place.text = String.format(getString(R.string.in_city_time), appPrefs.cityName) + " " +
                    when (prayTimeKey) {
                        FAJR_KEY -> prayTimes?.getFromStringId(R.string.fajr)?.toFormattedString()
                        SUNRISE_KEY -> prayTimes?.getFromStringId(R.string.sunrise)
                            ?.toFormattedString()

                        DHUHR_KEY -> prayTimes?.getFromStringId(R.string.dhuhr)?.toFormattedString()
                        ASR_KEY -> prayTimes?.getFromStringId(R.string.asr)?.toFormattedString()
                        MAGHRIB_KEY -> prayTimes?.getFromStringId(R.string.maghrib)
                            ?.toFormattedString()

                        ISHA_KEY -> prayTimes?.getFromStringId(R.string.isha)?.toFormattedString()
                        else -> ""
                    }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.activityRoot.updatePadding(bottom = insets.bottom)
            binding.activityRoot.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }

        handler.postDelayed(stopTask, TEN_SECONDS_IN_MILLIS)

        if (setting.isAscending) handler.post(ascendVolume)
        preventPhoneCallIntervention.startListener(this)
    }

    override fun onPause() {
        super.onPause()
        if (isLockedAndPassed2Second())
            stop()
    }

    private fun isLockedAndPassed2Second(): Boolean {
        val keyguardManager = getSystemService<KeyguardManager>() ?: return true
        val nowDate = Date(System.currentTimeMillis())
        val diffInSecond = TimeUnit.MILLISECONDS.toSeconds(nowDate.time - startDate.time)
        return if (!keyguardManager.isKeyguardLocked)
            true
        else diffInSecond > 2
    }

    private fun stop() {
        if (alreadyStopped) return
        alreadyStopped = true

        ringtone?.stop()
        preventPhoneCallIntervention.stopListener()
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
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    prepare()
                    setOnCompletionListener { finish() }
                }.onFailure(logException)
                start()
            }
    }

}
