package com.byagowi.persiancalendar.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.byagowi.persiancalendar.ALARMS_BASE_ID
import com.byagowi.persiancalendar.ALARM_TAG
import com.byagowi.persiancalendar.ASR_KEY
import com.byagowi.persiancalendar.BROADCAST_ALARM
import com.byagowi.persiancalendar.DEFAULT_ASCENDING_ATHAN_VOLUME
import com.byagowi.persiancalendar.DEFAULT_ATHAN_VOLUME
import com.byagowi.persiancalendar.DHUHR_KEY
import com.byagowi.persiancalendar.FAJR_KEY
import com.byagowi.persiancalendar.ISHA_KEY
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER_TIME
import com.byagowi.persiancalendar.MAGHRIB_KEY
import com.byagowi.persiancalendar.PREF_ASCENDING_ATHAN_VOLUME
import com.byagowi.persiancalendar.PREF_ATHAN_GAP
import com.byagowi.persiancalendar.PREF_ATHAN_URI
import com.byagowi.persiancalendar.PREF_ATHAN_VOLUME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SUNRISE_KRY
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.service.AlarmWorker
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.variants.debugLog
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.DEFAULT_FULL_SCREEN_METHOD
import ir.namoo.commons.DEFAULT_NOTIFICATION_METHOD
import ir.namoo.commons.PREF_AZKAR_REINDER
import ir.namoo.commons.PREF_FULL_SCREEN_METHOD
import ir.namoo.commons.PREF_NOTIFICATION_METHOD
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import ir.namoo.religiousprayers.ui.athan.NAthanActivity
import ir.namoo.religiousprayers.ui.athan.NAthanNotification
import ir.namoo.religiousprayers.ui.azkar.scheduleAzkars
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

// https://stackoverflow.com/a/69505596
fun Resources.getRawUri(@RawRes rawRes: Int) = "%s://%s/%s/%s".format(
    ContentResolver.SCHEME_ANDROID_RESOURCE, this.getResourcePackageName(rawRes),
    this.getResourceTypeName(rawRes), this.getResourceEntryName(rawRes)
)

val Context.athanVolume: Int get() = appPrefs.getInt(PREF_ATHAN_VOLUME, DEFAULT_ATHAN_VOLUME)

val Context.isAscendingAthanVolumeEnabled: Boolean
    get() = appPrefs.getBoolean(PREF_ASCENDING_ATHAN_VOLUME, DEFAULT_ASCENDING_ATHAN_VOLUME)

fun getAthanUri(context: Context): Uri =
    (context.appPrefs.getString(PREF_ATHAN_URI, null)?.takeIf { it.isNotEmpty() }
        ?: context.resources.getRawUri(R.raw.special)).toUri()

private var lastAthanKey = ""
private var lastAthanJdn: Jdn? = null
fun startAthan(context: Context, prayTimeKey: String, intendedTime: Long?) {
    debugLog("Alarms: startAthan for $prayTimeKey")
    if (intendedTime == null) return startAthanBody(context, prayTimeKey)
    // if alarm is off by 15 minutes, just skip
    if (abs(System.currentTimeMillis() - intendedTime) > FIFTEEN_MINUTES_IN_MILLIS) return

    // If at the of being is disabled by user, skip
    if (prayTimeKey !in getEnabledAlarms(context)) return

    // skips if already called through either WorkManager or AlarmManager
    val today = Jdn.today()
    if (lastAthanJdn == today && lastAthanKey == prayTimeKey) return
    lastAthanJdn = today; lastAthanKey = prayTimeKey

    startAthanBody(context, prayTimeKey)
}

private fun startAthanBody(context: Context, prayTimeKey: String) = runCatching {
    debugLog("Alarms: startAthanBody for $prayTimeKey")

    runCatching {
        context.getSystemService<PowerManager>()?.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK,
            "SunnahCalendar:alarm"
        )?.acquire(THIRTY_SECONDS_IN_MILLIS)
    }.onFailure(logException)

    val setting = AthanSettingsDB.getInstance(context.applicationContext).athanSettingsDAO()
        .getAllAthanSettings()
        .find { prayTimeKey.contains(it.athanKey) } ?: return@runCatching

    if (setting.playType != 0) {
        ContextCompat.startForegroundService(
            context, Intent(context, NAthanNotification::class.java)
                .putExtra(KEY_EXTRA_PRAYER, prayTimeKey)
        )
    } else {
        context.startActivity(
            Intent(context, NAthanActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(KEY_EXTRA_PRAYER, prayTimeKey)
        )
    }
}.onFailure(logException).let {}

