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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.namoo.commons.utils.fixTime
import ir.namoo.commons.utils.timeToDouble


@Database(entities = [DownloadedPrayTimesEntity::class], version = 3, exportSchema = false)
abstract class DownloadedPrayTimesDB : RoomDatabase() {
    abstract fun downloadedPrayTimes(): DownloadedPrayTimesDAO

    companion object {
        private var instance: DownloadedPrayTimesDB? = null

        fun getInstance(context: Context): DownloadedPrayTimesDB {
            return instance ?: synchronized(DownloadedPrayTimesDB::class) {
                val db = Room.databaseBuilder(
                    context.applicationContext, DownloadedPrayTimesDB::class.java, "dpt_db"
                ).addMigrations(MIGRATION_2_3).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                instance = db
                db
            }
        }

        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE DownloadedPrayTimes ADD COLUMN asr_hanafi TEXT NOT NULL DEFAULT \"00:00\"")
            }

        }
    }

}//end of class

@Entity(tableName = "DownloadedPrayTimes")
data class DownloadedPrayTimesEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "day") var day: Int,
    @ColumnInfo(name = "fajr") var fajr: String,
    @ColumnInfo(name = "sunrise") var sunrise: String,
    @ColumnInfo(name = "dhuhr") var dhuhr: String,
    @ColumnInfo(name = "asr") var asr: String,
    @ColumnInfo(name = "asr_hanafi") var asrHanafi: String,
    @ColumnInfo(name = "maghrib") var maghrib: String,
    @ColumnInfo(name = "isha") var isha: String,
    @ColumnInfo(name = "city_id") var cityID: Int,
    @ColumnInfo(name = "created_at") var createdAt: String,
    @ColumnInfo(name = "updated_at") var updatedAt: String
) {
    fun toDouble(time: String): Double = timeToDouble(time)

    fun toDoubleAndFix(time: String, fixMin: Int): Double = toDouble(fixTime(time, fixMin))
}


@Dao
interface DownloadedPrayTimesDAO {

    @Query("select * from DownloadedPrayTimes")
    suspend fun getAllDownloaded(): List<DownloadedPrayTimesEntity>

    @Query("select * from DownloadedPrayTimes where city_id=:cityID and day=:day")
    suspend fun getDownloadFor(cityID: Int, day: Int): DownloadedPrayTimesEntity?

    @Query("select * from DownloadedPrayTimes where city_id=:cityID")
    suspend fun getDownloadFor(cityID: Int): List<DownloadedPrayTimesEntity>

    @Query("select distinct city_id from DownloadedPrayTimes")
    suspend fun getDownloadedCitiesID(): List<Int>

    @Query("delete from DownloadedPrayTimes where city_id=:cityID")
    suspend fun clearDownloadFor(cityID: Int)

    @Insert(onConflict = REPLACE)
    suspend fun insertToDownload(prayTimes: List<DownloadedPrayTimesEntity>)

    @Insert(onConflict = REPLACE)
    suspend fun insertToDownload(prayTime: DownloadedPrayTimesEntity)

    @Update
    suspend fun updateDownload(prayTime: DownloadedPrayTimesEntity)

    @Update
    suspend fun updateDownload(prayTimes: List<DownloadedPrayTimesEntity>)
}
