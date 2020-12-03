package ir.namoo.quran.utils

import android.content.Context
import android.graphics.Typeface
import ir.namoo.religiousprayers.utils.appPrefsLite

lateinit var arabicFont: Typeface
var arabicFontSize: Float = 0.0f

lateinit var englishFont: Typeface
var englishFontSize = 0.0f

lateinit var kurdishFont: Typeface
var kurdishFontSize = 0.0f

lateinit var farsiFont: Typeface
var farsiFontSize = 0.0f


fun initQuranUtils(context: Context) {
    arabicFont = Typeface.createFromAsset(
        context.assets, context.appPrefsLite.getString(PREF_QURAN_FONT, DEFAULT_QURAN_FONT)
    )
    arabicFontSize = context.appPrefsLite.getFloat(PREF_QURAN_FONT_SIZE, DEFAULT_QURAN_FONT_SIZE)
    englishFont = Typeface.createFromAsset(
        context.assets, context.appPrefsLite.getString(PREF_ENGLISH_FONT, DEFAULT_ENGLISH_FONT)
    )
    englishFontSize =
        context.appPrefsLite.getFloat(PREF_ENGLISH_FONT_SIZE, DEFAULT_ENGLISH_FONT_SIZE)
    kurdishFont = Typeface.createFromAsset(
        context.assets, context.appPrefsLite.getString(PREF_KURDISH_FONT, DEFAULT_KURDISH_FONT)
    )
    kurdishFontSize =
        context.appPrefsLite.getFloat(PREF_KURDISH_FONT_SIZE, DEFAULT_KURDISH_FONT_SIZE)
    farsiFont = Typeface.createFromAsset(
        context.assets,
        context.appPrefsLite.getString(PREF_FARSI_FONT, DEFAULT_FARSI_FONT)
    )
    farsiFontSize = context.appPrefsLite.getFloat(PREF_FARSI_FONT_SIZE, DEFAULT_FARSI_FONT_SIZE)
}

fun isDigit(string: String): Boolean {
    for (c in string)
        if (!c.isDigit())
            return false
    return true
}

fun getQuranDirectoryPath(context: Context): String =
    context.getExternalFilesDir("quran")?.absolutePath ?: ""

fun getSuraFileName(sura: Int) = when {
    sura < 10 -> "00$sura.zip"
    sura < 100 -> "0$sura.zip"
    else -> "$sura.zip"
}

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