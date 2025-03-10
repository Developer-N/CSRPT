package com.byagowi.persiancalendar.utils

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RawRes
import androidx.core.app.ActivityCompat
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.byagowi.persiancalendar.ALARMS_BASE_ID
import com.byagowi.persiancalendar.ALARM_TAG
import com.byagowi.persiancalendar.BROADCAST_ALARM
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER_TIME
import com.byagowi.persiancalendar.LAST_PLAYED_ATHAN_JDN
import com.byagowi.persiancalendar.LAST_PLAYED_ATHAN_KEY
import com.byagowi.persiancalendar.PREF_ATHAN_ALARM
import com.byagowi.persiancalendar.PREF_ATHAN_GAP
import com.byagowi.persiancalendar.PREF_ATHAN_URI
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.entities.PrayTime.Companion.get
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.service.AlarmWorker
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.variants.debugLog
import ir.namoo.commons.DEFAULT_JUMMA_SILENT_MINUTE
import ir.namoo.commons.LAST_PLAYED_AFTER_ATHAN_KEY
import ir.namoo.commons.LAST_PLAYED_BEFORE_ATHAN_KEY
import ir.namoo.commons.LAST_SILENT_ATHAN_KEY
import ir.namoo.commons.LAST_STOP_SILENT_ATHAN_KEY
import ir.namoo.commons.PREF_AZKAR_REINDER
import ir.namoo.commons.PREF_JUMMA_SILENT
import ir.namoo.commons.PREF_JUMMA_SILENT_MINUTE
import ir.namoo.commons.koin.getKoinInstance
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import ir.namoo.religiousprayers.ui.athan.NAthanActivity
import ir.namoo.religiousprayers.ui.athan.NAthanNotification
import ir.namoo.religiousprayers.ui.azkar.scheduleAzkars
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

// https://stackoverflow.com/a/69505596
fun Resources.getRawUri(@RawRes rawRes: Int) = "%s://%s/%s/%s".format(
    ContentResolver.SCHEME_ANDROID_RESOURCE, this.getResourcePackageName(rawRes),
    this.getResourceTypeName(rawRes), this.getResourceEntryName(rawRes)
)

fun getAthanUri(context: Context): Uri =
    (context.preferences.getString(PREF_ATHAN_URI, null)?.takeIf { it.isNotEmpty() }
        ?: context.resources.getRawUri(R.raw.special)).toUri()

fun startAthan(context: Context, prayTime: String, intendedTime: Long?) {
    debugLog("Alarms: startAthan for $prayTime")
    if (intendedTime == null || prayTime.startsWith("SS_"))
        return startAthanBody(context, prayTime)
    // if alarm is off by 15 minutes, just skip
    if (abs(System.currentTimeMillis() - intendedTime).milliseconds > 15.minutes) return

    // If at the of being is disabled by user, skip
    if (prayTime !in getEnabledAlarms2(context)) return

    // skips if already called through either WorkManager or AlarmManager
    val preferences = context.preferences
    val lastPlayedAthanKey = preferences.getString(LAST_PLAYED_ATHAN_KEY, null)
    val lastBAthanKey = preferences.getString(LAST_PLAYED_BEFORE_ATHAN_KEY, null)
    val lastAAthanKey = preferences.getString(LAST_PLAYED_AFTER_ATHAN_KEY, null)
    val lastSAthanKey = preferences.getString(LAST_SILENT_ATHAN_KEY, null)
    val lastSSAthanKey = preferences.getString(LAST_STOP_SILENT_ATHAN_KEY, null)
    val lastPlayedAthanJdn = preferences.getJdnOrNull(LAST_PLAYED_ATHAN_JDN)
    val today = Jdn.today()
    if (lastPlayedAthanJdn == today && (lastPlayedAthanKey == prayTime || lastBAthanKey == prayTime || lastAAthanKey == prayTime || lastSAthanKey == prayTime || lastSSAthanKey == prayTime)) return
    preferences.edit {
        putString(
            if (prayTime.startsWith("B")) LAST_PLAYED_BEFORE_ATHAN_KEY
            else if (prayTime.startsWith("A") && prayTime != PrayTime.ASR.name) LAST_PLAYED_AFTER_ATHAN_KEY
            else if (prayTime.startsWith("S_")) LAST_SILENT_ATHAN_KEY
            else if (prayTime.startsWith("SS_")) LAST_STOP_SILENT_ATHAN_KEY
            else LAST_PLAYED_ATHAN_KEY, prayTime
        )
        putJdn(LAST_PLAYED_ATHAN_JDN, today)
    }

    startAthanBody(context, prayTime)
}

