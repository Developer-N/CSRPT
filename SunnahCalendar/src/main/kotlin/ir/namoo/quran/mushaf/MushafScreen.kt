package ir.namoo.quran.mushaf

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.NavigationOpenNavigationRailIcon
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.utils.logException
import ir.namoo.commons.APP_LINK
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.commons.utils.toastMessage
import ir.namoo.quran.player.isQuranDownloaded
import ir.namoo.quran.player.isTranslateDownloaded
import ir.namoo.quran.sura.PlayerComponent
import ir.namoo.quran.sura.data.TranslateType
import ir.namoo.quran.utils.KeepScreenOn
import ir.namoo.quran.utils.QCF2BISMLFont
import ir.namoo.quran.utils.englishFont
import ir.namoo.quran.utils.englishFontSize
import ir.namoo.quran.utils.farsiFont
import ir.namoo.quran.utils.farsiFontSize
import ir.namoo.quran.utils.kurdishFont
import ir.namoo.quran.utils.kurdishFontSize
import ir.namoo.quran.utils.quranFont
import ir.namoo.quran.utils.quranFontSize
import ir.namoo.quran.utils.uthmanTahaFont
import ir.namoo.quran.utils.vazirmatnFont
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.MushafScreen(
    sura: Int,
    aya: Int,
    animatedContentScope: AnimatedContentScope,
    openDrawer: () -> Unit,
    viewModel: MushafViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboard.current
    val listState = rememberLazyListState()
    var showTopBar by remember { mutableStateOf(true) }
    val numeral by numeral.collectAsState()
    LaunchedEffect(key1 = Unit) {
        viewModel.init(context)
    }
    LaunchedEffect(key1 = sura, key2 = aya) {
        viewModel.loadPage(sura, aya)
    }
    val suraInfo by viewModel.suraInfo.collectAsState()
    val error by viewModel.error.collectAsState()
    val pages = viewModel.pages
    val isPaginatingUp by viewModel.isPaginatingUp.collectAsState()
    val isPaginatingDown by viewModel.isPaginatingDown.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val hideToolbarOnScroll by viewModel.hideTopBar.collectAsState()
    val pageSurahPositions = remember { mutableStateMapOf<Int, List<Pair<Float, Int>>>() }
    val fontSize by viewModel.fontSize.collectAsState()
    var selectedVerse by remember { mutableStateOf<Verse?>(null) }
    var showVerseMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val bookmarkedVerse by viewModel.bookmarkedVerse.collectAsState()

    var showQuranFilesDialog by remember { mutableStateOf(false) }
    var showTranslateFilesDialog by remember { mutableStateOf(false) }
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playingAya by viewModel.playingAya.collectAsState()
    val playingSura by viewModel.playingSura.collectAsState()
    val currentMediaItem by viewModel.currentMediaItem.collectAsState()
    val playingSuraName by viewModel.playingSuraName.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val isPlayerStopped by viewModel.isPlayerStoped.collectAsState()
    val isFotnsDownloaded by viewModel.isFontsDownloaded.collectAsState()
    KeepScreenOn()
    LaunchedEffect(key1 = currentPage) {
        runCatching {
            while (pages.isEmpty()) {
                delay(1000)
            }
            val index = pages.indexOf(pages.find { it.page == currentPage })
            if (index >= 0) listState.animateScrollToItem(index)
        }.onFailure(logException)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }.collect { layoutInfo ->
            if (isLoading) return@collect

            val firstItem = layoutInfo.visibleItemsInfo.firstOrNull()
            if (firstItem != null && firstItem.index == 0 && firstItem.offset == 0) {
                viewModel.loadPreviousPages()
            }

            val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()
            if (lastItem != null && lastItem.index == layoutInfo.totalItemsCount - 1) {
                viewModel.loadNextPages()
            }
        }
    }

    LaunchedEffect(listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousOffset = listState.firstVisibleItemScrollOffset
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }.collect { (index, offset) ->
            if (pages.isNotEmpty() && index >= 0 && index < pages.size) {
                val currentPage = pages[index]
                viewModel.updateLastVisitedPage(currentPage.page)
                val positions = pageSurahPositions[currentPage.page]
                var targetSura = currentPage.verses.first().sura
                if (positions != null) {
                    val passedSurah = positions.lastOrNull { (y, _) -> y <= offset.toFloat() }
                    if (passedSurah != null) {
                        targetSura = passedSurah.second
                    }
                }
                viewModel.updateSuraName(targetSura)
            }
            if (hideToolbarOnScroll) {
                if (index == previousIndex) {
                    if (offset > previousOffset) {
                        showTopBar = false
                    } else if (offset < previousOffset) {
                        showTopBar = true
                    }
                } else if (index > previousIndex) {
                    showTopBar = false
                } else {
                    showTopBar = true
                }
                previousIndex = index
                previousOffset = offset
            }
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.saveLastVisitedPage()
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
    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = showTopBar, enter = expandVertically(), exit = shrinkVertically()
            ) {
                CenterAlignedTopAppBar(title = {
                    AnimatedContent(
                        targetState = suraInfo, transitionSpec = {
                            if (targetState.first > initialState.first) {
                                (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                            } else {
                                (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
                            }
                        }, label = "SuraNameAnimation"
                    ) { (_, name) ->
                        Text(
                            text = name, fontFamily = FontFamily(QCF2BISMLFont), fontSize = 26.sp
                        )
                    }
                }, colors = appTopAppBarColors(), navigationIcon = {
                    NavigationOpenNavigationRailIcon(animatedContentScope, openDrawer)
                }, actions = {
                    AppIconButton(
                        icon = Icons.Default.TextIncrease,
                        title = stringResource(R.string.increase_font_size),
                        onClick = viewModel::addFontSize
                    )
                    AppIconButton(
                        icon = Icons.Default.TextDecrease,
                        title = stringResource(R.string.decrease_font_size),
                        onClick = viewModel::minusFontSize
                    )

                })
            }
        }) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        if (zoom != 1f) {
                            viewModel.changeFontSize(zoom)
                        }
                    }
                }, shape = materialCornerExtraLargeTop()
        ) {
            if (!isFotnsDownloaded) Box(modifier = Modifier.fillMaxSize()) { DownloadMushafFiles() }
            else Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(visible = error.isNotEmpty()) {
                        Text(
                            text = error,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    AnimatedVisibility(visible = isPaginatingUp) {
                        Box(
                            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f), state = listState
                    ) {
                        items(items = pages, key = { it.page }) { page ->
                            MushafPageComponent(
                                pageState = page,
                                playingSura = playingSura,
                                playingVerse = playingAya,
                                getSuraName = viewModel::getSuraName,
                                fontSizeSp = fontSize,
                                selectedVerseId = selectedVerse?.id,
                                onVerseClick = { verse ->
                                    selectedVerse = verse
                                    showVerseMenu = true
                                },
                                onSurahPositionsCalculated = { positions ->
                                    pageSurahPositions[page.page] = positions
                                })
                        }
                        item {
                            Box(modifier = Modifier.height(500.dp))
                        }
                    }
                    AnimatedVisibility(visible = isPaginatingDown) {
                        Box(
                            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    visible = !isPlayerStopped,
                    enter = slideInVertically(
                        initialOffsetY = { it }),
                    exit = shrinkVertically()
                ) {
                    PlayerComponent(
                        modifier = Modifier.fillMaxWidth(),
                        isPlaying = isPlaying,
                        isAutoScroll = true,
                        showAutoScroll = false,
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
                            if (!isQuranDownloaded(context = context, sura)) Toast.makeText(
                                context, R.string.audio_files_error, Toast.LENGTH_SHORT
                            ).show()
                            else if (!isTranslateDownloaded(
                                    context = context, sura
                                )
                            ) Toast.makeText(
                                context, R.string.audio_translate_files_error, Toast.LENGTH_SHORT
                            ).show()
                            else viewModel.onPlay(context, sura, 1)
                        },
                        previous = viewModel::previous,
                        previousSura = {
                            val sura = if (playingSura == 1) 114 else playingSura - 1
                            if (!isQuranDownloaded(context = context, sura)) Toast.makeText(
                                context, R.string.audio_files_error, Toast.LENGTH_SHORT
                            ).show()
                            else if (!isTranslateDownloaded(
                                    context = context, sura
                                )
                            ) Toast.makeText(
                                context, R.string.audio_translate_files_error, Toast.LENGTH_SHORT
                            ).show()
                            else viewModel.onPlay(context, sura, 1)
                        },
                        onAutoScrollChange = {})
                }
            }
            if (showVerseMenu && selectedVerse != null) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showVerseMenu = false
                        selectedVerse = null
                    }, sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .verticalScroll(
                                rememberScrollState()
                            )
                    ) {
                        selectedVerse?.let { verse ->
                            val isVersePlaying =
                                verse.verseNumber == playingAya && verse.sura == playingSura
                            var noteText by remember { mutableStateOf(verse.note) }
                            val alpha by animateFloatAsState(targetValue = if (bookmarkedVerse == verse.id) 1f else 0.1f)
                            var showDeleteNoteDialog by remember { mutableStateOf(false) }
                            val btnPlaySize by animateIntAsState(
                                targetValue = if (isVersePlaying) 56 else 28, label = "size"
                            )
                            var content = ""
                            val quranText = buildAnnotatedString {
                                append(verse.verseNormalText)
                                append(" ")
                                withStyle(
                                    style = SpanStyle(
                                        fontFamily = FontFamily(uthmanTahaFont)
                                    )
                                ) {
                                    append("﴿")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        fontFamily = FontFamily(vazirmatnFont)
                                    )
                                ) {
                                    append(numeral.format(verse.verseNumber))
                                }
                                withStyle(
                                    style = SpanStyle(
                                        fontFamily = FontFamily(uthmanTahaFont)
                                    )
                                ) {
                                    append("﴾")
                                }
                            }
                            content += quranText
                            content += "\n\n"
                            Text(
                                text = stringResource(R.string.sura) + " ${
                                    viewModel.getNormalSuraName(
                                        verse.sura
                                    )
                                } ",
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            SelectionContainer {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp, 2.dp),
                                    text = quranText,
                                    fontFamily = FontFamily(quranFont),
                                    fontSize = quranFontSize.sp,
                                    lineHeight = (quranFontSize * 1.7).sp,
                                    softWrap = true,
                                    overflow = TextOverflow.Visible,

                                    )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Copy
                                IconButton(
                                    onClick = {
                                        var text = content
                                        text =
                                            context.getString(R.string.sura) + " " + viewModel.getNormalSuraName(
                                                verse.sura
                                            ) + "\n\n" + text + APP_LINK
                                        scope.launch {
                                            clipboardManager.setClipEntry(
                                                ClipEntry(
                                                    ClipData.newPlainText(
                                                        context.getString(R.string.quran), text
                                                    )
                                                )
                                            )
                                        }
                                        context.toastMessage(context.getString(R.string.copied))
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = stringResource(id = R.string.copy)
                                    )
                                }
                                // Share
                                IconButton(
                                    onClick = {
                                        var text = content
                                        text =
                                            context.getString(R.string.sura) + " " + viewModel.getNormalSuraName(
                                                verse.sura
                                            ) + "\n\n" + text + APP_LINK
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
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = stringResource(id = R.string.share)
                                    )
                                }
                                // Fav
                                AnimatedContent(targetState = verse.fav, label = "fav") {
                                    IconButton(
                                        onClick = {
                                            viewModel.toggleBookmark(verse)
                                            selectedVerse =
                                                verse.copy(fav = if (verse.fav == 1) 0 else 1)
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(
                                            imageVector = if (it == 1) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                                            contentDescription = stringResource(id = R.string.bookmarks)
                                        )
                                    }
                                }
                                // Play
                                IconButton(
                                    onClick = {
                                        if (!isQuranDownloaded(
                                                context = context, verse.sura
                                            )
                                        ) showQuranFilesDialog = true
                                        else if (!isTranslateDownloaded(
                                                context = context, verse.sura
                                            )
                                        ) showTranslateFilesDialog = true
                                        else {
                                            viewModel.onPlay(
                                                context = context,
                                                sura = verse.sura,
                                                aya = verse.verseNumber
                                            )
                                            showVerseMenu = false
                                            selectedVerse = null
                                        }
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        modifier = Modifier.size(btnPlaySize.dp),
                                        imageVector = if (isVersePlaying) Icons.Rounded.StopCircle
                                        else Icons.Rounded.PlayCircle,
                                        contentDescription = stringResource(id = R.string.play)
                                    )
                                }
                                if (showQuranFilesDialog) {
                                    AlertDialog(
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = ""
                                            )
                                        },
                                        title = {
                                            Text(
                                                text = stringResource(id = R.string.error),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        text = {
                                            Text(
                                                text = stringResource(id = R.string.audio_files_error),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        onDismissRequest = { showQuranFilesDialog = false },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                showQuranFilesDialog = false
                                                if (isNetworkConnected(context)) viewModel.downloadQuranFiles(
                                                    context, verse.sura
                                                )
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
                                                Icon(
                                                    imageVector = Icons.Default.CloudDownload,
                                                    contentDescription = ""
                                                )
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showQuranFilesDialog = false }) {
                                                Text(
                                                    text = stringResource(id = R.string.cancel),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        })
                                }
                                if (showTranslateFilesDialog) {
                                    AlertDialog(
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = ""
                                            )
                                        },
                                        title = {
                                            Text(
                                                text = stringResource(id = R.string.error),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        text = {
                                            Text(
                                                text = stringResource(id = R.string.audio_translate_files_error),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        onDismissRequest = { showTranslateFilesDialog = false },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                showTranslateFilesDialog = false
                                                if (isNetworkConnected(context)) viewModel.downloadTranslateFiles(
                                                    context, verse.sura
                                                )
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
                                                Icon(
                                                    imageVector = Icons.Default.CloudDownload,
                                                    contentDescription = ""
                                                )
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = {
                                                showTranslateFilesDialog = false
                                            }) {
                                                Text(
                                                    text = stringResource(id = R.string.cancel),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        })
                                }
                                // Bookmark
                                IconButton(
                                    onClick = { viewModel.updateBookmarkVerse(verse.id) },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.Book, contentDescription = "")
                                }
                            }
                            // Note
                            TextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp, 2.dp),
                                value = noteText,
                                onValueChange = { noteText = it },
                                label = { Text(text = stringResource(id = R.string.your_note)) },
                                leadingIcon = {
                                    IconButton(
                                        onClick = { showDeleteNoteDialog = true },
                                        enabled = verse.note.isNotBlank(),
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = stringResource(id = R.string.delete)
                                        )
                                    }

                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            viewModel.updateVerseNote(verse, noteText)
                                            selectedVerse = verse.copy(note = noteText)
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                        enabled = noteText.isNotEmpty() && verse.note != noteText
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Save,
                                            contentDescription = stringResource(id = R.string.delete)
                                        )
                                    }
                                },
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                )
                            )
                            // Delete Note Dialog
                            if (showDeleteNoteDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteNoteDialog = false },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                showDeleteNoteDialog = false
                                                noteText = ""
                                                viewModel.updateVerseNote(verse, noteText)
                                                selectedVerse = verse.copy(note = noteText)
                                            },
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.yes),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteNoteDialog = false }) {
                                            Text(
                                                text = stringResource(id = R.string.no),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    },
                                    title = { Text(text = stringResource(id = R.string.alert)) },
                                    text = {
                                        Text(
                                            text = stringResource(id = R.string.delete_note_alert_message),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    })
                            }

                            verse.translates.forEach { t ->
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp, 0.dp),
                                    text = t.name,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline,
                                )
                                content += t.name
                                content += ": "
                                CompositionLocalProvider(values = arrayOf(if (t.translateType == TranslateType.ENGLISH) LocalLayoutDirection provides LayoutDirection.Ltr else LocalLayoutDirection provides LayoutDirection.Rtl)) {
                                    SelectionContainer {
                                        Text(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(4.dp, 0.dp),
                                            text = t.text.trim(),
                                            fontFamily = FontFamily(
                                                when (t.translateType) {
                                                    TranslateType.KURDISH -> kurdishFont
                                                    TranslateType.FARSI -> farsiFont
                                                    TranslateType.ENGLISH -> englishFont
                                                }
                                            ),
                                            fontSize = when (t.translateType) {
                                                TranslateType.KURDISH -> kurdishFontSize
                                                TranslateType.FARSI -> farsiFontSize
                                                TranslateType.ENGLISH -> englishFontSize
                                            }.sp,
                                            lineHeight = (when (t.translateType) {
                                                TranslateType.KURDISH -> kurdishFontSize
                                                TranslateType.FARSI -> farsiFontSize
                                                TranslateType.ENGLISH -> englishFontSize
                                            } * 1.7).sp,
                                            softWrap = true,
                                            textAlign = TextAlign.Justify,
                                            overflow = TextOverflow.Visible
                                        )
                                    }
                                    content += t.text.trim()
                                    content += "\n\n"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadMushafFiles(downloaderViewModel: MushafFileDownloaderViewModel = koinViewModel()) {
    val context = LocalContext.current
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
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
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
                    Text(text = numeral.format(stringResource(R.string.megabyte, downloaded)))
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
