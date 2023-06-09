package com.byagowi.persiancalendar.entities

import android.content.res.Resources
import androidx.annotation.PluralsRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.amString
import com.byagowi.persiancalendar.global.clockIn24
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.pmString
import com.byagowi.persiancalendar.global.spacedAndInDates
import com.byagowi.persiancalendar.utils.formatNumber
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.math.absoluteValue

data class Clock(var hours: Int, var minutes: Int) {
    constructor(date: GregorianCalendar) :
            this(date[GregorianCalendar.HOUR_OF_DAY], date[GregorianCalendar.MINUTE])

    fun toMinutes() = hours * 60 + minutes

    fun toBasicFormatString(hours: Int = this.hours): String =
        formatNumber("%02d:%02d".format(Locale.ENGLISH, hours, minutes))

    fun toTimeZoneOffsetFormat(): String {
        val sign = if (hours < 0) "-" else "+"
        return "%s%02d:%02d".format(Locale.ENGLISH, sign, hours.absoluteValue, minutes)
    }

    fun toFormattedString(forcedIn12: Boolean = false, printAmPm: Boolean = true): String {
        if (clockIn24 && !forcedIn12) return toBasicFormatString()
        val clockString = toBasicFormatString((hours % 12).takeIf { it != 0 } ?: 12)
        if (!printAmPm) return clockString
        return language.clockAmPmOrder.format(clockString, if (hours >= 12) pmString else amString)
    }

    fun asRemainingTime(resources: Resources, short: Boolean = false): String {
        val pairs = listOf(R.plurals.n_hours to hours, R.plurals.n_minutes to minutes)
            .filter { (_, n) -> n != 0 }
        // if both present special casing the short form makes sense
        return if (pairs.size == 2 && short) resources.getString(
            R.string.n_hours_minutes, formatNumber(hours), formatNumber(minutes)
        ) else pairs.joinToString(spacedAndInDates) { (@PluralsRes pluralId: Int, n: Int) ->
            resources.getQuantityString(pluralId, n, formatNumber(n))
        }
    }

    fun toHoursFraction() = toMinutes() / 60.0

    companion object {
        fun fromHoursFraction(input: Double): Clock {
            val value = (input + 0.5 / 60) % 24 // add 0.5 minutes to round
            val hours = value.toInt()
            val minutes = ((value - hours) * 60.0).toInt()
            return Clock(hours, minutes)
        }

        fun fromMinutesCount(minutes: Int) = Clock(minutes / 60, minutes % 60)
    }
}
