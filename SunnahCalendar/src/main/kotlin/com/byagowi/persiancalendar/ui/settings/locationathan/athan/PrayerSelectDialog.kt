package com.byagowi.persiancalendar.ui.settings.locationathan.athan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_ATHAN_ALARM
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.splitFilterNotEmpty
import com.byagowi.persiancalendar.utils.startAthan

@Composable
fun PrayerSelectDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val alarms = rememberSaveable(
        saver = listSaver(save = { it.toList() }, restore = { it.toMutableStateList() })
    ) {
        (context.preferences.getString(PREF_ATHAN_ALARM, null) ?: "")
            .splitFilterNotEmpty(",").mapNotNull(PrayTime::fromName).toMutableStateList()
    }
    AppDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.athan_alarm)) },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                context.preferences.edit { putString(PREF_ATHAN_ALARM, alarms.joinToString(",")) }
            }) { Text(stringResource(R.string.accept)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
    ) {
        PrayTime.athans.forEach {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { if (it in alarms) alarms.remove(it) else alarms.add(it) }
                    .padding(horizontal = SettingsHorizontalPaddingItem.dp)
                    .height(SettingsItemHeight.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = it in alarms, onCheckedChange = null)
                Spacer(modifier = Modifier.width(SettingsHorizontalPaddingItem.dp))
                Text(stringResource(it.stringRes), Modifier.weight(1f, fill = true))
            }
        }
    }
}

@Composable
fun PrayerSelectPreviewDialog(onDismissRequest: () -> Unit) {
    AppDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.preview)) },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
    ) {
        val context = LocalContext.current
        PrayTime.athans.forEach {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        startAthan(context, it.name, null)
                    }
                    .height(SettingsItemHeight.dp)
                    .padding(horizontal = SettingsHorizontalPaddingItem.dp)
            ) { Text(stringResource(it.stringRes)) }
        }
    }
}
