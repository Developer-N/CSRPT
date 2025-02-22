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
    @SerialName("asr_hanafi")
    val asrHanafi: String,
    val maghrib: String,
    val isha: String,
    @SerialName("city_id")
    val cityID: Int,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
) : Parcelable


@Serializable
data class PrayTimesResponse(
    val status: Int,
    val data: List<PrayTimesModel>,
    val msg: String
)
