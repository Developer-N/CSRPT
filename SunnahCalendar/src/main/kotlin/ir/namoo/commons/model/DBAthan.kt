package ir.namoo.commons.model

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

@Entity(tableName = "athans")
data class Athan(
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "link")
    var link: String,
    @ColumnInfo(name = "type")
    var type: Int,
    @ColumnInfo(name = "title")
    var fileName: String = ""
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0
}

@Dao
interface AthanDAO {
    @Query("select * from athans")
    fun getAllAthans(): List<Athan>

    @Query("delete from athans")
    fun clearDB()

    @Query("select * from athans where id=:id")
    fun getAthan(id: Int): Athan?

    @Query("select * from athans where name=:name")
    fun getAthan(name: String): Athan?

    @Update
    fun update(athan: Athan)

    @Insert
    fun insert(athan: Athan)

    @Delete
    fun delete(athan: Athan)

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
                ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
                instance = ins
                ins
            }
        }
    }
}

