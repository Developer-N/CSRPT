package ir.namoo.religiousprayers.service

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import ir.namoo.religiousprayers.KEY_EXTRA_PRAYER_KEY
import ir.namoo.religiousprayers.KEY_EXTRA_PRAYER_TIME
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.db.AthanSetting
import ir.namoo.religiousprayers.db.AthanSettingsDB
import ir.namoo.religiousprayers.utils.*
import java.io.IOException
import java.util.concurrent.TimeUnit


private const val NOTIFICATION_ID = 1002
private const val NOTIFICATION_CHANNEL_ID = NOTIFICATION_ID.toString()

class AthanNotification : IntentService("NotificactionService") {


    override fun onBind(intent: Intent): IBinder? = null
    override fun onHandleIntent(intent: Intent?) {
        if (intent?.action != null && intent.action.equals(ACTION_STOP))
            stop(applicationContext)
    }

    companion object {
        private var lastStart = 0L
        private const val ACTION_STOP = "ir.namoo.owghateshareisardasht.ACTION_STOP"
        private var audioManager: AudioManager? = null
        private var mediaPlayer: MediaPlayer? = null
        private var isDoaPlayed = false
        private var doaPlayer: MediaPlayer? = null
        private const val ascendingVolumeStep = 6
        private var currentVolumeSteps = 1
        private val handler = Handler()
        lateinit var setting: AthanSetting
        private val ascendVolume = object : Runnable {
            override fun run() {
                currentVolumeSteps++
                audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, currentVolumeSteps, 0)
                handler.postDelayed(this, TimeUnit.SECONDS.toMillis(ascendingVolumeStep.toLong()))
                if (currentVolumeSteps == 10) handler.removeCallbacks(this)
            }
        }

        fun notify(context: Context, intent: Intent) {
            val notificationManager = context.getSystemService<NotificationManager>()

            // don't run if it is ran less than 10 seconds ago
            val currentMillis = System.currentTimeMillis()
            if (currentMillis - lastStart < TimeUnit.SECONDS.toMillis(10)) {
                stop(context)
                return
            }
            lastStart = currentMillis
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.app_name)
                    enableLights(true)
                    lightColor = Color.GREEN
                    vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                    enableVibration(true)
                }
                notificationManager?.createNotificationChannel(notificationChannel)
            }

            val prayerKey = intent.getStringExtra(KEY_EXTRA_PRAYER_KEY) ?: return
            setting = AthanSettingsDB.getInstance(context.applicationContext).athanSettingsDAO()
                .getAllAthanSettings()?.filter { prayerKey.contains(it.athanKey) }?.get(0)
                ?: return
            val athanTime = intent.getStringExtra(KEY_EXTRA_PRAYER_TIME)
            val cityName = getCityName(context, false)
            var title =
                if (cityName.isNotEmpty()) context.getString(getPrayTimeText(prayerKey))
                else "${context.getString(getPrayTimeText(prayerKey))} - ${context.getString(R.string.in_city_time)} $cityName"
            title += " $athanTime"

            val subtitle = when (prayerKey) {
                "FAJR" -> listOf(R.string.sunrise)
                "SUNRISE" -> listOf(R.string.dhuhr)
                "DHUHR" -> listOf(R.string.asr)
                "ASR" -> listOf(R.string.maghrib)
                "MAGHRIB" -> listOf(R.string.isha)
                "ISHA" -> listOf(R.string.fajr)
                else -> listOf(R.string.fajr)
            }.joinToString(" - ") {
                " ${context.getString(it)}: ${
                    getClockFromStringId(
                        it,
                        context
                    ).toFormattedString()
                }"
            }
            val stopIntent =
                Intent(context, AthanNotification::class.java).setAction(ACTION_STOP)
            val stop = PendingIntent.getService(context, 0, stopIntent, 0)

            val notificationBuilder = NotificationCompat.Builder(
                context,
                NOTIFICATION_CHANNEL_ID
            )
            notificationBuilder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(subtitle)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                notificationBuilder.addAction(
                    R.drawable.ic_stop,
                    context.resources.getString(R.string.stop),
                    stop
                )
            else
                notificationBuilder.addAction(
                    R.drawable.ic_stop,
                    context.resources.getString(R.string.stop),
                    stop
                )
            notificationBuilder.setDeleteIntent(stop)

            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val cv = RemoteViews(
                    context.applicationContext?.packageName, if (isLocaleRTL())
                        R.layout.custom_notification
                    else
                        R.layout.custom_notification_ltr
                )
                cv.setTextViewText(R.id.title, title)
                if (subtitle.isEmpty()) {
                    cv.setViewVisibility(R.id.body, View.GONE)
                } else {
                    cv.setTextViewText(R.id.body, subtitle)
                }

                notificationBuilder
                    .setCustomContentView(cv)
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            }

            if (prayerKey[0] == 'B') isDoaPlayed = true

            notificationBuilder.priority =
                if (setting.playType == 1 && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                ) NotificationCompat.PRIORITY_LOW
                else NotificationCompat.PRIORITY_HIGH

            notificationManager?.notify(NOTIFICATION_ID, notificationBuilder.build())

            if (setting.playType == 1)
                try {
                    audioManager = context.getSystemService()
                    audioManager?.setStreamVolume(
                        AudioManager.STREAM_ALARM,
                        setting.athanVolume,
                        0
                    )
                    mediaPlayer = MediaPlayer().apply {
                        try {
                            if (prayerKey[0] == 'B') {
                                setDataSource(
                                    context, if (setting.alertURI == "")
                                        getDefaultBeforeAlertUri(context) else setting.alertURI.toUri()
                                )

                            } else {
                                if (setting.athanURI == "")
                                    setDataSource(
                                        context,
                                        when (setting.athanKey) {
                                            "FAJR" -> getDefaultFajrAthanUri(context)
                                            "SUNRISE" -> getDefaultBeforeAlertUri(context)
                                            else -> getDefaultAthanUri(context)
                                        }
                                    )
                                else
                                    setDataSource(context, setting.athanURI.toUri())
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
                            setOnCompletionListener { if (!isDoaPlayed) playDoa(context) }
                            if (prayerKey[0] == 'B')
                                isLooping = true
                            prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        Handler(Looper.getMainLooper()).postDelayed({ start() }, 1000L)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            if (setting.isAscending) handler.post(ascendVolume)

            Handler(Looper.getMainLooper()).postDelayed({
                notificationManager?.cancel(NOTIFICATION_ID)
                stop(context)
            }, TimeUnit.MINUTES.toMillis(10))
        }

        private fun playDoa(context: Context) {
            isDoaPlayed = true
            if (setting.playDoa)//play doa
                doaPlayer = MediaPlayer().apply {
                    try {
                        setDataSource(
                            context,
                            getDefaultDOAUri(context)
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
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                    start()
                }
        }

        private fun stop(context: Context) {
            try {
                if (mediaPlayer != null)
                    if (mediaPlayer!!.isPlaying) {
                        mediaPlayer!!.release()
                        mediaPlayer!!.stop()
                    }
            } catch (ex: Exception) {
            }
            try {
                if (doaPlayer != null)
                    if (doaPlayer!!.isPlaying) {
                        doaPlayer!!.release()
                        doaPlayer!!.stop()
                    }
            } catch (ex: Exception) {
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            notificationManager!!.cancel(NOTIFICATION_ID)
            update(context, false)
            handler.removeCallbacks(ascendVolume)
            context.stopService(Intent(context, AthanNotification::class.java))

        }
    }//end of companion object
}
