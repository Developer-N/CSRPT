package ir.namoo.commons.model

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update

@Entity(tableName = "athans")
data class Athan(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "link")
    val link: String,
    @ColumnInfo(name = "type")
    val type: Int,
    @ColumnInfo(name = "title")
    val fileName: String
)

@Dao
interface AthanDAO {
    @Query("select * from athans")
    suspend fun getAllAthans(): List<Athan>

    @Query("delete from athans")
    suspend fun clearDB()

    @Query("select * from athans where id=:id")
    suspend fun getAthan(id: Int): Athan?

    @Query("select * from athans where name=:name")
    suspend fun getAthan(name: String): Athan?

    @Update
    suspend fun update(athan: Athan)

    @Insert
    suspend fun insert(athan: Athan)

    @Delete
    suspend fun delete(athan: Athan)

}

@Database(entities = [Athan::class], version = 4, exportSchema = false)
abstract class AthanDB : RoomDatabase() {
    abstract fun athanDAO(): AthanDAO

    companion object {
        private var instance: AthanDB? = null
        fun getInstance(applicationContext: Context): AthanDB {
            return instance ?: synchronized(AthanDB::class) {
                val ins = Room.databaseBuilder(
                    applicationContext, AthanDB::class.java, "athan.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                instance = ins
                ins
            }
        }
    }
}

