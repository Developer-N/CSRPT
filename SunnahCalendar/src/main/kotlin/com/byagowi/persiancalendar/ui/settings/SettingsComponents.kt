package com.byagowi.persiancalendar.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.theme.appColorAnimationSpec
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.utils.appPrefs

@Composable
fun SettingsSection(title: String, subtitle: String? = null) {
    Spacer(Modifier.padding(top = 16.dp))
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        AnimatedContent(
            title,
            label = "title",
            transitionSpec = appCrossfadeSpec,
        ) { state ->
            Text(
                state,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        AnimatedVisibility(visible = subtitle != null) {
            Text(
                subtitle ?: "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(AppBlendAlpha)
            )
        }
    }
}

@Composable
fun SettingsHorizontalDivider() {
    val color by animateColorAsState(
        MaterialTheme.colorScheme.outlineVariant,
        appColorAnimationSpec,
        label = "divider color"
    )
    HorizontalDivider(Modifier.padding(horizontal = 8.dp), color = color)
}

@Composable
fun SettingsClickable(
    title: String,
    summary: String? = null,
    defaultOpen: Boolean = false,
    dialog: @Composable (onDismissRequest: () -> Unit) -> Unit,
) {
    var showDialog by rememberSaveable { mutableStateOf(defaultOpen) }
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 16.dp, horizontal = 24.dp),
    ) {
        AnimatedContent(
            title,
            label = "title",
            transitionSpec = appCrossfadeSpec,
        ) { state -> Text(state, style = MaterialTheme.typography.bodyLarge) }
        AnimatedVisibility(visible = summary != null) {
            AnimatedContent(
                summary ?: "",
                label = "summary",
                transitionSpec = appCrossfadeSpec,
            ) { state ->
                Text(
                    state,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(AppBlendAlpha)
                )
            }
        }
    }
    if (showDialog) dialog { showDialog = false }
}

@Composable
fun SettingsSingleSelect(
    key: String,
    entries: List<String>,
    entryValues: List<String>,
    defaultValue: String,
    dialogTitleResId: Int,
    title: String,
    summaryResId: Int? = null
) {
    val context = LocalContext.current
    var summary by remember {
        mutableStateOf(
            if (summaryResId == null) entries[entryValues.indexOf(
                context.appPrefs.getString(key, null) ?: defaultValue
            )] else context.getString(summaryResId)
        )
    }
    SettingsClickable(title = title, summary = summary) { onDismissRequest ->
        AppDialog(
            title = { Text(stringResource(dialogTitleResId)) },
            dismissButton = {
                TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
            },
            onDismissRequest = onDismissRequest,
        ) {
            val currentValue = remember {
                context.appPrefs.getString(key, null) ?: defaultValue
            }
            entries.zip(entryValues) { entry, entryValue ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SettingsItemHeight.dp)
                        .clickable {
                            onDismissRequest()
                            context.appPrefs.edit { putString(key, entryValue) }
                            if (summaryResId == null) summary = entry
                        }
                        .padding(horizontal = SettingsHorizontalPaddingItem.dp),
                ) {
                    RadioButton(selected = entryValue == currentValue, onClick = null)
                    Spacer(modifier = Modifier.width(SettingsHorizontalPaddingItem.dp))
                    Text(entry)
                }
            }
        }
    }
}

@Composable
fun SettingsMultiSelect(
    key: String,
    entries: List<String>,
    entryValues: List<String>,
    defaultValue: Set<String>,
    dialogTitleResId: Int,
    title: String,
    summary: String? = null,
) {
    val context = LocalContext.current
    SettingsClickable(title = title, summary = summary) { onDismissRequest ->
        val result = rememberSaveable(
            saver = listSaver(save = { it.toList() }, restore = { it.toMutableStateList() })
        ) {
            (context.appPrefs.getStringSet(key, null) ?: defaultValue).toList()
                .toMutableStateList()
        }
        AppDialog(
            title = { Text(stringResource(dialogTitleResId)) },
            onDismissRequest = onDismissRequest,
            dismissButton = {
                TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDismissRequest()
                    context.appPrefs.edit { putStringSet(key, result.toSet()) }
                }) { Text(stringResource(R.string.accept)) }
            },
        ) {
            entries.zip(entryValues) { entry, entryValue ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SettingsItemHeight.dp)
                        .clickable {
                            if (entryValue in result) result.remove(entryValue)
                            else result.add(entryValue)
                        }
                        .padding(horizontal = SettingsHorizontalPaddingItem.dp),
                ) {
                    Checkbox(checked = entryValue in result, onCheckedChange = null)
                    Spacer(modifier = Modifier.width(SettingsHorizontalPaddingItem.dp))
                    Text(entry)
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchWithInnerState(
    key: String,
    defaultValue: Boolean,
    title: String,
    summary: String? = null,
) {
    val context = LocalContext.current
    var currentValue by remember { mutableStateOf(context.appPrefs.getBoolean(key, defaultValue)) }
    val toggle = {
        currentValue = !currentValue
        context.appPrefs.edit { putBoolean(key, currentValue) }
    }
    SettingsSwitchLayout(toggle, title, summary, currentValue)
}

@Composable
fun SettingsSwitch(
    key: String,
    value: Boolean,
    title: String,
    summary: String? = null,
    onBeforeToggle: (Boolean) -> Boolean = { it },
) {
    val context = LocalContext.current
    val toggle = {
        val newValue = onBeforeToggle(!value)
        if (value != newValue) context.appPrefs.edit { putBoolean(key, newValue) }
    }
    SettingsSwitchLayout(toggle, title, summary, value)
}

@Composable
private fun SettingsSwitchLayout(
    toggle: () -> Unit,
    title: String,
    summary: String?,
    value: Boolean,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = toggle)
            .padding(horizontal = 8.dp),
    ) {
        Column(
            Modifier
                .align(alignment = Alignment.CenterStart)
                // 68 is brought from androidx.preferences
                .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = (16 + 68).dp)
        ) {
            AnimatedContent(
                title,
                label = "title",
                transitionSpec = appCrossfadeSpec,
            ) { state -> Text(state, style = MaterialTheme.typography.bodyLarge) }
            if (summary != null) {
                AnimatedContent(
                    summary,
                    label = "summary",
                    transitionSpec = appCrossfadeSpec,
                ) { state ->
                    Text(
                        state,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.alpha(AppBlendAlpha)
                    )
                }
            }
        }
        Switch(
            modifier = Modifier
                .align(alignment = Alignment.CenterEnd)
                .padding(end = 16.dp),
            checked = value,
            onCheckedChange = null,
        )
    }
}

@Composable
fun SettingsSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Column(Modifier.padding(top = 16.dp, start = 24.dp, end = 24.dp)) {
        AnimatedContent(
            title,
            label = "title",
            transitionSpec = appCrossfadeSpec,
        ) { state -> Text(state, style = MaterialTheme.typography.bodyLarge) }
        Slider(
            value = value,
            onValueChange = onValueChange,
        )
    }
}
