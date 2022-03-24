package ir.namoo.commons.model


import android.content.Context
import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import ir.namoo.commons.utils.formatServerDate
import kotlinx.parcelize.Parcelize
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

        fun getInstance(applicationContext: Context): LocationsDB {
            return instance ?: synchronized(LocationsDB::class) {
                val ins = Room.databaseBuilder(
                    applicationContext,
                    LocationsDB::class.java, "city.db"
                ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
                instance = ins
                ins
            }
        }
    }
}

@Dao
interface CityDAO {
    @Query("select * from city")
    fun getAllCity(): List<CityModel>

    @Query("select * from city where id=:id")
    suspend fun getCity(id: Int): CityModel

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
    val is_hanafi: Int,
    val province_id: Int,
    val created_at: String,
    val updated_at: String,
) : Parcelable {
    val lastUpdate: String
        get() = formatServerDate(updated_at)
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
    val country_id: Int,
    val created_at: String,
    val updated_at: String,
) : Parcelable {
    val lastUpdate: String
        get() = formatServerDate(updated_at)
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
    val created_at: String,
    val updated_at: String,
) : Parcelable {
    val lastUpdate: String
        get() = formatServerDate(updated_at)
}

