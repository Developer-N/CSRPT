package ir.namoo.religiousprayers.ui.settings.athan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import org.koin.androidx.compose.koinViewModel

@Composable
fun AthanDownloadComponent(
    type: Int, onDismiss: () -> Unit, viewModel: AthanDownloadDialogViewModel = koinViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = type) {
        viewModel.loadData(type, context)
    }
    val isLoading by viewModel.isLoading.collectAsState()
    val athanState = viewModel.athanState

    AlertDialog(onDismissRequest = {}, confirmButton = {}, icon = {
        Box(contentAlignment = Alignment.Center) {
            AnimatedVisibility(visible = isLoading) {
                CircularProgressIndicator(strokeCap = StrokeCap.Round, strokeWidth = 4.dp)
            }
            IconButton(
                onClick = { onDismiss() },
                enabled = !isLoading,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.close)
                )
            }
        }
    }, text = {
        if (athanState.isNotEmpty()) {
            LazyColumn {
                itemsIndexed(
                    items = athanState,
                    key = { _, athan -> athan.id }) { index, athan ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp)
                            .animateItem()
                            .animateContentSize(),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        val progress by animateFloatAsState(
                            targetValue = athan.progress,
                            label = "progress"
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                                    .weight(4f),
                                text = formatNumber("${index + 1}: ${athan.name}")
                            )
                            AnimatedVisibility(
                                modifier = Modifier.weight(1f),
                                visible = !athan.isDownloading && !athan.isLoading,
                                enter = expandHorizontally(),
                                exit = shrinkHorizontally()
                            ) {
                                IconButton(
                                    onClick = { viewModel.download(context, athan) },
                                    enabled = !athan.isDownloaded && !athan.isLoading,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (athan.isDownloaded) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                                        contentDescription = ""
                                    )
                                }
                            }
                            AnimatedVisibility(
                                modifier = Modifier.weight(1f),
                                visible = athan.isLoading,
                                enter = expandHorizontally(),
                                exit = shrinkHorizontally()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                        AnimatedVisibility(visible = athan.isDownloading) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp, horizontal = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Text(text = formatNumber((progress * 100).toInt()) + "%")
                                    var size = (athan.totalSize / 1024 / 1024).toInt()
                                    var isKB = false
                                    if (size == 0) {
                                        size = (athan.totalSize / 1024).toInt()
                                        isKB = true
                                    }
                                    Text(text = formatNumber("$size" + if (isKB) "KB" else "MB"))
                                }
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp, horizontal = 8.dp),
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }
    })
}
