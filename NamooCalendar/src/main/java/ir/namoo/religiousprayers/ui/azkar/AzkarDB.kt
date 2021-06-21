package ir.namoo.religiousprayers.ui.azkar

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [AzkarTitels::class, AzkarsEntity::class], version = 3)
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
                    ).addMigrations(MIGRATE_2_3).allowMainThreadQueries().build()
                }
            return instance!!
        }
    }
}

private val MIGRATE_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("update azkars set info_fa='هر کس صبح سه بار و شب سه بار این ذکر را بخواند، هیچ چیزی به وی ضرر نمی رساند. روایت ابوداود: 5088-89، ترمذی: 3388، ابن ماجه: 3869 و احمد: 1/27.' where id=87")
    }
}

//#######################################################
@Dao
interface AzkarDAO {
    @Query("SELECT * FROM azkars")
    fun getAllAzkar(): List<AzkarsEntity>

    @Query("SELECT * FROM titles")
    fun getAzkarTitleFor(): List<AzkarTitels>

    @Query("SELECT * FROM titles where id=:id")
    fun getAzkarTitleFor(id: Int): AzkarTitels

    @Query("SELECT * FROM azkars where idTitle=:id")
    fun getAzkarsFor(id: Int): List<AzkarsEntity>

    @Update
    fun updateAzkarTitle(azkarTitleEntity: AzkarTitels)

}

//#######################################################
@Entity(tableName = "titles")
data class AzkarTitels(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "title_fa") var title_fa: String?,
    @ColumnInfo(name = "title_ku") var title_ku: String?,
    @ColumnInfo(name = "title_en") var title_en: String?,
    @ColumnInfo(name = "fav", defaultValue = 0.toString()) var fav: Int
)

@Entity(tableName = "azkars")
data class AzkarsEntity(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "idTitle") var idTitle: Int?,
    @ColumnInfo(name = "title") var title: String?,
    @ColumnInfo(name = "descryption_fa") var descryption_fa: String?,
    @ColumnInfo(name = "info_fa") var info_fa: String?,
    @ColumnInfo(name = "descryption_ku") var descryption_ku: String?,
    @ColumnInfo(name = "info_ku") var info_ku: String?,
    @ColumnInfo(name = "descryption_en") var descryption_en: String?,
    @ColumnInfo(name = "info_en") var info_en: String?,
    @ColumnInfo(name = "count") var count: Int?,
    @ColumnInfo(name = "music") var muzic: String?
)