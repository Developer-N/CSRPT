package ir.namoo.religiousprayers.ui.downup

import org.json.JSONArray
import org.json.JSONObject

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

fun toJson(jsonCity: JSONCity?, jsonPrayTime: List<JSONPrayTime>?): JSONObject? {
    if (jsonCity == null || jsonPrayTime.isNullOrEmpty()) return null
    val result = JSONObject()
    result.put("city", jsonCity)
    val plist = JSONArray()
    for (p in jsonPrayTime)
        plist.put(p)
    result.put("times", plist)
    return result
}
