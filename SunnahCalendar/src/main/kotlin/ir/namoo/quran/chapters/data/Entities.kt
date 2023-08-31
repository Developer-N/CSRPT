package ir.namoo.quran.chapters.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey @ColumnInfo(name = "sura") var sura: Int,
    @ColumnInfo(name = "ayas_count") var ayaCount: Int,
    @ColumnInfo(name = "first_aya_id") var firstAyaId: Int,
    @ColumnInfo(name = "name_arabic") var nameArabic: String,
    @ColumnInfo(name = "name_transliteration") var nameTransliteration: String,
    @ColumnInfo(name = "type") var type: String,
    @ColumnInfo(name = "revelation_order") var revelationOrder: Int,
    @ColumnInfo(name = "rukus") var rukus: Int,
    @ColumnInfo(name = "bismillah") var bismillah: Int,
    @ColumnInfo(name = "fav") var fav: Int,
    @ColumnInfo(name = "description") var description: String?
)//end of class ChaptersEntity

@Entity(tableName = "hezb")
data class HizbEntity(
    @ColumnInfo(name = "aya") var aya: Int,
    @ColumnInfo(name = "sura") var sura: Int,
    @ColumnInfo(name = "Page") var page: Int,
    @PrimaryKey @ColumnInfo(name = "hizb") var hizb: Int,
    @ColumnInfo(name = "JozA") var jozA: Int
)//end of class HezbEntity

@Entity(tableName = "juz")
data class JuzEntity(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "sura") var sura: Int,
    @ColumnInfo(name = "aya") var aya: Int
)//end of class JuzEntity

@Entity(tableName = "page")
data class PageEntity(
    @PrimaryKey @ColumnInfo(name = "MetaDataID") var metaDataID: Int,
    @ColumnInfo(name = "sura") var sura: Int,
    @ColumnInfo(name = "page") var page: Int,
    @ColumnInfo(name = "aya") var aya: Int
)
