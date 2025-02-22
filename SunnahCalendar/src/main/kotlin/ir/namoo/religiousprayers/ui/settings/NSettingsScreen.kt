package ir.namoo.religiousprayers.ui.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LiveHelp
import androidx.compose.material3.ElevatedButton
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
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.titleStringId
import com.byagowi.persiancalendar.utils.update
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.CalculationMethod
import ir.namoo.commons.PREF_SHOW_SYSTEM_RINGTONES
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

@Composable
fun NSettingsScreen(
    navigateToAthanSettings: (Int) -> Unit,
    athanSettings: AthanSettingsDB = koinInject(),
    athanDB: AthanDB = koinInject()
) {
    val context = LocalContext.current
    val prefs = context.preferences
    val coroutineScope = rememberCoroutineScope()
    val allAthansSetting = athanSettings.athanSettingsDAO().getAllAthanSettings()
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
        var showHelpDialog by remember { mutableStateOf(false) }
        ElevatedButton(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
            onClick = { showHelpDialog = true }) {
            Text(
                text = stringResource(id = R.string.help_fix_athan_problems),
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Default.LiveHelp,
                contentDescription = "Help",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        AnimatedVisibility(visible = showHelpDialog) {
            HelpFixAthanProblemDialog {
                showHelpDialog = false
            }
        }
    }
    run {
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
                }
            })
        }
    }

    HorizontalDivider()
    var isChecked by remember {
        mutableStateOf(prefs.getBoolean(PREF_SUMMER_TIME, false))
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
    var isShown by remember {
        mutableStateOf(prefs.getBoolean(PREF_SHOW_SYSTEM_RINGTONES, false))
    }
    SettingsSwitch(key = PREF_SHOW_SYSTEM_RINGTONES,
        value = isShown,
        title = stringResource(R.string.show_system_ringtones),
        summary = stringResource(R.string.show_system_ringtones_msg),
        onBeforeToggle = {
            isShown = it
            isShown
        })
    SettingsSection(title = stringResource(id = R.string.add_normal_athans))
    AthanAlarmComponent(type = 1)
    SettingsSection(title = stringResource(id = R.string.add_alarm))
    AthanAlarmComponent(type = 2)
    ClearAthansComponent {
        runCatching {
            coroutineScope.launch {
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
            }
        }.onFailure(logException)
    }
}
