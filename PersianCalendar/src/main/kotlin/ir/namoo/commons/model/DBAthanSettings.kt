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
    @ColumnInfo(name = "athan_key")
    var athanKey: String,
    @ColumnInfo(name = "state")
    var state: Boolean,
    @ColumnInfo(name = "play_doa")
    var playDoa: Boolean,
    // 0-FullScreen 1-InNotificationArea 2-JustNotification
    @ColumnInfo(name = "play_type")
    var playType: Int,
    @ColumnInfo(name = "is_before_enabled")
    var isBeforeEnabled: Boolean,
    @ColumnInfo(name = "before_minute")
    var beforeAlertMinute: Int,
    @ColumnInfo(name = "is_after_enabled")
    var isAfterEnabled: Boolean,
    @ColumnInfo(name = "after_minute")
    var afterAlertMinute: Int,
    @ColumnInfo(name = "is_ascending")
    var isAscending: Boolean,
    @ColumnInfo(name = "athan_volume")
    var athanVolume: Int,
    @ColumnInfo(name = "athan_uri")
    var athanURI: String,
    @ColumnInfo(name = "alert_uri")
    var alertURI: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0
}

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

@Database(entities = [AthanSetting::class], version = 6, exportSchema = false)
abstract class AthanSettingsDB : RoomDatabase() {
    abstract fun athanSettingsDAO(): AthanSettingsDAO

    companion object {
        private var instance: AthanSettingsDB? = null
        fun getInstance(applicationContext: Context): AthanSettingsDB {
            return instance ?: synchronized(AthanSettingsDB::class) {
                val ins = Room.databaseBuilder(
                    applicationContext,
                    AthanSettingsDB::class.java, "athan_settings.db"
                ).addMigrations(MIGRATION_5_6).fallbackToDestructiveMigration()
                    .allowMainThreadQueries().build()
                instance = ins
                ins
            }
        }

        private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE athans_settings ADD COLUMN is_after_enabled INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE athans_settings ADD COLUMN after_minute INTEGER NOT NULL DEFAULT 10")
            }
        }
    }
}
