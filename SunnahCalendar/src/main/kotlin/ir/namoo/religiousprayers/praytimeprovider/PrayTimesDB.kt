package ir.namoo.religiousprayers.praytimeprovider

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import ir.namoo.commons.utils.fixTime
import ir.namoo.commons.utils.timeToDouble


@Database(
    entities = [CurrentPrayTimesEntity::class, EditedPrayTimesEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PrayTimesDB : RoomDatabase() {
    abstract fun prayTimes(): PrayTimesDAO

    companion object {
        private var instance: PrayTimesDB? = null

        fun getInstance(context: Context): PrayTimesDB {
            return instance ?: synchronized(PrayTimesDB::class) {
                val db = Room.databaseBuilder(
                    context.applicationContext, PrayTimesDB::class.java, "sunnah_db"
                ).setJournalMode(JournalMode.TRUNCATE).build()
                instance = db
                db
            }
        }
    }

}//end of class

//########################################### DAO
@Dao
interface PrayTimesDAO {

    @Query("select * from CurrentPrayTimes")
    suspend fun getAll(): List<CurrentPrayTimesEntity>

    @Query("select * from CurrentPrayTimes where dayNumber=:dayNumber")
    suspend fun get(dayNumber: Int): CurrentPrayTimesEntity?

    @Insert(onConflict = REPLACE)
    suspend fun insert(prayTimes: List<CurrentPrayTimesEntity>)

    @Query("delete from CurrentPrayTimes")
    suspend fun cleanCurrentPrayTimes()

    //    $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
    @Query("select * from EditedPrayTimes")
    suspend fun getAllEdited(): List<EditedPrayTimesEntity>

    @Query("select * from EditedPrayTimes where dayNumber=:dayNumber")
    suspend fun getEdited(dayNumber: Int): EditedPrayTimesEntity?

    @Insert(onConflict = REPLACE)
    suspend fun insertEdited(prayTimes: List<EditedPrayTimesEntity>)

    @Update
    suspend fun updateEdited(prayTimes: List<EditedPrayTimesEntity>)

    @Query("delete from EditedPrayTimes")
    suspend fun clearEditedPrayTimes()

}

//######################################### Entities

@Entity(tableName = "CurrentPrayTimes")
data class CurrentPrayTimesEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "dayNumber") var dayNumber: Int,
    @ColumnInfo(name = "fajr") var fajr: String,
    @ColumnInfo(name = "sunrise") var sunrise: String,
    @ColumnInfo(name = "dhuhr") var dhuhr: String,
    @ColumnInfo(name = "asr") var asr: String,
    @ColumnInfo(name = "maghrib") var maghrib: String,
    @ColumnInfo(name = "isha") var isha: String
) {
    fun toDouble(time: String): Double = timeToDouble(time)

    fun toDoubleAndFix(time: String, fixMin: Int): Double = toDouble(fixTime(time, fixMin))
}


@Entity(tableName = "EditedPrayTimes")
data class EditedPrayTimesEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "dayNumber") var dayNumber: Int,
    @ColumnInfo(name = "fajr") var fajr: String,
    @ColumnInfo(name = "sunrise") var sunrise: String,
    @ColumnInfo(name = "dhuhr") var dhuhr: String,
    @ColumnInfo(name = "asr") var asr: String,
    @ColumnInfo(name = "maghrib") var maghrib: String,
    @ColumnInfo(name = "isha") var isha: String
) {
    fun toDouble(time: String): Double = timeToDouble(time)

    fun toDoubleAndFix(time: String, fixMin: Int): Double = toDouble(fixTime(time, fixMin))
}
