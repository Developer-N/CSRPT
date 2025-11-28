package ir.namoo.quran.db

import android.content.Context
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

@Entity("last_visited_page")
data class LastVisitedPageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, val page: Int
)

@Dao
interface LastVisitedPageDAO {
    @Query("select * from last_visited_page")
    suspend fun getAllVisitedPages(): List<LastVisitedPageEntity>

    @Insert
    suspend fun insert(lastVisitedPageEntity: LastVisitedPageEntity)

    @Update
    suspend fun update(lastVisitedPageEntity: LastVisitedPageEntity)

    @Delete
    suspend fun delete(lastVisitedPageEntity: LastVisitedPageEntity)
}

@Database(entities = [LastVisitedPageEntity::class], version = 1, exportSchema = false)
abstract class LastVisitedPageDB : RoomDatabase() {
    abstract fun lastVisitedPageDao(): LastVisitedPageDAO

    companion object {
        private var instance: LastVisitedPageDB? = null
        fun getInstance(context: Context): LastVisitedPageDB {
            return instance ?: synchronized(LastVisitedPageDB::class.java) {
                val ins = Room.databaseBuilder(
                    context.applicationContext, LastVisitedPageDB::class.java, "LastVisitedPage.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                instance = ins
                ins
            }
        }
    }
}
