package ir.namoo.religiousprayers.ui.settings.athan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AthanDownloadComponent(
    type: Int, onDismiss: () -> Unit, viewModel: AthanDownloadDialogViewModel = koinViewModel()
) {
    val context = LocalContext.current
    viewModel.loadData(type)
    val isLoading by viewModel.isLoading.collectAsState()
    val athanList by viewModel.athansList.collectAsState()
    val athanState by viewModel.athanState.collectAsState()

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
        if (athanList.isNotEmpty() && athanState.isNotEmpty()) {
            viewModel.checkAthansState(context)
            LazyColumn {
                itemsIndexed(
                    items = athanList.zip(athanState),
                    key = { _, athan -> athan.first.id }) { index, pair ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp)
                            .animateItemPlacement()
                            .animateContentSize(
                                spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                            .padding(2.dp)
                    ) {
                        val progress by animateFloatAsState(
                            targetValue = pair.second.progress,
                            label = "progress"
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                                    .weight(1f),
                                text = formatNumber(index + 1),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .weight(5f),
                                text = formatNumber(pair.first.name)
                            )
                            AnimatedVisibility(
                                visible = !pair.second.isDownloading,
                                enter = expandHorizontally(),
                                exit = shrinkHorizontally()
                            ) {
                                IconButton(
                                    modifier = Modifier.weight(2f),
                                    onClick = { viewModel.download(context, pair) },
                                    enabled = !pair.second.isDownloaded,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (pair.second.isDownloaded) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                                        contentDescription = ""
                                    )
                                }
                            }
                        }
                        AnimatedVisibility(visible = pair.second.isLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                                    .height(6.dp),
                                strokeCap = StrokeCap.Round
                            )
                        }
                        AnimatedVisibility(visible = pair.second.isDownloading) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Text(text = formatNumber((progress * 100).toInt()) + "%")
                                    val size = (pair.second.totalSize / 1024 / 1024).toInt()
                                    Text(text = formatNumber(if (size == 0) "1" else "$size") + "MB")
                                }
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                        .height(6.dp),
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
