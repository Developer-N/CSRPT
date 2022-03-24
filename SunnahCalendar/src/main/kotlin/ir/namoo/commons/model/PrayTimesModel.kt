package ir.namoo.commons.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@Parcelize
data class PrayTimesModel(
    val id: Int,
    val day: Int,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    @SerialName("city_id")
    val cityID: Int,
    val created_at: String,
    val updated_at: String
) : Parcelable
