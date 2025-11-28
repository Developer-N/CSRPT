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

@Entity("words")
data class Words(
    @PrimaryKey val id: Int,
    val location: String,
    val surah: Int,
    val ayah: Int,
    val word: Int,
    val text: String
)

@Dao
interface QPCDao {
    @Query("SELECT * FROM words WHERE surah = :surah AND ayah = :ayah")
    suspend fun getVerse(surah: Int, ayah: Int): List<Words>
}

@Database(entities = [Words::class], version = 1, exportSchema = false)
@SuppressLint("SdCardPath")
abstract class QPCDatabase : RoomDatabase() {
    abstract fun qpcDao(): QPCDao

    companion object {
        private var instance: QPCDatabase? = null
        fun getInstance(context: Context): QPCDatabase {
            if (!File("/data/data/${context.packageName}/databases/qpc-v2.db").exists()) {
                copyDB(context)
                unzip(context)
            }

            return instance ?: synchronized(this) {
                val ins = Room.databaseBuilder(
                    context.applicationContext,
                    QPCDatabase::class.java,
                    "qpc-v2.db"
                ).build()
                instance = ins
                ins
            }

        }

        private fun copyDB(context: Context): Boolean = runCatching {
            val dis = File("/data/data/${context.packageName}/databases")
            if (!dis.exists()) dis.mkdir()
            val outPutFile = File("/data/data/${context.packageName}/databases/qpc-v2.db.zip")
            val fileOutputStream = FileOutputStream(outPutFile)
            context.assets.open("qpc-v2.db.zip").copyTo(fileOutputStream)
            fileOutputStream.close()
            true
        }.onFailure(logException).getOrDefault(false)


        private fun unzip(context: Context): Boolean = runCatching {
            ZipFile("/data/data/${context.packageName}/databases/qpc-v2.db.zip").extractAll("/data/data/${context.packageName}/databases/")
            File("/data/data/${context.packageName}/databases/qpc-v2.db.zip").delete()
            true
        }.onFailure(logException).getOrDefault(false)
    }
}