fun getEnabledAlarms(context: Context): Set<String> {
//    if (coordinates == null) return emptySet()
//    return (context.appPrefs.getString(PREF_ATHAN_ALARM, null)?.trim() ?: return emptySet())
//        .splitIgnoreEmpty(",")
//        .toSet()
    val enabledAthans = AthanSettingsDB.getInstance(context.applicationContext).athanSettingsDAO()
        .getAllAthanSettings()
    return if (enabledAthans.isEmpty()) emptySet()
    else {
        val result = mutableSetOf<String>()
        for (a in enabledAthans) {
            if (a.state)
                result.add(a.athanKey)
            if (a.isBeforeEnabled)
                result.add("B${a.athanKey}")
            if (a.isAfterEnabled)
                result.add("A${a.athanKey}")
        }
        result
    }
}

fun scheduleAlarms(context: Context) {
    val enabledAlarms = getEnabledAlarms(context).takeIf { it.isNotEmpty() } ?: return
    val athanGap =
        ((context.appPrefs.getString(PREF_ATHAN_GAP, null)?.toDoubleOrNull()
            ?: .0) * 60.0 * 1000.0).toLong()
    val athanSettings = AthanSettingsDB.getInstance(context.applicationContext).athanSettingsDAO()
        .getAllAthanSettings()
    var prayTimes = coordinates?.calculatePrayTimes() ?: return
    prayTimes = PrayTimeProvider(context).nReplace(prayTimes, Jdn.today())!!
    // convert spacedComma separated string to a set
    enabledAlarms.forEachIndexed { _, name ->
        var id = -1
        val time = Calendar.getInstance().also {
            // if (name == ISHA_KEY) return@also it.add(Calendar.SECOND, 5)

            val alarmTime = when (name) {
                "B$FAJR_KEY" -> {
                    id = 7
                    Clock.fromMinutesCount(
                        prayTimes.getFromStringId(R.string.fajr)
                            .toMinutes() - athanSettings[0].beforeAlertMinute
                    )
                }
                "A$FAJR_KEY" -> {
                    id = 12
                    Clock.fromMinutesCount(
                        prayTimes.getFromStringId(R.string.fajr)
                            .toMinutes() + athanSettings[0].afterAlertMinute
                    )
                }
                "B$DHUHR_KEY" -> {
                    id = 8
                    Clock.fromMinutesCount(
                        prayTimes.getFromStringId(R.string.dhuhr)
                            .toMinutes() - athanSettings[2].beforeAlertMinute
                    )
                }
                "A$DHUHR_KEY" -> {
                    id = 13
                    Clock.fromMinutesCount(
                        prayTimes.getFromStringId(R.string.dhuhr)
                            .toMinutes() + athanSettings[2].afterAlertMinute
                    )
                }
                "B$ASR_KEY" -> {
                    id = 9
                    Clock.fromMinutesCount(
                        prayTimes.getFromStringId(R.string.asr)
                            .toMinutes() - athanSettings[3].beforeAlertMinute
                    )
                }
                "A$ASR_KEY" -> {
                    id = 14
                    Clock.fromMinutesCount(
                        prayTimes.getFromStringId(R.string.asr)
                            .toMinutes() + athanSettings[3].afterAlertMinute
                    )
                }
                "B$MAGHRIB_KEY" -> {
                    id = 10
                    Clock.fromMinutesCount(
                        prayTimes.getFromStringId(R.string.maghrib)
                            .toMinutes() - athanSettings[4].beforeAlertMinute
                    )
                }
                "A$MAGHRIB_KEY" -> {
                    id = 15
                    Clock.fromMinutesCount(
                        prayTimes.getFromStringId(R.string.maghrib)
                            .toMinutes() + athanSettings[4].afterAlertMinute
                    )
                }
                "B$ISHA_KEY" -> {
                    id = 11
                    Clock.fromMinutesCount(
                        prayTimes.getFromStringId(R.string.isha)
                            .toMinutes() - athanSettings[5].beforeAlertMinute
                    )
                }
                "A$ISHA_KEY" -> {
                    id = 16
                    Clock.fromMinutesCount(
                        prayTimes.getFromStringId(R.string.isha)
                            .toMinutes() + athanSettings[5].afterAlertMinute
                    )
                }
                else -> {
                    id = athanSettings.find { aS -> aS.athanKey == name }?.id ?: -1
                    prayTimes.getFromStringId(getPrayTimeName(name))
                }
            }
            it[Calendar.HOUR_OF_DAY] = alarmTime.hours
            it[Calendar.MINUTE] = alarmTime.minutes
            it[Calendar.SECOND] = 0
        }.timeInMillis - athanGap
        val playType = athanSettings.find { name.contains(it.athanKey) }?.playType ?: 0
        val isDefaultMethod = if (playType == 0 && context.appPrefsLite.getInt(
                PREF_FULL_SCREEN_METHOD,
                DEFAULT_FULL_SCREEN_METHOD
            ) == DEFAULT_FULL_SCREEN_METHOD
        ) true
        else playType != 0 && context.appPrefsLite.getInt(
            PREF_NOTIFICATION_METHOD,
            DEFAULT_NOTIFICATION_METHOD
        ) == DEFAULT_NOTIFICATION_METHOD
        if (isDefaultMethod)
            scheduleAlarm(context, name, time, id)
        else
            scheduleAlarm2(context, name, time, id)
    }
    if (context.appPrefsLite.getBoolean(PREF_AZKAR_REINDER, false))
        scheduleAzkars(context)
}

