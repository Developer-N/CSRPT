package ir.namoo.quran.chapters.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey @ColumnInfo(name = "sura") val sura: Int,
    @ColumnInfo(name = "ayas_count") val ayaCount: Int,
    @ColumnInfo(name = "first_aya_id") val firstAyaId: Int,
    @ColumnInfo(name = "name_arabic") val nameArabic: String,
    @ColumnInfo(name = "name_transliteration") val nameTransliteration: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "revelation_order") val revelationOrder: Int,
    @ColumnInfo(name = "rukus") val rukus: Int,
    @ColumnInfo(name = "bismillah") val bismillah: Int,
    @ColumnInfo(name = "fav") val fav: Int,
    @ColumnInfo(name = "description") val description: String?
)//end of class ChaptersEntity

@Entity(tableName = "hezb")
data class HizbEntity(
    @ColumnInfo(name = "aya") val aya: Int,
    @ColumnInfo(name = "sura") val sura: Int,
    @ColumnInfo(name = "Page") val page: Int,
    @PrimaryKey @ColumnInfo(name = "hizb") val hizb: Int,
    @ColumnInfo(name = "JozA") val jozA: Int
)//end of class HezbEntity

@Entity(tableName = "juz")
data class JuzEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "sura") val sura: Int,
    @ColumnInfo(name = "aya") val aya: Int
)//end of class JuzEntity

@Entity(tableName = "page")
data class PageEntity(
    @PrimaryKey @ColumnInfo(name = "MetaDataID") val metaDataID: Int,
    @ColumnInfo(name = "sura") val sura: Int,
    @ColumnInfo(name = "page") val page: Int,
    @ColumnInfo(name = "aya") val aya: Int
)
