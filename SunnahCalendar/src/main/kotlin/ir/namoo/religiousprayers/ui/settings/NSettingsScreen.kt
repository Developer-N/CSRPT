package ir.namoo.religiousprayers.ui.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.DEFAULT_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.PREF_ASR_HANAFI_JURISTIC
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.asrMethod
import com.byagowi.persiancalendar.ui.settings.SettingsClickable
import com.byagowi.persiancalendar.ui.settings.SettingsSection
import com.byagowi.persiancalendar.ui.settings.SettingsSingleSelect
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.titleStringId
import com.byagowi.persiancalendar.utils.update
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.CalculationMethod
import ir.namoo.commons.PREF_SUMMER_TIME
import ir.namoo.commons.model.AthanDB
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.utils.getAthansDirectoryPath
import ir.namoo.religiousprayers.ui.settings.athan.AthanAlarmComponent
import ir.namoo.religiousprayers.ui.settings.athan.ClearAthansComponent
import ir.namoo.religiousprayers.ui.settings.location.ShowLocationDialog
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NSettingsScreen(
    navigateToAthanSettings: (Int) -> Unit,
    athanSettings: AthanSettingsDB = koinInject(),
    athanDB: AthanDB = koinInject()
) {
    val context = LocalContext.current
    val prefs = context.appPrefs
    SettingsSection(title = stringResource(id = R.string.location))
    run {
        var summary by mutableStateOf(
            formatNumber(
                prefs.getString(PREF_GEOCODED_CITYNAME, "") + ": " + prefs.getString(
                    PREF_LONGITUDE, "0.0"
                ) + "-" + prefs.getString(PREF_LATITUDE, "0.0") + ""
            )
        )
        SettingsClickable(title = stringResource(id = R.string.location),
            summary = summary,
            dialog = { onDismiss ->
                ShowLocationDialog(closeDialog = {
                    onDismiss()
                    summary = formatNumber(
                        prefs.getString(PREF_GEOCODED_CITYNAME, "") + prefs.getString(
                            PREF_LONGITUDE, "0.0"
                        ) + prefs.getString(PREF_LATITUDE, "0.0") + ""
                    )
                    update(context, true)
                })
            })

    }
    HorizontalDivider()
    SettingsSection(title = stringResource(id = R.string.athan_settings))
    run {
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        val coroutineScope = rememberCoroutineScope()
        var isAthanNotificationEnable = false
        val allAthansSetting = athanSettings.athanSettingsDAO().getAllAthanSettings()
        allAthansSetting.forEach {
            if (it.state) isAthanNotificationEnable = true
        }
        val phoneStatePermissions = rememberMultiplePermissionsState(
            permissions = listOf(Manifest.permission.READ_PHONE_STATE)
        )
        var showPermissionMessage by remember { mutableStateOf(!phoneStatePermissions.allPermissionsGranted) }
        var showOverlayMessage by remember { mutableStateOf(false) }
        AnimatedVisibility(
            visible = isAthanNotificationEnable && showPermissionMessage,
            enter = slideInVertically(tween()), exit = shrinkVertically(tween())
        ) {
            ElevatedAssistChip(modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 16.dp)
                .fillMaxWidth(),
                onClick = {
                    showPermissionMessage = false
                    phoneStatePermissions.launchMultiplePermissionRequest()
                },
                label = {
                    Text(
                        modifier = Modifier.padding(4.dp),
                        text = stringResource(id = R.string.phone_state_permission_message),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(id = R.string.warning),
                        tint = MaterialTheme.colorScheme.error
                    )
                })
        }
        AnimatedVisibility(
            visible = showOverlayMessage,
            enter = slideInVertically(tween()),
            exit = shrinkVertically(tween())
        ) {
            ElevatedAssistChip(modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 16.dp)
                .fillMaxWidth(),
                onClick = {
                    showOverlayMessage = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + context.packageName)
                            )
                        )
                    }
                },
                label = {
                    Text(
                        modifier = Modifier.padding(4.dp),
                        text = stringResource(id = R.string.need_full_screen_permision),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(id = R.string.warning),
                        tint = MaterialTheme.colorScheme.error
                    )
                })
        }
        val athans = listOf(
            context.getString(R.string.fajr),
            context.getString(R.string.sunrise),
            context.getString(R.string.dhuhr),
            context.getString(R.string.asr),
            context.getString(R.string.maghrib),
            context.getString(R.string.isha)
        )
        athans.zip(allAthansSetting).forEach { athan ->
            var isActive by remember { mutableStateOf(athan.second.state) }
            SettingsAthanSwitch(title = athan.first, isActive = isActive, onBoxClick = {
                navigateToAthanSettings(athan.second.id)
            }, onSwitchClick = {
                isActive = !isActive
                coroutineScope.launch {
                    athanSettings.athanSettingsDAO().update(athan.second.apply {
                        state = isActive
                    })
                    if (isActive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(
                            context
                        )
                    ) showOverlayMessage = true
                }
            })
        }
    }

    HorizontalDivider()
    var isChecked by remember {
        mutableStateOf(context.appPrefs.getBoolean(PREF_SUMMER_TIME, false))
    }
    SettingsSwitch(key = PREF_SUMMER_TIME,
        value = isChecked,
        title = stringResource(id = R.string.summer_time),
        onBeforeToggle = {
            isChecked = it
            isChecked
        })
    SettingsSingleSelect(
        PREF_PRAY_TIME_METHOD,
        CalculationMethod.entries.map { stringResource(it.titleStringId) },
        CalculationMethod.entries.map { it.name },
        DEFAULT_PRAY_TIME_METHOD,
        dialogTitleResId = R.string.pray_methods_calculation,
        title = stringResource(R.string.pray_methods)
    )
    val asrMethod by asrMethod.collectAsState()
    SettingsSwitch(
        key = PREF_ASR_HANAFI_JURISTIC,
        value = asrMethod == AsrMethod.Hanafi,
        title = stringResource(R.string.asr_hanafi_juristic)
    )
    HorizontalDivider()
    SettingsSection(title = stringResource(id = R.string.add_normal_athans))
    AthanAlarmComponent(type = 1)
    SettingsSection(title = stringResource(id = R.string.add_alarm))
    AthanAlarmComponent(type = 2)
    HorizontalDivider()
    ClearAthansComponent {
        runCatching {
            athanDB.athanDAO().clearDB()
            val dir = File(getAthansDirectoryPath(context)).listFiles()
            if (dir != null && dir.isNotEmpty()) for (f in dir) f.delete()
            Toast.makeText(
                context, context.getString(R.string.done), Toast.LENGTH_SHORT
            ).show()
            val settings = athanSettings.athanSettingsDAO().getAllAthanSettings()
            if (settings.isNotEmpty()) for (s in settings) {
                s.athanURI = ""
                s.alertURI = ""
                athanSettings.athanSettingsDAO().update(s)
            }
        }.onFailure(logException)
    }
    HorizontalDivider()
    SettingsSection(title = stringResource(id = R.string.play_athan_problem))
    Text(
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 22.dp),
        text = stringResource(id = R.string.notification)
    )
    MethodsModeToggle(context = context, type = 1)
    Text(
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 22.dp),
        text = stringResource(id = R.string.full_screen)
    )

    MethodsModeToggle(context = context, type = 2)
}
