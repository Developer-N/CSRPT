package ir.namoo.religiousprayers.db

import android.content.Context
import androidx.room.*

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
    fun getAllAthanSettings(): List<AthanSetting>?

    @Update
    fun update(athanSetting: AthanSetting)

    @Insert
    fun insert(vararg athanSetting: AthanSetting)

    @Delete
    fun delete(athanSetting: AthanSetting)
}

@Database(entities = [AthanSetting::class], version = 5)
abstract class AthanSettingsDB : RoomDatabase() {
    abstract fun athanSettingsDAO(): AthanSettingsDAO

    companion object {
        private var instance: AthanSettingsDB? = null
        fun getInstance(applicationContext: Context): AthanSettingsDB {
            if (instance == null) {
                synchronized(AthanSettingsDB::class) {
                    instance = Room.databaseBuilder(
                        applicationContext,
                        AthanSettingsDB::class.java, "athan_settings.db"
                    ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
                }
            }
            return instance!!
        }
    }
}