package ir.namoo.religiousprayers.ui.azkar

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AzkarTitels::class, AzkarsEntity::class], version = 2)
abstract class AzkarDB : RoomDatabase() {
    abstract fun azkarsDAO(): AzkarDAO

    companion object {
        private var instance: AzkarDB? = null
        fun getInstance(applicationContext: Context): AzkarDB {
            if (instance == null)
                synchronized(AzkarDB::class) {
                    instance = Room.databaseBuilder(
                        applicationContext,
                        AzkarDB::class.java, "azkar.db"
                    ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
                }
            return instance!!
        }
    }
}