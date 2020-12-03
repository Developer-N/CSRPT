package ir.namoo.quran.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [QuranEntity::class, HizbEntity::class, JuzEntity::class, PageEntity::class, ChapterEntity::class],
    version = 2
)
abstract class QuranDB : RoomDatabase() {

    abstract fun chaptersDao(): ChaptersDao
    abstract fun quranDao(): QuranDao
    abstract fun pjhDao(): PJHDao

    //################### Singleton pattern for instance of database
    companion object {
        private var instance: QuranDB? = null
        internal fun getInstance(context: Context): QuranDB {
            if (instance == null) {
                synchronized(QuranDB::class.java) {
                    if (instance == null) {
                        instance = Room.databaseBuilder<QuranDB>(
                            context.applicationContext,
                            QuranDB::class.java,
                            "quran.db"
                        ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
                    }
                }
            }
            return instance!!
        }
    }

}//end of class QuranDB