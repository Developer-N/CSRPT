package ir.namoo.religiousprayers.ui.azkar

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.byagowi.persiancalendar.ALARMS_BASE_ID
import com.byagowi.persiancalendar.ALARM_TAG
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.utils.FIFTEEN_MINUTES_IN_MILLIS
import com.byagowi.persiancalendar.utils.THIRTY_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.enableWorkManager
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.variants.debugLog
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.BROADCAST_AZKAR
import ir.namoo.commons.KEY_AZKAR_EXTRA_NAME
import ir.namoo.commons.KEY_AZKAR_EXTRA_TIME
import ir.namoo.commons.PREF_AZKAR_REINDER
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import kotlinx.coroutines.coroutineScope
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

fun scheduleAzkars(context: Context) {
    var prayTimes: PrayTimes = coordinates.value?.calculatePrayTimes() ?: return
    PrayTimeProvider(context).nReplace(prayTimes, Jdn.today())?.let {
        prayTimes = it
    }
    val morningAzkarTime = Calendar.getInstance().also {
        val alarmTime = Clock.fromMinutesCount(
            prayTimes.getFromStringId(R.string.fajr).toMinutes() + 30
        )
        it[Calendar.HOUR_OF_DAY] = alarmTime.hours
        it[Calendar.MINUTE] = alarmTime.minutes
        it[Calendar.SECOND] = 0
    }.timeInMillis

    scheduleAzkar(context, "morning", morningAzkarTime, 17)

    val eveningAzkarTime = Calendar.getInstance().also {
        val alarmTime = Clock.fromMinutesCount(
            prayTimes.getFromStringId(R.string.asr).toMinutes() + 45
        )
        it[Calendar.HOUR_OF_DAY] = alarmTime.hours
        it[Calendar.MINUTE] = alarmTime.minutes
        it[Calendar.SECOND] = 0
    }.timeInMillis
    scheduleAzkar(context, "evening", eveningAzkarTime, 18)
}

private fun scheduleAzkar(context: Context, azkarName: String, timeInMillis: Long, i: Int) {
    val remainedMillis = timeInMillis - System.currentTimeMillis()
    debugLog("Azkar: $azkarName in ${remainedMillis / 60000} minutes")
    if (remainedMillis < 0) return // Don't set alarm in past

    if (enableWorkManager) { // Schedule in both, startAthan has the logic to skip duplicated calls
        val workerInputData = Data.Builder().putLong(KEY_AZKAR_EXTRA_TIME, timeInMillis)
            .putString(KEY_AZKAR_EXTRA_NAME, azkarName).build()
        val alarmWorker = OneTimeWorkRequest.Builder(AzkarWorker::class.java)
            .setInitialDelay(remainedMillis, TimeUnit.MILLISECONDS)
            .setInputData(workerInputData)
            .build()
        WorkManager.getInstance(context)
            .beginUniqueWork(ALARM_TAG + i, ExistingWorkPolicy.REPLACE, alarmWorker)
            .enqueue()
    }

    val am = context.getSystemService<AlarmManager>() ?: return
    val pendingIntent = PendingIntent.getBroadcast(
        context, ALARMS_BASE_ID + i,
        Intent(context, BroadcastReceivers::class.java)
            .putExtra(KEY_AZKAR_EXTRA_NAME, azkarName)
            .putExtra(KEY_AZKAR_EXTRA_TIME, timeInMillis)
            .setAction(BROADCAST_AZKAR),
        PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )
    when {
        Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 ->
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        else -> am.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }
}

class AzkarWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = coroutineScope {
        val key = inputData.getString(KEY_AZKAR_EXTRA_NAME) ?: "morning"
        val intendedTime = inputData.getLong(KEY_AZKAR_EXTRA_TIME, 0).takeIf { it != 0L }
        debugLog("Azkar: WorkManager for $key")
        startAzkar(applicationContext, key, intendedTime)
        Result.success()
    }
}

fun startAzkar(context: Context, name: String, intendedTime: Long?) {
    debugLog("Azkar: startAzkar for $name")
    if (intendedTime == null) return startAzkarBody(context, name)
    // if alarm is off by 15 minutes, just skip
    if (abs(System.currentTimeMillis() - intendedTime) > FIFTEEN_MINUTES_IN_MILLIS) return

    // If at the of being is disabled by user, skip
    if (!context.appPrefsLite.getBoolean(PREF_AZKAR_REINDER, false)) return

    startAzkarBody(context, name)
}


private var lastAzkarKey = ""
private var lastAzkarJdn: Jdn? = null

fun startAzkarBody(context: Context, name: String) = runCatching {
    applyAppLanguage(context)
    val today = Jdn.today()
    if (lastAzkarJdn == today && lastAzkarKey == name) return
    lastAzkarJdn = today; lastAzkarKey = name
    debugLog("Azkar: startAzkarBody for $name")
    runCatching {
        @Suppress("Deprecation")
        context.getSystemService<PowerManager>()?.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK,
            "SunnahCalendar:azkar"
        )?.acquire(THIRTY_SECONDS_IN_MILLIS)
    }.onFailure(logException)
    val notificationId = if (name == "morning") 2023 else 2024
    val notificationChannelId = notificationId.toString()

    val notificationManager = context.getSystemService<NotificationManager>()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            notificationChannelId,
            context.getString(if (name == "morning") R.string.morning_azkar else R.string.evening_azkar),
            NotificationManager.IMPORTANCE_HIGH
        ).also {
            it.description = context.getString(R.string.azkar_reminder)
            it.enableLights(true)
            it.lightColor = Color.GREEN
            it.vibrationPattern = LongArray(2) { 500 }
            it.enableVibration(true)
        }
        notificationManager?.createNotificationChannel(notificationChannel)
    }
    val title =
        if (name == "morning") context.getString(R.string.morning_azkar) else context.getString(R.string.evening_azkar)
    val subtitle = String.format(context.getString(R.string.for_read_azkar_tap), title)
    val notificationBuilder = NotificationCompat.Builder(context, notificationChannelId)

    val id = if (name == "morning") 27 else 28
    val azkarIntent = Intent(context.applicationContext, AzkarActivity::class.java).also { intent ->
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("chapterID", id)
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
        context,
        0,
        azkarIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )

    notificationBuilder.setAutoCancel(true).setWhen(System.currentTimeMillis())
        .setSmallIcon(R.drawable.ic_azkar).setContentTitle(title).setContentText(subtitle)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setContentIntent(pendingIntent)
    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
            notify(notificationId, notificationBuilder.build())
    }

}.onFailure(logException).let { }
