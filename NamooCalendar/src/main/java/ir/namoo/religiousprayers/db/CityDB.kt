package ir.namoo.religiousprayers.db

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.edit
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ir.namoo.religiousprayers.utils.appPrefs
import ir.namoo.religiousprayers.utils.copyCityDB
import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.FileOutputStream

@Database(entities = [CityInDB::class, ProvinceInDB::class, CountryInDB::class], version = 2)
abstract class CityDB : RoomDatabase() {
    abstract fun cityDBDAO(): CityDBDAO

    companion object {
        private var instance: CityDB? = null

        @SuppressLint("SdCardPath")
        fun getInstance(applicationContext: Context): CityDB {
            val db =
                File("/data/data/" + applicationContext.packageName.toString() + "/databases/city.db")
            val dbExists: Boolean =
                db.exists() && applicationContext.appPrefs.getBoolean("pref_first_city_copy", false)
            if (!dbExists) {
              copyCityDB(applicationContext)
            }
            if (instance == null)
                synchronized(CityDB::class) {
                    instance = Room.databaseBuilder(
                        applicationContext,
                        CityDB::class.java, "city.db"
                    ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
                }
            return instance!!
        }
    }
}