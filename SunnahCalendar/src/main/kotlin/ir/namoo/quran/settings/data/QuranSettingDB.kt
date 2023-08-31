package ir.namoo.quran.settings.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [TranslateSetting::class],
    exportSchema = false,
    version = 3
)
abstract class QuranSettingDB : RoomDatabase() {

    abstract fun quranSettingDAO(): QuranSettingsDAO

    companion object {
        private var instance: QuranSettingDB? = null
        fun getInstance(context: Context): QuranSettingDB {
            return instance ?: synchronized(QuranSettingDB::class) {
                val db = Room.databaseBuilder(
                    context.applicationContext, QuranSettingDB::class.java, "quran_setting.db"
                ).fallbackToDestructiveMigration().build()
                instance = db
                db
            }
        }
    }

}
