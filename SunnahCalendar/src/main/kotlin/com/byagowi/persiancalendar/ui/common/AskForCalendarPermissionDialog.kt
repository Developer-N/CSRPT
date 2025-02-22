package com.byagowi.persiancalendar.ui.common

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.utils.preferences

@Composable
fun AskForCalendarPermissionDialog(setGranted: (Boolean) -> Unit) {
    val context = LocalContext.current

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        setGranted(true)
        return
    }
    if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        context.preferences.edit { putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, true) }
        setGranted(true)
        return
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        context.preferences.edit { putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, isGranted) }
        updateStoredPreference(context)
        setGranted(isGranted)
    }

    // Maybe use ActivityCompat.shouldShowRequestPermissionRationale here? But in my testing it
    // didn't go well in Android 6.0 so better not risk I guess

    var showDialog by rememberSaveable { mutableStateOf(true) }
    if (showDialog) AppDialog(
        title = { Text(stringResource(R.string.calendar_access)) },
        confirmButton = {
            TextButton(onClick = {
                showDialog = false
                launcher.launch(Manifest.permission.READ_CALENDAR)
            }) { Text(stringResource(R.string.continue_button)) }
        },
        dismissButton = {
            TextButton(onClick = {
                context.preferences.edit { putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false) }
                setGranted(false)
            }) { Text(stringResource(R.string.cancel)) }
        },
        onDismissRequest = { setGranted(false) },
    ) {
        Text(
            stringResource(R.string.phone_calendar_required),
            Modifier.padding(horizontal = SettingsHorizontalPaddingItem.dp)
        )
    }
}
