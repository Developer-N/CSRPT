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
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import ir.namoo.religiousprayers.*
import ir.namoo.religiousprayers.databinding.ActivityAthanBinding
import ir.namoo.religiousprayers.utils.*
import java.io.IOException
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
            try {
                if (!isDoaPlayed &&
                    ((mediaPlayer != null && mediaPlayer?.isPlaying == false) || mediaPlayer == null)
                ) {
                    playDoa()
                } else if ((mediaPlayer == null && doaPlayer == null) ||
                    !(doaPlayer?.isPlaying == true || mediaPlayer?.isPlaying == true)
                ) return this@AthanActivity.finish()
                handler.postDelayed(this, TimeUnit.SECONDS.toMillis(1))
            } catch (e: Exception) {
                e.printStackTrace()
                return this@AthanActivity.finish()
            }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Workaround AlarmManager (or the way we use it) that calls it multiple times,
        // don't run if it is ran less than 10 seconds ago
        val currentMillis = System.currentTimeMillis()
        if (currentMillis - lastStart < TimeUnit.SECONDS.toMillis(10)) return finish()
        lastStart = currentMillis
        //
        audioManager = getSystemService()
        audioManager?.let { am ->
            am.setStreamVolume(
                AudioManager.STREAM_ALARM,
                athanVolume.takeUnless { it == DEFAULT_ATHAN_VOLUME } ?: am.getStreamVolume(
                    AudioManager.STREAM_ALARM
                ),
                0
            )
        }
        val prayerKey = intent.getStringExtra(KEY_EXTRA_PRAYER_KEY)
        val prayerTime = intent.getStringExtra(KEY_EXTRA_PRAYER_TIME)
        val isFajr = "FAJR" == prayerKey
        val isSunRise = "SUNRISE" == prayerKey
        val isBeforeFajr = "BFAJR" == prayerKey
        if (isSunRise) isDoaPlayed = true

        try {
            mediaPlayer = MediaPlayer().apply {
                try {
                    when {
                        isBeforeFajr -> setDataSource(
                            this@AthanActivity,
                            getBeforeFajrUri(this@AthanActivity)
                        )
                        isFajr -> setDataSource(
                            this@AthanActivity,
                            getFajrAthanUri(this@AthanActivity)
                        )
                        isSunRise -> setDataSource(
                            this@AthanActivity,
                            getSunriseUri(this@AthanActivity)
                        )
                        else -> setDataSource(
                            this@AthanActivity,
                            getAthanUri(this@AthanActivity)
                        )
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
                    if (isBeforeFajr)
                        isLooping = true
                    prepare()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


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

        if (isAscendingAthanVolumeEnabled) handler.post(ascendVolume)

        try {
            getSystemService<TelephonyManager>()?.listen(
                phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playDoa() {
        isDoaPlayed = true
        if (appPrefs.getBoolean(PREF_PLAY_DOA, false))//play doa
            doaPlayer = MediaPlayer().apply {
                try {
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
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
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

        try {
            getSystemService<TelephonyManager>()?.listen(
                phoneStateListener, PhoneStateListener.LISTEN_NONE
            )
            phoneStateListener = null
        } catch (e: RuntimeException) {
            Log.e("Athan", "TelephonyManager handling fail", e)
        }

        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                    release()
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        try {
            doaPlayer?.apply {
                if (isPlaying) {
                    stop()
                    release()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


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