private fun scheduleAlarm(context: Context, alarmTimeName: String, timeInMillis: Long, i: Int) {
    val remainedMillis = timeInMillis - System.currentTimeMillis()
    debugLog("Alarms: $alarmTimeName in ${remainedMillis / 60000} minutes")
    if (remainedMillis < 0 || i == -1) return // Don't set alarm in past

    if (enableWorkManager) { // Schedule in both, startAthan has the logic to skip duplicated calls
        val workerInputData = Data.Builder().putLong(KEY_EXTRA_PRAYER_TIME, timeInMillis)
            .putString(KEY_EXTRA_PRAYER, alarmTimeName).build()
        val alarmWorker = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
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
            .putExtra(KEY_EXTRA_PRAYER, alarmTimeName)
            .putExtra(KEY_EXTRA_PRAYER_TIME, timeInMillis)
            .setAction(BROADCAST_ALARM),
        PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )
    when {
        Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 ->
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        else -> am.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }
}

private fun scheduleAlarm2(context: Context, alarmTimeName: String, timeInMillis: Long, i: Int) {
    val remainedMillis = timeInMillis - System.currentTimeMillis()
    debugLog("Alarms: $alarmTimeName in ${remainedMillis / 60000} minutes")
    if (remainedMillis < 0 || i == -1) return // Don't set alarm in past

    if (enableWorkManager) { // Schedule in both, startAthan has the logic to skip duplicated calls
        val workerInputData = Data.Builder().putLong(KEY_EXTRA_PRAYER_TIME, timeInMillis)
            .putString(KEY_EXTRA_PRAYER, alarmTimeName).build()
        val alarmWorker = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
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
            .putExtra(KEY_EXTRA_PRAYER, alarmTimeName)
            .putExtra(KEY_EXTRA_PRAYER_TIME, timeInMillis)
            .setAction(BROADCAST_ALARM),
        PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )
    when {
        Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 ->
            am.setAlarmClock(
                AlarmManager.AlarmClockInfo(timeInMillis, pendingIntent),
                pendingIntent
            )
        else -> am.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }
}

private val prayTimesNames = mapOf(
    "B$FAJR_KEY" to R.string.bfajr,
    "A$FAJR_KEY" to R.string.afajr,
    FAJR_KEY to R.string.fajr,
    SUNRISE_KRY to R.string.sunrise,
    "B$DHUHR_KEY" to R.string.bdhuhr,
    "A$DHUHR_KEY" to R.string.adhuhr,
    DHUHR_KEY to R.string.dhuhr,
    "B$ASR_KEY" to R.string.basr,
    "A$ASR_KEY" to R.string.aasr,
    ASR_KEY to R.string.asr,
    "B$MAGHRIB_KEY" to R.string.bmaghrib,
    "A$MAGHRIB_KEY" to R.string.amaghrib,
    MAGHRIB_KEY to R.string.maghrib,
    "B$ISHA_KEY" to R.string.bisha,
    "A$ISHA_KEY" to R.string.aisha,
    ISHA_KEY to R.string.isha
)

@StringRes
fun getPrayTimeName(athanKey: String?): Int = prayTimesNames[athanKey] ?: R.string.fajr

fun PrayTimes.getFromStringId(@StringRes stringId: Int) = Clock.fromHoursFraction(
    when (stringId) {
        R.string.imsak -> imsak
        R.string.fajr -> fajr
        R.string.sunrise -> sunrise
        R.string.dhuhr -> dhuhr
        R.string.asr -> asr
        R.string.sunset -> sunset
        R.string.maghrib -> maghrib
        R.string.isha -> isha
        R.string.midnight -> midnight
        else -> .0
    }
)
