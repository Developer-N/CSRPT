package ir.namoo.religiousprayers.ui.azkar

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update

@Database(entities = [AzkarTitles::class, AzkarsEntity::class], version = 4, exportSchema = false)
abstract class AzkarDB : RoomDatabase() {
    abstract fun azkarsDAO(): AzkarDAO
}

//#######################################################
@Dao
interface AzkarDAO {
    @Query("SELECT * FROM azkars")
    fun getAllAzkar(): List<AzkarsEntity>?

    @Query("SELECT * FROM titles")
    suspend fun getAllAzkarsTitle(): List<AzkarTitles>

    @Query("SELECT * FROM titles where id=:id")
    suspend fun getAzkarTitleFor(id: Int): AzkarTitles

    @Query("SELECT * FROM azkars where idTitle=:id")
    suspend fun getAzkarsFor(id: Int): List<AzkarsEntity>

    @Update
    suspend fun updateAzkarTitle(azkarTitleEntity: AzkarTitles)

}

//#######################################################
@Entity(tableName = "titles")
data class AzkarTitles(
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
