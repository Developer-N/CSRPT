package ir.namoo.commons.model

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Entity(tableName = "athans_settings")
data class AthanSetting(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "athan_key") var athanKey: String,
    @ColumnInfo(name = "state") var state: Boolean,
    @ColumnInfo(name = "play_doa") var playDoa: Boolean,
    // 0-FullScreen 1-InNotificationArea 2-JustNotification
    @ColumnInfo(name = "play_type") var playType: Int,
    @ColumnInfo(name = "is_before_enabled") var isBeforeEnabled: Boolean,
    @ColumnInfo(name = "before_minute") var beforeAlertMinute: Int,
    @ColumnInfo(name = "is_after_enabled") var isAfterEnabled: Boolean,
    @ColumnInfo(name = "after_minute") var afterAlertMinute: Int,
    @ColumnInfo(name = "is_silent_enabled") var isSilentEnabled: Boolean,
    @ColumnInfo(name = "silent_minute") var silentMinute: Int,
    @ColumnInfo(name = "is_ascending") var isAscending: Boolean,
    @ColumnInfo(name = "athan_volume") var athanVolume: Int,
    @ColumnInfo(name = "athan_uri") var athanURI: String,
    @ColumnInfo(name = "alert_uri") var alertURI: String,
    @ColumnInfo(name = "background_uri") var backgroundUri: String?
)

@Dao
interface AthanSettingsDAO {
    @Query("select * from athans_settings")
    fun getAllAthanSettings(): List<AthanSetting>

    @Query("select * from athans_settings where id=:id")
    fun getSetting(id: Int): AthanSetting

    @Update
    fun update(athanSetting: AthanSetting)

    @Insert(onConflict = REPLACE)
    fun insert(vararg athanSetting: AthanSetting)

    @Delete
    fun delete(athanSetting: AthanSetting)
}

@Database(entities = [AthanSetting::class], version = 8, exportSchema = false)
abstract class AthanSettingsDB : RoomDatabase() {
    abstract fun athanSettingsDAO(): AthanSettingsDAO

    companion object {
        private var instance: AthanSettingsDB? = null
        fun getInstance(applicationContext: Context): AthanSettingsDB {
            return instance ?: synchronized(AthanSettingsDB::class) {
                val ins = Room.databaseBuilder(
                    applicationContext, AthanSettingsDB::class.java, "athan_settings.db"
                ).addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration(dropAllTables = true).allowMainThreadQueries()
                    .build()
                instance = ins
                ins
            }
        }

        private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE athans_settings ADD COLUMN is_after_enabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE athans_settings ADD COLUMN after_minute INTEGER NOT NULL DEFAULT 10")
            }
        }
        private val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE athans_settings ADD COLUMN is_silent_enabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE athans_settings ADD COLUMN silent_minute INTEGER NOT NULL DEFAULT 20")
            }
        }
        private val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE athans_settings ADD COLUMN background_uri TEXT")
            }
        }
    }
}
