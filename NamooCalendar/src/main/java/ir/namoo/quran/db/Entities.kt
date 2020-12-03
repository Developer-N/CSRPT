package ir.namoo.quran.db

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapters")
data class ChapterEntity(

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "sura")
    var sura: Int,

    @ColumnInfo(name = "ayas_count")
    var ayaCount: Int?,

    @ColumnInfo(name = "first_aya_id")
    var firstAyaId: Int?,

    @ColumnInfo(name = "name_arabic")
    var nameArabic: String?,

    @ColumnInfo(name = "name_transliteration")
    var nameTransliteration: String?,

    @ColumnInfo(name = "type")
    var type: String?,

    @ColumnInfo(name = "revelation_order")
    var revelationOrder: Int?,

    @ColumnInfo(name = "rukus")
    var rukus: Int?,

    @ColumnInfo(name = "bismillah")
    var bismillah: Int?,

    @ColumnInfo(name = "fav")
    var fav: Int?
)//end of class ChaptersEntity

@Entity(tableName = "hezb")
data class HizbEntity(

    @ColumnInfo(name = "aya")
    var aya: Int?,
    @ColumnInfo(name = "sura")
    var sura: Int?,

    @ColumnInfo(name = "Page")
    var Page: Int?,

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "hizb")
    var hizb: Int,

    @ColumnInfo(name = "JozA")
    var JozA: Int?
)//end of class HezbEntity


@Entity(tableName = "juz")
data class JuzEntity(

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    var id: Int,

    @ColumnInfo(name = "sura")
    var sura: Int?,

    @ColumnInfo(name = "aya")
    var aya: Int?
)//end of class JuzEntity

@Entity(tableName = "safhe")
data class PageEntity(

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "MetaDataID")
    var MetaDataID: Int,

    @ColumnInfo(name = "sura")
    var sura: Int?,

    @ColumnInfo(name = "page")
    var page: Int?,

    @ColumnInfo(name = "aya")
    var aya: Int?
)

@Entity(tableName = "quran_all")
data class QuranEntity(

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "index")
    var index: Int,

    @ColumnInfo(name = "sura")
    var sura: Int?,

    @ColumnInfo(name = "aya")
    var aya: Int?,

    @ColumnInfo(name = "simple")
    var simple: String?,

    @ColumnInfo(name = "simple_clean")
    var simple_clean: String?,

    @ColumnInfo(name = "uthmani")
    var uthmani: String?,

    @ColumnInfo(name = "en_transilation")
    var en_transilation: String?,

    @ColumnInfo(name = "en_pickthall")
    var en_pickthall: String?,

    @ColumnInfo(name = "fa_khorramdel")
    var fa_khorramdel: String?,

    @ColumnInfo(name = "ku_asan")
    var ku_asan: String?,

    @ColumnInfo(name = "note")
    var note: String?,

    @ColumnInfo(name = "fav")
    var fav: Int?

)//end of class QuranEntity
