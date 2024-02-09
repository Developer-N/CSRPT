package ir.namoo.quran.sura

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.commons.APP_LINK
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.quran.player.isQuranDownloaded
import ir.namoo.quran.player.isTranslateDownloaded
import ir.namoo.quran.utils.KeepScreenOn
import ir.namoo.quran.utils.initQuranUtils
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import ir.namoo.religiousprayers.ui.shared.SearchAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.lang.Thread.sleep
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SuraScreen(
    startSura: Int,
    aya: Int,
    play: Boolean = false,
    viewModel: SuraViewModel = koinViewModel(),
    drawerState: DrawerState,
    navController: NavController
) {
    val context = LocalContext.current
    initQuranUtils(context)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    viewModel.loadDate(startSura, navController)
    val isLoading by viewModel.isLoading.collectAsState()
    val quranList by viewModel.quranList.collectAsState()
    val enabledTranslates by viewModel.enabledTranslates.collectAsState()
    val chapter by viewModel.chapter.collectAsState()
    val query by viewModel.query.collectAsState()
    val playingAya by viewModel.playingAya.collectAsState()
    val sura by viewModel.sura.collectAsState()
    val isSearchBarOpen by viewModel.isSearchBarOpen.collectAsState()
    var showQuranFilesDialog by remember { mutableStateOf(false) }
    var showTranslateFilesDialog by remember { mutableStateOf(false) }

    val infiniteAnimation = rememberInfiniteTransition(label = "bar animation repeat")
    val animations = mutableListOf<State<Float>>()
    val random = remember { Random(System.currentTimeMillis()) }
    repeat(20) {
        val durationMillis = random.nextInt(500, 2000)
        animations += infiniteAnimation.animateFloat(
            initialValue = 0f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis),
                repeatMode = RepeatMode.Reverse,
            ), label = "bar animation $it"
        )
    }

    Scaffold(topBar = {
        AnimatedVisibility(
            visible = !isSearchBarOpen, enter = expandVertically(), exit = shrinkVertically()
        ) {
            DefaultSuraAppBar(title = chapter?.nameArabic ?: "-",
                aya = chapter?.ayaCount ?: 0,
                onSearchClick = {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                    viewModel.openSearchBar()
                },
                onMenuClick = {
                    scope.launch {
                        drawerState.open()
                    }
                },
                navController = navController,
                sura = sura
            )
        }
        AnimatedVisibility(
            visible = isSearchBarOpen, enter = expandVertically(), exit = shrinkVertically()
        ) {
            SearchAppBar(query = query,
                updateQuery = { viewModel.onQuery(it) },
                closeSearchBar = { viewModel.closeSearchBar() })
        }
    }) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            shape = materialCornerExtraLargeTop()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                KeepScreenOn(timeoutMillis = 1000 * 60 * 30)
                LaunchedEffect(key1 = aya) {
                    withContext(Dispatchers.IO) {
                        sleep(1000)
                        withContext(Dispatchers.Main) {
                            if (aya > 1) listState.scrollToItem(aya - 1)
                        }
                    }
                }
                LaunchedEffect(key1 = playingAya) {
                    if (playingAya > 1) listState.animateScrollToItem(playingAya - 1, 0)
                }
                LaunchedEffect(key1 = query) {
                    if (query.isNotEmpty() && query.isDigitsOnly() && query.toInt() > 1 && query.toInt() < quranList.size + 1) listState.animateScrollToItem(
                        query.toInt() - 1, 0
                    )
                }
                AnimatedVisibility(visible = isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .height(4.dp),
                        strokeCap = StrokeCap.Round
                    )
                }
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_PAUSE -> {
                                viewModel.saveLastVisited()
                                viewModel.updateSureViewSate(false)
                            }

                            Lifecycle.Event.ON_CREATE -> {
                                viewModel.setupPlayer(context)
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        sleep(2000)
                                        withContext(Dispatchers.Main) {
                                            if (play && isQuranDownloaded(
                                                    context, sura
                                                ) && isTranslateDownloaded(
                                                    context, sura
                                                ) && playingAya == 0
                                            ) viewModel.onPlay(context, sura, 1)
                                            else if (play && playingAya == 0) Toast.makeText(
                                                context,
                                                context.getString(R.string.audio_files_error),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }

                            Lifecycle.Event.ON_START -> {}

                            Lifecycle.Event.ON_RESUME -> {
                                viewModel.updateSureViewSate(true)
                            }

                            Lifecycle.Event.ON_STOP -> {}
                            Lifecycle.Event.ON_DESTROY -> {}
                            Lifecycle.Event.ON_ANY -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }
                if (quranList.isNotEmpty() && enabledTranslates.isNotEmpty()) LazyColumn(state = listState) {
                    viewModel.updateLastVisited(listState.firstVisibleItemIndex + 1)
                    items(items = quranList, key = { it.id }) { quran ->
                        AyaItem(modifier = Modifier.animateItemPlacement(),
                            quran = quran,
                            translates = try {
                                val index = enabledTranslates.indexOf(enabledTranslates.find {
                                    it.any { t -> t.ayaID == quran.verseID }
                                })
                                enabledTranslates[index]
                            } catch (ex: Exception) {
                                emptyList()
                            },
                            isPlaying = quran.verseID == playingAya,
                            animations= animations,
                            onCopyClick = {
                                var text = it
                                text =
                                    context.getString(R.string.sura) + " " + chapter?.nameArabic + "\n\n" + text + APP_LINK
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(
                                    context, context.getString(R.string.copied), Toast.LENGTH_SHORT
                                ).show()
                            },
                            onShareClick = {
                                var text = it
                                text =
                                    context.getString(R.string.sura) + " " + chapter?.nameArabic + "\n\n" + text + APP_LINK
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        sendIntent, context.getString(R.string.share)
                                    )
                                )
                            },
                            onBookmarkClick = { viewModel.updateBookMark(it) },
                            onNoteUpdate = { viewModel.updateNote(quran, it) },
                            onBtnPlayClick = {
                                if (!isQuranDownloaded(context = context, quran.surahID)) {
                                    showQuranFilesDialog = true
                                } else if (!isTranslateDownloaded(
                                        context = context, quran.surahID
                                    )
                                ) {
                                    showTranslateFilesDialog = true
                                } else viewModel.onPlay(
                                    context = context, sura = quran.surahID, aya = quran.verseID
                                )

                            })
                    }
                }
                else if (query.isNotEmpty()) NothingFoundUIElement()
            }
        }
        if (showQuranFilesDialog) {
            AlertDialog(title = { Text(text = stringResource(id = R.string.error)) },
                text = { Text(text = stringResource(id = R.string.audio_files_error)) },
                onDismissRequest = { showQuranFilesDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showQuranFilesDialog = false
                        if (isNetworkConnected(context)) viewModel.downloadQuranFiles(context)
                        else Toast.makeText(
                            context,
                            context.getString(R.string.network_error_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Text(text = stringResource(id = R.string.download))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuranFilesDialog = false }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                })
        }
        if (showTranslateFilesDialog) {
            AlertDialog(title = { Text(text = stringResource(id = R.string.error)) },
                text = { Text(text = stringResource(id = R.string.audio_translate_files_error)) },
                onDismissRequest = { showTranslateFilesDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showTranslateFilesDialog = false
                        if (isNetworkConnected(context)) viewModel.downloadTranslateFiles(context)
                        else Toast.makeText(
                            context,
                            context.getString(R.string.network_error_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Text(text = stringResource(id = R.string.download))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTranslateFilesDialog = false }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                })
        }

        BackHandler(enabled = isSearchBarOpen || drawerState.isOpen) {
            if (drawerState.isOpen) scope.launch {
                drawerState.close()
            }
            else {
                viewModel.closeSearchBar()
                if (query.isNotEmpty()) viewModel.onQuery("")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultSuraAppBar(
    title: String,
    aya: Int,
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit,
    navController: NavController,
    sura: Int
) {
    CenterAlignedTopAppBar(title = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 18.sp)
            Text(
                text = formatNumber(
                    String.format(
                        stringResource(id = R.string.aya_count), aya
                    )
                ), fontSize = 12.sp
            )
        }
    }, colors = appTopAppBarColors(), actions = {
        IconButton(onClick = { onSearchClick() }) {
            Icon(
                imageVector = Icons.Filled.Search, contentDescription = stringResource(
                    id = R.string.search
                )
            )
        }
        IconButton(onClick = {
            navController.navigate("sura/${if (sura == 1) 114 else (sura - 1)}/1") {
                popUpTo("chapters")
            }
        }) {
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "Next")
        }
        IconButton(onClick = {
            navController.navigate("sura/${if (sura == 114) 1 else (sura + 1)}/1") {
                popUpTo("chapters")
            }
        }) {
            Icon(
                modifier = Modifier.rotate(180f),
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Before"
            )
        }
    }, navigationIcon = {
        IconButton(onClick = { onMenuClick() }) {
            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Navigation Menu")
        }
    })
}
