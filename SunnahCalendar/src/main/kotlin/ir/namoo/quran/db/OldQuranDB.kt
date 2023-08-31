package ir.namoo.quran.db

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [OldQuranEntity::class, OldHizbEntity::class, OldJuzEntity::class, OldPageEntity::class, OldChapterEntity::class],
    version = 3,
    exportSchema = false
)
abstract class OldQuranDB : RoomDatabase() {
    abstract fun chaptersDao(): OldChaptersDao
    abstract fun quranDao(): OldQuranDao
    abstract fun pjhDao(): OldPJHDao

    companion object {
        private var instance: OldQuranDB? = null

        fun getInstance(context: Context): OldQuranDB {
            return instance ?: synchronized(OldQuranDB::class) {
                val db = Room.databaseBuilder(
                    context.applicationContext, OldQuranDB::class.java, "quran.db"
                ).addMigrations(MIGRATION_2_3).build()
                instance = db
                db
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        }
    }
}//end of class QuranDB

@Dao
interface OldChaptersDao {
    @Query("SELECT * FROM chapters")
    fun getAllChapters(): MutableList<OldChapterEntity>

    @Query("SELECT * FROM chapters where sura=:sura")
    fun getChapter(sura: Int): OldChapterEntity

    @Query("SELECT * FROM chapters where sura=:sura")
    fun getChapter1(sura: Int): OldChapterEntity

    @Update
    fun update(chaptersEntity: OldChapterEntity)

}//end of interface

@Dao
interface OldPJHDao {
    @Query("SELECT * FROM safhe where page=:page")
    fun getAllPage(page: Int): LiveData<List<OldPageEntity>>

    @Query("SELECT * FROM juz")
    fun getAllJuz(): LiveData<List<OldJuzEntity>>

    @Query("SELECT * FROM hezb")
    fun getAllHezb(): LiveData<List<OldHizbEntity>>
}

@Dao
interface OldQuranDao {
    @Query("SELECT * FROM quran_all WHERE sura=:sura")
    fun getAllFor(sura: Int): MutableList<OldQuranEntity>?

    @Query("SELECT * FROM quran_all WHERE `index`=:index")
    fun getVerseByIndex(index: Int): OldQuranEntity?

    @Query("SELECT * FROM quran_all WHERE fav==1")
    fun getAllBookmarks(): MutableList<OldQuranEntity>?

    @Query("SELECT * FROM quran_all WHERE note!='-'")
    fun getAllNotes(): MutableList<OldQuranEntity>?

    @Query("SELECT * FROM quran_all")
    fun getAll(): MutableList<OldQuranEntity>

    @Update
    fun update(quranEntity: OldQuranEntity)

}//end of interface QuranTN

@Entity(tableName = "chapters")
data class OldChapterEntity(

    @PrimaryKey
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
data class OldHizbEntity(

    @ColumnInfo(name = "aya")
    var aya: Int?,
    @ColumnInfo(name = "sura")
    var sura: Int?,

    @ColumnInfo(name = "Page")
    var page: Int?,

    @PrimaryKey
    @ColumnInfo(name = "hizb")
    var hizb: Int,

    @ColumnInfo(name = "JozA")
    var jozA: Int?
)//end of class HezbEntity

@Entity(tableName = "juz")
data class OldJuzEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Int,

    @ColumnInfo(name = "sura")
    var sura: Int?,

    @ColumnInfo(name = "aya")
    var aya: Int?
)//end of class JuzEntity

@Entity(tableName = "safhe")
data class OldPageEntity(

    @PrimaryKey
    @ColumnInfo(name = "MetaDataID")
    var metaDataID: Int,

    @ColumnInfo(name = "sura")
    var sura: Int?,

    @ColumnInfo(name = "page")
    var page: Int?,

    @ColumnInfo(name = "aya")
    var aya: Int?
)

@Entity(tableName = "quran_all")
data class OldQuranEntity(

    @PrimaryKey
    @ColumnInfo(name = "index")
    var index: Int,

    @ColumnInfo(name = "sura")
    var sura: Int?,

    @ColumnInfo(name = "aya")
    var aya: Int?,

    @ColumnInfo(name = "simple")
    var simple: String?,

    @ColumnInfo(name = "simple_clean")
    var simpleClean: String?,

    @ColumnInfo(name = "uthmani")
    var uthmani: String?,

    @ColumnInfo(name = "en_transilation")
    var enTransilation: String?,

    @ColumnInfo(name = "en_pickthall")
    var enPickthall: String?,

    @ColumnInfo(name = "fa_khorramdel")
    var faKhorramdel: String?,

    @ColumnInfo(name = "ku_asan")
    var kuAsan: String?,

    @ColumnInfo(name = "note")
    var note: String?,

    @ColumnInfo(name = "fav")
    var fav: Int?

)//end of class QuranEntity
