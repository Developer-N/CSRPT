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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.cardColor
import ir.namoo.commons.utils.colorAppBar
import ir.namoo.commons.utils.colorOnAppBar

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
                    text = stringResource(id = R.string.download_quran_db),
                    fontFamily = FontFamily(appFont)
                )
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorAppBar,
                titleContentColor = colorOnAppBar,
                navigationIconContentColor = colorOnAppBar,
                actionIconContentColor = colorOnAppBar
            )
        )
    }) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.elevatedCardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.quran_database_not_exist),
                        fontFamily = FontFamily(appFont),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
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
                            fontFamily = FontFamily(appFont),
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
                                strokeCap = StrokeCap.Round,
                                trackColor = MaterialTheme.colorScheme.scrim
                            )
                            else {
                                LinearProgressIndicator(
                                    progress = progress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                        .height(10.dp),
                                    strokeCap = StrokeCap.Round,
                                    trackColor = MaterialTheme.colorScheme.scrim
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Text(
                                        text = formatNumber("% ${(progress * 100).toInt()}"),
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily(appFont)
                                    )
                                    Text(
                                        text = formatNumber("${totalSize / 1024 / 1024} مگابایت"),
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily(appFont)
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
                            strokeCap = StrokeCap.Round,
                            trackColor = MaterialTheme.colorScheme.scrim
                        )
                    }
                    AnimatedVisibility(visible = message.isNotBlank()) {
                        Text(
                            text = message,
                            fontFamily = FontFamily(appFont),
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
