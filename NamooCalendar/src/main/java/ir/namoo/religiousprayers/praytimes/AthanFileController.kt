package ir.namoo.religiousprayers.praytimes

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import ir.namoo.religiousprayers.DEFAULT_CITY
import ir.namoo.religiousprayers.PREF_GEOCODED_CITYNAME
import ir.namoo.religiousprayers.utils.appPrefs
import ir.namoo.religiousprayers.utils.getAthansDirectoryPath
import ir.namoo.religiousprayers.utils.getTimesDirectoryPath
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

fun getAllAvailableAthans(context: Context): ArrayList<File> {
    val result = arrayListOf<File>()
    try {
        val fileList = File(getAthansDirectoryPath(context)).listFiles()
        if (!fileList.isNullOrEmpty())
            result.addAll(fileList)
    } catch (ex: Exception) {
    }
    return result
}

fun getAthanUriFor(context: Context, name: String): Uri? {
    val list = getAllAvailableAthans(context)
    for (f in list)
        if (f.absolutePath.toString().contains(name))
            return f.toUri()
    return null
}