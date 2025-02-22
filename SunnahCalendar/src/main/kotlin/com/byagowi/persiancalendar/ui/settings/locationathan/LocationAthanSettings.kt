package com.byagowi.persiancalendar.ui.settings.locationathan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.byagowi.persiancalendar.DEFAULT_HIGH_LATITUDES_METHOD
import com.byagowi.persiancalendar.DEFAULT_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.EN_DASH
import com.byagowi.persiancalendar.PREF_ASCENDING_ATHAN_VOLUME
import com.byagowi.persiancalendar.PREF_ASR_HANAFI_JURISTIC
import com.byagowi.persiancalendar.PREF_ATHAN_ALARM
import com.byagowi.persiancalendar.PREF_ATHAN_VIBRATION
import com.byagowi.persiancalendar.PREF_HIGH_LATITUDES_METHOD
import com.byagowi.persiancalendar.PREF_MIDNIGHT_METHOD
import com.byagowi.persiancalendar.PREF_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.global.ascendingAthan
import com.byagowi.persiancalendar.global.asrMethod
import com.byagowi.persiancalendar.global.athanSoundName
import com.byagowi.persiancalendar.global.athanVibration
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.notificationAthan
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.settings.SettingsClickable
import com.byagowi.persiancalendar.ui.settings.SettingsHorizontalDivider
import com.byagowi.persiancalendar.ui.settings.SettingsSection
import com.byagowi.persiancalendar.ui.settings.SettingsSingleSelect
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.AthanGapDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.AthanSelectDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.AthanVolumeDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.PrayerSelectDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.PrayerSelectPreviewDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.CoordinatesDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.GPSLocationDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.LocationDialog
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.utils.enableHighLatitudesConfiguration
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.titleStringId
import io.github.persiancalendar.praytimes.AsrMethod
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.HighLatitudesMethod
import io.github.persiancalendar.praytimes.MidnightMethod

