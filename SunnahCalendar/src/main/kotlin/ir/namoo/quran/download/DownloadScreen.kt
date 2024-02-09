package ir.namoo.quran.download

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.quran.settings.QariSelector
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    drawerState: DrawerState, viewModel: DownloadQuranAudioViewModel = koinViewModel()
) {
    val context = LocalContext.current
    viewModel.loadData(context)
    val scope = rememberCoroutineScope()
    val qariList by viewModel.qariList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedQari by viewModel.selectedQari.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val selectedStates by viewModel.selectedStateList.collectAsState()

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                text = stringResource(id = R.string.download_audios),
                fontSize = 16.sp
            )
        }, navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    drawerState.apply {
                        if (isOpen) close() else open()
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Menu, contentDescription = "Menu"
                )
            }
        }, colors = appTopAppBarColors()
        )
    }) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            shape = materialCornerExtraLargeTop()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {

                AnimatedVisibility(visible = isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .width(4.dp),
                        strokeCap = StrokeCap.Round
                    )
                }
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = stringResource(id = R.string.select_quran_audio_for_download),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        QariSelector(title = qariList.find { it.folderName == selectedQari }?.name
                            ?: "-",
                            enable = !isLoading,
                            list = qariList,
                            selected = selectedQari,
                            onSelectChange = { viewModel.updateSelectedQari(context, it) })
                    }
                }
                //################################################
                if (chapters.isNotEmpty() && selectedStates.isNotEmpty())
                    LazyColumn(state = rememberLazyListState()) {
                        items(items = chapters, key = { it.sura }) { chapter ->
                            var showDeleteDialog by remember { mutableStateOf(false) }
                            QuranDownloadItem(
                                modifier = Modifier,
                                title = "${chapter.sura}: ${chapter.nameArabic}",
                                state = selectedStates.find { it.sura == chapter.sura }
                                    ?: QuranDownloadItemState(1),
                                onDownload = {
                                    if (isNetworkConnected(context))
                                        viewModel.downloadFiles(context, chapter.sura)
                                    else
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.network_error_message),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                },
                                onDelete = { showDeleteDialog = true },
                                onCancel = { viewModel.cancelDownload(chapter.sura) }
                            )
                            if (showDeleteDialog) {
                                AlertDialog(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = ""
                                        )
                                    },
                                    title = { Text(text = stringResource(id = R.string.warning)) },
                                    text = { Text(text = stringResource(id = R.string.delete_quran_audio_alert_message)) },
                                    onDismissRequest = { showDeleteDialog = false },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            showDeleteDialog = false
                                            viewModel.deleteFiles(context, chapter.sura)
                                        }) {
                                            Text(text = stringResource(id = R.string.yes))
                                        }
                                    }, dismissButton = {
                                        TextButton(onClick = { showDeleteDialog = false }) {
                                            Text(text = stringResource(id = R.string.no))
                                        }
                                    })
                            }
                        }
                    }
            }
        }
    }
}