private fun startAthanBody(context: Context, prayTime: String) {
    runCatching {
        debugLog("Alarms: startAthanBody for $prayTime")

        runCatching {
            @Suppress("DEPRECATION")
            context.getSystemService<PowerManager>()?.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK,
                "persiancalendar:alarm"
            )?.acquire(30.seconds.inWholeMilliseconds)
        }.onFailure(logException)
        val setting = AthanSettingsDB.getInstance(context.applicationContext)
            .athanSettingsDAO().getAllAthanSettings().find { prayTime.contains(it.athanKey) }
            ?: return@runCatching
        when {
            prayTime.startsWith("SS_") -> {
                runCatching {
                    context.getSystemService<AudioManager>()?.let { audioManager ->
                        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    }
                }
            }

            prayTime.startsWith("S_") -> {
                runCatching {
                    context.getSystemService<AudioManager>()?.let { audioManager ->
                        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                        val time = Calendar.getInstance().also {
                            it.add(
                                Calendar.MINUTE,
                                if (Jdn.today().weekDay == 6 && prayTime.contains(PrayTime.DHUHR.name) && context.preferences.getBoolean(
                                        PREF_JUMMA_SILENT,
                                        false
                                    )
                                ) context.preferences.getInt(
                                    PREF_JUMMA_SILENT_MINUTE, DEFAULT_JUMMA_SILENT_MINUTE
                                )
                                else setting.silentMinute
                            )
                        }
                        val id = when {
                            prayTime.contains(PrayTime.FAJR.name) -> 123
                            prayTime.contains(PrayTime.DHUHR.name) -> 124
                            prayTime.contains(PrayTime.ASR.name) -> 125
                            prayTime.contains(PrayTime.MAGHRIB.name) -> 126
                            prayTime.contains(PrayTime.ISHA.name) -> 127
                            else -> 122
                        }
                        scheduleAlarm(context, "S$prayTime", time.timeInMillis, id)
                    }
                }
            }

            setting.playType != 0 && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                context.startService(
                    Intent(context, NAthanNotification::class.java)
                        .putExtra(KEY_EXTRA_PRAYER, prayTime)
                )
            }

            else -> startAthanActivity(context, prayTime)
        }
    }.onFailure(logException)
}

