package ir.namoo.quran.db

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.byagowi.persiancalendar.utils.logException
import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.FileOutputStream

@Entity(tableName = "chapters")
data class QCFChapters(@PrimaryKey val id: Int, val text: String)

@Entity(tableName = "quran")
data class QCFQuran(@PrimaryKey val id: Int, val page: Int, val text: String)

@Dao
interface QCFDao {
    @Query("SELECT * FROM chapters")
    suspend fun getChapters(): List<QCFChapters>

    @Query("SELECT * FROM quran WHERE page = :page")
    suspend fun getQuran(page: Int): List<QCFQuran>
}

@Database(entities = [QCFChapters::class, QCFQuran::class], version = 2, exportSchema = false)
@SuppressLint("SdCardPath")
abstract class QCFDatabase : RoomDatabase() {
    abstract fun qcfDao(): QCFDao

    companion object {
        private var instance: QCFDatabase? = null
        fun getInstance(context: Context): QCFDatabase {
            if (!File("/data/data/${context.packageName}/databases/quran_qcf2.db").exists()) {
                copyDB(context)
                unzip(context)
            }
            return instance ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext, QCFDatabase::class.java, "quran_qcf2.db"
                ).build()
                instance = inst
                inst
            }
        }

        private fun copyDB(context: Context): Boolean = runCatching {
            val dis = File("/data/data/${context.packageName}/databases")
            if (!dis.exists()) dis.mkdir()
            val outPutFile = File("/data/data/${context.packageName}/databases/quran_qcf2.zip")
            val fileOutputStream = FileOutputStream(outPutFile)
            context.assets.open("quran_qcf2.zip").copyTo(fileOutputStream)
            fileOutputStream.close()
            true
        }.onFailure(logException).getOrDefault(false)


        private fun unzip(context: Context): Boolean = runCatching {
            ZipFile("/data/data/${context.packageName}/databases/quran_qcf2.zip").extractAll("/data/data/${context.packageName}/databases/")
            File("/data/data/${context.packageName}/databases/quran_qcf2.zip").delete()
            true
        }.onFailure(logException).getOrDefault(false)
    }
}
