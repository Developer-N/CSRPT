package ir.namoo.quran.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ir.namoo.quran.chapters.data.ChapterEntity
import ir.namoo.quran.chapters.data.ChaptersDao
import ir.namoo.quran.chapters.data.HizbEntity
import ir.namoo.quran.chapters.data.JuzEntity
import ir.namoo.quran.chapters.data.PJHDao
import ir.namoo.quran.chapters.data.PageEntity
import ir.namoo.quran.sura.data.AyaEntity
import ir.namoo.quran.sura.data.QuranDAO
import ir.namoo.quran.sura.data.QuranEntity
import ir.namoo.quran.sura.data.QuranWordEntity
import ir.namoo.quran.sura.data.TafsirEntity

@Database(
    entities = [QuranEntity::class, QuranWordEntity::class, TafsirEntity::class, ChapterEntity::class, HizbEntity::class, JuzEntity::class, PageEntity::class, AyaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class QuranDB : RoomDatabase() {
    abstract fun quranDAO(): QuranDAO
    abstract fun chapterDAO(): ChaptersDao
    abstract fun pjhDAO(): PJHDao

    companion object {
        private var instance: QuranDB? = null
        fun getInstance(context: Context): QuranDB {
            return instance ?: synchronized(QuranDB::class) {
                val db = Room.databaseBuilder(
                    context.applicationContext, QuranDB::class.java, "quran_v3.db"
                ).build()
                instance = db
                db
            }
        }
    }
}
