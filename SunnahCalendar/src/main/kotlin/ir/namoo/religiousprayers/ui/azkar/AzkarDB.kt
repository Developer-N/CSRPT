package ir.namoo.religiousprayers.ui.azkar

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.byagowi.persiancalendar.utils.logException
import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.FileOutputStream

@Database(
    entities = [AzkarCategory::class, AzkarChapter::class, AzkarItem::class, AzkarReference::class, Tasbih::class],
    version = 1,
    exportSchema = false
)
@SuppressLint("SdCardPath")
abstract class AzkarDB : RoomDatabase() {
    abstract fun azkarDao(): AzkarDAO

    companion object {
        private var instance: AzkarDB? = null

        fun getInstance(context: Context): AzkarDB {
            if (!File("/data/data/" + context.packageName.toString() + "/databases/azkarv2.db").exists()) {
                copyDB(context)
                unzip(context)
            }
            return instance ?: synchronized(AzkarDB::class) {
                val db = Room.databaseBuilder(
                    context.applicationContext, AzkarDB::class.java, "azkarv2.db"
                ).allowMainThreadQueries().build()
                instance = db
                db
            }
        }

        private fun copyDB(context: Context): Boolean = runCatching {
            val dis = File("/data/data/${context.packageName}/databases")
            if (!dis.exists()) dis.mkdir()
            val outPutFile = File("/data/data/${context.packageName}/databases/azkarv2.zip")
            val fileOutputStream = FileOutputStream(outPutFile)
            context.assets.open("azkarv2.zip").copyTo(fileOutputStream)
            fileOutputStream.close()
            true
        }.onFailure(logException).getOrDefault(false)


        private fun unzip(context: Context): Boolean = runCatching {
            ZipFile(
                "/data/data/${context.packageName}/databases/azkarv2.zip", ("@zKa6").toCharArray()
            ).extractAll("/data/data/${context.packageName}/databases/")
            File("/data/data/${context.packageName}/databases/azkarv2.zip").delete()
            true
        }.onFailure(logException).getOrDefault(false)
    }
}

@Dao
interface AzkarDAO {
    @Query("select * from azkar_categories")
    suspend fun getAzkarCategories(): List<AzkarCategory>

    @Query("select * from azkar_chapters")
    suspend fun getAzkarChapters(): List<AzkarChapter>

    @Query("select * from azkar_chapters where id=:chapterID")
    suspend fun getAzkarChapter(chapterID: Int): AzkarChapter

    @Update
    suspend fun updateAzkarChapter(azkarChapter: AzkarChapter)

    @Query("select * from azkar_items where chapter_id=:chapterID")
    suspend fun getAzkarItems(chapterID: Int): List<AzkarItem>

    @Query("select * from azkar_references where chapter_id=:chapterID")
    suspend fun getAzkarReferences(chapterID: Int): List<AzkarReference>

    @Query("select * from tasbih")
    suspend fun getTasbihList(): List<Tasbih>

}

@Entity(tableName = "azkar_categories")
data class AzkarCategory(
    @ColumnInfo(name = "id") @PrimaryKey val id: Int,
    @ColumnInfo(name = "ckb") val kurdish: String?,
    @ColumnInfo(name = "ar") val arabic: String?,
    @ColumnInfo(name = "fa") val persian: String?,
    @ColumnInfo(name = "en") val english: String?
)

@Entity(tableName = "azkar_chapters")
data class AzkarChapter(
    @ColumnInfo(name = "id") @PrimaryKey val id: Int,
    @ColumnInfo(name = "category_id") val categoryID: Int,
    @ColumnInfo(name = "ckb") val kurdish: String?,
    @ColumnInfo(name = "ar") val arabic: String?,
    @ColumnInfo(name = "fa") val persian: String?,
    @ColumnInfo(name = "en") val english: String?,
    @ColumnInfo(name = "fav") var fav: Int
)

@Entity(tableName = "azkar_items")
data class AzkarItem(
    @ColumnInfo(name = "id") @PrimaryKey val id: Int,
    @ColumnInfo(name = "chapter_id") val chapterID: Int,
    @ColumnInfo(name = "ckb") val kurdish: String?,
    @ColumnInfo(name = "ar") val arabic: String?,
    @ColumnInfo(name = "fa") val persian: String?,
    @ColumnInfo(name = "en") val english: String?,
    @ColumnInfo(name = "sound") val sound: String?
)

@Entity(tableName = "azkar_references")
data class AzkarReference(
    @ColumnInfo(name = "id") @PrimaryKey val id: Int,
    @ColumnInfo(name = "chapter_id") val chapterID: Int,
    @ColumnInfo(name = "ckb") val kurdish: String?,
    @ColumnInfo(name = "ar") val arabic: String?,
    @ColumnInfo(name = "fa") val persian: String?,
    @ColumnInfo(name = "en") val english: String?
)

@Entity(tableName = "tasbih")
data class Tasbih(
    @ColumnInfo(name = "id") @PrimaryKey val id: Int,
    @ColumnInfo(name = "zikr") val zikr: String,
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "time") val totalCount: Int
)
