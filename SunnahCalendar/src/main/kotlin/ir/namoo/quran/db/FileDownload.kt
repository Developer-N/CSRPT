package ir.namoo.quran.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Entity
data class FileDownloadEntity(
    @PrimaryKey val id: Int=0,
    val downloadRequest: Long,
    val downloadFile: String,
    val folderPath: String,
    val sura: Int
)

@Dao
interface FileDownloadDAO {

    @Query("SELECT * FROM FileDownloadEntity")
    suspend fun findAllDownloads(): List<FileDownloadEntity>

    @Query("DELETE FROM FileDownloadEntity WHERE id = :id")
    suspend fun removeByFileId(id: Int)

    @Insert(onConflict = REPLACE)
    suspend fun insert(fileDownloadEntity: FileDownloadEntity)

}

@Database(version = 4, exportSchema = false, entities = [FileDownloadEntity::class])
abstract class FileDownloadDB : RoomDatabase() {
    abstract fun getFileDownloadDao(): FileDownloadDAO

    companion object {
        private var instance: FileDownloadDB? = null
        fun getInstance(context: Context): FileDownloadDB {
            return instance ?: synchronized(FileDownloadDB::class) {
                val db = Room.databaseBuilder(
                    context.applicationContext, FileDownloadDB::class.java, "fileDownload.db"
                ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
                instance = db
                db
            }
        }
    }
}

class FileDownloadRepository constructor(private val fileDownloadDAO: FileDownloadDAO) {
    suspend fun findDownloadByFileId(): List<FileDownloadEntity> {
        runCatching {
            return fileDownloadDAO.findAllDownloads()
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        runCatching {
            fileDownloadDAO.removeByFileId(id)
        }
    }

    suspend fun insert(fileDownloadEntity: FileDownloadEntity) = withContext(Dispatchers.IO) {
        runCatching {
            fileDownloadDAO.insert(fileDownloadEntity)
        }.onFailure { ex ->
            Timber.tag("NAMOO").e(ex, "insert error -> %s", ex.message)
        }
    }
}
