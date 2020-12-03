package ir.namoo.religiousprayers.praytimes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CurrentPrayTimes")
data class CurrentPrayTimesEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "dayNumber") var dayNumber: Int,
    @ColumnInfo(name = "fajr") var fajr: String,
    @ColumnInfo(name = "sunrise") var sunrise: String,
    @ColumnInfo(name = "dhuhr") var dhuhr: String,
    @ColumnInfo(name = "asr") var asr: String,
    @ColumnInfo(name = "maghrib") var maghrib: String,
    @ColumnInfo(name = "isha") var isha: String
)


@Entity(tableName = "EditedPrayTimes")
data class EditedPrayTimesEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "dayNumber") var dayNumber: Int,
    @ColumnInfo(name = "fajr") var fajr: String,
    @ColumnInfo(name = "sunrise") var sunrise: String,
    @ColumnInfo(name = "dhuhr") var dhuhr: String,
    @ColumnInfo(name = "asr") var asr: String,
    @ColumnInfo(name = "maghrib") var maghrib: String,
    @ColumnInfo(name = "isha") var isha: String
)