@Composable
fun ColumnScope.LocationAthanSettings(navigateToMap: () -> Unit, destination: String) {
    SettingsSection(stringResource(R.string.location))
    SettingsClickable(
        title = stringResource(R.string.gps_location),
        summary = stringResource(R.string.gps_location_help),
    ) { onDismissRequest -> GPSLocationDialog(onDismissRequest) }
    SettingsClickable(
        title = stringResource(R.string.location),
        summary = stringResource(R.string.location_help),
    ) { onDismissRequest -> LocationDialog(onDismissRequest) }

    val coordinates by coordinates.collectAsState()
    val context = LocalContext.current
    val cityName by cityName.collectAsState()
    SettingsClickable(stringResource(R.string.coordination), cityName) { onDismissRequest ->
        CoordinatesDialog(navigateToMap = navigateToMap, onDismissRequest = onDismissRequest)
    }

    val isLocationSet = coordinates != null
    val calculationMethod by calculationMethod.collectAsState()
    val notificationAthan by notificationAthan.collectAsState()
    val ascendingAthan by ascendingAthan.collectAsState()
    val language by language.collectAsState()
    SettingsHorizontalDivider()
    SettingsSection(
        stringResource(R.string.athan),
        if (isLocationSet) null else stringResource(R.string.athan_disabled_summary)
    )
    AnimatedVisibility(isLocationSet) {
        SettingsSingleSelect(
            PREF_PRAY_TIME_METHOD,
            CalculationMethod.entries.map { stringResource(it.titleStringId) },
            CalculationMethod.entries.map { it.name },
            DEFAULT_PRAY_TIME_METHOD,
            dialogTitleResId = R.string.pray_methods_calculation,
            title = stringResource(R.string.pray_methods)
        )
    }
    AnimatedVisibility(coordinates?.enableHighLatitudesConfiguration == true) {
        SettingsSingleSelect(
            PREF_HIGH_LATITUDES_METHOD,
            HighLatitudesMethod.entries.map { stringResource(it.titleStringId) },
            HighLatitudesMethod.entries.map { it.name },
            DEFAULT_HIGH_LATITUDES_METHOD,
            dialogTitleResId = R.string.high_latitudes_method,
            title = stringResource(R.string.high_latitudes_method)
        )
    }
    AnimatedVisibility(isLocationSet && !calculationMethod.isJafari) {
        val asrMethod by asrMethod.collectAsState()
        SettingsSwitch(
            key = PREF_ASR_HANAFI_JURISTIC,
            value = asrMethod == AsrMethod.Hanafi,
            title = stringResource(R.string.asr_hanafi_juristic)
        )
    }
    AnimatedVisibility(isLocationSet) {
        SettingsClickable(
            stringResource(R.string.athan_gap),
            stringResource(R.string.athan_gap_summary),
        ) { onDismissRequest -> AthanGapDialog(onDismissRequest) }
    }
    AnimatedVisibility(isLocationSet) {
        SettingsClickable(
            stringResource(R.string.athan_alarm),
            stringResource(R.string.athan_alarm_summary),
            defaultOpen = destination == PREF_ATHAN_ALARM,
        ) { onDismissRequest -> PrayerSelectDialog(onDismissRequest) }
    }
    AnimatedVisibility(isLocationSet) {
        val athanSoundName by athanSoundName.collectAsState()
        SettingsClickable(
            stringResource(R.string.custom_athan),
            athanSoundName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.default_athan),
        ) { onDismissRequest -> AthanSelectDialog(onDismissRequest) }
    }
    AnimatedVisibility(isLocationSet) {
        SettingsClickable(stringResource(R.string.preview)) { onDismissRequest ->
            PrayerSelectPreviewDialog(onDismissRequest)
        }
    }
    AnimatedVisibility(isLocationSet) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            context.preferences.edit { putBoolean(PREF_NOTIFICATION_ATHAN, isGranted) }
            updateStoredPreference(context)
        }
        SettingsSwitch(
            PREF_NOTIFICATION_ATHAN,
            notificationAthan,
            stringResource(R.string.notification_athan),
            stringResource(R.string.enable_notification_athan),
            onBeforeToggle = { value ->
                if (value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    false
                } else value
            },
        )
    }
    AnimatedVisibility(isLocationSet && !notificationAthan) {
        SettingsSwitch(
            PREF_ASCENDING_ATHAN_VOLUME,
            ascendingAthan,
            stringResource(R.string.ascending_athan_volume),
            stringResource(R.string.enable_ascending_athan_volume),
        )
    }
    AnimatedVisibility(isLocationSet && !notificationAthan && !ascendingAthan) {
        SettingsClickable(
            stringResource(R.string.athan_volume), stringResource(R.string.athan_volume_summary)
        ) { onDismissRequest -> AthanVolumeDialog(onDismissRequest) }
    }
    AnimatedVisibility(isLocationSet) {
        SettingsSwitch(
            PREF_ATHAN_VIBRATION,
            athanVibration.collectAsState().value,
            stringResource(R.string.vibration),
            language.tryTranslateAthanVibrationSummary()
        )
    }
    AnimatedVisibility(isLocationSet) {
        var midnightSummary by remember {
            mutableStateOf(getMidnightMethodPreferenceSummary(context))
        }
        SettingsClickable(stringResource(R.string.midnight), midnightSummary) { onDismissRequest ->
            AppDialog(
                title = { Text(stringResource(R.string.midnight)) },
                onDismissRequest = onDismissRequest,
                dismissButton = {
                    TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
                },
            ) {
                val currentSelectionKey =
                    context.preferences.getString(PREF_MIDNIGHT_METHOD, null) ?: "DEFAULT"
                (listOf(midnightDefaultTitle(context.resources) to "DEFAULT") + MidnightMethod.entries.filter { !it.isJafariOnly || calculationMethod.isJafari }
                    .map {
                        midnightMethodToString(context.resources, it) to it.name
                    }).forEach { (title, key) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(SettingsItemHeight.dp)
                            .clickable {
                                onDismissRequest()
                                context.preferences.edit {
                                    if (key == "DEFAULT") remove(PREF_MIDNIGHT_METHOD)
                                    else putString(PREF_MIDNIGHT_METHOD, key)
                                }
                                midnightSummary = title
                            }
                            .padding(horizontal = SettingsHorizontalPaddingItem.dp),
                    ) {
                        RadioButton(selected = key == currentSelectionKey, onClick = null)
                        Spacer(modifier = Modifier.width(SettingsHorizontalPaddingItem.dp))
                        Text(title)
                    }
                }
            }
        }
    }
}

private fun midnightDefaultTitle(resources: Resources): String {
    return resources.getString(calculationMethod.value.titleStringId) + spacedComma + midnightMethodToString(
        resources, calculationMethod.value.defaultMidnight
    )
}

private fun getMidnightMethodPreferenceSummary(context: Context): String {
    return context.preferences.getString(PREF_MIDNIGHT_METHOD, null)
        ?.let { midnightMethodToString(context.resources, MidnightMethod.valueOf(it)) }
        ?: midnightDefaultTitle(context.resources)
}

private fun midnightMethodToString(resources: Resources, method: MidnightMethod): String {
    return PrayTime.pairFromMidnightMethod(method).joinToString(EN_DASH) {
        resources.getString(it.stringRes)
    }
}
