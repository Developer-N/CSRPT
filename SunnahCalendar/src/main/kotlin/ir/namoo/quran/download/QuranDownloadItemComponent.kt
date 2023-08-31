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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.cardColor
import ir.namoo.commons.utils.iconColor

@Composable
fun QuranDownloadItem(
    modifier: Modifier,
    title: String,
    state: QuranDownloadItemState,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    val progress by animateFloatAsState(targetValue = state.progress, label = "")
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(text = title, fontFamily = FontFamily(appFont))
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(
                visible = state.isDownloaded,
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                IconButton(
                    onClick = { onDelete() }
                ) {
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
                IconButton(onClick = { onDownload() }) {
                    Icon(
                        imageVector = if (state.isDownloaded) Icons.Filled.FileDownloadDone else Icons.Filled.CloudDownload,
                        contentDescription = stringResource(id = R.string.download),
                        tint = iconColor
                    )
                }
            }
            AnimatedVisibility(
                visible = state.isDownloading,
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                IconButton(onClick = { onCancel() }) {
                    Icon(
                        imageVector = Icons.Filled.StopCircle,
                        contentDescription = stringResource(id = R.string.download),
                        tint = iconColor
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
                    strokeWidth = 2.dp
                )
            }
            AnimatedVisibility(
                visible = state.isDownloading,
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        progress = progress,
                        trackColor = MaterialTheme.colorScheme.scrim,
                        strokeCap = StrokeCap.Round,
                        strokeWidth = 2.dp
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = formatNumber((progress * 100).toInt()) + "%",
                            fontFamily = FontFamily(appFont),
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = formatNumber((state.totalSize / 1024 / 1024).toInt()) + "MB",
                            fontFamily = FontFamily(appFont),
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, locale = "fa")
@Composable
fun PrevQDI() {
    Mdc3Theme {
        QuranDownloadItem(
            modifier = Modifier,
            title = "الفاتحه",
            state = QuranDownloadItemState(1).apply {
                progress = 0.5f
                isChecking = false
                isDownloading = true
                totalSize = 100 * 1024 * 1024
            },
            onDownload = {},
            onDelete = {}
        ) {}
    }
}
