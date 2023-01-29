package ir.namoo.commons.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerAthanModel(
    val id: Int,
    val name: String,
    @SerialName("file_link")
    val fileLink: String,
    @SerialName("file_title")
    val fileTitle: String
)
