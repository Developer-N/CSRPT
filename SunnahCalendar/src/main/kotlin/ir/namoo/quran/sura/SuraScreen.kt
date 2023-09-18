package ir.namoo.quran.sura

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.namoo.commons.appLink
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.colorAppBar
import ir.namoo.commons.utils.colorOnAppBar
import ir.namoo.commons.utils.iconColor
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.quran.chapters.SearchBarState
import ir.namoo.quran.utils.KeepScreenOn
import ir.namoo.quran.utils.initQuranUtils
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.lang.Thread.sleep

@Composable
fun SuraScreen(
    sura: Int,
    aya: Int,
    viewModel: SuraViewModel = koinViewModel(),
    drawerState: DrawerState,
    navController: NavController
) {
    val context = LocalContext.current
    initQuranUtils(context)
    viewModel.loadDate(sura)
    val isLoading by viewModel.isLoading.collectAsState()
    val quranList by viewModel.quranList.collectAsState()
    val enabledTranslates by viewModel.enabledTranslates.collectAsState()
    val chapter by viewModel.chapter.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val query by viewModel.query.collectAsState()
    val playingAya by viewModel.playingAya.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    Scaffold(topBar = {
        AnimatedVisibility(
            visible = viewModel.searchBarState == SearchBarState.CLOSED,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            DefaultSuraAppBar(
                title = chapter?.nameArabic ?: "-",
                aya = chapter?.ayaCount ?: 0,
                onSearchClick = {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                    viewModel.searchBarState = SearchBarState.OPENED
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
            visible = viewModel.searchBarState == SearchBarState.OPENED,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            SearchAppBar(viewModel, query)
        }
    }) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
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
                if (playingAya > 1) listState.animateScrollToItem(playingAya - 1, 0)
            }
            LaunchedEffect(key1 = query) {
                if (query.isNotEmpty() && query.isDigitsOnly() && query.toInt() > 1 && query.toInt() < quranList.size + 1)
                    listState.animateScrollToItem(query.toInt() - 1, 0)
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
                        Lifecycle.Event.ON_PAUSE -> viewModel.saveLastVisited()
                        Lifecycle.Event.ON_CREATE -> {}
                        Lifecycle.Event.ON_START -> viewModel.setupPlayer(context)
                        Lifecycle.Event.ON_RESUME -> {}
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
            if (quranList.isNotEmpty()) LazyColumn(state = listState) {
                viewModel.updateLastVisited(listState.firstVisibleItemIndex + 1)
                items(items = quranList, key = { it.id }) { quran ->
                    AyaItem(quran = quran,
                        translates = try {
                            val index = enabledTranslates.indexOf(enabledTranslates.find {
                                it.any { t -> t.ayaID == quran.verseID }
                            })
                            enabledTranslates[index]
                        } catch (ex: Exception) {
                            emptyList()
                        },
                        isPlaying = quran.verseID == playingAya,
                        onCopyClick = {
                            var text = it
                            text =
                                context.getString(R.string.sura) + " " + chapter?.nameArabic + "\n\n" + text + appLink
                            clipboardManager.setText(AnnotatedString(text))
                            Toast.makeText(
                                context, context.getString(R.string.copied), Toast.LENGTH_SHORT
                            ).show()
                        },
                        onShareClick = {
                            var text = it
                            text =
                                context.getString(R.string.sura) + " " + chapter?.nameArabic + "\n\n" + text + appLink
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
                            if (!viewModel.isQuranDownloaded(
                                    context = context, quran.surahID
                                )
                            ) MaterialAlertDialogBuilder(context).setTitle(R.string.error)
                                .setMessage(R.string.audio_files_error)
                                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                                .setPositiveButton(R.string.download) { _, _ ->
                                    if (isNetworkConnected(context)) viewModel.downloadQuranFiles(
                                        context
                                    )
                                    else Toast.makeText(
                                        context,
                                        context.getString(R.string.network_error_message),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }.show()
                            else if (!viewModel.isTranslateDownloaded(
                                    context = context, quran.surahID
                                )
                            ) MaterialAlertDialogBuilder(context).setTitle(R.string.error)
                                .setMessage(R.string.audio_translate_files_error)
                                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                                .setPositiveButton(R.string.download) { _, _ ->
                                    viewModel.downloadTranslateFiles(context)
                                }.show()
                            else viewModel.onPlay(
                                context = context, sura = quran.surahID, aya = quran.verseID
                            )

                        })
                }
            }
            else if (query.isNotEmpty()) NothingFoundUIElement()
        }
        BackHandler(enabled = viewModel.searchBarState == SearchBarState.OPENED || drawerState.isOpen) {
            if (drawerState.isOpen)
                scope.launch {
                    drawerState.close()
                }
            else {
                viewModel.searchBarState = SearchBarState.CLOSED
                if (query.isNotEmpty())
                    viewModel.onQuery("")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(viewModel: SuraViewModel, query: String) {
    val focus = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorAppBar)
            .padding(8.dp)
    ) {
        SearchBar(modifier = Modifier.fillMaxWidth(),
            query = query,
            onQueryChange = { viewModel.onQuery(it) },
            onSearch = {
                focus.clearFocus()
                viewModel.onQuery(it)
            },
            active = false,
            onActiveChange = { },
            placeholder = {
                Text(
                    text = stringResource(id = R.string.search), fontFamily = FontFamily(
                        appFont
                    )
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(id = R.string.search),
                    tint = iconColor
                )
            },
            trailingIcon = {
                Icon(
                    modifier = Modifier.clickable {
                        focus.clearFocus()
                        if (viewModel.query.value.isNotEmpty()) viewModel.onQuery("")
                        else viewModel.searchBarState = SearchBarState.CLOSED
                    },
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(id = R.string.close),
                    tint = iconColor
                )
            }) { }
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
            Text(
                text = title, fontFamily = FontFamily(appFont), fontSize = 18.sp
            )
            Text(
                text = formatNumber(
                    String.format(
                        stringResource(id = R.string.aya_count), aya
                    )
                ), fontFamily = FontFamily(appFont), fontSize = 12.sp
            )
        }
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = colorAppBar,
        titleContentColor = colorOnAppBar,
        navigationIconContentColor = colorOnAppBar,
        actionIconContentColor = colorOnAppBar
    ), actions = {
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
            Icon(imageVector = Icons.Filled.NavigateNext, contentDescription = "Next")
        }
        IconButton(onClick = {
            navController.navigate("sura/${if (sura == 114) 1 else (sura + 1)}/1") {
                popUpTo("chapters")
            }
        }) {
            Icon(imageVector = Icons.Filled.NavigateBefore, contentDescription = "Before")
        }
    }, navigationIcon = {
        IconButton(onClick = { onMenuClick() }) {
            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Navigation Menu")
        }
    })
}
