package ir.namoo.commons.model

import kotlinx.serialization.Serializable

@Serializable
data class ServerResponseModel<T>(
    val status: Int,
    val data: T,
    val msg: String
)
