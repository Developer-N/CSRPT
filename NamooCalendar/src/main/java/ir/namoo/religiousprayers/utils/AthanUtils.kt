package ir.namoo.religiousprayers.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import androidx.core.net.toUri
import ir.namoo.religiousprayers.*
import ir.namoo.religiousprayers.service.AthanNotification
import ir.namoo.religiousprayers.ui.AthanActivity

// https://stackoverflow.com/a/27788209
fun getDefaultAthanUri(context: Context): Uri =
    (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
            context.resources.getResourcePackageName(R.raw.adhan_nasser_al_qatami) + "/" +
            context.resources.getResourceTypeName(R.raw.adhan_nasser_al_qatami) + "/" +
            context.resources.getResourceEntryName(R.raw.adhan_nasser_al_qatami)).toUri()

fun getDefaultFajrAthanUri(context: Context): Uri =
    (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
            context.resources.getResourcePackageName(R.raw.adhan_morning_mishari) + "/" +
            context.resources.getResourceTypeName(R.raw.adhan_morning_mishari) + "/" +
            context.resources.getResourceEntryName(R.raw.adhan_morning_mishari)).toUri()

fun getDefaultBeforeFajrUri(context: Context): Uri =
    (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
            context.resources.getResourcePackageName(R.raw.beep) + "/" +
            context.resources.getResourceTypeName(R.raw.beep) + "/" +
            context.resources.getResourceEntryName(R.raw.beep)).toUri()

fun getDefaultSunriseUri(context: Context): Uri =
    (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
            context.resources.getResourcePackageName(R.raw.beep) + "/" +
            context.resources.getResourceTypeName(R.raw.beep) + "/" +
            context.resources.getResourceEntryName(R.raw.beep)).toUri()

fun getDefaultDOAUri(context: Context): Uri =
    (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
            context.resources.getResourcePackageName(R.raw.doa) + "/" +
            context.resources.getResourceTypeName(R.raw.doa) + "/" +
            context.resources.getResourceEntryName(R.raw.doa)).toUri()

val Context.athanVolume: Int
    get() = appPrefs.getInt(PREF_ATHAN_VOLUME, DEFAULT_ATHAN_VOLUME)

val Context.isAscendingAthanVolumeEnabled: Boolean
    get() = appPrefs.getBoolean(PREF_ASCENDING_ATHAN_VOLUME, false)

fun getCustomAthanUri(context: Context): Uri? =
    context.appPrefs.getString(PREF_ATHAN_URI, null)?.takeUnless { it.isEmpty() }?.toUri()

fun startAthan(context: Context, prayTimeKey: String, prayTime: String) {
    try {
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SRPT::MyWakelockTag").apply {
                acquire(1 * 60 * 1000L /*1 minutes*/)
            }
        }
    } catch (e: Exception) {
        appendLog(context, "error: " + e.message)
    }
    appendLog(context, "startAthan................")
    when {
        prayTimeKey == context.resources.getString(R.string.alarm_before_fajr_name) -> {
            context.startActivity(
                Intent(context, AthanActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(KEY_EXTRA_PRAYER_KEY, prayTimeKey)
                    .putExtra(KEY_EXTRA_PRAYER_TIME, prayTime)
            )
        }
        notificationAthan -> {
            val intent = Intent(context, AthanNotification::class.java)
                .putExtra(KEY_EXTRA_PRAYER_KEY, prayTimeKey)
                .putExtra(KEY_EXTRA_PRAYER_TIME, prayTime)
            context.startService(intent)
            AthanNotification.notify(context, intent)
        }
        else -> {
            context.startActivity(
                Intent(context, AthanActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(KEY_EXTRA_PRAYER_KEY, prayTimeKey)
                    .putExtra(KEY_EXTRA_PRAYER_TIME, prayTime)
            )
        }
    }
}

fun getAthanUri(context: Context): Uri {
    return if ((context.appPrefs.getString(
            PREF_NORMAL_ATHAN_URI,
            ""
        )) == null ||
        (context.appPrefs.getString(
            PREF_NORMAL_ATHAN_URI,
            ""
        )) == ""
    ) getDefaultAthanUri(context)
    else (context.appPrefs.getString(PREF_NORMAL_ATHAN_URI, ""))?.toUri()!!

}

fun getFajrAthanUri(context: Context): Uri {
    return if ((context.appPrefs.getString(
            PREF_FAJR_ATHAN_URI,
            ""
        )) == null ||
        (context.appPrefs.getString(
            PREF_FAJR_ATHAN_URI,
            ""
        )) == ""
    ) getDefaultFajrAthanUri(context)
    else (context.appPrefs.getString(PREF_FAJR_ATHAN_URI, ""))?.toUri()!!
}

fun getBeforeFajrUri(context: Context): Uri {
    return if ((context.appPrefs.getString(
            PREF_BEFORE_FAJR_URI,
            ""
        )) == null ||
        (context.appPrefs.getString(
            PREF_BEFORE_FAJR_URI,
            ""
        )) == ""
    ) getDefaultBeforeFajrUri(context)
    else (context.appPrefs.getString(PREF_BEFORE_FAJR_URI, ""))?.toUri()!!
}

fun getSunriseUri(context: Context): Uri {
    return if ((context.appPrefs.getString(
            PREF_SUNRISE_URI,
            ""
        )) == null ||
        (context.appPrefs.getString(
            PREF_SUNRISE_URI,
            ""
        )) == ""
    ) getDefaultSunriseUri(context)
    else (context.appPrefs.getString(PREF_SUNRISE_URI, ""))?.toUri()!!
}