package ir.namoo.religiousprayers.ui.calendar

import android.content.res.Resources
import android.graphics.Typeface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getNextOwghatTimeId
import com.byagowi.persiancalendar.utils.scheduleAlarms
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.model.AthanSettingsDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.GregorianCalendar
import java.util.Locale

@Composable
fun NewTimeAdapter(
    times: PrayTimes,
    isToday: Boolean = true,
    resource: Resources,
    settings: AthanSettingsDB = koinInject(),
    iconColor: Color,
    cardColor: Color,
    remTextColor: Color,
    textFont: Typeface
) {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()
    val timeNames = listOf(
        R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr,
        R.string.maghrib, R.string.isha
    )
    val nowClock = Clock(GregorianCalendar(Locale.getDefault()))
    val next = times.getNextOwghatTimeId(nowClock)

    Column(modifier = Modifier.padding(4.dp)) {
        for (t in timeNames) {
            val position = when (t) {
                R.string.fajr -> 0
                R.string.sunrise -> 1
                R.string.dhuhr -> 2
                R.string.asr -> 3
                R.string.maghrib -> 4
                R.string.isha -> 5
                else -> 0
            }
            var rem = ""
            val tt = times.getFromStringId(t)
            if (isToday && tt.toMinutes() - nowClock.toMinutes() > 0) {
                val difference = Clock.fromMinutesCount(tt.toMinutes() - nowClock.toMinutes())
                rem = if ((t == next || (next == R.string.sunset && t == R.string.maghrib)) &&
                    difference.toMinutes() > 0
                ) difference.asRemainingTime(resource, short = true)
                else ""
            }
            val timeState = TimeState().apply {
                state = settings.athanSettingsDAO().getAllAthanSettings()[position].state
                name = stringResource(id = t)
                time = times.getFromStringId(t).toFormattedString()
                remaining = rem
            }
            TimeItemUIElement(
                modifier = Modifier,
                timeState = timeState,
                icon = when (t) {
                    R.string.fajr -> R.drawable.ic_fajr_isha
                    R.string.sunrise -> R.drawable.ic_sunrise
                    R.string.dhuhr -> R.drawable.ic_dhuhr_asr
                    R.string.asr -> R.drawable.ic_dhuhr_asr
                    R.string.maghrib -> R.drawable.ic_maghrib
                    R.string.isha -> R.drawable.ic_fajr_isha
                    else -> R.drawable.ic_fajr_isha
                },
                cardColor = cardColor,
                iconColor = iconColor,
                textFont = textFont,
                remTextColor = remTextColor,
                stateClick = {
                    coroutine.launch(Dispatchers.IO) {
                        val setting = settings.athanSettingsDAO().getAllAthanSettings()[position]
                        setting.state = !setting.state
                        settings.athanSettingsDAO().update(setting)
                        scheduleAlarms(context)
                        timeState.state = setting.state
                    }
                }
            )
        }
    }
}
