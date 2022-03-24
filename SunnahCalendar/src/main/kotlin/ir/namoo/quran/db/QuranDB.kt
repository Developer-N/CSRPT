package ir.namoo.quran.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [QuranEntity::class, HizbEntity::class, JuzEntity::class, PageEntity::class, ChapterEntity::class],
    version = 2, exportSchema = false
)
abstract class QuranDB : RoomDatabase() {

    abstract fun chaptersDao(): ChaptersDao
    abstract fun quranDao(): QuranDao
    abstract fun pjhDao(): PJHDao

}//end of class QuranDB
