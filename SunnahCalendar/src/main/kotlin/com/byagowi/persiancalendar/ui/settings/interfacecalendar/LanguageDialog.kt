package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.AFGHANISTAN_TIMEZONE_ID
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.saveLanguage
import java.util.TimeZone

@Composable
fun LanguageDialog(onDismissRequest: () -> Unit) {
    AppDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.language)) },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        }
    ) {
        val currentLanguage by language.collectAsState()
        val languages = Language.entries.let { languages ->
            if (TimeZone.getDefault().id in listOf(IRAN_TIMEZONE_ID, AFGHANISTAN_TIMEZONE_ID))
                languages else languages.sortedBy { it.code }
        }.let { languages ->
            // Put the current language on top as one might don't know more exist above the current selection
            listOf(currentLanguage) + languages.filter { it != currentLanguage }
        }

        val context = LocalContext.current
        languages.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SettingsItemHeight.dp)
                    .clickable {
                        if (item != currentLanguage) context.preferences.saveLanguage(item)
                        onDismissRequest()
                    }
                    .padding(horizontal = SettingsHorizontalPaddingItem.dp)
            ) {
                RadioButton(selected = item == currentLanguage, onClick = null)
                Spacer(modifier = Modifier.width(SettingsHorizontalPaddingItem.dp))
                Text(item.nativeName)
            }
        }
    }
}

@Preview
@Composable
private fun LanguagePreferenceDialogPreview() = LanguageDialog {}
