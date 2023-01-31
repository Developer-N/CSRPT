package ir.namoo.quran.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import androidx.core.text.HtmlCompat
import ir.namoo.commons.utils.appPrefsLite
import java.io.File
import java.util.*

lateinit var arabicFont: Typeface
var arabicFontSize: Float = 0.0f

lateinit var englishFont: Typeface
var englishFontSize = 0.0f

lateinit var kurdishFont: Typeface
var kurdishFontSize = 0.0f

lateinit var farsiFont: Typeface
var farsiFontSize = 0.0f

fun initQuranUtils(context: Context, prefs: SharedPreferences) {
    arabicFont = Typeface.createFromAsset(
        context.assets, prefs.getString(PREF_QURAN_FONT, DEFAULT_QURAN_FONT)
    )
    arabicFontSize = prefs.getFloat(PREF_QURAN_FONT_SIZE, DEFAULT_QURAN_FONT_SIZE)
    englishFont = Typeface.createFromAsset(
        context.assets, prefs.getString(PREF_ENGLISH_FONT, DEFAULT_ENGLISH_FONT)
    )
    englishFontSize =
        prefs.getFloat(PREF_ENGLISH_FONT_SIZE, DEFAULT_ENGLISH_FONT_SIZE)
    kurdishFont = Typeface.createFromAsset(
        context.assets, prefs.getString(PREF_KURDISH_FONT, DEFAULT_KURDISH_FONT)
    )
    kurdishFontSize =
        prefs.getFloat(PREF_KURDISH_FONT_SIZE, DEFAULT_KURDISH_FONT_SIZE)
    farsiFont = Typeface.createFromAsset(
        context.assets,
        prefs.getString(PREF_FARSI_FONT, DEFAULT_FARSI_FONT)
    )
    farsiFontSize = prefs.getFloat(PREF_FARSI_FONT_SIZE, DEFAULT_FARSI_FONT_SIZE)
}

fun isDigit(string: String): Boolean {
    for (c in string)
        if (!c.isDigit())
            return false
    return true
}

fun getSelectedQuranDirectoryPath(context: Context): String {
    val inPref = context.appPrefsLite.getString(PREF_STORAGE_PATH, "-") ?: "-"
    return if (inPref.contains("emulated") || inPref == "-")
        context.getExternalFilesDir("quran")?.absolutePath ?: ""
    else "${inPref}/Android/data/${context.packageName}/files/quran"
}

fun getQuranDirectoryInSD(context: Context): String {
    val res = getRootDirs(context).filter { it != null && !it.absolutePath.contains("emulated") }
    return if (res.isNotEmpty() && res[0] != null) res[0]?.absolutePath + "/Android/data/${context.packageName}/files/quran"
    else "-"
}

fun getQuranDirectoryInInternal(context: Context): String =
    context.getExternalFilesDir("quran")?.absolutePath ?: ""

fun getSuraFileName(sura: Int) = when {
    sura < 10 -> "00$sura.zip"
    sura < 100 -> "0$sura.zip"
    else -> "$sura.zip"
}

fun getQuranDBDownloadFolder(context: Context): String =
    context.getExternalFilesDir("quranDB")?.absolutePath ?: ""

fun getAyaFileName(sura: Int, aya: Int) =
    when {
        sura < 10 -> when {
            aya < 10 -> "00${sura}00${aya}.mp3"
            aya < 100 -> "00${sura}0${aya}.mp3"
            else -> "00${sura}${aya}.mp3"
        }
        sura < 100 -> when {
            aya < 10 -> "0${sura}00${aya}.mp3"
            aya < 100 -> "0${sura}0${aya}.mp3"
            else -> "0${sura}${aya}.mp3"
        }
        else -> when {
            aya < 10 -> "${sura}00${aya}.mp3"
            aya < 100 -> "${sura}0${aya}.mp3"
            else -> "${sura}${aya}.mp3"
        }
    }

fun kyFarsiToArabicCharacters(text: String?): String {
    return text?.replace("ک", "ك")?.replace("ی", "ي") ?: ""
}

fun kKurdishToArabicCharacters(text: String?): String {
    return text?.replace("ک", "ك") ?: ""
}

fun getRootDirs(context: Context): ArrayList<File?> {
    var result: ArrayList<File?>? = null
    if (Build.VERSION.SDK_INT >= 19) {
        val dirs: Array<File?> = context.applicationContext.getExternalFilesDirs(null)
        for (a in dirs.indices) {
            if (dirs[a] == null) {
                continue
            }
            val path = dirs[a]!!.absolutePath
            val idx = path.indexOf("/Android")
            if (idx >= 0) {
                if (result == null) {
                    result = ArrayList()
                }
                result.add(File(path.substring(0, idx)))
            }
        }
    }
    if (result == null) {
        result = ArrayList()
    }
    if (result.isEmpty()) {
        @Suppress("DEPRECATION")
        result.add(Environment.getExternalStorageDirectory())
    }
    return result
}

fun formatHtmlToSpanned(htmlContent: String): Spanned {
    return when {
        htmlContent.isEmpty() -> SpannableString("")
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ->
            Html.fromHtml(htmlContent, HtmlCompat.FROM_HTML_MODE_LEGACY)
        else ->
            @Suppress("DEPRECATION")
            Html.fromHtml(htmlContent)
    }
}
