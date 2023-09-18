package ir.namoo.quran.chapters

import android.annotation.SuppressLint
import android.text.InputType
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.colorAppBar
import ir.namoo.commons.utils.colorOnAppBar
import ir.namoo.commons.utils.iconColor
import ir.namoo.quran.utils.chapterException
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
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

    viewModel.loadData()
    Scaffold(topBar = {
        AnimatedVisibility(
            visible = viewModel.searchBarState == SearchBarState.CLOSED,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            DefaultAppBar(scope, drawerState, viewModel, listState, navController)
        }
        AnimatedVisibility(
            visible = viewModel.searchBarState == SearchBarState.OPENED,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            SearchAppBar(viewModel)
        }
    }) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
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
                LazyRow(modifier = Modifier.fillMaxWidth(), state = rememberLazyListState().apply {
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
                                    )), fontFamily = FontFamily(appFont)
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
                            fontFamily = FontFamily(appFont),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(viewModel: ChapterViewModel) {
    val query by viewModel.query.collectAsState()
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
fun DefaultAppBar(
    scope: CoroutineScope,
    drawerState: DrawerState,
    viewModel: ChapterViewModel,
    listState: LazyListState,
    navController: NavHostController
) {
    val context = LocalContext.current
    var showMore by remember { mutableStateOf(false) }
    val isFavShowing by viewModel.isFavShowing.collectAsState()
    val scale = remember { Animatable(1f) }
    TopAppBar(title = {
        Text(
            text = stringResource(id = R.string.chapter), fontFamily = FontFamily(appFont)
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
    ), actions = {
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
            viewModel.searchBarState = SearchBarState.OPENED
        }) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = stringResource(id = R.string.search)
            )
        }
        //More ...
        IconButton(onClick = { showMore = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "DropDown Menu"
            )
        }
        DropdownMenu(expanded = showMore, onDismissRequest = { showMore = false }) {
            //Chapter Sort
            DropdownMenuItem(text = {
                Text(
                    text = stringResource(id = R.string.sort_order),
                    fontFamily = FontFamily(appFont)
                )
            }, onClick = {
                val sortList = arrayOf(
                    context.getString(R.string.default_sort),
                    context.getString(R.string.alphabet_sort),
                    context.getString(R.string.revelation_sort),
                    context.getString(R.string.aya_inc_sort),
                    context.getString(R.string.aya_dec_sort)
                )
                MaterialAlertDialogBuilder(context).apply {
                    setTitle(context.getString(R.string.chapter_sort_dialog_title))
                    setItems(sortList) { _, which ->
                        run {
                            when (which) {
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
                        }
                    }
                    show()
                }
                showMore = false
            })
            //Go to Page
            DropdownMenuItem(text = {
                Text(
                    text = stringResource(id = R.string.go_to_page),
                    fontFamily = FontFamily(appFont)
                )
            }, onClick = {
                showMore = false
                MaterialAlertDialogBuilder(context).apply {
                    setTitle(context.getString(R.string.select_page))
                    val input = AppCompatEditText(context)
                    input.inputType = InputType.TYPE_CLASS_NUMBER
                    setView(input)
                    setPositiveButton(context.getString(R.string.go_to_page)) { _, _ ->
                        if (input.text.toString().toInt() in 1..604) run {
                            scope.launch {
                                val page = viewModel.getPage(input.text.toString().toInt())
                                navController.navigate("sura/${page.sura}/${page.aya}")
                            }
                        }
                        else Toast.makeText(
                            context,
                            context.getString(R.string.page_out_of_bounds),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    show()
                }
            })
            //Go to Juz
            DropdownMenuItem(text = {
                Text(
                    text = stringResource(id = R.string.go_to_juz),
                    fontFamily = FontFamily(appFont)
                )
            }, onClick = {
                showMore = false
                MaterialAlertDialogBuilder(context).apply {
                    setTitle(context.getString(R.string.select_juz))
                    val juz = mutableListOf<String>()
                    for (i in 1..30) juz.add(formatNumber(i))
                    setItems(juz.toTypedArray()) { _, witch ->
                        run {
                            scope.launch {
                                val j = viewModel.getJuz(witch + 1)
                                navController.navigate("sura/${j.sura}/${j.aya}")
                            }
                        }
                    }
                    show()
                }
            })
            //Go to hizb
            DropdownMenuItem(text = {
                Text(
                    text = stringResource(id = R.string.go_to_hizb),
                    fontFamily = FontFamily(appFont)
                )
            }, onClick = {
                showMore = false
                MaterialAlertDialogBuilder(context).apply {
                    setTitle(context.getString(R.string.select_hizb))
                    val hizb = mutableListOf<String>()
                    for (i in 1..120) hizb.add(formatNumber(i))
                    setItems(hizb.toTypedArray()) { _, witch ->
                        run {
                            scope.launch {
                                val h = viewModel.getHizb(witch + 1)
                                navController.navigate("sura/${h.sura}/${h.aya}")
                            }
                        }
                    }
                    show()
                }
            })
        }

    })
}
