package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_RED_HOLIDAYS
import com.byagowi.persiancalendar.PREF_SYSTEM_DARK_THEME
import com.byagowi.persiancalendar.PREF_SYSTEM_LIGHT_THEME
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.PREF_THEME_GRADIENT
import com.byagowi.persiancalendar.PREF_VAZIR_ENABLED
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.isGradient
import com.byagowi.persiancalendar.global.isRedHolidays
import com.byagowi.persiancalendar.global.isVazirEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.systemDarkTheme
import com.byagowi.persiancalendar.global.systemLightTheme
import com.byagowi.persiancalendar.global.userSetTheme
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.common.SwitchWithLabel
import com.byagowi.persiancalendar.ui.theme.Theme
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.utils.preferences

@Composable
fun ThemeDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val userSetTheme by userSetTheme.collectAsState()
    var showMore by rememberSaveable { mutableStateOf(false) }
    val systemLightTheme by systemLightTheme.collectAsState()
    val systemDarkTheme by systemDarkTheme.collectAsState()
    val themesToCheck = run {
        if (userSetTheme == Theme.SYSTEM_DEFAULT) listOf(systemLightTheme, systemDarkTheme)
        else listOf(userSetTheme)
    }
    val anyThemeHasGradient = themesToCheck.any { it.hasGradient }
    val anyThemeIsDynamicColors = themesToCheck.any { it.isDynamicColors }
    AppDialog(
        title = { Text(stringResource(R.string.select_skin)) },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        neutralButton = {
            AnimatedVisibility(visible = !showMore && (anyThemeHasGradient || anyThemeIsDynamicColors)) {
                TextButton(onClick = { showMore = true }) { Text(stringResource(R.string.more)) }
            }
        },
    ) {
        val invisible = Modifier
            .alpha(0f)
            .height(8.dp)
            .semantics { this.hideFromAccessibility() }
        val systemThemeOptions = listOf(
            Triple(R.string.theme_light, PREF_SYSTEM_LIGHT_THEME, systemLightTheme),
            Triple(R.string.theme_dark, PREF_SYSTEM_DARK_THEME, systemDarkTheme)
        )
        Theme.entries.forEach { entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SettingsItemHeight.dp)
                    .clickable {
                        onDismissRequest()
                        context.preferences.edit {
                            putString(PREF_THEME, entry.key)
                            // Consider returning to system default as some sort of theme reset
                            if (userSetTheme != Theme.SYSTEM_DEFAULT && entry == Theme.SYSTEM_DEFAULT) {
                                remove(PREF_SYSTEM_LIGHT_THEME)
                                remove(PREF_SYSTEM_DARK_THEME)
                                remove(PREF_RED_HOLIDAYS)
                                remove(PREF_THEME_GRADIENT)
                            }
                        }
                    }
                    .padding(start = SettingsHorizontalPaddingItem.dp),
            ) {
                RadioButton(selected = entry == userSetTheme, onClick = null)
                Spacer(modifier = Modifier.width(SettingsHorizontalPaddingItem.dp))
                Text(stringResource(entry.title))
                AnimatedVisibility(visible = showMore && userSetTheme == Theme.SYSTEM_DEFAULT) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        systemThemeOptions.forEach { (label, preferenceKey, selectedTheme) ->
                            // To make sure the label and radio button will take the same size
                            Box(contentAlignment = Alignment.TopCenter) {
                                val isDark = entry.isDark == true
                                val disabledRadio =
                                    !isDark.xor(preferenceKey == PREF_SYSTEM_LIGHT_THEME)
                                RadioButton(
                                    selected = selectedTheme == entry,
                                    enabled = !disabledRadio,
                                    onClick = {
                                        context.preferences.edit {
                                            putString(preferenceKey, entry.key)
                                        }
                                    },
                                    modifier = if (disabledRadio || entry == Theme.SYSTEM_DEFAULT) invisible else Modifier,
                                )
                                Text(
                                    stringResource(label),
                                    modifier = if (entry == Theme.SYSTEM_DEFAULT) Modifier else invisible,
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = showMore && anyThemeHasGradient,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            val isGradient by isGradient.collectAsState()
            SwitchWithLabel(
                label = stringResource(R.string.color_gradient),
                checked = isGradient,
            ) { context.preferences.edit { putBoolean(PREF_THEME_GRADIENT, !isGradient) } }
        }
        AnimatedVisibility(
            visible = showMore && anyThemeIsDynamicColors,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            val isRedHolidays by isRedHolidays.collectAsState()
            SwitchWithLabel(
                label = stringResource(R.string.holidays_in_red),
                checked = isRedHolidays,
            ) { context.preferences.edit { putBoolean(PREF_RED_HOLIDAYS, !isRedHolidays) } }
        }
        val language by language.collectAsState()
        AnimatedVisibility(
            visible = showMore && BuildConfig.DEVELOPMENT && language.isArabicScript,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            val isVazirEnabled by isVazirEnabled.collectAsState()
            SwitchWithLabel(
                label = "وزیر",
                checked = isVazirEnabled,
            ) { context.preferences.edit { putBoolean(PREF_VAZIR_ENABLED, !isVazirEnabled) } }
        }
    }
}
