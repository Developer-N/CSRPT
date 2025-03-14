package ir.namoo.religiousprayers.ui.athan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.entities.PrayTime.Companion.get
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.athan.PreventPhoneCallIntervention
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.setDirection
import ir.namoo.commons.model.AthanSetting
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.utils.getAthanUri
import ir.namoo.commons.utils.getDefaultDOAUri
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val ACTION_STOP = "ir.namoo.religiousprayers.ACTION_STOP"

class NAthanNotification : Service() {
    private val prayTimeProvider: PrayTimeProvider = get()
    val athanSettingsDB: AthanSettingsDB = get()
    private val notificationId = if (BuildConfig.DEVELOPMENT) Random.nextInt(2000, 4000) else 3000
    private var notificationManager: NotificationManager? = null
    private var setting: AthanSetting? = null

    private val ascendingVolumeStep = 6
    private var currentVolumeSteps = 1
    private val handler = Handler(Looper.getMainLooper())
    private var ringtone: Ringtone? = null
    private var alreadyStopped = false
    private var spentSeconds = 0
    private var originalVolume = -1
    private var isDoaPlayed = false
    private var doaPlayer: MediaPlayer? = null
    private var stopAtHalfMinute = false
    private var prayerKey: String? = null
    private val preventPhoneCallIntervention = PreventPhoneCallIntervention(::onDestroy)
    private var bFajrCount = 0
    private val stopTask = object : Runnable {
        override fun run() = runCatching {
            spentSeconds += 5
            if (ringtone == null || ringtone?.isPlaying == false || spentSeconds > 360 || (stopAtHalfMinute && spentSeconds > 30)) {
                if (prayerKey == "BFAJR" && bFajrCount < 5 && ringtone?.isPlaying == false) {
                    ringtone?.play()
                    bFajrCount++
                    handler.postDelayed(this, 5.seconds.inWholeMilliseconds)
                } else if (!isDoaPlayed) playDoa() else {
                    //Do Nothing
                }
            } else {
                handler.postDelayed(
                    this, 5.seconds.inWholeMilliseconds
                )
            }
        }.onFailure(logException).let {}
    }
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

    override fun onBind(intent: Intent): IBinder? = null

