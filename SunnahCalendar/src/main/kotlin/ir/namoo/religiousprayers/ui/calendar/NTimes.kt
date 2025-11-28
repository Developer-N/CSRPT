package ir.namoo.religiousprayers.ui.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.entities.PrayTime.Companion.get
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import io.github.persiancalendar.praytimes.PrayTimes
import org.koin.androidx.compose.koinViewModel

@Composable
fun NTimes(
    isToday: Boolean = true,
    prayTimes: PrayTimes,
    navigateToAthanSetting: (Int) -> Unit,
    navigateToDownload: () -> Unit,
    viewModel: NTimesViewModel = koinViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = prayTimes) {
        viewModel.load(context, prayTimes, isToday)
    }
    val isTimeAvailable by viewModel.isTimesAvailableForDownload.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val times = viewModel.times

    Column(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(visible = isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), strokeCap = StrokeCap.Round
            )
        }

        AnimatedVisibility(visible = isTimeAvailable) {
            ElevatedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 10.dp),
                onClick = { navigateToDownload() },
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    text = stringResource(id = R.string.available_exact_times),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                )
            }
        }
        times.forEach { time ->
            TimeItem(
                modifier = Modifier,
                time = time,
                prayTimes = prayTimes,
                changeTimeState = { viewModel.changeTimeState(context, time) },
                navigationToAthanSetting = { navigateToAthanSetting(time.position + 1) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimeItem(
    modifier: Modifier = Modifier,
    time: TimesState,
    prayTimes: PrayTimes?,
    changeTimeState: () -> Unit,
    navigationToAthanSetting: () -> Unit
) {
    val cardScale by animateFloatAsState(
        targetValue = if (time.isNext) 1f else 0.94f,
        label = "scale",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val scale by animateFloatAsState(
        targetValue = if (time.isNext) 1.2f else 1.1f,
        label = "scale",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Surface(
        modifier = modifier
            .padding(vertical = 1.dp, horizontal = 4.dp)
            .scale(cardScale),
        border = BorderStroke(
            if (time.isNext) (1.5).dp else 1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = if (time.isNext) 0.5f else 0.3f)
        ),
        shape = MaterialTheme.shapes.extraLarge,
        shadowElevation = if (time.isNext) 1.dp else 0.5.dp
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
                    .scale(scale),
                painter = painterResource(
                    id = time.icon
                ),
                contentDescription = stringResource(id = time.time.stringRes),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                modifier = Modifier.weight(3f),
                text = stringResource(id = time.time.stringRes),
                fontWeight = if (time.isNext) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = MaterialTheme.typography.titleMedium.fontSize
            )
            AnimatedContent(
                modifier = Modifier.weight(2f),
                targetState = prayTimes?.get(time.time)
                    ?.toFormattedString() ?: "--:--",
                label = "time"
            ) {
                Text(
                    modifier = Modifier.alpha(AppBlendAlpha),
                    text = it,
                    fontWeight = if (time.isNext) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                )
            }
            AnimatedContent(
                modifier = Modifier.weight(2f),
                targetState = time.remainingTime,
                label = "rem"
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = if (time.isNext) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                )
            }

            Icon(
                modifier = Modifier
                    .weight(1f)
                    .scale(scale)
                    .clip(MaterialTheme.shapes.large)
                    .combinedClickable(
                        onClick = changeTimeState,
                        onLongClick = navigationToAthanSetting
                    ),
                imageVector = if (time.isActive) Icons.AutoMirrored.Default.VolumeUp else Icons.AutoMirrored.Default.VolumeOff,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showSystemUi = true, locale = "fa")
@Composable
private fun PreviewTimeItem() {
    MaterialTheme {
        Column(modifier = Modifier.padding(top = 56.dp)) {
            TimeItem(
                time = TimesState(
                    position = 0,
                    icon = R.drawable.ic_fajr_isha,
                    time = PrayTime.FAJR,
                    remainingTime = "",
                    isActive = false,
                    isNext = false
                ),
                prayTimes = null,
                changeTimeState = {},
                navigationToAthanSetting = {}
            )
            TimeItem(
                time = TimesState(
                    position = 1,
                    icon = R.drawable.ic_dhuhr_asr,
                    time = PrayTime.DHUHR,
                    remainingTime = "",
                    isActive = true,
                    isNext = true
                ),
                prayTimes = null,
                changeTimeState = {},
                navigationToAthanSetting = {}
            )
        }
    }
}
