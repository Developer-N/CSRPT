package ir.namoo.commons.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.byagowi.persiancalendar.ASR_KEY
import com.byagowi.persiancalendar.DHUHR_KEY
import com.byagowi.persiancalendar.FAJR_KEY
import com.byagowi.persiancalendar.ISHA_KEY
import com.byagowi.persiancalendar.MAGHRIB_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SUNRISE_KRY
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toJavaCalendar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.STORAGE_PERMISSION_REQUEST_CODE
import ir.namoo.commons.model.AthanSetting
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.model.PrayTimesModel
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesEntity
import java.text.SimpleDateFormat
import java.util.*

fun String.numbersOf(): String {
    val res = StringBuilder()
    for (c in this)
        if (c.isDigit())
            res.append(c)
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
    sh = t[0].toInt()
    sm = t[1].toInt()
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
    val imm =
        view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as (InputMethodManager)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun askForStoragePermission(activity: Activity?) {
    if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

    MaterialAlertDialogBuilder(activity)
        .setTitle(R.string.storage_access)
        .setMessage(R.string.phone_storage_required)
        .setPositiveButton(R.string.continue_button) { _, _ ->
            activity.requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }.show()
}

fun isHaveStoragePermission(activity: Activity?): Boolean {
    return when {
        activity == null -> false
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> true
        else -> ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}

@SuppressLint("SdCardPath")
fun getDatabasesDirectory(applicationContext: Context): String =
    "/data/data/${applicationContext.packageName}/databases/"

fun snackMessage(view: View?, msg: String) {
    runCatching {
        view ?: return
        val snack = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
//        (snack.view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView).typeface =
//            getAppFont(view.context)
        snack.view.setBackgroundColor(view.context.resolveColor(R.attr.colorCard))
        snack.show()
    }.onFailure(logException)
}

fun Context.toastMessage(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun createBitmapFromView3(v: View): Bitmap {
    v.layoutParams = LinearLayoutCompat.LayoutParams(
        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
        LinearLayoutCompat.LayoutParams.WRAP_CONTENT
    )
    v.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    v.layout(0, 0, v.measuredWidth, v.measuredHeight)
    val bitmap = Bitmap.createBitmap(
        v.measuredWidth,
        v.measuredHeight,
        Bitmap.Config.ARGB_8888
    )

    val c = Canvas(bitmap)
    v.layout(v.left, v.top, v.right, v.bottom)
    v.draw(c)
    return bitmap
}

fun animateVisibility(view: View, visible: Boolean) {
    val targetAlpha = if (visible) 1f else 0f
    view.alpha = if (visible) 0f else 1f
    if (view.alpha == targetAlpha) return
    view.visibility = View.VISIBLE
    val anim = view.animate().alpha(targetAlpha)
    if (!visible)
        anim.withEndAction { view.visibility = View.GONE }
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
    val min = if (add) 60 else -60
    imsak.set(
        prayTimes,
        timeToDouble(fixTime(Clock.fromHoursFraction(prayTimes.imsak).toFormattedString(), min))
    )
    fajr.set(
        prayTimes,
        timeToDouble(fixTime(Clock.fromHoursFraction(prayTimes.fajr).toFormattedString(), min))
    )

    sunrise.set(
        prayTimes,
        timeToDouble(fixTime(Clock.fromHoursFraction(prayTimes.sunrise).toFormattedString(), min))
    )
    dhuhr.set(
        prayTimes,
        timeToDouble(fixTime(Clock.fromHoursFraction(prayTimes.dhuhr).toFormattedString(), min))
    )
    asr.set(
        prayTimes,
        timeToDouble(fixTime(Clock.fromHoursFraction(prayTimes.asr).toFormattedString(), min))
    )
    sunset.set(
        prayTimes,
        timeToDouble(fixTime(Clock.fromHoursFraction(prayTimes.sunset).toFormattedString(), min))
    )
    maghrib.set(
        prayTimes,
        timeToDouble(fixTime(Clock.fromHoursFraction(prayTimes.maghrib).toFormattedString(), min))
    )
    isha.set(
        prayTimes,
        timeToDouble(fixTime(Clock.fromHoursFraction(prayTimes.isha).toFormattedString(), min))
    )

    return prayTimes
}

fun timeToDouble(time: String): Double {
    val hour = time.split(":")[0].toInt()
    val minute = time.split(":")[1].toInt()
    val ashari = (minute * 100) / 60
    return if (minute < 6)
        "$hour.0$ashari".toDouble()
    else
        "$hour.$ashari".toDouble()
}

fun getAthansDirectoryPath(context: Context): String =
    context.getExternalFilesDir("athans")?.absolutePath ?: ""


fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
    return runCatching {
        packageManager.getPackageInfo(packageName, 0)
        true
    }.onFailure(logException).getOrDefault(false)
}

fun Activity.openUrlInCustomTab(url: String) {
    CustomTabsIntent.Builder().build().apply {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (isPackageInstalled("com.android.chrome", packageManager))
            intent.setPackage("com.android.chrome")
    }.launchUrl(this, Uri.parse(url))
}

fun modelToDBTimes(models: List<PrayTimesModel>): List<DownloadedPrayTimesEntity> {
    val res = mutableListOf<DownloadedPrayTimesEntity>()
    for (m in models)
        res.add(
            DownloadedPrayTimesEntity(
                m.id,
                m.day,
                m.fajr,
                m.sunrise,
                m.dhuhr,
                m.asr,
                m.maghrib,
                m.isha,
                m.cityID,
                m.created_at,
                m.updated_at
            )
        )
    return res
}

@SuppressLint("SimpleDateFormat")
fun formatServerDate(serverDate: String): String {
    var date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(serverDate)
    if (date == null)
        date = Date()
    return formatDate(
        Jdn(date.toJavaCalendar().toCivilDate().toJdn())
            .toCalendar(mainCalendar)
    )
}

fun getFileNameFromLink(link: String): String =
    link.substring(link.lastIndexOf('/') + 1, link.length)

fun createAthansSettingDB(context: Context) {
    val athanSettingDB =
        AthanSettingsDB.getInstance(context.applicationContext).athanSettingsDAO()
    if (athanSettingDB.getAllAthanSettings().isNullOrEmpty()) {
        athanSettingDB.insert(
            AthanSetting(
                FAJR_KEY,
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAscending = false,
                athanVolume = 1,
                athanURI = "",
                alertURI = ""
            )
        )
        athanSettingDB.insert(
            AthanSetting(
                SUNRISE_KRY,
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAscending = false,
                athanVolume = 1,
                athanURI = "",
                alertURI = ""
            )
        )
        athanSettingDB.insert(
            AthanSetting(
                DHUHR_KEY,
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAscending = false,
                athanVolume = 1,
                athanURI = "",
                alertURI = ""
            )
        )
        athanSettingDB.insert(
            AthanSetting(
                ASR_KEY,
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAscending = false,
                athanVolume = 1,
                athanURI = "",
                alertURI = ""
            )
        )
        athanSettingDB.insert(
            AthanSetting(
                MAGHRIB_KEY,
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAscending = false,
                athanVolume = 1,
                athanURI = "",
                alertURI = ""
            )
        )
        athanSettingDB.insert(
            AthanSetting(
                ISHA_KEY,
                state = false,
                playDoa = false,
                playType = 0,
                isBeforeEnabled = false,
                beforeAlertMinute = 10,
                isAscending = false,
                athanVolume = 1,
                athanURI = "",
                alertURI = ""
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

fun getAthanUri(setting: AthanSetting, key: String, context: Context): Uri {
    return if (key.startsWith("B") || key == "SUNRISE")
        if (setting.alertURI == "")
            getDefaultBeforeAlertUri(context)
        else
            setting.alertURI.toUri()
    else
        if (setting.athanURI == "")
            when (setting.athanKey) {
                "FAJR" -> getDefaultFajrAthanUri(context)
                "SUNRISE" -> getDefaultBeforeAlertUri(context)
                else -> getDefaultAthanUri(context)
            }
        else
            setting.athanURI.toUri()
}

fun createBitmapFromView(v: View): Bitmap {
    v.layoutParams = RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.WRAP_CONTENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT
    )
    v.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    v.layout(0, 0, v.measuredWidth, v.measuredHeight)
    val bitmap = Bitmap.createBitmap(
        v.measuredWidth,
        v.measuredHeight,
        Bitmap.Config.ARGB_8888
    )

    val c = Canvas(bitmap)
    v.layout(v.left, v.top, v.right, v.bottom)
    v.draw(c)
    return bitmap
}

fun createBitmapFromView2(v: View): Bitmap {
    v.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    v.layout(0, 0, v.measuredWidth, v.measuredHeight)
    val bitmap = Bitmap.createBitmap(
        v.measuredWidth,
        v.measuredHeight,
        Bitmap.Config.ARGB_8888
    )

    val c = Canvas(bitmap)
    v.layout(v.left, v.top, v.right, v.bottom)
    v.draw(c)
    return bitmap
}