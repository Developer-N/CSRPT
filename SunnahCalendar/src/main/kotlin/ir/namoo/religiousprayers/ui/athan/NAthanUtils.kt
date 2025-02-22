package ir.namoo.religiousprayers.ui.athan

import android.content.Context
import android.util.Log
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.PrayTime

fun Context.getStringForKey(key: String): String {
    runCatching {
        val time = PrayTime.fromName(key)
        return if (time != null) getString(time.stringRes)
        else when (key) {
            "BFAJR" -> getString(R.string.bfajr)
            "AFAJR" -> getString(R.string.afajr)
            "BDHUHR" -> getString(R.string.bdhuhr)
            "ADHUHR" -> getString(R.string.adhuhr)
            "BASR" -> getString(R.string.basr)
            "AASR" -> getString(R.string.aasr)
            "BMAGHRIB" -> getString(R.string.bmaghrib)
            "AMAGHRIB" -> getString(R.string.amaghrib)
            "BISHA" -> getString(R.string.isha)
            "AISHA" -> getString(R.string.aisha)
            else -> ""
        }
    }.onFailure {
        Log.e("getStringForKey", "getStringForKey: ${it.message}", it)
        return ""
    }.getOrElse { return "" }
}
