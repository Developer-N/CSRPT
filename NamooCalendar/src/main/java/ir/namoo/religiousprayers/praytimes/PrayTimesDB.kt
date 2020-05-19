package ir.namoo.religiousprayers.praytimes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CurrentPrayTimesEntity::class,EditedPrayTimesEntity::class], version = 1)
abstract class PrayTimesDB : RoomDatabase() {
    abstract fun prayTimes(): PrayTimesDAO

    companion object {
        private var instance: PrayTimesDB? = null
        fun getInstance(applicationContext: Context): PrayTimesDB {
            if (instance == null)
                synchronized(PrayTimesDB::class) {
                    instance = Room.databaseBuilder(
                        applicationContext,
                        PrayTimesDB::class.java, "sunnah_db"
                    ).allowMainThreadQueries().build()
                }
            return instance!!
        }
    }
}//end of class