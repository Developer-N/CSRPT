package ir.namoo.commons.model

import android.os.Parcelable
import com.byagowi.persiancalendar.global.language
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
    val serverFormatUpdatedAt: String,
) : Parcelable {
    val updatedAt: Date
        get() {
            var date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", language.value.asSystemLocale())
                .parse(serverFormatUpdatedAt)
            if (date == null)
                date = Date()
            return date
        }

}

