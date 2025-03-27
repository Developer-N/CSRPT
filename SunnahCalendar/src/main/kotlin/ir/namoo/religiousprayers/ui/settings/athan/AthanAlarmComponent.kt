package ir.namoo.religiousprayers.ui.settings.athan

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.io.File
import java.io.FileOutputStream

@Composable
fun AthanAlarmComponent(
    modifier: Modifier = Modifier, type: Int, athanDB: AthanDB = koinInject()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val filePicker = rememberLauncherForActivityResult(contract = PickSound()) { intent ->
        intent ?: return@rememberLauncherForActivityResult
        coroutineScope.launch {
            runCatching {
                if (intent.extras?.getInt(
                        FILE_PICKER_REQUEST_CODE, -1
                    ) == REQ_CODE_PICK_ALARM_FILE || intent.extras?.getInt(
                        FILE_PICKER_REQUEST_CODE, -1
                    ) == REQ_CODE_PICK_ATHAN_FILE
                ) {
                    val athanFileUri = intent.data ?: return@launch
                    val inputFile = context.contentResolver.openInputStream(athanFileUri)
                        ?: return@launch
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
                        name = getFileNameFromLink(outputFile.absolutePath),
                        link = "local/${getFileNameFromLink(outputFile.absolutePath)}",
                        type = when (intent.getIntExtra(FILE_PICKER_REQUEST_CODE, -1)) {
                            REQ_CODE_PICK_ATHAN_FILE -> 1
                            REQ_CODE_PICK_ALARM_FILE -> 2
                            else -> 2
                        },
                        fileName = outputFile.name
                    )
                    Log.e("TAG", "AthanAlarmComponent: $athan")
                    athanDB.athanDAO().insert(athan)
                }
            }.onFailure(logException)
        }
    }
    var networkErrorDialog by remember { mutableStateOf(false) }
    var showAthanDownloadDialog by remember { mutableStateOf(false) }
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ElevatedButton(
            onClick = {
                if (!isNetworkConnected(context)) networkErrorDialog = true
                else showAthanDownloadDialog = true
            }, enabled = !showAthanDownloadDialog
        ) {
            Text(
                text = stringResource(id = if (type == 1) R.string.add_online_athan else R.string.add_online_alarm),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = stringResource(id = if (type == 1) R.string.add_online_athan else R.string.add_online_alarm)
            )
        }

        ElevatedButton(onClick = { filePicker.launch(type) }) {
            Text(
                text = stringResource(id = if (type == 1) R.string.add_local_athan else R.string.add_local_alarm),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = if (type == 1) R.string.add_local_athan else R.string.add_local_alarm)
            )
        }

        AnimatedVisibility(visible = networkErrorDialog) {
            AlertDialog(onDismissRequest = { networkErrorDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(R.string.error)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { networkErrorDialog = false }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                },
                title = { Text(text = stringResource(id = R.string.network_error_title)) },
                text = {
                    Text(
                        text = stringResource(id = R.string.network_error_message),
                        fontWeight = FontWeight.SemiBold
                    )
                })
        }
        AnimatedVisibility(visible = showAthanDownloadDialog) {
            AthanDownloadComponent(type = type, onDismiss = { showAthanDownloadDialog = false })
        }
    }
}
