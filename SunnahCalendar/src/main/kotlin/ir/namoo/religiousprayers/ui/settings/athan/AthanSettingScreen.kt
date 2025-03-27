package ir.namoo.religiousprayers.ui.settings.athan

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AthanSettingsScreen(
    athanId: Int, navigateUp: () -> Unit, viewModel: AthanSettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    viewModel.loadData(context, athanId)
    val isLoading by viewModel.isLoading.collectAsState()
    val athanState by viewModel.athanState.collectAsState()
    val playType by viewModel.playType.collectAsState()
    val playDoa by viewModel.playDoa.collectAsState()
    val beforeState by viewModel.enableBefore.collectAsState()
    val beforeMinutes by viewModel.beforeMinute.collectAsState()
    val afterState by viewModel.enableAfter.collectAsState()
    val afterMinutes by viewModel.afterMinute.collectAsState()
    val silentState by viewModel.enableSilent.collectAsState()
    val silentMinutes by viewModel.silentMinute.collectAsState()
    val jummaSilent by viewModel.enableJummaSilent.collectAsState()
    val jummaMinutes by viewModel.silentJummaMinute.collectAsState()
    val isAscending by viewModel.ascendingVolume.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val athanNames by viewModel.athanNames.collectAsState()
    val selectedAthan by viewModel.selectedAthan.collectAsState()
    val alarmNames by viewModel.alarmNames.collectAsState()
    val selectedAlarm by viewModel.selectedAlarm.collectAsState()
    val isAthanPlaying by viewModel.isAthanPlaying.collectAsState()
    val isAlarmPlaying by viewModel.isAlarmPlaying.collectAsState()
    val selectedBackground by viewModel.selectedBackground.collectAsState()

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(
                        id = when (athanId) {
                            1 -> R.string.fajr
                            2 -> R.string.sunrise
                            3 -> R.string.dhuhr
                            4 -> R.string.asr
                            5 -> R.string.maghrib
                            else -> R.string.isha
                        }

                    )
                )
            },
            navigationIcon = { NavigationNavigateUpIcon(navigateUp) },
            colors = appTopAppBarColors()
        )
    }) { paddingValues ->
        Surface(
            shape = materialCornerExtraLargeTop(),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(4.dp)
            ) {
                AnimatedVisibility(isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .padding(vertical = 0.dp, horizontal = 10.dp)
                            .fillMaxWidth()
                            .height(8.dp), strokeCap = StrokeCap.Round
                    )
                }
                SettingSwitch(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(id = if (athanId == 2) R.string.enable_alarm else R.string.enable_athan),
                    isChecked = athanState,
                    onClick = { viewModel.updateAthanState(context) },
                    checkedIcon = Icons.AutoMirrored.Default.VolumeUp,
                    unCheckedIcon = Icons.AutoMirrored.Default.VolumeOff
                )
                HorizontalDivider(modifier = Modifier.padding(8.dp))
                PlayTypeComponent(
                    playType = playType,
                    onTypeChange = { viewModel.updatePlayType(it) })
                if (athanId != 2) {
                    HorizontalDivider(modifier = Modifier.padding(8.dp))
                    SettingSwitch(
                        modifier = Modifier.fillMaxWidth(),
                        isChecked = playDoa,
                        title = stringResource(id = R.string.broadcast_prayer_after_adhan),
                        onClick = { viewModel.updatePlayDoa() },
                        checkedIcon = Icons.AutoMirrored.Default.VolumeUp,
                        unCheckedIcon = Icons.AutoMirrored.Default.VolumeOff
                    )
                    HorizontalDivider(modifier = Modifier.padding(8.dp))
                    AlertComponent(
                        title = stringResource(id = R.string.alarm_before),
                        togleTitle = stringResource(id = R.string.enable_alarm),
                        isChecked = beforeState,
                        onCheck = { viewModel.updateBeforeState(context) },
                        range = 5..if (athanId == 1) 180 else 60,
                        minute = beforeMinutes,
                        onMinute = { viewModel.updateBeforeMinutes(context, it) })
                    HorizontalDivider(modifier = Modifier.padding(8.dp))
                    AlertComponent(
                        title = stringResource(id = R.string.alarm_after),
                        togleTitle = stringResource(id = R.string.enable_alarm),
                        isChecked = afterState,
                        onCheck = { viewModel.updateAfterState(context) },
                        range = 5..60,
                        minute = afterMinutes,
                        onMinute = { viewModel.updateAfterMinutes(context, it) })
                    HorizontalDivider(modifier = Modifier.padding(8.dp))
                    AlertComponent(
                        title = stringResource(id = R.string.silent_after),
                        togleTitle = stringResource(id = R.string.enable_silent),
                        isChecked = silentState,
                        onCheck = { viewModel.updateSilentState(context) },
                        range = 10..60,
                        minute = silentMinutes,
                        onMinute = { viewModel.updateSilentMinutes(context, it) })
                    if (athanId == 3) {
                        HorizontalDivider(modifier = Modifier.padding(8.dp))
                        AlertComponent(
                            title = stringResource(id = R.string.jumma_silent),
                            togleTitle = stringResource(id = R.string.enable_silent),
                            isChecked = jummaSilent,
                            onCheck = { viewModel.updateJummaSilentState(context) },
                            range = 20..120,
                            minute = jummaMinutes,
                            onMinute = { viewModel.updateJummaSilentMinute(context, it) }
                        )
                    }

                }
                HorizontalDivider(modifier = Modifier.padding(8.dp))
                SettingSwitch(
                    isChecked = isAscending,
                    title = stringResource(id = R.string.ascending_athan_volume),
                    onClick = { viewModel.updateAscendingState() },
                    checkedIcon = Icons.Default.CheckCircleOutline,
                    unCheckedIcon = Icons.Default.Close
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.enable_ascending_athan_volume),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                VolumeComponent(
                    enable = !isAscending,
                    volume = volume,
                    updateVolume = { viewModel.updateVolume(it) })
                HorizontalDivider(modifier = Modifier.padding(8.dp))
                if (athanId != 2) {
                    AthanSelector(
                        title = stringResource(id = R.string.str_selectAthan),
                        items = athanNames,
                        selectedItem = selectedAthan,
                        onSelectChanged = { viewModel.updateSelectedAthan(context, it) })
                }
                AthanSelector(
                    title = stringResource(id = R.string.select_alarm),
                    items = alarmNames,
                    selectedItem = selectedAlarm,
                    onSelectChanged = { viewModel.updateSelectedAlarm(context, it) })

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (athanId != 2)
                        AnimatedContent(
                            targetState = isAthanPlaying,
                            label = "isAthanPlaying"
                        ) { isPlay ->
                            ElevatedButton(onClick = { viewModel.playAthan(context, activity) }) {
                                Text(
                                    text = stringResource(id = if (isPlay) R.string.stop else R.string.play_athan),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.padding(4.dp))
                                Icon(
                                    imageVector = if (isPlay) Icons.Default.StopCircle else Icons.Default.PlayCircle,
                                    contentDescription = ""
                                )
                            }
                        }
                    AnimatedContent(
                        targetState = isAlarmPlaying,
                        label = "isAlarmPlaying"
                    ) { isPlay ->
                        ElevatedButton(onClick = { viewModel.playAlarm(context, activity) }) {
                            Text(
                                text = stringResource(id = if (isPlay) R.string.stop else R.string.play_alert),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Icon(
                                imageVector = if (isPlay) Icons.Default.StopCircle else Icons.Default.PlayCircle,
                                contentDescription = ""
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(8.dp))
                AthanBackgroundSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    backgroundUri = selectedBackground,
                    id = athanId,
                    onSelectBackground = { viewModel.updateSelectedBackground(it) }
                )
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}
