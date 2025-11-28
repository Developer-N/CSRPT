package ir.namoo.quran.chapters

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Shortcut
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavHostController
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.NavigationOpenNavigationRailIcon
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import ir.namoo.quran.utils.chapterException
import ir.namoo.quran.utils.getWordsForSearch
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import ir.namoo.religiousprayers.ui.shared.SearchAppBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.File

@SuppressLint("SdCardPath")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ChaptersScreen(
    animatedContentScope: AnimatedContentScope,
    openDrawer: () -> Unit,
    navController: NavHostController,
    checkDBFile: () -> Unit,
    viewModel: ChapterViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val numeral by numeral.collectAsState()
    LaunchedEffect(key1 = Unit) {
        viewModel.loadData()
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val chapters = viewModel.chapterList
    val qcfChapters = viewModel.qcfChapters
    val lastVisitedList = viewModel.lastVisitedList
    val lastVisitedPages = viewModel.lastVisitedPages
    val pageType by viewModel.pageType.collectAsState()
    val isFavShowing by viewModel.isFavShowing.collectAsState()
    val bookmarkedVerse by viewModel.bookmarkedVerse.collectAsState()

    var isSearchBarOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredChapters by remember {
        derivedStateOf {
            when {
                searchQuery.isEmpty() -> chapters
                searchQuery.isDigitsOnly() -> {
                    if (searchQuery.isNotEmpty()) scope.launch {
                        delay(1000)
                        listState.animateScrollToItem(searchQuery.toInt() - 1)
                    }
                    chapters
                }

                searchQuery.startsWith("fav") -> chapters.filter { it.fav == 1 }
                searchQuery == "default" -> chapters.sortedBy { it.sura }
                searchQuery == "alphabet" -> chapters.sortedBy { it.nameArabic }
                searchQuery == "ayaIncrease" -> chapters.sortedBy { it.ayaCount }
                searchQuery == "ayaDecrease" -> chapters.sortedByDescending { it.ayaCount }
                searchQuery == "revelation" -> chapters.sortedBy { it.revelationOrder }
                else -> chapters.filter { isContains(it.nameArabic, searchQuery) }
            }
        }
    }

    Scaffold(topBar = {
        AnimatedVisibility(
            visible = !isSearchBarOpen, enter = expandVertically(), exit = shrinkVertically()
        ) {
            DefaultAppBar(
                animatedContentScope = animatedContentScope,
                openDrawer = openDrawer,
                isFavShowing = isFavShowing,
                onShowBookmarkClick = { show ->
                    searchQuery = if (show) "fav" else ""
                    viewModel.showFav(show)
                },
                query = searchQuery,
                onQuery = { searchQuery = it },
                scrollToTop = {
                    scope.launch {
                        delay(1000)
                        listState.animateScrollToItem(0)
                    }
                },
                openSearchBar = {
                    if (isFavShowing) viewModel.showFav(false)
                    if (searchQuery.isNotEmpty()) searchQuery = ""
                    if (listState.firstVisibleItemIndex > 1) scope.launch {
                        listState.animateScrollToItem(0)
                    }
                    isSearchBarOpen = true
                },
                navigateToPage = {
                    viewModel.navigateToPage(it, navController)
                },
                navigateToJuz = {
                    viewModel.navigateToJuz(it, navController)
                },
                navigateToHizb = {
                    viewModel.navigateToHizb(it, navController)
                })
        }
        AnimatedVisibility(
            visible = isSearchBarOpen, enter = expandVertically(), exit = shrinkVertically()
        ) {
            SearchAppBar(
                query = searchQuery,
                updateQuery = { searchQuery = it },
                closeSearchBar = { isSearchBarOpen = false })
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
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                AnimatedVisibility(visible = isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 16.dp),
                        strokeCap = StrokeCap.Round
                    )
                }
                AnimatedVisibility(visible = chapters.isNotEmpty() && !isFavShowing && !isSearchBarOpen) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(), state = rememberLazyListState().apply {
                            scope.launch {
                                animateScrollToItem(0)
                            }
                        }) {
                        bookmarkedVerse?.let { verse ->
                            item {
                                ElevatedButton(
                                    modifier = Modifier.padding(horizontal = 2.dp),
                                    onClick = { navController.navigate("sura/${verse.surahID}/${verse.verseID}") }) {
                                    Icon(
                                        imageVector = Icons.Default.Book, contentDescription = ""
                                    )
                                    AnimatedContent(targetState = verse) { v ->
                                        Text(
                                            text = chapters.find { it.sura == v.surahID }?.nameArabic + (" " + stringResource(
                                                id = R.string.aya
                                            ) + " " + numeral.format(v.verseID)),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                        )
                                    }
                                }
                            }
                        }
                        if (pageType == 0 && lastVisitedList.isNotEmpty()) items(
                            items = lastVisitedList,
                            key = { it.id }) { last ->
                            ElevatedButton(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null,
                                        placementSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    ),
                                onClick = { navController.navigate("sura/${last.suraID}/${last.ayaID}") }) {
                                Text(
                                    text = chapters.find { it.sura == last.suraID }?.nameArabic + (" " + stringResource(
                                        id = R.string.aya
                                    ) + " " + numeral.format(
                                        last.ayaID
                                    )),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                )
                            }
                        }
                        else if (pageType == 1 && lastVisitedPages.isNotEmpty()) items(
                            items = lastVisitedPages,
                            key = { it.id }) { page ->
                            ElevatedButton(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null,
                                        placementSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    ), onClick = {
                                    viewModel.navigateToPage(page.page, navController)
                                }) {
                                Text(
                                    text = numeral.format(stringResource(R.string.page) + " ${page.page}"),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                )
                            }
                        }
                    }
                }
                AnimatedVisibility(visible = !isLoading && searchQuery.isEmpty() && chapters.isEmpty()) {
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
                            File("/data/data/${context.packageName}/databases/quran_v3.db-shm").let {
                                if (it.exists()) it.delete()
                            }
                            File("/data/data/${context.packageName}/databases/quran_v3.db-wal").let {
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
                    if (filteredChapters.isNotEmpty() && qcfChapters.isNotEmpty()) itemsIndexed(
                        filteredChapters, key = { _, chapter -> chapter.sura }) { index, chapter ->
                        QuranChapterItem(
                            modifier = Modifier.animateItem(
                                fadeInSpec = null, fadeOutSpec = null, placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                            chapter = chapter,
                            rowID = index + 1,
                            query = searchQuery,
                            isInSearch = isSearchBarOpen,
                            qcfText = qcfChapters[chapter.sura - 1].text,
                            onFavClick = {
                                viewModel.updateFav(chapter)
                            },
                            cardClick = {
                                navController.navigate("sura/${chapter.sura}/1")
                            })
                    }
                    else if (searchQuery.isNotEmpty()) item {
                        NothingFoundUIElement()
                    }
                }
            }
        }
    }
    BackHandler(enabled = isSearchBarOpen) {
        isSearchBarOpen = false
        if (searchQuery.isNotEmpty()) searchQuery = ""
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DefaultAppBar(
    animatedContentScope: AnimatedContentScope,
    openDrawer: () -> Unit,
    isFavShowing: Boolean,
    onShowBookmarkClick: (Boolean) -> Unit,
    query: String,
    onQuery: (String) -> Unit,
    scrollToTop: () -> Unit,
    openSearchBar: () -> Unit,
    navigateToPage: (Int) -> Unit,
    navigateToJuz: (Int) -> Unit,
    navigateToHizb: (Int) -> Unit
) {
    val context = LocalContext.current
    var showSortDialog by remember { mutableStateOf(false) }
    var showGoToPageDialog by remember { mutableStateOf(false) }
    var showGoToJuzDialog by remember { mutableStateOf(false) }
    var showGoToHizbDialog by remember { mutableStateOf(false) }
    val numeral by numeral.collectAsState()
    TopAppBar(title = {
        Text(text = stringResource(id = R.string.chapter))
    }, navigationIcon = {
        NavigationOpenNavigationRailIcon(animatedContentScope, openDrawer)
    }, colors = appTopAppBarColors(), actions = {
        // Favorite
        AnimatedContent(
            targetState = isFavShowing, transitionSpec = {
                if (isFavShowing) slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) { -it } togetherWith slideOutHorizontally { it }
                else slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) { it } togetherWith slideOutHorizontally { -it }
            }) {
            AppIconButton(
                icon = if (it) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                title = stringResource(id = R.string.favorite)
            ) {
                onShowBookmarkClick(!isFavShowing)
            }
        }
        //Search
        AppIconButton(
            icon = Icons.Filled.Search, title = stringResource(id = R.string.search)
        ) {
            openSearchBar()
        }
        //More ...
        ThreeDotsDropdownMenu(animatedContentScope) { closeMenu ->
            //Chapter Sort
            AppDropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.sort_order)) },
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
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text(text = stringResource(R.string.cancel), fontWeight = FontWeight.SemiBold)
                }
            },
            text = {
                LazyColumn {
                    itemsIndexed(items = sortList, key = { index, _ -> index }) { index, item ->
                        TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
                            showSortDialog = false
                            when (index) {
                                0 -> onQuery("default")
                                1 -> onQuery("alphabet")
                                2 -> onQuery("revelation")
                                3 -> onQuery("ayaIncrease")
                                4 -> onQuery("ayaDecrease")
                            }
                            scrollToTop()
                        }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = item,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                when (query) {
                                    "" -> if (index == 0) Icon(
                                        imageVector = Icons.Default.Check, contentDescription = ""
                                    )

                                    "default" -> if (index == 0) Icon(
                                        imageVector = Icons.Default.Check, contentDescription = ""
                                    )

                                    "alphabet" -> if (index == 1) Icon(
                                        imageVector = Icons.Default.Check, contentDescription = ""
                                    )

                                    "revelation" -> if (index == 2) Icon(
                                        imageVector = Icons.Default.Check, contentDescription = ""
                                    )

                                    "ayaIncrease" -> if (index == 3) Icon(
                                        imageVector = Icons.Default.Check, contentDescription = ""
                                    )

                                    "ayaDecrease" -> if (index == 4) Icon(
                                        imageVector = Icons.Default.Check, contentDescription = ""
                                    )

                                    else -> Unit
                                }
                            }
                        }
                    }
                }
            },
            title = { Text(text = stringResource(id = R.string.chapter_sort_dialog_title)) },
            icon = {
                Icon(imageVector = Icons.AutoMirrored.Default.Sort, contentDescription = "")
            })
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
                navigateToPage(pageNumber.toInt())
            }, enabled = isCorrectPage) {
                Text(
                    text = stringResource(id = R.string.go_to_page),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }, dismissButton = {
            TextButton(onClick = { showGoToPageDialog = false }) {
                Text(text = stringResource(id = R.string.cancel), fontWeight = FontWeight.SemiBold)
            }
        }, text = {
            TextField(
                value = pageNumber,
                onValueChange = {
                    pageNumber = it
                    isCorrectPage =
                        pageNumber.isNotEmpty() && pageNumber.isDigitsOnly() && pageNumber.toInt() > 0 && pageNumber.toInt() <= 604
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(text = stringResource(id = R.string.page)) },
                isError = !isCorrectPage,
                supportingText = {
                    AnimatedVisibility(visible = !isCorrectPage) {
                        Text(text = numeral.format(stringResource(id = R.string.page_out_of_bounds)))
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
        }, icon = {
            Icon(imageVector = Icons.AutoMirrored.Default.Shortcut, contentDescription = "")
        })

    }

    //GoToJuz Dialog
    if (showGoToJuzDialog) {
        val juz = mutableListOf<String>()
        for (i in 1..30) juz.add(numeral.format(i))
        AlertDialog(onDismissRequest = { showGoToJuzDialog = false }, confirmButton = {
            TextButton(onClick = { showGoToJuzDialog = false }) {
                Text(text = stringResource(R.string.cancel), fontWeight = FontWeight.SemiBold)
            }
        }, text = {
            Box {
                val state = rememberLazyListState()
                LazyColumn(state = state) {
                    itemsIndexed(items = juz, key = { index, _ -> index }) { index, item ->
                        AppDropdownMenuItem(text = {
                            Text(text = item, fontWeight = FontWeight.SemiBold)
                        }, onClick = {
                            showGoToJuzDialog = false
                            navigateToJuz(index + 1)
                        })
                    }
                }
                ScrollShadow(listState = state)
            }
        }, title = { Text(text = stringResource(id = R.string.select_juz)) }, icon = {
            Icon(imageVector = Icons.AutoMirrored.Default.Shortcut, contentDescription = "")
        })
    }

    //GoToHizb Dialog
    if (showGoToHizbDialog) {
        val hizb = mutableListOf<String>()
        for (i in 1..120) hizb.add(numeral.format(i))
        AlertDialog(onDismissRequest = { showGoToHizbDialog = false }, confirmButton = {
            TextButton(onClick = { showGoToHizbDialog = false }) {
                Text(text = stringResource(R.string.cancel), fontWeight = FontWeight.SemiBold)
            }
        }, text = {
            Box {
                val state = rememberLazyListState()
                LazyColumn(state = state) {
                    itemsIndexed(items = hizb, key = { index, _ -> index }) { index, item ->
                        AppDropdownMenuItem(text = {
                            Text(text = item, fontWeight = FontWeight.SemiBold)
                        }, onClick = {
                            showGoToHizbDialog = false
                            navigateToHizb(index + 1)
                        })
                    }
                }
                ScrollShadow(listState = state)
            }
        }, title = { Text(text = stringResource(id = R.string.select_hizb)) }, icon = {
            Icon(imageVector = Icons.AutoMirrored.Default.Shortcut, contentDescription = "")
        })
    }
}

private fun isContains(name: String, text: String): Boolean {
    text.getWordsForSearch().forEach { word ->
        if (name.contains(word)) return true
    }
    return false
}
