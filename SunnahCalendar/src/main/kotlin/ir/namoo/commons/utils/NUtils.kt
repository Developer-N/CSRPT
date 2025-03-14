package ir.namoo.commons.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.model.AthanSetting
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.model.PrayTimesModel
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


fun String.digitsOf(): String {
    val res = StringBuilder()
    for (c in this) if (c.isDigit()) res.append(c)
    return res.toString()
}

fun getDayNum(month: Int, day: Int): Int = when (month) {
    1 -> day
    2 -> 31 + day
    3 -> 62 + day
    4 -> 93 + day
    5 -> 124 + day
    6 -> 155 + day
    7 -> 186 + day
    8 -> 216 + day
    9 -> 246 + day
    10 -> 276 + day
    11 -> 306 + day
    else -> 336 + day
}

fun fixTime(time: String, min: Int): String {
    var sh: Int
    var sm: Int // source hour and min
    val t = time.split(":".toRegex()).toTypedArray()
    sh = t[0].digitsOf().toInt()
    sm = t[1].digitsOf().toInt()
    sm += min
    if (sm >= 60) {
        sm -= 60
        sh++
        if (sh >= 24) sh = 0
    } else if (sm < 0) {
        sm += 60
        sh--
        if (sh < 0) sh = 23
    }
    var nh: String = sh.toString() + ""
    var nm: String = sm.toString() + ""
    if (sh <= 9) nh = "0$nh"
    if (sm <= 9) nm = "0$nm"
    return "$nh:$nm"
}

@Suppress("deprecation")
fun isNetworkConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}

fun hideKeyBoard(view: View) {
    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as (InputMethodManager)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

@SuppressLint("SdCardPath")
fun getDatabasesDirectory(applicationContext: Context): String =
    "/data/data/${applicationContext.packageName}/databases/"

fun Context.toastMessage(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun animateVisibility(view: View, visible: Boolean) {
    val targetAlpha = if (visible) 1f else 0f
    view.alpha = if (visible) 0f else 1f
    if (view.alpha == targetAlpha) return
    view.visibility = View.VISIBLE
    val anim = view.animate().alpha(targetAlpha)
    if (!visible) anim.withEndAction { view.visibility = View.GONE }
}

fun getDayMonthForDayOfYear(day: Int): String {
    val res: String
    var m = 0
    var d = 0
    when {
        day <= 31 -> {// 1
            m = 1
            d = day
        }

        day <= 62 -> {// 2
            m = 2
            d = day - 31
        }

        day <= 93 -> {// 3
            m = 3
            d = day - 62
        }

        day <= 124 -> {//4
            m = 4
            d = day - 93
        }

        day <= 155 -> {//5
            m = 5
            d = day - 124
        }

        day <= 186 -> {//6
            m = 6
            d = day - 155
        }

        day <= 216 -> {//7
            m = 7
            d = day - 186
        }

        day <= 246 -> {//8
            m = 8
            d = day - 216
        }

        day <= 276 -> {//9
            m = 9
            d = day - 246
        }

        day <= 306 -> {//10
            m = 10
            d = day - 276
        }

        day <= 336 -> {//11
            m = 11
            d = day - 306
        }

        day <= 366 -> {//12
            m = 12
            d = day - 336
        }
    }
    val strM: String = if (m <= 9) "0$m" else m.toString()
    val strD: String = if (d <= 9) "0$d" else d.toString()
    res = "$strM/$strD"
    return res
}

fun fixSummerTimes(prayTimes: PrayTimes?, add: Boolean = true): PrayTimes? {
    if (prayTimes == null) return null
    val imsak = prayTimes.javaClass.getDeclaredField("imsak").apply { isAccessible = true }
    val fajr = prayTimes.javaClass.getDeclaredField("fajr").apply { isAccessible = true }
    val sunrise = prayTimes.javaClass.getDeclaredField("sunrise").apply { isAccessible = true }
    val dhuhr = prayTimes.javaClass.getDeclaredField("dhuhr").apply { isAccessible = true }
    val asr = prayTimes.javaClass.getDeclaredField("asr").apply { isAccessible = true }
    val sunset = prayTimes.javaClass.getDeclaredField("sunset").apply { isAccessible = true }
    val maghrib = prayTimes.javaClass.getDeclaredField("maghrib").apply { isAccessible = true }
    val isha = prayTimes.javaClass.getDeclaredField("isha").apply { isAccessible = true }

    imsak.set(
        prayTimes,
        if (add)
            Clock(prayTimes.imsak).plus(Clock(1.0)).value
        else
            Clock(prayTimes.imsak).minus(Clock(1.0)).value
    )
    fajr.set(
        prayTimes,
        if (add)
            Clock(prayTimes.fajr).plus(Clock(1.0)).value
        else
            Clock(prayTimes.fajr).minus(Clock(1.0)).value
    )

    sunrise.set(
        prayTimes,
        if (add)
            Clock(prayTimes.sunrise).plus(Clock(1.0)).value
        else
            Clock(prayTimes.sunrise).minus(Clock(1.0)).value
    )
    dhuhr.set(
        prayTimes,
        if (add)
            Clock(prayTimes.dhuhr).plus(Clock(1.0)).value
        else
            Clock(prayTimes.dhuhr).minus(Clock(1.0)).value
    )
    asr.set(
        prayTimes,
        if (add)
            Clock(prayTimes.asr).plus(Clock(1.0)).value
        else
            Clock(prayTimes.asr).minus(Clock(1.0)).value
    )
    sunset.set(
        prayTimes,
        if (add)
            Clock(prayTimes.sunset).plus(Clock(1.0)).value
        else
            Clock(prayTimes.sunset).minus(Clock(1.0)).value
    )
    maghrib.set(
        prayTimes,
        if (add)
            Clock(prayTimes.maghrib).plus(Clock(1.0)).value
        else
            Clock(prayTimes.maghrib).minus(Clock(1.0)).value
    )
    isha.set(
        prayTimes,
        if (add)
            Clock(prayTimes.isha).plus(Clock(1.0)).value
        else
            Clock(prayTimes.isha).minus(Clock(1.0)).value
    )

    return prayTimes
}

fun timeToDouble(time: String): Double {
    val hour = time.split(":")[0].toInt()
    val minute = time.split(":")[1].toInt()
    val ashari = (minute * 100) / 60
    return if (minute < 6) "$hour.0$ashari".toDouble()
    else "$hour.$ashari".toDouble()
}

fun getAthansDirectoryPath(context: Context): String =
    context.getExternalFilesDir("athans")?.absolutePath ?: ""


fun isPackageInstalled(
    packageName: String, packageManager: PackageManager, flags: Int = 0
): Boolean {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName, PackageManager.PackageInfoFlags.of(flags.toLong())
            )
        } else {
            packageManager.getPackageInfo(packageName, flags)
        }
        true
    }.onFailure(logException).getOrDefault(false)
}

