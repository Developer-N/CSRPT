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


fun getTimesForCurrentCity(context: Context): MutableList<JSONUtils.PrayTime>? {
    return getTimesForCity(
        context,
        context.appPrefs.getString(PREF_GEOCODED_CITYNAME, DEFAULT_CITY) ?: DEFAULT_CITY
    )
}

fun getTimesForCity(context: Context, city: String): MutableList<JSONUtils.PrayTime>? {
    try {
        val fileList = File(getTimesDirectoryPath(context)).listFiles()
        for (file in fileList!!) {
            val inFile = File("${getTimesDirectoryPath(context)}/${file.name}")
            val input = InputStreamReader(FileInputStream(inFile))
            val strJson = input.readText()
            val c = JSONUtils.getCity(strJson)
            if (c?.name.equals(city)) {
                return JSONUtils.getPrayTimes(strJson)
            }
        }
        return null
    } catch (ex: Exception) {
        return null
    }
}

fun getAllAvailableCityName(context: Context): ArrayList<String> {
    val res = arrayListOf<String>()
    try {
        val fileList = File(getTimesDirectoryPath(context)).listFiles()
        if (!fileList.isNullOrEmpty())
            for (file in fileList) {
                val inFile = File("${getTimesDirectoryPath(context)}/${file.name}")
                val input = InputStreamReader(FileInputStream(inFile))
                val strJson = input.readText()
                val city = JSONUtils.getCity(strJson)
                res.add(city?.name ?: "ERR")
            }
    } catch (ex: Exception) {

    }
    return res
}

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