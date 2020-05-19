package ir.namoo.religiousprayers.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city")
data class CityInDB(
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id:Int,
    @ColumnInfo(name = "province_id")
    var provinceID:Int?,
    @ColumnInfo(name = "name")
    var name:String,
    @ColumnInfo(name = "latitude")
    var latitude:Double?,
    @ColumnInfo(name = "longitude")
    var longitude:Double?
)

@Entity(tableName = "province")
data class ProvinceInDB(
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id:Int,
    @ColumnInfo(name = "country_id")
    var countryID:Int,
    @ColumnInfo(name = "name")
    var name:String,
    @ColumnInfo(name = "latitude")
    var latitude:Double,
    @ColumnInfo(name = "longitude")
    var longitude:Double
)

@Entity(tableName = "country")
data class CountryInDB(
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id:Int,
    @ColumnInfo(name = "name")
    var name:String,
    @ColumnInfo(name = "latitude")
    var latitude:Double,
    @ColumnInfo(name = "longitude")
    var longitude:Double
)