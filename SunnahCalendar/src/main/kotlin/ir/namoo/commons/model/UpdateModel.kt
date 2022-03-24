package ir.namoo.commons.model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
@Parcelize
class UpdateModel(
    val id: Int,
    val changes: String,
    @SerialName("version_name")
    val versionName: String,
    @SerialName("version_code")
    val versionCode: Int,
    @SerialName("release_date")
    val releaseDate: String,
    @SerialName("file_link")
    val fileLink: String,
    @SerialName("file_title")
    val fileTitle: String,
    @SerialName("updated_at")
    val _updatedAt: String,
) : Parcelable {
    val updatedAt: Date
        @SuppressLint("SimpleDateFormat")
        get() {
            var date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(_updatedAt)
            if (date == null)
                date = Date()
            return date
        }

}

