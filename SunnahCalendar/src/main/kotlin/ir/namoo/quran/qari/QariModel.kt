package ir.namoo.quran.qari

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QariModel(
    val id: Int,
    val name: String,
    @SerialName("folder_name") val folderName: String,
    @SerialName("photo_link") val photoLink: String?,
    @SerialName("verse_by_verse_base_link") val verseByVerseBaseLink: String?,
    @SerialName("sura_zips_base_link") val suraZipsBaseLink: String?,
    @SerialName("full_zip_link") val fullZipLink: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String?
) {
    fun toEntity(): QariEntity {
        return QariEntity(
            id,
            name,
            folderName,
            photoLink,
            verseByVerseBaseLink,
            suraZipsBaseLink,
            fullZipLink,
            createdAt,
            updatedAt,
            deletedAt
        )
    }
}
