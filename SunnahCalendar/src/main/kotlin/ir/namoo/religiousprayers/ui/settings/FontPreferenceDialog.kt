package ir.namoo.religiousprayers.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.utils.preferences
import ir.namoo.commons.PREF_APP_FONT
import ir.namoo.commons.SYSTEM_DEFAULT_FONT

@Composable
fun FontPreferencesDialog(closeDialog: () -> Unit) {
    AlertDialog(onDismissRequest = { closeDialog() }, title = {
        Text(stringResource(id = R.string.select_font))
    }, confirmButton = {}, dismissButton = {
        TextButton(onClick = { closeDialog() }) {
            Text(stringResource(R.string.cancel))
        }
    }, text = {
        val context = LocalContext.current
        val fontNames = listOf(
            context.getString(R.string.theme_default),
            context.getString(R.string.vazir),
            context.getString(R.string.vazir_light)
        )
        val fontValues = listOf("", SYSTEM_DEFAULT_FONT, "fonts/Vazirmatn-Light.ttf")
        val fontInPref = context.preferences.getString(PREF_APP_FONT, SYSTEM_DEFAULT_FONT)
        val currentFont = fontNames[fontValues.indexOf(fontInPref)]
        fun onClick(item: String) {
            context.preferences.edit {
                putString(PREF_APP_FONT, fontValues[fontNames.indexOf(item)])
            }
            closeDialog()
        }
        LazyColumn {
            items(fontNames) { item ->
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable { onClick(item) }) {
                    RadioButton(selected = item == currentFont, onClick = { onClick(item) })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item)
                }
            }
        }
    })
}


@Preview(locale = "fa")
@Composable
private fun LanguagePreferenceDialogPreview() = AppTheme { FontPreferencesDialog {} }
