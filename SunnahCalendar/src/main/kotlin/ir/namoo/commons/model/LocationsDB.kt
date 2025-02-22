package ir.namoo.commons.model


import android.content.Context
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import ir.namoo.commons.utils.formatServerDate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Database(
    entities = [CityModel::class, ProvinceModel::class, CountryModel::class],
    version = 5,
    exportSchema = false
)
abstract class LocationsDB : RoomDatabase() {
    abstract fun cityDAO(): CityDAO
    abstract fun countryDAO(): CountryDAO
    abstract fun provinceDAO(): ProvinceDAO

    companion object {
        private var instance: LocationsDB? = null

        fun getInstance(context: Context): LocationsDB {
            return instance ?: synchronized(LocationsDB::class) {
                val ins = Room.databaseBuilder(
                    context.applicationContext,
                    LocationsDB::class.java, "city.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).allowMainThreadQueries()
                    .build()
                instance = ins
                ins
            }
        }
    }
}

@Dao
interface CityDAO {
    @Query("select * from city")
    suspend fun getAllCity(): List<CityModel>

    @Query("select * from city where id=:id")
    suspend fun getCity(id: Int): CityModel

    @Query("select * from city where province_id=:id")
    suspend fun getCityForProvince(id: Int): List<CityModel>

    @Query("select * from city where name=:name")
    suspend fun getCity(name: String): CityModel?

    @Insert(onConflict = REPLACE)
    suspend fun insert(cities: List<CityModel>)
}

@Dao
interface ProvinceDAO {
    @Query("select * from province")
    suspend fun getAllProvinces(): List<ProvinceModel>

    @Insert(onConflict = REPLACE)
    suspend fun insert(cities: List<ProvinceModel>)
}

@Dao
interface CountryDAO {
    @Query("select * from country")
    suspend fun getAllCountries(): List<CountryModel>

    @Insert(onConflict = REPLACE)
    suspend fun insert(cities: List<CountryModel>)
}

@Serializable
@Parcelize
@Entity(tableName = "city")
data class CityModel(
    @PrimaryKey
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("is_hanafi")
    @ColumnInfo(name = "is_hanafi")
    val isHanafi: Int,
    @SerialName("province_id")
    @ColumnInfo(name = "province_id")
    val provinceId: Int,
    @SerialName("created_at")
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @SerialName("updated_at")
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
) : Parcelable {
    val lastUpdate: String
        get() = formatServerDate(updatedAt)
}

@Serializable
@Parcelize
@Entity(tableName = "province")
data class ProvinceModel(
    @PrimaryKey
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("country_id")
    @ColumnInfo(name = "country_id")
    val countryId: Int,
    @SerialName("created_at")
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @SerialName("updated_at")
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
) : Parcelable {
    val lastUpdate: String
        get() = formatServerDate(updatedAt)
}

@Serializable
@Parcelize
@Entity(tableName = "country")
data class CountryModel(
    @PrimaryKey
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("created_at")
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @SerialName("updated_at")
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
) : Parcelable {
    val lastUpdate: String
        get() = formatServerDate(updatedAt)
}

