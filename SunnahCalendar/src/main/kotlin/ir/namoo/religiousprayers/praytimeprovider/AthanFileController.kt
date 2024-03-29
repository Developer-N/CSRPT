package ir.namoo.religiousprayers.praytimeprovider

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.byagowi.persiancalendar.utils.logException
import ir.namoo.commons.utils.getAthansDirectoryPath
import java.io.File

fun getAllAvailableAthans(context: Context): ArrayList<File> {
    val result = arrayListOf<File>()
    runCatching {
        val fileList = File(getAthansDirectoryPath(context)).listFiles()
        if (!fileList.isNullOrEmpty())
            result.addAll(fileList)
    }.onFailure(logException)
    return result
}

fun getAthanUriFor(context: Context, name: String): Uri? {
    val list = getAllAvailableAthans(context)
    for (f in list)
        if (f.absolutePath.toString().contains(name))
            return f.toUri()
    return null
}
