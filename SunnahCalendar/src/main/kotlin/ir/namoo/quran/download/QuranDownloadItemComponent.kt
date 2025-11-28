package ir.namoo.quran.download

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.numeral

@Composable
fun QuranDownloadItem(
    modifier: Modifier,
    title: String,
    state: QuranDownloadItemState,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    val numeral by numeral.collectAsState()
    val progress by animateFloatAsState(targetValue = state.progress, label = "progress")
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp),
                text = numeral.format(title),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(
                visible = state.isDownloaded,
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            AnimatedVisibility(
                visible = !state.isDownloading && !state.isChecking,
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                IconButton(
                    onClick = onDownload,
                    enabled = !state.isDownloaded,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = if (state.isDownloaded) Icons.Filled.FileDownloadDone else Icons.Filled.CloudDownload,
                        contentDescription = stringResource(id = R.string.download)
                    )
                }
            }
            AnimatedVisibility(
                visible = state.isDownloading,
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                IconButton(
                    onClick = onCancel,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.StopCircle,
                        contentDescription = stringResource(id = R.string.download)
                    )
                }
            }
            AnimatedVisibility(
                visible = state.isChecking,
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeCap = StrokeCap.Round,
                    strokeWidth = 4.dp
                )
            }
            AnimatedVisibility(
                visible = state.isDownloading,
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(34.dp),
                        progress = { progress },
                        strokeCap = StrokeCap.Round,
                        strokeWidth = 2.dp
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            lineHeight = 8.sp,
                            text = numeral.format((progress * 100).toInt()) + "%",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val text =
                            if (state.totalSize / 1024 / 1024 < 1)
                                numeral.format((state.totalSize / 1024).toInt()) + "KB"
                            else numeral.format((state.totalSize / 1024 / 1024).toInt()) + "MB"
                        Text(
                            lineHeight = 8.sp,
                            text = text,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
