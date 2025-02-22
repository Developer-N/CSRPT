package ir.namoo.religiousprayers.ui.calendar

import com.byagowi.persiancalendar.entities.PrayTime

data class TimesState(
    val position: Int,
    val icon: Int,
    val time: PrayTime,
    val remainingTime: String,
    val isActive: Boolean,
    val isNext: Boolean,
)
