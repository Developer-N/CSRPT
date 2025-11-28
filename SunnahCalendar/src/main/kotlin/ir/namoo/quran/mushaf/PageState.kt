package ir.namoo.quran.mushaf

import android.content.Context
import android.graphics.Typeface
import com.byagowi.persiancalendar.utils.logException
import ir.namoo.quran.sura.data.TranslateItem
import ir.namoo.quran.utils.getMushafFolder
import java.io.File
import java.util.Locale

data class PageState(
    val page: Int,
    val verses: List<Verse>,
    val isLoading: Boolean = false,
    var error: String? = null
) {
    fun getFont(context: Context): Typeface {
        runCatching {
            val folder = getMushafFolder(context) + "/QCF2BSMLfonts"
            val fontName = "QCF2${"%03d".format(Locale.ENGLISH, page)}.ttf"
            return Typeface.createFromFile(File(folder, fontName))
        }.onFailure {
            error = it.message ?: ""
            logException(it)
        }.getOrElse {
            error = it.message ?: ""
            return Typeface.SANS_SERIF
        }
    }
}

data class Verse(
    val id: Int,
    val sura: Int,
    val verseNumber: Int,
    val verseQCFText: String,
    val verseNormalText: String,
    val verseCleanText: String,
    val note: String,
    val fav: Int,
    val translates: List<TranslateItem>
)
