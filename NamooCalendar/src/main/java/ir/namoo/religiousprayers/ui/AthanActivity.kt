package ir.namoo.religiousprayers.ui

import android.app.KeyguardManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import ir.namoo.religiousprayers.*
import ir.namoo.religiousprayers.databinding.ActivityAthanBinding
import ir.namoo.religiousprayers.db.AthanSetting
import ir.namoo.religiousprayers.db.AthanSettingsDB
import ir.namoo.religiousprayers.utils.*
import java.util.concurrent.TimeUnit

class AthanActivity : AppCompatActivity() {

    private val ascendingVolumeStep = 6
    private var currentVolumeSteps = 1
    private var audioManager: AudioManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null
    private var doaPlayer: MediaPlayer? = null
    private var alreadyStopped = false
    private var isDoaPlayed = false
    private val stopTask = object : Runnable {
        override fun run() {
            runCatching {
                if (!isDoaPlayed &&
                    ((mediaPlayer != null && mediaPlayer?.isPlaying == false) || mediaPlayer == null)
                ) {
                    playDoa()
                } else if ((mediaPlayer == null && doaPlayer == null) ||
                    !(doaPlayer?.isPlaying == true || mediaPlayer?.isPlaying == true)
                ) return this@AthanActivity.finish()
                handler.postDelayed(this, TimeUnit.SECONDS.toMillis(1))
            }.onFailure(logException).getOrElse { return this@AthanActivity.finish() }
        }
    }

    private val ascendVolume = object : Runnable {
        override fun run() {
            currentVolumeSteps++
            audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, currentVolumeSteps, 0)
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

    lateinit var setting: AthanSetting

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(getThemeFromName(getThemeFromPreference(this, appPrefs)))
        // Workaround AlarmManager (or the way we use it) that calls it multiple times,
        // don't run if it is ran less than 10 seconds ago
        val currentMillis = System.currentTimeMillis()
        if (currentMillis - lastStart < TimeUnit.SECONDS.toMillis(10)) return finish()
        lastStart = currentMillis
        //
        val prayerKey = intent.getStringExtra(KEY_EXTRA_PRAYER_KEY) ?: return
        setting =
            AthanSettingsDB.getInstance(applicationContext).athanSettingsDAO().getAllAthanSettings()
                ?.filter { prayerKey.contains(it.athanKey) }?.get(0)
                ?: return
        audioManager = getSystemService()
        audioManager?.setStreamVolume(
            AudioManager.STREAM_ALARM,
            setting.athanVolume,
            0
        )
        val prayerTime = intent.getStringExtra(KEY_EXTRA_PRAYER_TIME)
        if (prayerKey[0] == 'B') isDoaPlayed = true

        runCatching {
            mediaPlayer = MediaPlayer().apply {
                runCatching {
                    if (prayerKey[0] == 'B') {
                        setDataSource(
                            this@AthanActivity, if (setting.alertURI == "")
                                getDefaultBeforeAlertUri(this@AthanActivity) else setting.alertURI.toUri()
                        )

                    } else {
                        if (setting.athanURI == "")
                            setDataSource(
                                this@AthanActivity,
                                when (setting.athanKey) {
                                    "FAJR" -> getDefaultFajrAthanUri(this@AthanActivity)
                                    "SUNRISE" -> getDefaultBeforeAlertUri(this@AthanActivity)
                                    else -> getDefaultAthanUri(this@AthanActivity)
                                }
                            )
                        else
                            setDataSource(this@AthanActivity, setting.athanURI.toUri())
                    }
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
                    volumeControlStream = AudioManager.STREAM_ALARM
                    if (prayerKey[0] == 'B')
                        isLooping = true
                    prepare()
                }.onFailure(logException)
                start()
            }
        }.onFailure(logException)

        applyAppLanguage(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            getSystemService<KeyguardManager>()?.requestDismissKeyguard(this, null)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }



        ActivityAthanBinding.inflate(layoutInflater).apply {
            setContentView(root)
            athanName.setText(getPrayTimeText(prayerKey))

            btnStop.setOnClickListener { stop() }
//            root.setBackgroundResource(getPrayTimeImage(prayerKey))
            root.setBackgroundResource(R.drawable.adhan_background)

            place.text = listOf(
                getString(R.string.in_city_time), getCityName(this@AthanActivity, true), prayerTime
            ).joinToString(" ")
        }

        handler.postDelayed(stopTask, TimeUnit.SECONDS.toMillis(30))

        if (setting.isAscending) handler.post(ascendVolume)

        runCatching {
            getSystemService<TelephonyManager>()?.listen(
                phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE
            )
        }.onFailure(logException)
    }

    fun playDoa() {
        isDoaPlayed = true
        if (setting.playDoa)//play doa
            doaPlayer = MediaPlayer().apply {
                runCatching {
                    setDataSource(
                        this@AthanActivity,
                        getDefaultDOAUri(this@AthanActivity)
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
                    volumeControlStream = AudioManager.STREAM_ALARM
                    prepare()
                }.onFailure(logException)
                start()
            }
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

        runCatching {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                    release()
                }
            }
        }.onFailure(logException)

        runCatching {
            doaPlayer?.apply {
                if (isPlaying) {
                    stop()
                    release()
                }
            }
        }.onFailure(logException)

        handler.removeCallbacks(stopTask)
        handler.removeCallbacks(ascendVolume)
        finish()
    }

    override fun onStop() {
        update(this, false)
        super.onStop()
    }

    companion object {
        private var lastStart = 0L
    }
}
