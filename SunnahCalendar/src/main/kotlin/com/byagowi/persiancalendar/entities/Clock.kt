package com.byagowi.persiancalendar.entities

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.collection.IntIntPair
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.amString
import com.byagowi.persiancalendar.global.clockIn24
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.pmString
import com.byagowi.persiancalendar.global.spacedAndInDates
import com.byagowi.persiancalendar.utils.formatNumber
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@JvmInline
value class Clock(val value: Double/*A real number, usually [0-24), portion of a day*/) {
    constructor(date: GregorianCalendar) : this(
        (date[GregorianCalendar.HOUR_OF_DAY].hours +
                date[GregorianCalendar.MINUTE].minutes +
                date[GregorianCalendar.SECOND].seconds +
                date[GregorianCalendar.MILLISECOND].milliseconds) / 1.hours
    )

    fun toMillis() = if (value.isNaN()) 0L else value.hours.inWholeMilliseconds

    fun toHoursAndMinutesPair(): IntIntPair {
        if (value.isNaN()) return IntIntPair(0, 0)
        val rounded = (value * 60).roundToInt()
        return IntIntPair(floor(rounded / 60.0).toInt(), floor(rounded % 60.0).toInt())
    }

    fun toBasicFormatString(): String {
        val (hours, minutes) = toHoursAndMinutesPair()
        return linearFormat(hours, minutes)
    }

    fun toFormattedString(forcedIn12: Boolean = false, printAmPm: Boolean = true): String {
        if (clockIn24 && !forcedIn12) return toBasicFormatString()
        val (hours, minutes) = toHoursAndMinutesPair()
        val clockString = linearFormat((hours % 12).takeIf { it != 0 } ?: 12, minutes)
        if (!printAmPm) return clockString
        return language.value.clockAmPmOrder.format(
            clockString,
            if (hours >= 12) pmString else amString
        )
    }

    fun asRemainingTime(resources: Resources, short: Boolean = false): String {
        val (hours, minutes) = toHoursAndMinutesPair()
        val pairs = listOf(R.plurals.hours to hours, R.plurals.minutes to minutes)
            .filter { (_, n) -> n != 0 }
        // if both present special casing the short form makes sense
        return if (pairs.size == 2 && short) resources.getString(
            R.string.n_hours_minutes, formatNumber(hours), formatNumber(minutes)
        ) else pairs.joinToString(spacedAndInDates) { (@PluralsRes pluralId: Int, n: Int) ->
            resources.getQuantityString(pluralId, n, formatNumber(n))
        }
    }

    operator fun compareTo(clock: Clock) = value compareTo clock.value
    operator fun minus(clock: Clock) = Clock(value - clock.value)
    operator fun plus(clock: Clock) = Clock(value + clock.value)

    companion object {
        private fun linearFormat(hours: Int, minutes: Int) =
            formatNumber("%02d:%02d".format(Locale.ENGLISH, hours, minutes))

        val zero = Clock(.0)
    }
}
