package ir.namoo.religiousprayers.ui.settings.athan

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.logException
import ir.namoo.commons.FILE_PICKER_REQUEST_CODE
import ir.namoo.commons.REQ_CODE_PICK_ALARM_FILE
import ir.namoo.commons.REQ_CODE_PICK_ATHAN_FILE
import ir.namoo.commons.model.Athan
import ir.namoo.commons.model.AthanDB
import ir.namoo.commons.utils.getAthansDirectoryPath
import ir.namoo.commons.utils.getFileNameFromLink
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.religiousprayers.ui.settings.PickSound
import org.koin.compose.koinInject
import java.io.File
import java.io.FileOutputStream

@Composable
fun AthanAlarmComponent(
    modifier: Modifier = Modifier, type: Int, athanDB: AthanDB = koinInject()
) {
    val context = LocalContext.current
    val filePicker = rememberLauncherForActivityResult(contract = PickSound()) { intent ->
        intent ?: return@rememberLauncherForActivityResult
        runCatching {
            if (intent.extras?.getInt(
                    FILE_PICKER_REQUEST_CODE, -1
                ) == REQ_CODE_PICK_ALARM_FILE || intent.extras?.getInt(
                    FILE_PICKER_REQUEST_CODE, -1
                ) == REQ_CODE_PICK_ATHAN_FILE
            ) {
                val athanFileUri = intent.data ?: return@rememberLauncherForActivityResult
                val inputFile = context.contentResolver.openInputStream(athanFileUri)
                    ?: return@rememberLauncherForActivityResult
                var outputFile = File(getAthansDirectoryPath(context) + "/1." + "mp3")
                var index = 2
                while (outputFile.exists()) {
                    outputFile = File(getAthansDirectoryPath(context) + "/${index++}." + "mp3")
                }
                val outputStream = FileOutputStream(outputFile)
                inputFile.copyTo(outputStream, DEFAULT_BUFFER_SIZE)
                outputStream.close()
                inputFile.close()
                val athan = Athan(
                    getFileNameFromLink(outputFile.absolutePath),
                    "local/${getFileNameFromLink(outputFile.absolutePath)}",
                    when (intent.getIntExtra(FILE_PICKER_REQUEST_CODE, -1)) {
                        REQ_CODE_PICK_ATHAN_FILE -> 1
                        REQ_CODE_PICK_ALARM_FILE -> 2
                        else -> 2
                    }
                )
                athanDB.athanDAO().insert(athan)
            }
        }.onFailure(logException)
    }
    var showNetoworkError by remember { mutableStateOf(false) }
    var showAthanDownloadDialog by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ElevatedAssistChip(onClick = {
            if (!isNetworkConnected(context)) showNetoworkError = true
            else showAthanDownloadDialog = true
        }, label = {
            Text(text = stringResource(id = if (type == 1) R.string.add_online_athan else R.string.add_online_alarm))
        }, trailingIcon = {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = stringResource(id = if (type == 1) R.string.add_online_athan else R.string.add_online_alarm)
            )
        }, enabled = !showAthanDownloadDialog)

        ElevatedAssistChip(onClick = { filePicker.launch(type) }, label = {
            Text(text = stringResource(id = if (type == 1) R.string.add_local_athan else R.string.add_local_alarm))
        }, trailingIcon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = if (type == 1) R.string.add_local_athan else R.string.add_local_alarm)
            )
        })
        AnimatedVisibility(visible = showNetoworkError) {
            AlertDialog(onDismissRequest = { showNetoworkError = false },
                confirmButton = {
                    TextButton(onClick = { showNetoworkError = false }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                },
                title = { Text(text = stringResource(id = R.string.network_error_title)) },
                text = { Text(text = stringResource(id = R.string.network_error_message)) })
        }
        AnimatedVisibility(visible = showAthanDownloadDialog) {
            AthanDownloadComponent(type = type, onDismiss = { showAthanDownloadDialog = false })
        }
    }
}
