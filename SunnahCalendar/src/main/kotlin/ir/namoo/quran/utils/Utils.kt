package ir.namoo.quran.utils

import android.content.Context
import android.graphics.Typeface
import android.os.Environment
import androidx.core.content.edit
import ir.namoo.commons.utils.appPrefsLite
import java.io.File

var quranFont: Typeface = Typeface.SANS_SERIF
    private set
var quranFontSize: Float = 0.0f
    private set

var englishFont: Typeface = Typeface.SANS_SERIF
    private set
var englishFontSize = 0.0f
    private set

var kurdishFont: Typeface = Typeface.SANS_SERIF
    private set
var kurdishFontSize = 0.0f
    private set

var farsiFont: Typeface = Typeface.SANS_SERIF
    private set
var farsiFontSize = 0.0f
    private set

var uthmanTahaFont: Typeface = Typeface.SANS_SERIF
    private set

var chapterException: Throwable? = null

fun initQuranUtils(context: Context) {
    val prefs = context.appPrefsLite

    uthmanTahaFont = Typeface.createFromAsset(
        context.assets, "fonts/Quran_UthmanTahaN1B.ttf"
    )

    //Set new fonts if old font selected
    if (prefs.getString(PREF_QURAN_FONT, DEFAULT_QURAN_FONT)
            ?.contains("Vazir.ttf") == true || prefs.getString(PREF_QURAN_FONT, DEFAULT_QURAN_FONT)
            ?.startsWith("fonts/arabic_") == true
    ) prefs.edit { putString(PREF_QURAN_FONT, DEFAULT_QURAN_FONT) }
    if (prefs.getString(PREF_FARSI_FONT, DEFAULT_QURAN_FONT)
            ?.contains("Vazir.ttf") == true
    ) prefs.edit { putString(PREF_FARSI_FONT, "fonts/Vazirmatn.ttf") }
    if (prefs.getString(PREF_KURDISH_FONT, DEFAULT_QURAN_FONT)
            ?.contains("Vazir.ttf") == true
    ) prefs.edit { putString(PREF_KURDISH_FONT, "fonts/Vazirmatn.ttf") }

    quranFont = Typeface.createFromAsset(
        context.assets, prefs.getString(PREF_QURAN_FONT, DEFAULT_QURAN_FONT)
    )
    quranFontSize = prefs.getFloat(PREF_QURAN_FONT_SIZE, DEFAULT_QURAN_FONT_SIZE)
    englishFont = Typeface.createFromAsset(
        context.assets, prefs.getString(PREF_ENGLISH_FONT, DEFAULT_ENGLISH_FONT)
    )
    englishFontSize = prefs.getFloat(PREF_ENGLISH_FONT_SIZE, DEFAULT_ENGLISH_FONT_SIZE)
    kurdishFont = Typeface.createFromAsset(
        context.assets, prefs.getString(PREF_KURDISH_FONT, DEFAULT_KURDISH_FONT)
    )
    kurdishFontSize = prefs.getFloat(PREF_KURDISH_FONT_SIZE, DEFAULT_KURDISH_FONT_SIZE)
    farsiFont = Typeface.createFromAsset(
        context.assets, prefs.getString(PREF_FARSI_FONT, DEFAULT_FARSI_FONT)
    )
    farsiFontSize = prefs.getFloat(PREF_FARSI_FONT_SIZE, DEFAULT_FARSI_FONT_SIZE)
}

fun getSelectedQuranDirectoryPath(context: Context): String {
    val inPref = context.appPrefsLite.getString(PREF_STORAGE_PATH, "-") ?: "-"
    return if (inPref.contains("emulated") || inPref == "-") context.getExternalFilesDir("quran")?.absolutePath
        ?: ""
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

fun getAyaFileName(sura: Int, aya: Int) = when {
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

fun getRootDirs(context: Context): ArrayList<File?> {
    var result: ArrayList<File?>? = null
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
    if (result == null) {
        result = ArrayList()
    }
    if (result.isEmpty()) {
        result.add(Environment.getExternalStorageDirectory())
    }
    return result
}

fun String.getWordsForSearch(): List<String> {
    if (this.isEmpty()) return emptyList()
    val wordsForSearch = mutableListOf<String>()
    wordsForSearch.add(this)
    if (this.contains("ی"))
        wordsForSearch.add(this.replace("ی", "ي"))
    if (this.contains("ک"))
        wordsForSearch.add(this.replace("ک", "ك"))
    if (this.contains("ی") && this.contains("ک"))
        wordsForSearch.add(this.replace("ی", "ي").replace("ک", "ك"))
    if (this.contains("ا")) {
        wordsForSearch.add(this.replace("ا", "إ"))
        wordsForSearch.add(this.replace("ا", "أ"))
    }
    return wordsForSearch
}
