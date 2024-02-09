package ir.namoo.religiousprayers.ui.calendar

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getNextOwghatTimeId
import com.byagowi.persiancalendar.utils.scheduleAlarms
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.religiousprayers.praytimeprovider.prayTimesFrom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.util.GregorianCalendar
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NTimes(
    isToday: Boolean = true,
    prayTimes: PrayTimes,
    navigateToAthanSetting: (Int) -> Unit,
    navigateToDownload: () -> Unit,
    settings: AthanSettingsDB = koinInject(),
    prayTimeRepository: PrayTimeRepository = koinInject()
) {
    val timeNames = listOf(
        R.string.fajr,
        R.string.sunrise,
        R.string.dhuhr,
        R.string.asr,
        R.string.maghrib,
        R.string.isha
    )
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()
    val nowClock = Clock(GregorianCalendar(Locale.getDefault()))
    val next = prayTimes.getNextOwghatTimeId(nowClock)
    var isTimeAvailable by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = "AvailableTime") {
        withContext(Dispatchers.IO) {
            isTimeAvailable = isTimeAvailableForDownload(context, prayTimeRepository)
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(visible = isTimeAvailable) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 10.dp),
                onClick = { navigateToDownload() },
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    text = stringResource(id = R.string.available_exact_times),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        timeNames.forEach { timeId ->
            val position = when (timeId) {
                R.string.fajr -> 0
                R.string.sunrise -> 1
                R.string.dhuhr -> 2
                R.string.asr -> 3
                R.string.maghrib -> 4
                R.string.isha -> 5
                else -> 0
            }
            var rem = ""
            val tt = prayTimes.getFromStringId(timeId)
            if (isToday && tt.toMinutes() - nowClock.toMinutes() > 0) {
                val difference = Clock.fromMinutesCount(tt.toMinutes() - nowClock.toMinutes())
                rem =
                    if ((timeId == next || (next == R.string.sunset && timeId == R.string.maghrib)) && difference.toMinutes() > 0) difference.asRemainingTime(
                        context.resources, short = true
                    )
                    else ""
            }
            val isNext = rem.isNotEmpty()
            var athanState by remember {
                mutableStateOf(settings.athanSettingsDAO().getAllAthanSettings()[position].state)
            }
            val cardScale by animateFloatAsState(
                targetValue = if (isNext) 1f else 0.96f, label = "scale", animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            val stateScale = remember { Animatable(if (isNext) 1.2f else 1.1f) }

            OutlinedCard(
                modifier = Modifier
                    .padding(2.dp)
                    .scale(cardScale),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .weight(1f)
                            .scale(if (isNext) 1.2f else 1.1f),
                        painter = painterResource(
                            id = when (timeId) {
                                R.string.fajr -> R.drawable.ic_fajr_isha
                                R.string.sunrise -> R.drawable.ic_sunrise
                                R.string.dhuhr -> R.drawable.ic_dhuhr_asr
                                R.string.asr -> R.drawable.ic_dhuhr_asr
                                R.string.maghrib -> R.drawable.ic_maghrib
                                R.string.isha -> R.drawable.ic_fajr_isha
                                else -> R.drawable.ic_fajr_isha
                            }
                        ),
                        contentDescription = stringResource(id = timeId),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        modifier = Modifier.weight(3f),
                        text = stringResource(id = timeId),
                        fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Text(
                        modifier = Modifier
                            .weight(2f)
                            .alpha(AppBlendAlpha),
                        text = prayTimes.getFromStringId(timeId).toFormattedString(),
                        fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Text(
                        modifier = Modifier.weight(2f),
                        text = rem,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Normal
                    )

                    Icon(
                        modifier = Modifier
                            .weight(1f)
                            .scale(stateScale.value)
                            .clip(MaterialTheme.shapes.large)
                            .combinedClickable(onClick = {
                                coroutine.launch(Dispatchers.IO) {
                                    stateScale.animateTo(0.1f, tween(50))
                                    athanState = !athanState
                                    stateScale.animateTo(if (isNext) 1.2f else 1.1f, tween(100))
                                    withContext(Dispatchers.IO) {
                                        val setting = settings
                                            .athanSettingsDAO()
                                            .getAllAthanSettings()[position]
                                        setting.state = !setting.state
                                        settings
                                            .athanSettingsDAO()
                                            .update(setting)
                                        scheduleAlarms(context)
                                    }
                                }
                            }, onLongClick = { navigateToAthanSetting(position + 1) }),
                        imageVector = if (athanState) Icons.AutoMirrored.Default.VolumeUp else Icons.AutoMirrored.Default.VolumeOff,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private suspend fun isTimeAvailableForDownload(
    context: Context, prayTimeRepository: PrayTimeRepository
): Boolean {
    return withContext(Dispatchers.IO) {
        if (!isNetworkConnected(context) || prayTimesFrom.value != 0) return@withContext false
        val list = prayTimeRepository.getLocalCityList()
        val currentCity = cityName.value
        if (currentCity.isNullOrEmpty()) return@withContext false
        return@withContext list.find { cityModel -> cityModel.name == currentCity } != null
    }
}
