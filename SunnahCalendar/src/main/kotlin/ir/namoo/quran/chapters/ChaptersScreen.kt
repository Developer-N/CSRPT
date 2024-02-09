package ir.namoo.quran.chapters

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Shortcut
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.quran.utils.chapterException
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import ir.namoo.religiousprayers.ui.shared.SearchAppBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.File

@SuppressLint("SdCardPath")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChaptersScreen(
    drawerState: DrawerState,
    navController: NavHostController,
    checkDBFile: () -> Unit,
    viewModel: ChapterViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val isLoading by viewModel.isLoading.collectAsState()
    val chapters by viewModel.chapterList.collectAsState()
    val query by viewModel.query.collectAsState()
    val lastVisitedList by viewModel.lastVisitedList.collectAsState()
    val isSearchBarOpen by viewModel.isSearchBarOpen.collectAsState()

    viewModel.loadData()
    Scaffold(topBar = {
        AnimatedVisibility(
            visible = !isSearchBarOpen,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            DefaultAppBar(scope, drawerState, viewModel, listState, navController)
        }
        AnimatedVisibility(
            visible = isSearchBarOpen,
            enter = expandVertically(),
            exit = shrinkVertically()
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
                            .height(2.dp),
                        strokeCap = StrokeCap.Round
                    )
                }
                if (lastVisitedList.isNotEmpty() && query.isEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        state = rememberLazyListState().apply {
                            scope.launch {
                                animateScrollToItem(0)
                            }
                        }) {
                        items(items = lastVisitedList, key = { it.id }) { last ->
                            ElevatedAssistChip(modifier = Modifier.padding(2.dp, 0.dp),
                                onClick = { navController.navigate("sura/${last.suraID}/${last.ayaID}") },
                                label = {
                                    Text(
                                        text = chapters.find { it.sura == last.suraID }?.nameArabic + (" " + stringResource(
                                            id = R.string.aya
                                        ) + " " + formatNumber(
                                            last.ayaID
                                        ))
                                    )
                                })
                        }
                    }
                }
                if (!isLoading && query.isEmpty() && chapters.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val context = LocalContext.current
                        Button(onClick = {
                            File("/data/data/${context.packageName}/databases/quran_v3.db").let {
                                if (it.exists()) it.delete()
                            }
                            checkDBFile()
                        }) {
                            Text(
                                text = stringResource(id = R.string.load_quran_chapter_error),
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp
                            )
                        }

                        chapterException?.let {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    text = it.message + "\n" + it.toString()
                                )
                            }
                        }
                    }
                }
                LazyColumn(state = listState) {
                    if (chapters.isNotEmpty()) itemsIndexed(chapters,
                        key = { _, chapter -> chapter.sura }) { index, chapter ->
                        QuranChapterItem(modifier = Modifier.animateItemPlacement(),
                            chapter = chapter,
                            rowID = index + 1,
                            query = query,
                            onFavClick = {
                                viewModel.updateFav(chapter.sura)
                            },
                            cardClick = {
                                navController.navigate("sura/${chapter.sura}/1")
                            })
                    }
                    else if (query.isNotEmpty()) item {
                        NothingFoundUIElement()
                    }
                }
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultAppBar(
    scope: CoroutineScope,
    drawerState: DrawerState,
    viewModel: ChapterViewModel,
    listState: LazyListState,
    navController: NavHostController
) {
    val context = LocalContext.current
    var showSortDialog by remember { mutableStateOf(false) }
    var showGoToPageDialog by remember { mutableStateOf(false) }
    var showGoToJuzDialog by remember { mutableStateOf(false) }
    var showGoToHizbDialog by remember { mutableStateOf(false) }
    val isFavShowing by viewModel.isFavShowing.collectAsState()
    val scale = remember { Animatable(1f) }
    TopAppBar(title = {
        Text(text = stringResource(id = R.string.chapter))
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
    }, colors = appTopAppBarColors(), actions = {
        // Favorite
        IconButton(modifier = Modifier.scale(scale.value), onClick = {
            viewModel.showFav()
            scope.launch {
                scale.animateTo(0.1f)
                scale.animateTo(1f)
            }
        }) {
            Icon(
                imageVector = if (isFavShowing) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = stringResource(id = R.string.favorite)
            )
        }
        //Search
        IconButton(onClick = {
            if (isFavShowing) viewModel.showFav()
            viewModel.onQuery("")
            scope.launch {
                listState.animateScrollToItem(0)
            }
            viewModel.openSearchBar()
        }) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = stringResource(id = R.string.search)
            )
        }
        //More ...
        ThreeDotsDropdownMenu { closeMenu ->
            //Chapter Sort
            AppDropdownMenuItem(text = { Text(text = stringResource(id = R.string.sort_order)) },
                onClick = {
                    showSortDialog = true
                    closeMenu()
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.Sort,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primary
                    )
                })

            //Go to Page
            AppDropdownMenuItem(text = {
                Text(text = stringResource(id = R.string.go_to_page))
            }, onClick = {
                showGoToPageDialog = true
                closeMenu()
            }, trailingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Shortcut,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
            })
            //Go to Juz
            AppDropdownMenuItem(text = {
                Text(text = stringResource(id = R.string.go_to_juz))
            }, onClick = {
                showGoToJuzDialog = true
                closeMenu()
            }, trailingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Shortcut,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
            })
            //Go to hizb
            AppDropdownMenuItem(text = {
                Text(text = stringResource(id = R.string.go_to_hizb))
            }, onClick = {
                showGoToHizbDialog = true
                closeMenu()
            }, trailingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Shortcut,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
            })
        }

    })
    //SortDialog
    if (showSortDialog) {
        val sortList = arrayOf(
            context.getString(R.string.default_sort),
            context.getString(R.string.alphabet_sort),
            context.getString(R.string.revelation_sort),
            context.getString(R.string.aya_inc_sort),
            context.getString(R.string.aya_dec_sort)
        )
        AlertDialog(onDismissRequest = { showSortDialog = false }, confirmButton = { }, text = {
            LazyColumn {
                itemsIndexed(items = sortList, key = { index, _ -> index }) { index, item ->
                    AppDropdownMenuItem(text = {
                        Text(text = item)
                    }, onClick = {
                        showSortDialog = false
                        when (index) {
                            0 -> viewModel.onQuery("default")
                            1 -> viewModel.onQuery("alphabet")
                            2 -> viewModel.onQuery("revelation")
                            3 -> viewModel.onQuery("ayaIncrease")
                            4 -> viewModel.onQuery("ayaDecrease")
                        }
                        scope.launch {
                            delay(1000)
                            listState.animateScrollToItem(0)
                        }
                    })
                }
            }
        }, title = { Text(text = stringResource(id = R.string.chapter_sort_dialog_title)) })
    }

    //GoToPage Dialog
    if (showGoToPageDialog) {
        var pageNumber by remember { mutableStateOf("") }
        var isCorrectPage by remember { mutableStateOf(false) }

        AlertDialog(onDismissRequest = { showGoToPageDialog = false }, title = {
            Text(text = stringResource(id = R.string.select_page))
        }, confirmButton = {
            TextButton(onClick = {
                showGoToPageDialog = false
                scope.launch {
                    val page = viewModel.getPage(pageNumber.toInt())
                    navController.navigate("sura/${page.sura}/${page.aya}")
                }
            }, enabled = isCorrectPage) {
                Text(text = stringResource(id = R.string.go_to_page))
            }
        }, dismissButton = {
            TextButton(onClick = { showGoToPageDialog = false }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }, text = {
            OutlinedTextField(
                value = pageNumber,
                onValueChange = {
                    pageNumber = it
                    isCorrectPage =
                        pageNumber.isNotEmpty() && pageNumber.toInt() > 0 && pageNumber.toInt() < 604
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                label = { Text(text = stringResource(id = R.string.page)) },
                isError = !isCorrectPage,
                supportingText = {
                    AnimatedVisibility(visible = !isCorrectPage) {
                        Text(text = formatNumber(stringResource(id = R.string.page_out_of_bounds)))
                    }
                }
            )
        })

    }

    //GoToJuz Dialog
    if (showGoToJuzDialog) {
        val juz = mutableListOf<String>()
        for (i in 1..30) juz.add(formatNumber(i))
        AlertDialog(onDismissRequest = { showGoToJuzDialog = false }, confirmButton = { }, text = {
            LazyColumn {
                itemsIndexed(items = juz, key = { index, _ -> index }) { index, item ->
                    AppDropdownMenuItem(text = {
                        Text(text = item, fontWeight = FontWeight.SemiBold)
                    }, onClick = {
                        showGoToJuzDialog = false
                        scope.launch {
                            val j = viewModel.getJuz(index + 1)
                            navController.navigate("sura/${j.sura}/${j.aya}")
                        }
                    })
                }
            }
        }, title = { Text(text = stringResource(id = R.string.select_juz)) })
    }

    //GoToHizb Dialog
    if (showGoToHizbDialog) {
        val hizb = mutableListOf<String>()
        for (i in 1..120) hizb.add(formatNumber(i))
        AlertDialog(onDismissRequest = { showGoToHizbDialog = false }, confirmButton = { }, text = {
            LazyColumn {
                itemsIndexed(items = hizb, key = { index, _ -> index }) { index, item ->
                    AppDropdownMenuItem(text = {
                        Text(text = item, fontWeight = FontWeight.SemiBold)
                    }, onClick = {
                        showGoToHizbDialog = false
                        scope.launch {
                            val h = viewModel.getHizb(index + 1)
                            navController.navigate("sura/${h.sura}/${h.aya}")
                        }
                    })
                }
            }
        }, title = { Text(text = stringResource(id = R.string.select_hizb)) })
    }
}
