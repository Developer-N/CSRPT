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
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.byagowi.persiancalendar.ALARMS_BASE_ID
import com.byagowi.persiancalendar.ALARM_TAG
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.debugLog
import com.byagowi.persiancalendar.utils.logException
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.BROADCAST_AZKAR
import ir.namoo.commons.KEY_AZKAR_EXTRA_NAME
import ir.namoo.commons.KEY_AZKAR_EXTRA_TIME
import ir.namoo.commons.PREF_AZKAR_REINDER
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import kotlinx.coroutines.coroutineScope
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun scheduleAzkars(context: Context, prayTimeProvider: PrayTimeProvider) {
    var prayTimes: PrayTimes = coordinates.value?.calculatePrayTimes() ?: return
    prayTimes = prayTimeProvider.replace(prayTimes, Jdn.today())
    val morningAzkarTime = Calendar.getInstance().also {
        val alarmTime = Clock(prayTimes.fajr).plus(Clock(0.5)).toHoursAndMinutesPair()
        it[Calendar.HOUR_OF_DAY] = alarmTime.first
        it[Calendar.MINUTE] = alarmTime.second
        it[Calendar.SECOND] = 0
    }.timeInMillis

    scheduleAzkar(context, "morning", morningAzkarTime, 17)

    val eveningAzkarTime = Calendar.getInstance().also {
        val alarmTime = Clock(prayTimes.asr).plus(Clock(0.5)).toHoursAndMinutesPair()
        it[Calendar.HOUR_OF_DAY] = alarmTime.first
        it[Calendar.MINUTE] = alarmTime.second
        it[Calendar.SECOND] = 0
    }.timeInMillis
    scheduleAzkar(context, "evening", eveningAzkarTime, 18)

    val sleepAzkarTime = Calendar.getInstance().also {
        it[Calendar.HOUR_OF_DAY] = 21
        it[Calendar.MINUTE] = 45
        it[Calendar.SECOND] = 0
    }.timeInMillis
    scheduleAzkar(context, "sleep", sleepAzkarTime, 19)
}

private fun scheduleAzkar(context: Context, azkarName: String, timeInMillis: Long, i: Int) {
    val remainedMillis = timeInMillis - System.currentTimeMillis()
    debugLog("Azkar: $azkarName in ${remainedMillis / 60000} minutes")
    if (remainedMillis < 0) return // Don't set alarm in past

    run { // Schedule in both alarmmanager and workmanager, startAthan has the logic to skip duplicated calls
        val workerInputData = Data.Builder().putLong(KEY_AZKAR_EXTRA_TIME, timeInMillis)
            .putString(KEY_AZKAR_EXTRA_NAME, azkarName).build()
        val alarmWorker = OneTimeWorkRequestBuilder<AzkarWorker>()
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
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms())
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            am, AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
        )
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
    applyAppLanguage(context)
    val preferences = context.appPrefsLite
    if (intendedTime == null) return startAzkarBody(context, name)
    // if alarm is off by 15 minutes, just skip
    if (abs(System.currentTimeMillis() - intendedTime).milliseconds > 15.minutes) return

    // If at the of being is disabled by user, skip
    if (!preferences.getBoolean(PREF_AZKAR_REINDER, false)) return

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
        )?.acquire(30.seconds.inWholeMilliseconds)
    }.onFailure(logException)
    val notificationId = when (name) {
        "morning" -> 2023
        "evening" -> 2024
        else -> 2025
    }
    val notificationChannelId = notificationId.toString()

    val notificationManager = context.getSystemService<NotificationManager>()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            notificationChannelId,
            context.getString(
                when (name) {
                    "morning" -> R.string.morning_azkar
                    "evening" -> R.string.evening_azkar
                    else -> R.string.sleep_azkar
                }
            ),
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
    val title = context.getString(R.string.azkar_notification_title)
    val zikrTitle =
        when (name) {
            "morning" -> context.getString(R.string.morning_azkar)
            "evening" -> context.getString(R.string.evening_azkar)
            else -> context.getString(R.string.sleep_azkar)
        }
    val subtitle = String.format(context.getString(R.string.azkar_notification_message), zikrTitle)
    val notificationBuilder = NotificationCompat.Builder(context, notificationChannelId)

    val id = when (name) {
        "morning" -> 27
        "evening" -> 28
        else -> 29
    }
    val azkarIntent = Intent(context.applicationContext, AzkarActivity::class.java).also { intent ->
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("chapterID", id)
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
        context,
        0,
        azkarIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    notificationBuilder.setAutoCancel(true).setWhen(System.currentTimeMillis())
        .setSmallIcon(R.drawable.ic_azkar).setContentTitle(title).setContentText(subtitle)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setContentIntent(pendingIntent)
        .addAction(
            NotificationCompat.Action.Builder(
                R.drawable.ic_azkar,
                context.getString(R.string.read_azkar),
                pendingIntent
            ).build()
        )
    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
            notify(notificationId, notificationBuilder.build())
    }

}.onFailure(logException).let { }