fun startAthanActivity(context: Context, prayTime: String?) {
    context.startActivity(
        Intent(context, NAthanActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(KEY_EXTRA_PRAYER, prayTime)
    )
}

fun getEnabledAlarms(context: Context): Set<PrayTime> {
    if (coordinates.value == null) return emptySet()
    return (context.preferences.getString(PREF_ATHAN_ALARM, null)?.trim() ?: return emptySet())
        .splitFilterNotEmpty(",")
        .mapNotNull { PrayTime.fromName(it) }
        .toSet()
}

fun getEnabledAlarms3(context: Context): Set<PrayTime> {
    if (coordinates.value == null) return emptySet()
    val athans = AthanSettingsDB.getInstance(context.applicationContext).athanSettingsDAO()
        .getAllAthanSettings()
    return if (athans.isEmpty()) emptySet()
    else {
        val result = mutableSetOf<PrayTime>()
        for (athan in athans) {
            if (athan.state) PrayTime.fromName(athan.athanKey)?.let { result.add(it) }
        }
        result
    }
}

fun getEnabledAlarms2(context: Context): Set<String> {
    if (coordinates.value == null) return emptySet()
    val athans = AthanSettingsDB.getInstance(context.applicationContext).athanSettingsDAO()
        .getAllAthanSettings()
    return if (athans.isEmpty()) emptySet()
    else {
        val result = mutableSetOf<String>()
        for (athan in athans) {
            if (athan.state) result.add(athan.athanKey)
            if (athan.isBeforeEnabled) result.add("B${athan.athanKey}")
            if (athan.isAfterEnabled) result.add("A${athan.athanKey}")
            if (athan.isSilentEnabled) result.add("S_${athan.athanKey}")
            if (
                athan.athanKey == PrayTime.DHUHR.name && Jdn.today().weekDay == 6 &&
                context.preferences.getBoolean(PREF_JUMMA_SILENT, false) &&
                !result.contains("S_${athan.athanKey}")
            )
                result.add("S_${athan.athanKey}")
        }
        result
    }
}

fun scheduleAlarms(context: Context) {
    val prayTimeProvider: PrayTimeProvider = getKoinInstance()
    if (context.appPrefsLite.getBoolean(PREF_AZKAR_REINDER, false))
        scheduleAzkars(context, prayTimeProvider)
    val enabledAlarms = getEnabledAlarms2(context).takeIf { it.isNotEmpty() } ?: return
    val athanGap =
        ((context.preferences.getString(PREF_ATHAN_GAP, null)?.toDoubleOrNull()
            ?: .0) * 60.0 * 1000.0).toLong()
    val athanSettings = AthanSettingsDB.getInstance(context.applicationContext).athanSettingsDAO()
        .getAllAthanSettings()
    var prayTimes = coordinates.value?.calculatePrayTimes() ?: return
    prayTimes = prayTimeProvider.replace(prayTimes, Jdn.today())
    // convert spacedComma separated string to a set
    enabledAlarms.forEachIndexed { i, prayTime ->
        var id = -1
        val time = Calendar.getInstance().also {
            // if (name == ISHA_KEY) return@also it.add(Calendar.SECOND, 5)
            val minute: Int
            val clock: Clock
            when (prayTime) {
                "B${PrayTime.FAJR.name}" -> {
                    id = 7
                    clock = prayTimes[PrayTime.FAJR]
                    minute = -athanSettings[0].beforeAlertMinute
                }

                "A${PrayTime.FAJR.name}" -> {
                    id = 12
                    clock = prayTimes[PrayTime.FAJR]
                    minute = athanSettings[0].afterAlertMinute
                }

                "S_${PrayTime.FAJR.name}" -> {
                    id = 17
                    clock = prayTimes[PrayTime.FAJR]
                    minute = 0
                }

                "B${PrayTime.DHUHR.name}" -> {
                    id = 8
                    clock = prayTimes[PrayTime.DHUHR]
                    minute = -athanSettings[2].beforeAlertMinute
                }

                "A${PrayTime.DHUHR.name}" -> {
                    id = 13
                    clock = prayTimes[PrayTime.DHUHR]
                    minute = athanSettings[2].afterAlertMinute
                }

                "S_${PrayTime.DHUHR.name}" -> {
                    id = 18
                    clock = prayTimes[PrayTime.DHUHR]
                    minute = 0
                }

                "B${PrayTime.ASR.name}" -> {
                    id = 9
                    clock = prayTimes[PrayTime.ASR]
                    minute = -athanSettings[3].beforeAlertMinute
                }

                "A${PrayTime.ASR.name}" -> {
                    id = 14
                    clock = prayTimes[PrayTime.ASR]
                    minute = athanSettings[3].afterAlertMinute
                }

                "S_${PrayTime.ASR.name}" -> {
                    id = 19
                    clock = prayTimes[PrayTime.ASR]
                    minute = 0
                }

                "B${PrayTime.MAGHRIB.name}" -> {
                    id = 10
                    clock = prayTimes[PrayTime.MAGHRIB]
                    minute = -athanSettings[4].beforeAlertMinute
                }

                "A${PrayTime.MAGHRIB.name}" -> {
                    id = 15
                    clock = prayTimes[PrayTime.MAGHRIB]
                    minute = athanSettings[4].afterAlertMinute
                }

                "S_${PrayTime.MAGHRIB.name}" -> {
                    id = 20
                    clock = prayTimes[PrayTime.MAGHRIB]
                    minute = 0
                }

                "B${PrayTime.ISHA.name}" -> {
                    id = 11
                    clock = prayTimes[PrayTime.ISHA]
                    minute = -athanSettings[5].beforeAlertMinute
                }

                "A${PrayTime.ISHA.name}" -> {
                    id = 16
                    clock = prayTimes[PrayTime.ISHA]
                    minute = athanSettings[5].afterAlertMinute
                }

                "S_${PrayTime.ISHA.name}" -> {
                    id = 21
                    clock = prayTimes[PrayTime.ISHA]
                    minute = 0
                }

                else -> {
                    id = athanSettings.find { aS -> aS.athanKey == prayTime }?.id ?: 1
                    clock = prayTimes[PrayTime.fromName(prayTime) ?: PrayTime.FAJR]
                    minute = 0
                }
            }
            var h = clock.toHoursAndMinutesPair().first
            var m = clock.toHoursAndMinutesPair().second
            m += minute
            if (m > 59) {
                h++
                m -= 60
            }
            if (m < 0) {
                h--
                m += 60
            }
            it[GregorianCalendar.HOUR_OF_DAY] = h
            it[GregorianCalendar.MINUTE] = m
            it[GregorianCalendar.SECOND] = 0
        }.timeInMillis - athanGap
        scheduleAlarm(context, prayTime, time, id)
    }
}

private fun scheduleAlarm(context: Context, prayTime: String, timeInMillis: Long, i: Int) {
    val remainedMillis = timeInMillis - System.currentTimeMillis()
    debugLog("Alarms: $prayTime in ${remainedMillis / 60000} minutes")
    if (remainedMillis < 0) return // Don't set alarm in past

    run { // Schedule in both alarmmanager and workmanager, startAthan has the logic to skip duplicated calls
        val workerInputData = Data.Builder().putLong(KEY_EXTRA_PRAYER_TIME, timeInMillis)
            .putString(KEY_EXTRA_PRAYER, prayTime).build()
        val alarmWorker =OneTimeWorkRequestBuilder<AlarmWorker>()
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
            .putExtra(KEY_EXTRA_PRAYER, prayTime)
            .putExtra(KEY_EXTRA_PRAYER_TIME, timeInMillis)
            .setAction(BROADCAST_ALARM),
        PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms())
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            am, AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
        )
}
