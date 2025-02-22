package ir.namoo.religiousprayers.ui.settings.athan

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R

@Composable
fun ClearAthansComponent(onClear: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), contentAlignment = Alignment.Center
    ) {
        ElevatedButton(
            onClick = { showDialog = true },
            colors = ButtonDefaults.elevatedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(
                text = stringResource(id = R.string.clear_added_athans),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(id = R.string.clear_added_athans)
            )
        }

        AnimatedVisibility(visible = showDialog) {
            AlertDialog(onDismissRequest = { showDialog = false }, confirmButton = {
                ElevatedButton(
                    onClick = {
                        showDialog = false
                        onClear()
                    },
                    colors = ButtonDefaults.elevatedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = stringResource(id = R.string.yes), fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }, dismissButton = {
                ElevatedButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(id = R.string.no), fontWeight = FontWeight.SemiBold)
                }
            }, title = { Text(text = stringResource(id = R.string.warning)) }, text = {
                Text(
                    text = stringResource(id = R.string.all_added_athans_will_cleared),
                    fontWeight = FontWeight.SemiBold
                )
            }, icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(id = R.string.warning)
                )
            })
        }

    }
}

@Preview(name = "day", locale = "fa", showBackground = true)
@Preview(
    name = "night", locale = "fa", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PrevClearDialog() {
    ClearAthansComponent {}
}
