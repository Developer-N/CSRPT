package ir.namoo.quran.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.utils.formatNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadQuranDBScreen(
    download: () -> Unit,
    viewModel: QuranDownloadViewModel
) {
    val p by viewModel.progress.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()
    val isUnzipping by viewModel.isUnzipping.collectAsState()
    val message by viewModel.message.collectAsState()
    val progress by animateFloatAsState(targetValue = p, label = "progress")
    val totalSize by viewModel.totalSize.collectAsState()

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.download_quran_db)
                )
            }, colors = appTopAppBarColors()
        )
    }) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            shape = materialCornerExtraLargeTop()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = stringResource(id = R.string.quran_database_not_exist),
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Button(
                            modifier = Modifier.padding(4.dp),
                            onClick = {
                                viewModel.clearErrorMessage()
                                download()
                            },
                            enabled = !isDownloading
                        ) {
                            Text(
                                modifier = Modifier.padding(4.dp),
                                text = stringResource(id = R.string.download),
                                fontSize = 16.sp
                            )
                            Icon(
                                modifier = Modifier.padding(4.dp),
                                imageVector = Icons.Filled.CloudDownload,
                                contentDescription = stringResource(id = R.string.download)
                            )
                        }
                        AnimatedVisibility(visible = isDownloading) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (isUnzipping || progress == 0f) LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                        .height(10.dp),
                                    strokeCap = StrokeCap.Round
                                )
                                else {
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(4.dp)
                                            .height(10.dp),
                                        strokeCap = StrokeCap.Round
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Text(
                                            text = formatNumber("% ${(progress * 100).toInt()}"),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = formatNumber("${totalSize / 1024 / 1024} مگابایت"),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                        AnimatedVisibility(visible = isUnzipping) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                                    .height(10.dp),
                                strokeCap = StrokeCap.Round
                            )
                        }
                        AnimatedVisibility(visible = message.isNotBlank()) {
                            Text(
                                text = message,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
