package ir.namoo.religiousprayers.ui.azkar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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