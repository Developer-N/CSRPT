package ir.namoo.religiousprayers.ui.edit

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.NavigationOpenNavigationRailIcon
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import ir.namoo.religiousprayers.ui.settings.athan.SettingSwitch
import org.koin.androidx.compose.koinViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PrayTimesEditScreen(
    openNavigationRail: () -> Unit,
    animatedContentScope: AnimatedContentScope,
    viewModel: EditPrayTimeViewModel = koinViewModel()
) {
    val context = LocalContext.current
    viewModel.loadData(context)

    val isLoading by viewModel.isLoading.collectAsState()
    val isEnabled by viewModel.isEnabled.collectAsState()
    val progress by viewModel.progress.collectAsState()

    val month by viewModel.month.collectAsState()
    val day by viewModel.day.collectAsState()
    val dayNumber by viewModel.dayNumber.collectAsState()
    val editedTimes by viewModel.editedTimes.collectAsState()
    val originalTimes by viewModel.originalTimes.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showGroupEditDialog by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.edit_times)) },
            colors = appTopAppBarColors(),
            navigationIcon = {
                NavigationOpenNavigationRailIcon(
                    animatedContentScope,
                    openNavigationRail
                )
            },
            actions = {
                AnimatedVisibility(visible = isEnabled) {
                    Row {
                        AppIconButton(
                            title = stringResource(id = R.string.save),
                            onClick = { showSaveDialog = true },
                            icon = Icons.Default.Save
                        )
                        ThreeDotsDropdownMenu(animatedContentScope) { closeMenu ->
                            AppDropdownMenuItem(text = {
                                Text(text = stringResource(id = R.string.send_times))
                            }, onClick = {
                                closeMenu()
                                sendPrayTimes(context)
                            }, trailingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.Send,
                                    contentDescription = stringResource(id = R.string.send_times)
                                )
                            })
                            AppDropdownMenuItem(text = {
                                Text(text = stringResource(id = R.string.group_edit))
                            }, onClick = {
                                closeMenu()
                                showGroupEditDialog = true
                            }, trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = stringResource(id = R.string.group_edit)
                                )
                            })
                            AppDropdownMenuItem(text = {
                                Text(text = stringResource(id = R.string.clear_change))
                            }, onClick = {
                                closeMenu()
                                showClearDialog = true
                            }, trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(id = R.string.clear_change)
                                )
                            })
                        }
                    }
                }

            })
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
                    .padding(4.dp)
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                SettingSwitch(
                    modifier = Modifier.padding(4.dp),
                    isChecked = isEnabled,
                    title = stringResource(id = R.string.enable_edit_times),
                    onClick = {
                        viewModel.updateIsEnabled(context)
                    },
                    checkedIcon = Icons.Default.CheckCircle,
                    unCheckedIcon = Icons.Default.Close
                )
                AnimatedVisibility(visible = isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .height(8.dp),
                        strokeCap = StrokeCap.Round
                    )
                }
                AnimatedVisibility(visible = progress > 0f && progress < 1f) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .height(10.dp),
                        progress = { progress },
                        strokeCap = StrokeCap.Round
                    )
                }
                AnimatedVisibility(visible = isEnabled) {
                    Column {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                text = stringResource(id = R.string.edit_time_message),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                        EditDateComponent(
                            month = month,
                            day = day,
                            onMonthChanged = { viewModel.updateMonth(it) },
                            onDayChanged = { viewModel.updateDay(it) })
                        AnimatedContent(targetState = dayNumber, label = "fs") { d ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                EditAthanComponent(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp),
                                    title = stringResource(id = R.string.fajr),
                                    editedTime = editedTimes.find { it.dayNumber == d }?.fajr,
                                    originalTime = originalTimes.find { it.dayNumber == d }?.fajr,
                                    onTimeChanged = { viewModel.updateFajr(it) })
                                EditAthanComponent(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp),
                                    title = stringResource(id = R.string.sunrise),
                                    editedTime = editedTimes.find { it.dayNumber == d }?.sunrise,
                                    originalTime = originalTimes.find { it.dayNumber == d }?.sunrise,
                                    onTimeChanged = { viewModel.updateSunrise(it) })
                            }
                        }
                        AnimatedContent(targetState = dayNumber, label = "da") { d ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                EditAthanComponent(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp),
                                    title = stringResource(id = R.string.dhuhr),
                                    editedTime = editedTimes.find { it.dayNumber == d }?.dhuhr,
                                    originalTime = originalTimes.find { it.dayNumber == d }?.dhuhr,
                                    onTimeChanged = { viewModel.updateDhuhr(it) })
                                EditAthanComponent(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp),
                                    title = stringResource(id = R.string.asr),
                                    editedTime = editedTimes.find { it.dayNumber == d }?.asr,
                                    originalTime = originalTimes.find { it.dayNumber == d }?.asr,
                                    onTimeChanged = { viewModel.updateAsr(it) })
                            }
                        }
                        AnimatedContent(targetState = dayNumber, label = "mi") { d ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                EditAthanComponent(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp),
                                    title = stringResource(id = R.string.maghrib),
                                    editedTime = editedTimes.find { it.dayNumber == d }?.maghrib,
                                    originalTime = originalTimes.find { it.dayNumber == d }?.maghrib,
                                    onTimeChanged = { viewModel.updateMaghrib(it) })
                                EditAthanComponent(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp),
                                    title = stringResource(id = R.string.isha),
                                    editedTime = editedTimes.find { it.dayNumber == d }?.isha,
                                    originalTime = originalTimes.find { it.dayNumber == d }?.isha,
                                    onTimeChanged = { viewModel.updateIsha(it) })
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = showSaveDialog) {
                    AlertDialog(
                        onDismissRequest = { showSaveDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showSaveDialog = false
                                viewModel.saveToDB()
                                Toast.makeText(
                                    context, context.getString(R.string.saved), Toast.LENGTH_LONG
                                ).show()
                            }) {
                                Text(text = stringResource(id = R.string.yes))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSaveDialog = false }) {
                                Text(text = stringResource(id = R.string.no))
                            }
                        },
                        title = { Text(text = stringResource(id = R.string.str_dialog_save)) },
                        text = { Text(text = stringResource(id = R.string.str_dialog_save_message)) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Warning, contentDescription = ""
                            )
                        })
                }

                AnimatedVisibility(visible = showClearDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showClearDialog = false
                                viewModel.clearDB(context)
                            }) {
                                Text(text = stringResource(id = R.string.yes))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearDialog = false }) {
                                Text(text = stringResource(id = R.string.no))
                            }
                        },
                        title = { Text(text = stringResource(id = R.string.str_dialog_clear_edited_title)) },
                        text = { Text(text = stringResource(id = R.string.str_dialog_clear_edited_message)) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Warning, contentDescription = ""
                            )
                        })
                }
                AnimatedVisibility(visible = showGroupEditDialog) {
                    GroupEditDialog(
                        onDismiss = { showGroupEditDialog = false },
                        onGroupEdit = { athan, fm, tm, fd, td, m, f ->
                            viewModel.groupEdit(athan, fm, tm, fd, td, m, f)
                        })
                }
            }
        }
    }
}

private fun sendPrayTimes(context: Context) {
    val dbFile = File(context.getDatabasePath("sunnah_db").path)
    val uri = FileProvider.getUriForFile(
        context, "${BuildConfig.APPLICATION_ID}.provider", dbFile
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/octet_stream"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooserIntent =
        Intent.createChooser(shareIntent, context.getString(R.string.send_times))
    context.startActivity(chooserIntent)
}
