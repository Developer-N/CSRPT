package ir.namoo.quran.qari

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update

@Database(entities = [QariEntity::class], version = 1, exportSchema = false)
abstract class QariDB : RoomDatabase() {
    abstract fun qariDAO(): QariDAO

    companion object {
        private var instance: QariDB? = null
        fun getInstance(context: Context): QariDB {
            return instance ?: synchronized(QariDB::class) {
                val db =
                    Room.databaseBuilder(context.applicationContext, QariDB::class.java, "qari.db")
                        .build()
                instance = db
                db
            }
        }
    }
}

@Dao
interface QariDAO {

    @Query("select * from qari")
    suspend fun getAllQari(): List<QariEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(qariEntity: QariEntity)

    @Query("delete from qari")
    suspend fun clearQariDB()

    @Delete
    suspend fun delete(qariEntity: QariEntity)

    @Update
    suspend fun update(qariEntity: QariEntity)
}

@Entity(tableName = "qari")
data class QariEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    @ColumnInfo("folder_name") val folderName: String,
    @ColumnInfo("photo_link") val photoLink: String?,
    @ColumnInfo("verse_by_verse_base_link") val verseByVerseBaseLink: String?,
    @ColumnInfo("sura_zips_base_link") val suraZipsBaseLink: String?,
    @ColumnInfo("full_zip_link") val fullZipLink: String?,
    @ColumnInfo("created_at") val createdAt: String,
    @ColumnInfo("updated_at") val updatedAt: String,
    @ColumnInfo("deleted_at") val deletedAt: String?
)
