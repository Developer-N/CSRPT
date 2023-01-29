package ir.namoo.religiousprayers.ui.shared

import android.graphics.Typeface
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import com.byagowi.persiancalendar.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NSpinner(options: List<String>, itemClick: (String) -> Unit, typeface: Typeface) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = selectedOptionText,
            onValueChange = { },
            textStyle = TextStyle(fontFamily = FontFamily(typeface)),
            label = {
                Text(
                    text = stringResource(id = R.string.season), fontFamily = FontFamily(typeface)
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = {
            expanded = false
        }) {
            options.forEach { selectionOption ->
                DropdownMenuItem(onClick = {
                    selectedOptionText = selectionOption
                    expanded = false
                    itemClick(selectedOptionText)
                }, text = { Text(text = selectionOption, fontFamily = FontFamily(typeface)) })
            }
        }
    }
}
