package ir.namoo.quran.download

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.quran.settings.QariSelector
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.cardColor
import ir.namoo.commons.utils.colorAppBar
import ir.namoo.commons.utils.colorOnAppBar
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
                fontFamily = FontFamily(appFont),
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
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorAppBar,
            titleContentColor = colorOnAppBar,
            navigationIconContentColor = colorOnAppBar,
            actionIconContentColor = colorOnAppBar
        )
        )
    }) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding)
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
                elevation = CardDefaults.elevatedCardElevation()
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
                        fontFamily = FontFamily(appFont),
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
                        QuranDownloadItem(
                            modifier = Modifier,
                            title = chapter.nameArabic,
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
                            onDelete = {
                                MaterialAlertDialogBuilder(context).apply {
                                    setTitle(R.string.warning)
                                    setMessage(R.string.delete_quran_audio_alert_message)
                                    setPositiveButton(R.string.yes) { _, _ ->
                                        viewModel.deleteFiles(context, chapter.sura)
                                    }
                                    setNegativeButton(R.string.no) { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    show()
                                }
                            },
                            onCancel = { viewModel.cancelDownload(chapter.sura) }
                        )
                    }
                }
        }
    }
}
