package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.utils.calendar
import com.byagowi.persiancalendar.utils.formatNumber

@Composable
fun DatePicker(calendar: Calendar, jdn: Jdn, setJdn: (Jdn) -> Unit) {
    Crossfade(targetState = calendar, label = "day picker") { state ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            val date = remember(jdn.value, state) { jdn on state }
            val daysFormat = remember(state, date.year, date.month) {
                val monthStart = Jdn(state, date.year, date.month, 1);
                { item: Int -> (monthStart + item - 1).weekDayName + " / " + formatNumber(item) }
            }
            val monthsLength = remember(state, date.year, date.month) {
                state.getMonthLength(date.year, date.month)
            }
            val yearMonths = remember(state, date.year) {
                state.getYearMonths(date.year)
            }
            val monthsFormat = remember(state, date.year) {
                val months = date.calendar.monthsNames
                { item: Int -> months[item - 1] + " / " + formatNumber(item) }
            }
            val todayYear = remember(state) { Jdn.today().on(state).year }
            val startYear = remember(state) { todayYear - 200 }
            var monthChangeToken by remember { mutableIntStateOf(0) }
            var previousMonth by remember { mutableIntStateOf(0) }
            if (previousMonth != date.month) ++monthChangeToken
            previousMonth = date.month
            Row(modifier = Modifier.fillMaxWidth()) {
                val view = LocalView.current
                NumberPicker(
                    modifier = Modifier.weight(1f),
                    label = daysFormat,
                    range = 1..monthsLength,
                    value = date.dayOfMonth,
                    onClickLabel = stringResource(R.string.day),
                ) {
                    setJdn(Jdn(state, date.year, date.month, it))
                    view.performHapticFeedbackVirtualKey()
                }
                Spacer(modifier = Modifier.width(8.dp))
                NumberPicker(
                    modifier = Modifier.weight(1f),
                    label = monthsFormat,
                    range = 1..yearMonths,
                    value = date.month,
                    onClickLabel = stringResource(R.string.month),
                ) { month ->
                    val day =
                        date.dayOfMonth.coerceIn(1, state.getMonthLength(date.year, month))
                    setJdn(Jdn(state, date.year, month, day))
                    view.performHapticFeedbackVirtualKey()
                }
                Spacer(modifier = Modifier.width(8.dp))
                NumberPicker(
                    modifier = Modifier.weight(1f),
                    range = startYear..startYear + 400,
                    value = date.year,
                    onClickLabel = stringResource(R.string.year),
                ) { year ->
                    val month = date.month.coerceIn(1, state.getYearMonths(year))
                    val day = date.dayOfMonth.coerceIn(1, state.getMonthLength(year, month))
                    setJdn(Jdn(state, year, month, day))
                    view.performHapticFeedbackVirtualKey()
                }
            }
        }
    }
}
