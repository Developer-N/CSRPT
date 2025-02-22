package ir.namoo.quran.sura

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.SuraScreen(
    startSura: Int,
    aya: Int,
    animatedContentScope: AnimatedContentScope,
    openDrawer: () -> Unit,
    viewModel: SuraViewModel = koinViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = startSura) {
        viewModel.loadDate(startSura, false)
    }
    LaunchedEffect(key1 = Unit) {
        initQuranUtils(context)
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboard.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val isLoading by viewModel.isLoading.collectAsState()
    val qList = viewModel.quranList
    val resultIDs = viewModel.resultIDs
    val quranList by remember {
        derivedStateOf {
            qList.filter { resultIDs.contains(it.id) }
        }
    }
    val translateList = viewModel.enabledTranslates
    val enabledTranslates = translateList.groupBy { it.verseID }
    val chapter by viewModel.chapter.collectAsState()
    val query by viewModel.query.collectAsState()
    val playingAya by viewModel.playingAya.collectAsState()
    val playingSura by viewModel.playingSura.collectAsState()
    val currentSura by viewModel.currentSura.collectAsState()
    val isSearchBarOpen by viewModel.isSearchBarOpen.collectAsState()
    val scrollToTop by viewModel.scrollToTop.collectAsState()
    var showQuranFilesDialog by remember { mutableStateOf(false) }
    var showTranslateFilesDialog by remember { mutableStateOf(false) }

    val infiniteAnimation = rememberInfiniteTransition(label = "bar animation repeat")
    val animations = remember { mutableStateListOf<State<Float>>() }
    val random = remember { Random(System.currentTimeMillis()) }
    repeat(20) {
        val durationMillis = random.nextInt(500, 2000)
        animations.add(
            infiniteAnimation.animateFloat(
                initialValue = 0f, targetValue = 15f, animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = durationMillis, easing = EaseInBounce),
                    repeatMode = RepeatMode.Reverse,
                ), label = "bar animation $it"
            )
        )
    }

    val isPlaying by viewModel.isPlaying.collectAsState()
    val autoScroll by viewModel.autoScroll.collectAsState()
    val currentMediaItem by viewModel.currentMediaItem.collectAsState()
    val playingSuraName by viewModel.playingSuraName.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val isPlayerStopped by viewModel.isPlayerStoped.collectAsState()
    val bottomPadding by animateFloatAsState(targetValue = if (!isPlayerStopped) 140f else 0f)

    KeepScreenOn()
    LaunchedEffect(key1 = aya) {
        withContext(Dispatchers.IO) {
            sleep(1000)
            withContext(Dispatchers.Main) {
                if (aya > 1) listState.scrollToItem(aya - 1)
            }
        }
    }
    LaunchedEffect(key1 = playingAya) {
        if (!autoScroll) return@LaunchedEffect
        if (isSearchBarOpen) return@LaunchedEffect
        if (currentSura != playingSura) return@LaunchedEffect
        if (playingAya > 1) listState.animateScrollToItem(playingAya - 1, 0)
    }
    LaunchedEffect(key1 = scrollToTop) {
        if (scrollToTop > 0) listState.animateScrollToItem(0, 0)
    }
    LaunchedEffect(key1 = query) {
        if (query.isNotEmpty() && query.isDigitsOnly() && query.toInt() > 1 && query.toInt() < quranList.size + 1) listState.animateScrollToItem(
            query.toInt() - 1, 0
        )
    }

    Scaffold(topBar = {
        AnimatedVisibility(
            visible = !isSearchBarOpen, enter = expandVertically(), exit = shrinkVertically()
        ) {
            DefaultSuraAppBar(
                title = chapter?.nameArabic ?: "-",
                aya = chapter?.ayaCount ?: 0,
                animatedContentScope = animatedContentScope,
                onSearchClick = {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                    viewModel.openSearchBar()
                },
                openDrawer = openDrawer,
                nextSura = viewModel::goToNextSura,
                prevSura = viewModel::goToPrevSura
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
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = bottomPadding.dp)
                        .align(Alignment.TopCenter)
                ) {
                    AnimatedVisibility(visible = isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            strokeCap = StrokeCap.Round
                        )
                    }
                    if (quranList.isNotEmpty()) LazyColumn(state = listState) {
                        viewModel.updateLastVisited(listState.firstVisibleItemIndex + 1)
                        items(items = quranList, key = { it.id }) { quran ->
                            AyaItem(modifier = Modifier.animateItem(
                                fadeInSpec = null, fadeOutSpec = null
                            ),
                                quran = quran,
                                translates = try {
                                    enabledTranslates[quran.verseID] ?: emptyList()
                                } catch (ex: Exception) {
                                    emptyList()
                                },
                                isPlaying = quran.verseID == playingAya && quran.surahID == playingSura && autoScroll,
                                animations = animations,
                                onCopyClick = {
                                    var text = it
                                    text =
                                        context.getString(R.string.sura) + " " + chapter?.nameArabic + "\n\n" + text + APP_LINK
                                    scope.launch {
                                        clipboardManager.setClipEntry(
                                            ClipEntry(
                                                ClipData.newPlainText(
                                                    context.getString(R.string.quran), text
                                                )
                                            )
                                        )
                                    }
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.copied),
                                        Toast.LENGTH_SHORT
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
                                    if (!isQuranDownloaded(context = context, quran.surahID))
                                        showQuranFilesDialog = true
                                    else if (!isTranslateDownloaded(
                                            context = context,
                                            quran.surahID
                                        )
                                    )
                                        showTranslateFilesDialog = true
                                    else viewModel.onPlay(
                                        context = context,
                                        sura = quran.surahID,
                                        aya = quran.verseID
                                    )

                                })
                        }
                    }
                    else if (query.isNotEmpty()) NothingFoundUIElement()
                }
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    visible = !isPlayerStopped,
                    enter = slideInVertically(
                        initialOffsetY = { it }
                    ),
                    exit = shrinkVertically()
                ) {
                    PlayerComponent(
                        modifier = Modifier.fillMaxWidth(),
                        isPlaying = isPlaying,
                        isAutoScroll = autoScroll && playingSura == currentSura,
                        showAutoScroll = playingSura == currentSura,
                        qariPhotoLink = currentMediaItem?.mediaMetadata?.artworkUri?.toString()
                            ?: "",
                        aya = playingAya,
                        sura = playingSuraName,
                        duration = duration,
                        currentPosition = currentPosition,
                        pause = viewModel::pause,
                        resume = viewModel::resume,
                        stop = viewModel::stop,
                        seekTo = viewModel::seekTo,
                        next = viewModel::next,
                        nextSura = {
                            val sura = if (playingSura == 114) 1 else playingSura + 1
                            if (!isQuranDownloaded(context = context, sura))
                                Toast.makeText(
                                    context,
                                    R.string.audio_files_error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            else if (!isTranslateDownloaded(context = context, sura))
                                Toast.makeText(
                                    context,
                                    R.string.audio_translate_files_error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            else viewModel.onPlay(context, sura, 1)
                        },
                        previous = viewModel::previous,
                        previousSura = {
                            val sura = if (playingSura == 1) 114 else playingSura - 1
                            if (!isQuranDownloaded(context = context, sura))
                                Toast.makeText(
                                    context,
                                    R.string.audio_files_error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            else if (!isTranslateDownloaded(context = context, sura))
                                Toast.makeText(
                                    context,
                                    R.string.audio_translate_files_error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            else
                                viewModel.onPlay(context, sura, 1)
                        },
                        onAutoScrollChange = viewModel::updateAutoScroll
                    )
                }
            }
        }
        if (showQuranFilesDialog) {
            AlertDialog(icon = {
                Icon(imageVector = Icons.Default.Warning, contentDescription = "")
            }, title = {
                Text(
                    text = stringResource(id = R.string.error), fontWeight = FontWeight.SemiBold
                )
            }, text = {
                Text(
                    text = stringResource(id = R.string.audio_files_error),
                    fontWeight = FontWeight.SemiBold
                )
            }, onDismissRequest = { showQuranFilesDialog = false }, confirmButton = {
                TextButton(onClick = {
                    showQuranFilesDialog = false
                    if (isNetworkConnected(context)) viewModel.downloadQuranFiles(context)
                    else Toast.makeText(
                        context,
                        context.getString(R.string.network_error_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                    Text(
                        text = stringResource(id = R.string.download),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = "")
                }
            }, dismissButton = {
                TextButton(onClick = { showQuranFilesDialog = false }) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            })
        }
        if (showTranslateFilesDialog) {
            AlertDialog(icon = {
                Icon(imageVector = Icons.Default.Warning, contentDescription = "")
            }, title = {
                Text(
                    text = stringResource(id = R.string.error), fontWeight = FontWeight.SemiBold
                )
            }, text = {
                Text(
                    text = stringResource(id = R.string.audio_translate_files_error),
                    fontWeight = FontWeight.SemiBold
                )
            }, onDismissRequest = { showTranslateFilesDialog = false }, confirmButton = {
                TextButton(onClick = {
                    showTranslateFilesDialog = false
                    if (isNetworkConnected(context)) viewModel.downloadTranslateFiles(context)
                    else Toast.makeText(
                        context,
                        context.getString(R.string.network_error_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                    Text(
                        text = stringResource(id = R.string.download),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = "")
                }
            }, dismissButton = {
                TextButton(onClick = { showTranslateFilesDialog = false }) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            })
        }

        BackHandler(enabled = isSearchBarOpen) {
            viewModel.closeSearchBar()
            if (query.isNotEmpty()) viewModel.onQuery("")
        }
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DefaultSuraAppBar(
    title: String,
    aya: Int,
    animatedContentScope: AnimatedContentScope,
    onSearchClick: () -> Unit,
    openDrawer: () -> Unit,
    nextSura: () -> Unit,
    prevSura: () -> Unit
) {
    CenterAlignedTopAppBar(title = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(targetState = title, label = "title") {
                Text(text = it, fontSize = 18.sp)
            }
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
        IconButton(onClick = prevSura) {
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "Prev")
        }
        IconButton(onClick = nextSura) {
            Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = "Next")
        }
    }, navigationIcon = {
        NavigationOpenDrawerIcon(animatedContentScope, openDrawer)
    })
}