fun Activity.openUrlInCustomTab(url: String) {
    CustomTabsIntent.Builder().build().apply {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (isPackageInstalled(
                "com.android.chrome", packageManager
            )
        ) intent.setPackage("com.android.chrome")
    }.launchUrl(this, url.toUri())
}

fun modelToDBTimes(models: List<PrayTimesModel>): List<DownloadedPrayTimesEntity> {
    val res = mutableListOf<DownloadedPrayTimesEntity>()
    for (m in models) res.add(
        DownloadedPrayTimesEntity(
            m.id,
            m.day,
            m.fajr,
            m.sunrise,
            m.dhuhr,
            m.asr,
            m.asrHanafi,
            m.maghrib,
            m.isha,
            m.cityID,
            m.createdAt,
            m.updatedAt
        )
    )
    return res
}

fun formatServerDate(serverDate: String): String {
    var date = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", language.value.asSystemLocale()
    ).parse(serverDate)
    if (date == null) date = Date()
    return formatDate(
        Jdn(date.toGregorianCalendar().toCivilDate().toJdn()) on mainCalendar
    )
}

fun getFileNameFromLink(link: String): String =
    link.substring(link.lastIndexOf('/') + 1, link.length)

fun createAthansSettingDB(context: Context) {
    val athanSettingDB = AthanSettingsDB.getInstance(context.applicationContext).athanSettingsDAO()
    if (athanSettingDB.getAllAthanSettings().isEmpty()) {
        athanSettingDB.insert(
            AthanSetting(
                athanKey = PrayTime.FAJR.name,
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAfterEnabled = false,
                afterAlertMinute = 10,
                isSilentEnabled = false,
                silentMinute = 20,
                isAscending = false,
                athanVolume = 3,
                athanURI = "",
                alertURI = "",
                backgroundUri = ""
            )
        )
        athanSettingDB.insert(
            AthanSetting(
                athanKey = PrayTime.SUNRISE.name,
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAfterEnabled = false,
                afterAlertMinute = 10,
                isSilentEnabled = false,
                silentMinute = 20,
                isAscending = false,
                athanVolume = 3,
                athanURI = "",
                alertURI = "",
                backgroundUri = ""
            )
        )
        athanSettingDB.insert(
            AthanSetting(
                athanKey = PrayTime.DHUHR.name,
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAfterEnabled = false,
                afterAlertMinute = 10,
                isSilentEnabled = false,
                silentMinute = 20,
                isAscending = false,
                athanVolume = 3,
                athanURI = "",
                alertURI = "",
                backgroundUri = ""
            )
        )
        athanSettingDB.insert(
            AthanSetting(
                athanKey = PrayTime.ASR.name,
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAfterEnabled = false,
                afterAlertMinute = 10,
                isSilentEnabled = false,
                silentMinute = 20,
                isAscending = false,
                athanVolume = 3,
                athanURI = "",
                alertURI = "",
                backgroundUri = ""
            )
        )
        athanSettingDB.insert(
            AthanSetting(
                athanKey = PrayTime.MAGHRIB.name,
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAfterEnabled = false,
                afterAlertMinute = 10,
                isSilentEnabled = false,
                silentMinute = 20,
                isAscending = false,
                athanVolume = 3,
                athanURI = "",
                alertURI = "",
                backgroundUri = ""
            )
        )
        athanSettingDB.insert(
            AthanSetting(
                athanKey = PrayTime.ISHA.name,
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAfterEnabled = false,
                afterAlertMinute = 10,
                isSilentEnabled = false,
                silentMinute = 20,
                isAscending = false,
                athanVolume = 3,
                athanURI = "",
                alertURI = "",
                backgroundUri = ""
            )
        )
    }
}

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

