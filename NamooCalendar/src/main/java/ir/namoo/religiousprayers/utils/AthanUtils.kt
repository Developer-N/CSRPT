package ir.namoo.religiousprayers.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import androidx.core.net.toUri
import ir.namoo.religiousprayers.KEY_EXTRA_PRAYER_KEY
import ir.namoo.religiousprayers.KEY_EXTRA_PRAYER_TIME
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.db.AthanSettingsDB
import ir.namoo.religiousprayers.service.AthanNotification
import ir.namoo.religiousprayers.ui.AthanActivity

// https://stackoverflow.com/a/27788209
fun getDefaultAthanUri(context: Context): Uri = "%s://%s/%s/%s".format(
    ContentResolver.SCHEME_ANDROID_RESOURCE,
    context.resources.getResourcePackageName(R.raw.adhan_nasser_al_qatami),
    context.resources.getResourceTypeName(R.raw.adhan_nasser_al_qatami),
    context.resources.getResourceEntryName(R.raw.adhan_nasser_al_qatami)
).toUri()

fun getDefaultFajrAthanUri(context: Context): Uri = "%s://%s/%s/%s".format(
    ContentResolver.SCHEME_ANDROID_RESOURCE,
    context.resources.getResourcePackageName(R.raw.adhan_morning_mishari),
    context.resources.getResourceTypeName(R.raw.adhan_morning_mishari),
    context.resources.getResourceEntryName(R.raw.adhan_morning_mishari)
).toUri()

fun getDefaultBeforeAlertUri(context: Context): Uri = "%s://%s/%s/%s".format(
    ContentResolver.SCHEME_ANDROID_RESOURCE,
    context.resources.getResourcePackageName(R.raw.beep),
    context.resources.getResourceTypeName(R.raw.beep),
    context.resources.getResourceEntryName(R.raw.beep)
).toUri()

fun getDefaultDOAUri(context: Context): Uri = "%s://%s/%s/%s".format(
    ContentResolver.SCHEME_ANDROID_RESOURCE,
    context.resources.getResourcePackageName(R.raw.doa),
    context.resources.getResourceTypeName(R.raw.doa),
    context.resources.getResourceEntryName(R.raw.doa)
).toUri()

fun startAthan(context: Context, prayTimeKey: String, prayTime: String) {
    runCatching {
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SRPT::MyWakelockTag").apply {
                acquire(1 * 60 * 1000L /*1 minutes*/)
            }
        }
    }.onFailure(logException).getOrElse {
        appendLog(context, "error: " + it.message)
    }
    appendLog(context, "startAthan................ $prayTimeKey")
    val setting = AthanSettingsDB.getInstance(context.applicationContext).athanSettingsDAO()
        .getAllAthanSettings()?.filter { prayTimeKey.contains(it.athanKey) }?.get(0) ?: return
    if (!setting.state || (prayTimeKey.startsWith("B") && !setting.isBeforeEnabled)) return
    when (setting.playType) {
        0 -> {
            context.startActivity(
                Intent(context, AthanActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(KEY_EXTRA_PRAYER_KEY, prayTimeKey)
                    .putExtra(KEY_EXTRA_PRAYER_TIME, prayTime)
            )
        }
        else -> {
            val intent = Intent(context, AthanNotification::class.java)
                .putExtra(KEY_EXTRA_PRAYER_KEY, prayTimeKey)
                .putExtra(KEY_EXTRA_PRAYER_TIME, prayTime)
            context.startService(intent)
            AthanNotification.notify(context, intent)
        }
    }
}
