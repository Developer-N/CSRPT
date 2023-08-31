package ir.namoo.quran.db

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update

@Entity("last_visited")
data class LastVisitedEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "aya_id") val ayaID: Int,
    @ColumnInfo(name = "sura_id") val suraID: Int
)

@Dao
interface LastVisitedDAO {

    @Query("select * from last_visited")
    suspend fun getAllVisited(): List<LastVisitedEntity>

    @Insert
    suspend fun insert(lastVisitedEntity: LastVisitedEntity)

    @Update
    suspend fun update(lastVisitedEntity: LastVisitedEntity)

    @Delete
    suspend fun delete(lastVisitedEntity: LastVisitedEntity)
}

@Database(entities = [LastVisitedEntity::class], version = 1, exportSchema = false)
abstract class LastVisitedDB : RoomDatabase() {
    abstract fun lastVisitedDao(): LastVisitedDAO

    companion object {
        private var instance: LastVisitedDB? = null
        fun getInstance(context: Context): LastVisitedDB {
            return instance ?: synchronized(LastVisitedDB::class.java) {
                val ins = Room.databaseBuilder(
                    context.applicationContext, LastVisitedDB::class.java, "LastQuranVisited.db"
                ).fallbackToDestructiveMigration().build()
                instance = ins
                ins
            }
        }
    }
}