    @Suppress("Deprecation")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return START_NOT_STICKY
        applyAppLanguage(this)
        runCatching {
            val telephonyManager = getSystemService<TelephonyManager>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyManager?.registerTelephonyCallback(mainExecutor,
                    object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                        override fun onCallStateChanged(state: Int) {
                            when (state) {
                                TelephonyManager.CALL_STATE_IDLE -> {}
                                else -> stopSelf()
                            }
                        }
                    })
            } else if (telephonyManager?.callState != TelephonyManager.CALL_STATE_IDLE) stopSelf()
            else {
                //do nothing
            }
        }.onFailure(logException)
        if (intent.action?.equals(ACTION_STOP) == true) {
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        } else {
            applyAppLanguage(this)

            val notificationChannelId = notificationId.toString()

            prayerKey = intent.getStringExtra(KEY_EXTRA_PRAYER) ?: ""

            notificationManager = getSystemService()
            runBlocking {
                setting = athanSettingsDB.athanSettingsDAO().getAllAthanSettings()
                    .find { prayerKey?.contains(it.athanKey) == true }
                    ?: return@runBlocking super.onStartCommand(intent, flags, startId)
                val soundUri = getAthanUri(setting, prayerKey, applicationContext)
                if (prayerKey?.startsWith("B") == true || (prayerKey?.startsWith("A") == true && prayerKey != PrayTime.ASR.name) || setting?.playDoa == false) isDoaPlayed =
                    true
                runCatching {
                    // ensure custom reminder sounds play well
                    grantUriPermission(
                        "com.android.systemui", soundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }.onFailure(logException)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    notificationChannelId,
                    getString(R.string.athan),
                    NotificationManager.IMPORTANCE_HIGH
                ).also {
                    it.description = getString(R.string.athan)
                    it.enableLights(true)
                    it.lightColor = Color.GREEN
                    it.vibrationPattern = LongArray(2) { 500 }
                    it.enableVibration(true)
                    it.setBypassDnd(prayerKey == PrayTime.FAJR.name || prayerKey == "B${PrayTime.FAJR.name}")
                }
                notificationManager?.createNotificationChannel(notificationChannel)
            }


            val cityName = cityName.value
            val prayTimeName = prayerKey?.let { getStringForKey(it) } ?: "-"
            val title = if (cityName == null) prayTimeName
            else "$prayTimeName$spacedComma${getString(R.string.in_city_time, cityName)}"

            var prayTimes = coordinates.value?.calculatePrayTimes() ?: return super.onStartCommand(
                intent, flags, startId
            )
            prayTimes = prayTimeProvider.replace(prayTimes, Jdn.today())
            val subtitle = when (prayerKey) {
                PrayTime.FAJR.name -> listOf(PrayTime.SUNRISE, PrayTime.DHUHR)
                PrayTime.DHUHR.name -> listOf(PrayTime.ASR, PrayTime.MAGHRIB)
                PrayTime.ASR.name -> listOf(PrayTime.MAGHRIB, PrayTime.ISHA)
                PrayTime.MAGHRIB.name -> listOf(PrayTime.ISHA)
                PrayTime.ISHA.name -> listOf()
                else -> listOf()
            }.joinToString(" - ") {
                getString(it.stringRes)
                "${getString(it.stringRes)}: ${Clock(prayTimes[it].value).toFormattedString()}"
            }

            val stopIntent = Intent(this, NAthanNotification::class.java).apply {
                action = ACTION_STOP
            }
            val stop = PendingIntent.getService(
                this, 0, stopIntent, if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            notificationBuilder.setOngoing(true).setWhen(System.currentTimeMillis())
                .setSilent(setting?.playType != 2)//silent if not just notification
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title).setContentText(subtitle)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.ic_stop, getString(R.string.close), stop)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val cv = RemoteViews(applicationContext?.packageName, R.layout.custom_notification)
                cv.setDirection(R.id.custom_notification_root, this.resources)
                cv.setTextViewText(R.id.title, title)
                if (subtitle.isEmpty()) {
                    cv.setViewVisibility(R.id.body, View.GONE)
                } else {
                    cv.setTextViewText(R.id.body, subtitle)
                }

                notificationBuilder.setCustomContentView(cv)
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            }

            notificationManager?.notify(notificationId, notificationBuilder.build())

            preventPhoneCallIntervention.startListener(this)
            //########################################################## Play Athan
            if (setting?.playType == 1) {
                val isFajr = prayerKey == PrayTime.FAJR.name
                var goMute = false

                getSystemService<AudioManager>()?.let { audioManager ->
                    originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_ALARM, setting?.athanVolume ?: 1, 0
                    )
                    // Mute if system alarm is set to lowest, ringer mode is silent/vibration and it isn't Fajr
                    if (originalVolume == 1 && !isFajr && audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) goMute =
                        true
                }
                if (!goMute) runCatching {
                    val athanUri = getAthanUri(setting, prayerKey, this)
                    runCatching {
                        MediaPlayer.create(this, athanUri).duration.milliseconds
                    }.onFailure(logException)
                        .onSuccess { if (it < 30.seconds) stopAtHalfMinute = true }
                    ringtone =
                        RingtoneManager.getRingtone(this, getAthanUri(setting, prayerKey, this))
                            .also {
                                it.audioAttributes = AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_ALARM).build()
                                it.play()
                            }
                }.onFailure(logException)
                handler.postDelayed(stopTask, 10.seconds.inWholeMilliseconds)
                if (setting?.isAscending == true) handler.post(ascendVolume)

            }
            return START_STICKY
        }
    }

    override fun onDestroy() {
        notificationManager?.cancel(notificationId)
        if (originalVolume != -1) getSystemService<AudioManager>()?.setStreamVolume(
            AudioManager.STREAM_ALARM, originalVolume, 0
        )
        if (alreadyStopped) return
        alreadyStopped = true

        ringtone?.stop()
        preventPhoneCallIntervention.stopListener()
        runCatching {
            if (doaPlayer != null) if (doaPlayer!!.isPlaying) {
                doaPlayer!!.release()
                doaPlayer!!.stop()
            }
        }.onFailure(logException)

        if (setting?.isAscending == true) handler.removeCallbacks(ascendVolume)
        handler.removeCallbacks(stopTask)
        super.onDestroy()
    }

    private fun playDoa() {
        isDoaPlayed = true
        if (setting?.playDoa == true)//play doa
            doaPlayer = MediaPlayer().apply {
                runCatching {
                    setDataSource(
                        this@NAthanNotification, getDefaultDOAUri(this@NAthanNotification)
                    )
                    setAudioAttributes(
                        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
                    )
                    prepare()
                }.onFailure(logException)
                start()
            }
    }

}//end of class NAthanNotification
