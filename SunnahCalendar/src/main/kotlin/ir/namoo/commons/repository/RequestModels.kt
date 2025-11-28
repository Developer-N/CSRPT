package ir.namoo.commons.repository

import kotlinx.serialization.Serializable

@Serializable
data class EventRequest(val event: String)