fun getDefaultAlertUri(context: Context): Uri = "%s://%s/%s/%s".format(
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

fun getAthanUri(setting: AthanSetting?, key: String?, context: Context): Uri {
    return if (setting == null || key == null) getDefaultAthanUri(context)
    else if (key.startsWith("B") || (key.startsWith("A") && key != PrayTime.ASR.name) || key == "SUNRISE")
        if (setting.alertURI == "") getDefaultAlertUri(context)
        else setting.alertURI.toUri()
    else if (setting.athanURI == "") when (setting.athanKey) {
        "FAJR" -> getDefaultFajrAthanUri(context)
        "SUNRISE" -> getDefaultAlertUri(context)
        else -> getDefaultAthanUri(context)
    }
    else setting.athanURI.toUri()
}

fun String.smartTruncate(length: Int): String {
    val words = split(" ")
    var added = 0
    var hasMore = false
    val builder = StringBuilder()
    for (word in words) {
        if (builder.length > length) {
            hasMore = true
            break
        }
        builder.append(word)
        builder.append(" ")
        added += 1
    }

    listOf(", ", "; ", ": ", " ").map {
        if (builder.endsWith(it)) {
            builder.replace(builder.length - it.length, builder.length, "")
        }
    }

    if (hasMore) {
        builder.append("...")
    }
    return builder.toString()
}


//@RequiresApi(Build.VERSION_CODES.Q)
//fun Activity.askFullScreenPermission() {
//    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FULL_SCREEN_INTENT)
//        != PackageManager.PERMISSION_GRANTED
//    ) MaterialAlertDialogBuilder(this).apply {
//        setTitle(R.string.requset_permision)
//        setMessage(R.string.need_full_screen_permision)
//        setPositiveButton(R.string.ok) { _, _ ->
//            requestPermissions(arrayOf(Manifest.permission.USE_FULL_SCREEN_INTENT), 23)
//        }
//        setNegativeButton(R.string.cancel) { dialog, _ ->
//            dialog.dismiss()
//        }
//        show()
//    }
//}

fun Activity.turnScreenOnAndKeyguardOff() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    } else {
        @Suppress("DEPRECATION") window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    }
}

fun File.writeLog(log: String) {
    if (!exists()) createNewFile()
    val currentText = readText()
    writeText("$currentText\r\n$log")
}
