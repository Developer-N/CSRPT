package ir.namoo.quran.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Entity
data class FileDownloadEntity(
    @PrimaryKey
    val id: Int,
    val downloadRequest: Long,
    val downloadFile: String,
    val folderPath: String,
    val position: Int
)

@Dao
interface FileDownloadDAO {

    @Query("SELECT * FROM FileDownloadEntity")
    suspend fun findAllDownloads(): List<FileDownloadEntity>?

    @Query("DELETE FROM FileDownloadEntity WHERE id = :id")
    suspend fun removeByFileId(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fileDownloadEntity: FileDownloadEntity)

}

@Database(version = 3, exportSchema = false, entities = [FileDownloadEntity::class])
abstract class FileDownloadDB : RoomDatabase() {
    abstract fun getFileDownloadDao(): FileDownloadDAO
}

class FileDownloadRepository @Inject constructor(private val fileDownloadDAO: FileDownloadDAO) {
    suspend fun findDownloadByFileId() = withContext(Dispatchers.IO) {
        fileDownloadDAO.findAllDownloads()
    }

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        fileDownloadDAO.removeByFileId(id)
    }

    suspend fun insert(fileDownloadEntity: FileDownloadEntity) = withContext(Dispatchers.IO) {
        fileDownloadDAO.insert(fileDownloadEntity)
    }
}
