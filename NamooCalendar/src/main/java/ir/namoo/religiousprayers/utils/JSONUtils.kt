package ir.namoo.religiousprayers.utils

import android.util.Log
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

class JSONCity {
    var name: String? = null
    var lat = 0.0
    var lng = 0.0

    override fun toString(): String {
        return "{\"name\":" + "\"" + name + "\"," +
                "\"lat\":" + lat + "," +
                "\"lng\":" + lng + "}"
    }
}

class JSONPrayTime {
    var dayNum = 0
    var fajr: String? = null
    var sunrise: String? = null
    var dhuhr: String? = null
    var asr: String? = null
    var maghrib: String? = null
    var isha: String? = null

    override fun toString(): String {
        return "{\"dayNum\":" + dayNum +
                ",\"fajr\":\"" + fajr + "\"," +
                "\"sunrise\":\"" + sunrise + "\"," +
                "\"dhuhr\":\"" + dhuhr + "\"," +
                "\"asr\":\"" + asr + "\"," +
                "\"maghrib\":\"" + maghrib + "\"," +
                "\"isha\":\"" + isha + "\"" +
                "}"
    }
}

fun toJson(JSONCity: JSONCity?, JSONPrayTimes: List<JSONPrayTime?>?): JSONObject? {
    if (JSONCity == null || JSONPrayTimes == null) return null
    val res = JSONObject()
    res["city"] = JSONCity
    val plist = JSONArray()
    plist.addAll(JSONPrayTimes)
    res["times"] = plist
    return res
}

fun getCity(strJson: String?): JSONCity? {
    return try {
        val jsonParser = JSONParser()
        val obj: Any = jsonParser.parse(strJson)
        val jsonObject = obj as JSONObject
        val jsonCity = jsonObject["city"]!!
        val jCity = jsonCity as JSONObject
        val res = JSONCity()
        res.name = jCity["name"].toString()
        res.lat = jCity["lat"].toString().toDouble()
        res.lng = jCity["lng"].toString().toDouble()
        res
    } catch (ex: Exception) {
        Log.e(TAG, "getCity Error: $ex")
        null
    }
}

fun getPrayTimes(strJson: String?): List<JSONPrayTime>? {
    return try {
        val res: MutableList<JSONPrayTime> = ArrayList()
        val jsonParser = JSONParser()
        val obj = jsonParser.parse(strJson)
        val jsonObject = obj as JSONObject
        val jsonTimes = jsonObject["times"]!!
        val times = jsonTimes as JSONArray
        for (j in times) {
            val t = j as JSONObject
            val temp = JSONPrayTime()
            temp.dayNum = t["dayNum"].toString().toInt()
            temp.fajr = t["fajr"] as String?
            temp.sunrise = t["sunrise"] as String?
            temp.dhuhr = t["dhuhr"] as String?
            temp.asr = t["asr"] as String?
            temp.maghrib = t["maghrib"] as String?
            temp.isha = t["isha"] as String?
            res.add(temp)
        }
        res
    } catch (ex: java.lang.Exception) {
        Log.e(TAG, "Error: $ex")
        null
    }
}


