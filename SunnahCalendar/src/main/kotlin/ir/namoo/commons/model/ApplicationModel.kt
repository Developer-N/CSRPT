package ir.namoo.commons.model


import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class ApplicationModel(val id: Int, val name: String) : Parcelable
