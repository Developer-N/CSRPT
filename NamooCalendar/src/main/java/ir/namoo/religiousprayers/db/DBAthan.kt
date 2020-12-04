package ir.namoo.religiousprayers.db

import android.content.Context
import androidx.room.*

@Entity(tableName = "athans")
data class Athan(
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "link")
    var link: String,
    @ColumnInfo(name = "type")
    var type: Int
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0
}

@Dao
interface AthanDAO {
    @Query("select * from athans")
    fun getAllAthans(): List<Athan>

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

@Database(entities = [Athan::class], version = 3)
abstract class AthanDB : RoomDatabase() {
    abstract fun athanDAO(): AthanDAO

    companion object {
        private var instance: AthanDB? = null
        fun getInstance(applicationContext: Context): AthanDB {
            if (instance == null) {
                synchronized(AthanDB::class) {
                    instance = Room.databaseBuilder(
                        applicationContext,
                        AthanDB::class.java, "athan.db"
                    ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
                }
            }
            return instance!!
        }
    }
}