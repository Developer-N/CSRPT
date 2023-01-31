package ir.namoo.quran.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [QuranEntity::class, HizbEntity::class, JuzEntity::class, PageEntity::class, ChapterEntity::class],
    version = 2,
    exportSchema = false
)
abstract class QuranDB : RoomDatabase() {

    abstract fun chaptersDao(): ChaptersDao
    abstract fun quranDao(): QuranDao
    abstract fun pjhDao(): PJHDao

    companion object {
        private var instance: QuranDB? = null

        fun getInstance(context: Context): QuranDB {
            return instance ?: synchronized(QuranDB::class) {
                val db = Room.databaseBuilder(
                    context.applicationContext, QuranDB::class.java, "quran.db"
                ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
                instance = db
                db
            }
        }
    }

}//end of class QuranDB
