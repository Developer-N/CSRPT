package ir.namoo.religiousprayers.praytimes

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.util.*

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

fun toJson(
    city: JSONCity?,
    prayTimes: List<JSONPrayTime?>?
): JSONObject? {
    if (city == null || prayTimes == null) return null
    val res = JSONObject()
    res["city"] = city
    val plist = JSONArray()
    plist.addAll(prayTimes)
    res["times"] = plist
    return res
}

fun getCity(strJson: String?): JSONCity? {
    return try {
        val jsonParser = JSONParser()
        val obj = jsonParser.parse(strJson)
        val jsonObject = obj as JSONObject
        val jsonCity = jsonObject["city"]
        val jCity = jsonCity as JSONObject? ?: return null
        val res = JSONCity()
        res.name = Objects.requireNonNull<Any>(jCity["name"]).toString()
        res.lat =
            Objects.requireNonNull<Any>(jCity["lat"]).toString().toDouble()
        res.lng =
            Objects.requireNonNull<Any>(jCity["lng"]).toString().toDouble()
        res
    } catch (ex: Exception) {
        println("Error: $ex")
        ex.printStackTrace()
        null
    }
}

fun getPrayTimes(strJson: String?): List<DownloadedPrayTimesEntity>? {
    return try {
        val res: MutableList<DownloadedPrayTimesEntity> =
            ArrayList()
        val jsonParser = JSONParser()
        val obj = jsonParser.parse(strJson)
        val jsonObject = obj as JSONObject
        val jsonTimes = jsonObject["times"]
        val times = jsonTimes as JSONArray? ?: return null
        for (j in times) {
            val t = j as JSONObject
            val temp = DownloadedPrayTimesEntity(
                id = 0,
                city = getCity(strJson)!!.name!!,
                dayNumber = t["dayNum"].toString().toInt(),
                fajr = t["fajr"] as String,
                sunrise = t["sunrise"] as String,
                dhuhr = t["dhuhr"] as String,
                asr = t["asr"] as String,
                maghrib = t["maghrib"] as String,
                isha = t["isha"] as String
            )

            res.add(temp)
        }
        res
    } catch (ex: java.lang.Exception) {
        println("Error: $ex")
        ex.printStackTrace()
        null
    }
}
