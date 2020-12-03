package ir.namoo.religiousprayers.praytimes

import android.content.Context
import androidx.room.*


@Database(entities = [DownloadedPrayTimesEntity::class], version = 1)
abstract class DPTDB : RoomDatabase() {
    abstract fun downloadedPrayTimes(): DownloadedPrayTimesDAO

    companion object {
        private var instance: DPTDB? = null
        fun getInstance(applicationContext: Context): DPTDB {
            if (instance == null)
                synchronized(DPTDB::class) {
                    instance = Room.databaseBuilder(
                        applicationContext,
                        DPTDB::class.java, "dpt_db"
                    ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
                }
            return instance!!
        }
    }
}//end of class

@Entity(tableName = "DownloadedPrayTimes")
data class DownloadedPrayTimesEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "dayNumber") var dayNumber: Int,
    @ColumnInfo(name = "fajr") var fajr: String,
    @ColumnInfo(name = "sunrise") var sunrise: String,
    @ColumnInfo(name = "dhuhr") var dhuhr: String,
    @ColumnInfo(name = "asr") var asr: String,
    @ColumnInfo(name = "maghrib") var maghrib: String,
    @ColumnInfo(name = "isha") var isha: String,
    @ColumnInfo(name = "city") var city: String

)

@Dao
interface DownloadedPrayTimesDAO {

    @Query("select * from DownloadedPrayTimes")
    fun getAllDownloaded(): List<DownloadedPrayTimesEntity>?

    @Query("select * from DownloadedPrayTimes where city=:city and dayNumber=:dayNumber")
    fun getDownloadFor(city: String, dayNumber: Int): DownloadedPrayTimesEntity?

    @Query("select * from DownloadedPrayTimes where city=:city")
    fun getDownloadFor(city: String): List<DownloadedPrayTimesEntity>?

    @Query("select city from DownloadedPrayTimes")
    fun getCities(): MutableList<String>?

    @Query("delete from DownloadedPrayTimes where city=:city")
    fun clearDownloadFor(city: String)

    @Insert
    fun insertToDownload(prayTimes: List<DownloadedPrayTimesEntity>)

    @Insert
    fun insertToDownload(prayTime: DownloadedPrayTimesEntity)

    @Update
    fun updateDownload(prayTime: DownloadedPrayTimesEntity)

    @Update
    fun updateDownload(prayTimes: List<DownloadedPrayTimesEntity>)
}