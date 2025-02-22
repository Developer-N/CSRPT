package ir.namoo.hadeeth.repository

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update

@Dao
interface LanguageDao {
    @Query("SELECT * FROM languages")
    suspend fun getAllLanguages(): List<LanguageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguages(languages: List<LanguageEntity>)

    @Query("DELETE FROM languages")
    suspend fun deleteAllLanguages()

}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE language = :language")
    suspend fun getAllCategories(language:String): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE language = :language")
    suspend fun deleteAllCategories( language: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettings(): SettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingEntity)

    @Update
    suspend fun updateSettings(settings: SettingEntity)
}

@Database(
    entities = [LanguageEntity::class, CategoryEntity::class, SettingEntity::class],
    version = 3,
    exportSchema = false
)
abstract class HadeethDB : RoomDatabase() {
    abstract fun languageDao(): LanguageDao
    abstract fun categoryDao(): CategoryDao
    abstract fun settingDao(): SettingDao

    companion object {
        private var instance: HadeethDB? = null
        fun getInstance(context: Context): HadeethDB {
            return instance ?: synchronized(HadeethDB::class.java) {
                val ins = Room.databaseBuilder(
                    context.applicationContext, HadeethDB::class.java, "hadeeth.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                instance = ins
                ins
            }
        }
    }
}
