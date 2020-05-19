package ir.namoo.religiousprayers.ui.downup

import ir.namoo.religiousprayers.praytimes.EditedPrayTimesEntity
import kotlin.math.abs

class CityList {
    var id = -1
    var name = ""

    var insertDate: String = ""
        private set

    fun setInsertDate(value: String) {
        insertDate = numberOf(value)
    }
}

fun numberOf(value: String): String {
    var res = ""
    for (c in value)
        if (c.isDigit())
            res += c
    return res
}

fun isOk(times: MutableList<EditedPrayTimesEntity>?): String {
    if (times == null) return "no edit time found!"
    val prayTimes = arrayListOf<EditedPrayTimesEntity>()
    prayTimes.addAll(times)
    var res = "OK"
    for (i in prayTimes.indices) {
        var pIndex = i - 1
        if (pIndex < 0) pIndex = prayTimes.size - 1

        //check of minute different of i and pIndex is > 4 Type Error
        val morning1: String = prayTimes[i].fajr
        val morning2: String = prayTimes[pIndex].fajr
        var sp1 = morning1.split(":".toRegex()).toTypedArray()
        var sp2 = morning2.split(":".toRegex()).toTypedArray()
        if (abs(
                sp1[1].toInt() - sp2[1].toInt()
            ) > 4 && sp1[0] == sp2[0]
        ) {
            res = ("morning bad data in " + (i + 1) + " and " + (pIndex + 1))
        }
        val sunrise1: String = prayTimes[i].sunrise
        val sunrise2: String = prayTimes[pIndex].sunrise
        sp1 = sunrise1.split(":".toRegex()).toTypedArray()
        sp2 = sunrise2.split(":".toRegex()).toTypedArray()
        if (abs(
                sp1[1].toInt() - sp2[1].toInt()
            ) > 4 && sp1[0] == sp2[0]
        ) {
            res = ("sunrise bad data in " + (i + 1) + " and " + (pIndex + 1))
        }
        val noon1: String = prayTimes[i].dhuhr
        val noon2: String = prayTimes[pIndex].dhuhr
        sp1 = noon1.split(":".toRegex()).toTypedArray()
        sp2 = noon2.split(":".toRegex()).toTypedArray()
        if (abs(
                sp1[1].toInt() - sp2[1].toInt()
            ) > 4 && sp1[0] == sp2[0]
        ) {
            res = ("noon bad data in " + (i + 1) + " and " + (pIndex + 1))
        }
        val evening1: String = prayTimes[i].asr
        val evening2: String = prayTimes[pIndex].asr
        sp1 = evening1.split(":".toRegex()).toTypedArray()
        sp2 = evening2.split(":".toRegex()).toTypedArray()
        if (abs(
                sp1[1].toInt() - sp2[1].toInt()
            ) > 4 && sp1[0] == sp2[0]
        ) {
            res = ("evening shafeii bad data in " + (i + 1) + " and " + (pIndex + 1))
        }
        val sunset1: String = prayTimes[i].maghrib
        val sunset2: String = prayTimes[pIndex].maghrib
        sp1 = sunset1.split(":".toRegex()).toTypedArray()
        sp2 = sunset2.split(":".toRegex()).toTypedArray()
        if (abs(
                sp1[1].toInt() - sp2[1].toInt()
            ) > 4 && sp1[0] == sp2[0]
        ) {
            res = ("sun set bad data in " + (i + 1) + " and " + (pIndex + 1))
        }
        val isha1: String = prayTimes[i].isha
        val isha2: String = prayTimes[pIndex].isha
        sp1 = isha1.split(":".toRegex()).toTypedArray()
        sp2 = isha2.split(":".toRegex()).toTypedArray()
        if (abs(
                sp1[1].toInt() - sp2[1].toInt()
            ) > 4 && sp1[0] == sp2[0]
        ) {
            res = ("isha bad data in " + (i + 1) + " and " + (pIndex + 1))
        }
    }
    return res
}