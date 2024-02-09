package com.byagowi.persiancalendar.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.os.postDelayed
import com.byagowi.persiancalendar.ASR_KEY
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.DHUHR_KEY
import com.byagowi.persiancalendar.FAJR_KEY
import com.byagowi.persiancalendar.ISHA_KEY
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.MAGHRIB_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.notificationAthan
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.athan.PreventPhoneCallIntervention
import com.byagowi.persiancalendar.utils.SIX_MINUTES_IN_MILLIS
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.getAthanUri
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getPrayTimeName
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.setDirection
import com.byagowi.persiancalendar.utils.startAthanActivity
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import org.koin.android.ext.android.get
import kotlin.random.Random

class AthanNotification : Service() {

    private val prayTimeProvider: PrayTimeProvider = get()
    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return START_STICKY
        applyAppLanguage(this)

        val notificationId =
            if (BuildConfig.DEVELOPMENT) Random.nextInt(2000, 4000)
            else (if (notificationAthan.value) 3000 else 3001)
        val notificationChannelId = notificationId.toString()

        val notificationManager = getSystemService<NotificationManager>()

        val athanKey = intent.getStringExtra(KEY_EXTRA_PRAYER)
        if (!notificationAthan.value) startAthanActivity(this, athanKey)

        val soundUri = if (notificationAthan.value) getAthanUri(this) else null
        if (soundUri != null) runCatching {
            // ensure custom reminder sounds play well
            grantUriPermission(
                "com.android.systemui", soundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }.onFailure(logException)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                notificationChannelId, getString(R.string.athan),
                if (notificationAthan.value) NotificationManager.IMPORTANCE_HIGH
                else NotificationManager.IMPORTANCE_DEFAULT
            ).also {
                it.description = getString(R.string.athan)
                it.enableLights(true)
                it.lightColor = Color.GREEN
                it.vibrationPattern = LongArray(2) { 500 }
                it.enableVibration(true)
                it.setBypassDnd(athanKey == FAJR_KEY)
                if (soundUri == null) it.setSound(null, null) else it.setSound(
                    soundUri, AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build()
                )
            }
            notificationManager?.createNotificationChannel(notificationChannel)
        }

        val cityName = cityName.value
        val prayTimeName = getString(getPrayTimeName(athanKey))
        val title =
            if (cityName == null) prayTimeName
            else "$prayTimeName$spacedComma${getString(R.string.in_city_time, cityName)}"

        var prayTimes = coordinates.value?.calculatePrayTimes()
        prayTimes = prayTimeProvider.replace(prayTimes, Jdn.today())
        val subtitle = when (athanKey) {
            FAJR_KEY -> listOf(R.string.sunrise)
            DHUHR_KEY ->
                if (calculationMethod.value.isJafari) listOf(R.string.sunset)
                else listOf(R.string.asr, R.string.maghrib)

            ASR_KEY -> listOf(R.string.maghrib)
            MAGHRIB_KEY ->
                if (calculationMethod.value.isJafari) listOf(R.string.midnight)
                else listOf(R.string.isha, R.string.midnight)

            ISHA_KEY -> listOf(R.string.midnight)
            else -> listOf(R.string.midnight)
        }.joinToString(" - ") {
            "${getString(it)}: ${prayTimes?.getFromStringId(it)?.toFormattedString() ?: ""}"
        }

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
        notificationBuilder.setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.sun)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (notificationAthan.value) {
            notificationBuilder.priority = NotificationCompat.PRIORITY_MAX
            notificationBuilder.setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
            notificationBuilder.setCategory(NotificationCompat.CATEGORY_ALARM)
        } else {
            notificationBuilder.setSound(null)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val cv = RemoteViews(applicationContext?.packageName, R.layout.custom_notification)
            cv.setDirection(R.id.custom_notification_root, this.resources)
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

        startForeground(notificationId, notificationBuilder.build())

        var stop = {}
        val preventPhoneCallIntervention =
            if (notificationAthan.value) PreventPhoneCallIntervention(stop) else null
        stop = {
            preventPhoneCallIntervention?.let { it.stopListener() }
            notificationManager?.cancel(notificationId)
            stopSelf()
        }

        preventPhoneCallIntervention?.startListener(this)
        Handler(Looper.getMainLooper()).postDelayed(SIX_MINUTES_IN_MILLIS) { stop() }

        return super.onStartCommand(intent, flags, startId)
    }
}
