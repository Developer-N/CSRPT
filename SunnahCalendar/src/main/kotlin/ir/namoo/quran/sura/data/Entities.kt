package ir.namoo.quran.sura.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran")
data class QuranEntity(
    @PrimaryKey @ColumnInfo("id") val id: Int,
    @ColumnInfo("surah_id") val surahID: Int,
    @ColumnInfo("verse_id") val verseID: Int,
    @ColumnInfo("page") val page: Int,
    @ColumnInfo("quran_arabic") val quranArabic: String,
    @ColumnInfo("quran_withoutharkat") val quranClean: String,
    @ColumnInfo("note") val note: String?,
    @ColumnInfo("fav") val fav: Int
)

@Entity(tableName = "bywords")
data class QuranWordEntity(
    @PrimaryKey @ColumnInfo("id") val id: Int,
    @ColumnInfo("surah_id") val surahID: Int,
    @ColumnInfo("verse_id") val verseID: Int,
    @ColumnInfo("words_id") val wordID: Int,
    @ColumnInfo("words_ar") val word: String,
    @ColumnInfo("translate_en") val translateEnglish: String,
)

@Entity(tableName = "tafsirs")
data class TafsirEntity(
    @PrimaryKey @ColumnInfo("id") val id: Int,
    @ColumnInfo("sura_id") val surahID: Int,
    @ColumnInfo("aya_id") val verseID: Int,
    @ColumnInfo("asan") val asan: String,
    @ColumnInfo("hazhar") val hazhar: String,
    @ColumnInfo("khorramdel") val khorramdel: String,
    @ColumnInfo("maisar") val maisar: String,
    @ColumnInfo("puxta") val puxta: String,
    @ColumnInfo("raman") val raman: String,
    @ColumnInfo("rebar") val rebar: String,
    @ColumnInfo("roshn") val roshn: String,
    @ColumnInfo("sahih_international") val sahihInternational: String,
    @ColumnInfo("sanahi") val sanahi: String,
    @ColumnInfo("tawhid") val tawhid: String,
    @ColumnInfo("zhian") val zhian: String
)

@Entity(tableName = "aya")
data class AyaEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("id") val id: Int,
    @ColumnInfo("sura") val surahID: Int,
    @ColumnInfo("aya") val verseID: Int,
    @ColumnInfo("text") val text: String,
)
