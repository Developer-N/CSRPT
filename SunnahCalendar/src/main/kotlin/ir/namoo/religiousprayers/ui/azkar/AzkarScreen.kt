package ir.namoo.religiousprayers.ui.azkar

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuCheckableItem
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuExpandableItem
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuRadioItem
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import ir.namoo.commons.DEFAULT_AZKAR_LANG
import ir.namoo.commons.PREF_AZKAR_LANG
import ir.namoo.commons.PREF_AZKAR_REINDER
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.religiousprayers.ui.shared.LoadingUIElement
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import ir.namoo.religiousprayers.ui.shared.SearchAppBar
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AzkarScreen(openDrawer: () -> Unit, viewModel: AzkarViewModel = koinViewModel()) {
    viewModel.loadData()
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val query by viewModel.query.collectAsState()
    val azkarLang by viewModel.azkarLang.collectAsState()
    val isFavoriteShown by viewModel.isFavShowing.collectAsState()
    val isSearchBoxIsOpen by viewModel.isSearchBoxIsOpen.collectAsState()

    Scaffold(topBar = {
        BackHandler(enabled = isSearchBoxIsOpen) { viewModel.closeSearch() }
        AnimatedVisibility(
            visible = isSearchBoxIsOpen, enter = expandVertically(), exit = shrinkVertically()
        ) {
            SearchAppBar(query = query,
                updateQuery = { viewModel.search(it) },
                closeSearchBar = { viewModel.closeSearch() })
        }
        AnimatedVisibility(
            !isSearchBoxIsOpen, enter = expandVertically(), exit = shrinkVertically()
        ) {
            DefaultAppBar(openDrawer, isFavoriteShown, viewModel)
        }
    }) { paddingValues ->
        Surface(
            shape = materialCornerExtraLargeTop(),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                AnimatedVisibility(isLoading) {
                    LoadingUIElement()
                }
                val listState = rememberLazyListState()
                if (chapters.isNotEmpty()) {
                    LazyColumn(state = listState) {
                        items(items = chapters, key = { chapter -> chapter.id }) { zikr ->
                            Box(modifier = Modifier.animateItemPlacement()) {
                                ZikrChapterUI(zikr,
                                    searchText = query,
                                    lang = azkarLang,
                                    onFavClick = { zkr -> viewModel.updateAzkarChapter(zkr) },
                                    onCardClick = { id ->
                                        context.startActivity(Intent(
                                            context, AzkarActivity::class.java
                                        ).apply {
                                            putExtra("chapterID", id)
                                        })
                                    })
                            }
                        }
                    }
                } else if (!isLoading && query.isNotEmpty()) {
                    NothingFoundUIElement()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultAppBar(
    openDrawer: () -> Unit, isFavoriteShown: Boolean, viewModel: AzkarViewModel
) {
    val context = LocalContext.current

    TopAppBar(title = {
        Column {
            Text(text = stringResource(id = R.string.azkar))
            Text(
                text = stringResource(id = R.string.hisnulmuslim),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    },
        colors = appTopAppBarColors(),
        navigationIcon = { NavigationOpenDrawerIcon(openDrawer) },
        actions = {
            AppIconButton(
                icon = Icons.Default.Search, title = stringResource(id = R.string.search)
            ) {
                viewModel.openSearch()
            }
            AppIconButton(
                icon = if (isFavoriteShown) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                title = stringResource(id = R.string.favorite)
            ) { viewModel.showBookmarks() }

            ThreeDotsDropdownMenu { closeMenu ->
                AppDropdownMenuCheckableItem(text = stringResource(id = R.string.azkar_reminder),
                    isChecked = context.appPrefsLite.getBoolean(PREF_AZKAR_REINDER, false),
                    setChecked = {
                        context.appPrefsLite.edit {
                            putBoolean(PREF_AZKAR_REINDER, it)
                        }
                        closeMenu()
                    })
                HorizontalDivider()
                var showLanguageSubMenu by rememberSaveable { mutableStateOf(false) }
                AppDropdownMenuExpandableItem(
                    text = stringResource(R.string.language),
                    isExpanded = showLanguageSubMenu,
                    onClick = { showLanguageSubMenu = !showLanguageSubMenu },
                )
                val selectedLang =
                    context.appPrefsLite.getString(PREF_AZKAR_LANG, DEFAULT_AZKAR_LANG)
                        ?: DEFAULT_AZKAR_LANG

                listOf(Language.FA, Language.CKB, Language.AR).forEach { lang ->
                    AnimatedVisibility(visible = showLanguageSubMenu) {
                        AppDropdownMenuRadioItem(text = lang.nativeName,
                            isSelected = lang.code == selectedLang,
                            setSelected = {
                                context.appPrefsLite.edit {
                                    putString(PREF_AZKAR_LANG, lang.code)
                                }
                                viewModel.setLang(lang.code)
                                closeMenu()
                            })
                    }
                }
            }
        })
}
