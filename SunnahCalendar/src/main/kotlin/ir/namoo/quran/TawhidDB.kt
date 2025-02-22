package ir.namoo.quran

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity("tawhid")
data class Tawhid(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "surah_id") val surahID: Int,
    @ColumnInfo(name = "verse_id") val verseID: Int,
    val text: String
)

@Dao
interface TawhidDAO {
    @Query("select * from tawhid")
    suspend fun getAll(): List<Tawhid>
}

@Database(entities = [Tawhid::class], version = 2, exportSchema = false)
abstract class TawhidDB : RoomDatabase() {
    abstract fun tawhidDAO(): TawhidDAO

    companion object {
        private var instance: TawhidDB? = null
        fun getInstance(context: Context): TawhidDB {
            return instance ?: synchronized(TawhidDB::class) {
                val ins = Room.databaseBuilder(
                    context.applicationContext, TawhidDB::class.java, "tafseeri_tawhid.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                instance = ins
                ins
            }
        }
    }
}
