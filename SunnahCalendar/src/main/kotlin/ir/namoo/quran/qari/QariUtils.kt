package ir.namoo.quran.qari

import android.content.Context
import java.io.File

fun Context.getQariFolder() = this.getExternalFilesDir("QariPhotos")

fun Context.getQariLocalPhotoFile(photoLink: String?): File? {
    if (photoLink.isNullOrBlank()) return null
    val photoName = photoLink.split("/").last()
    return File("${getQariFolder()}/$photoName")
}
