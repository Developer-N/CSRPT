package ir.namoo.quran.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.numeral
import ir.namoo.quran.mushaf.MushafFileDownloaderViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PageTypeComponent(
    viewModel: SettingViewModel,
    reload: () -> Unit,
    downloaderViewModel: MushafFileDownloaderViewModel = koinViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        downloaderViewModel.init(context)
    }
    val pageType by viewModel.pageType.collectAsState()
    val hideToolbarOnScroll by viewModel.hideToolbarOnScroll.collectAsState()

    val items = listOf(stringResource(R.string.list), stringResource(R.string.mushaf))
    val isDownloading by downloaderViewModel.isDownloading.collectAsState()
    val isUnzipping by downloaderViewModel.isUnzipping.collectAsState()
    val isDownloaded by downloaderViewModel.isDownloaded.collectAsState()
    val error by downloaderViewModel.error.collectAsState()
    val totalSize by downloaderViewModel.totalSize.collectAsState()
    val p by downloaderViewModel.progress.collectAsState()
    val bytes by downloaderViewModel.downloadedBytes.collectAsState()
    val downloaded by animateFloatAsState(targetValue = bytes, visibilityThreshold = 0.0000001f)
    val progress by animateFloatAsState(targetValue = p)
    val numeral by numeral.collectAsState()
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        MyBtnGroup(
            modifier = Modifier.padding(4.dp),
            title = stringResource(R.string.quran_page_type),
            items = items,
            checkedItem = items[pageType],
            onCheckChanged = { viewModel.updatePageType(items.indexOf(it), reload) })
        AnimatedVisibility(visible = pageType == 1 && !isDownloaded) {
            ElevatedCard(
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .align(Alignment.CenterHorizontally),
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                    text = stringResource(R.string.download_mushaf_msg)
                )
                ElevatedButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
                    onClick = { downloaderViewModel.download(context) },
                    enabled = !isDownloading && !isUnzipping && !isDownloaded
                ) {
                    Text(text = stringResource(R.string.download), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
                }

                AnimatedVisibility(visible = isUnzipping) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 8.dp),
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 8.dp),
                            text = stringResource(R.string.download_completed_unzip),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                AnimatedVisibility(visible = isDownloading && !isUnzipping) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 8.dp),
                            strokeCap = StrokeCap.Round,
                            progress = { progress })
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = numeral.format(
                                    stringResource(
                                        R.string.megabyte, totalSize / 1024 / 1024
                                    )
                                )
                            )
                            Text(
                                text = numeral.format(
                                    stringResource(
                                        R.string.megabyte,
                                        downloaded
                                    )
                                )
                            )
                        }
                    }
                }
                AnimatedVisibility(visible = error.isNotEmpty()) {
                    Text(
                        text = error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        MySwitchBox(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            checked = hideToolbarOnScroll,
            label = stringResource(R.string.hide_toolbar_on_scroll),
            onCheckChanged = { isActive -> viewModel.updateHideToolbarOnScroll(isActive) }
        )
    }
}
