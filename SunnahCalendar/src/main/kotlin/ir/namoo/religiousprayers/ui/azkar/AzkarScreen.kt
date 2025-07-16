package ir.namoo.religiousprayers.ui.azkar

import android.content.Intent
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.AzkarScreen(
    openDrawer: () -> Unit,
    animatedContentScope: AnimatedContentScope,
    viewModel: AzkarViewModel = koinViewModel()
) {
    viewModel.loadData()
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val chapters = viewModel.chapters
    val azkarLang by viewModel.azkarLang.collectAsState()
    val isFavoriteShown by viewModel.isFavShowing.collectAsState()
    val isSearchBoxIsOpen by viewModel.isSearchBoxIsOpen.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val filteredChapters by remember {
        derivedStateOf {
            when {
                searchQuery.isBlank() -> chapters
                searchQuery.startsWith("fav") -> chapters.filter { it.fav == 1 }
                else -> chapters.filter {
                    when (azkarLang) {
                        Language.FA.code -> it.persian?.contains(searchQuery) == true
                        Language.CKB.code -> it.kurdish?.contains(searchQuery) == true
                        else -> it.arabic?.contains(searchQuery) == true
                    }
                }
            }
        }
    }

    Scaffold(topBar = {
        BackHandler(enabled = isSearchBoxIsOpen) { viewModel.closeSearch() }
        AnimatedVisibility(
            visible = isSearchBoxIsOpen, enter = expandVertically(), exit = shrinkVertically()
        ) {
            SearchAppBar(
                query = searchQuery,
                updateQuery = { searchQuery = it },
                closeSearchBar = { viewModel.closeSearch() })
        }
        AnimatedVisibility(
            !isSearchBoxIsOpen, enter = expandVertically(), exit = shrinkVertically()
        ) {
            DefaultAppBar(
                openDrawer,
                isFavoriteShown,
                openSearchBar = { viewModel.openSearch() },
                onShowBookmarkClick = { show ->
                    searchQuery = if (show) "fav" else ""
                    viewModel.showBookmarks(show)
                },
                setLang = { viewModel.setLang(it) },
                animatedContentScope = animatedContentScope
            )
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
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                AnimatedVisibility(isLoading) {
                    LoadingUIElement()
                }
                val listState = rememberLazyListState()
                if (filteredChapters.isNotEmpty()) {
                    LazyColumn(state = listState) {
                        items(items = filteredChapters, key = { chapter -> chapter.id }) { zikr ->
                            Box(
                                modifier = Modifier.animateItem(
                                    fadeInSpec = null, fadeOutSpec = null, placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            ) {
                                AzkarChapterUI(
                                    zikr,
                                    searchText = searchQuery,
                                    lang = azkarLang,
                                    onFavClick = { zkr -> viewModel.updateAzkarChapter(zkr) },
                                    onCardClick = { id ->
                                        context.startActivity(
                                            Intent(
                                                context, AzkarActivity::class.java
                                            ).apply {
                                                putExtra("chapterID", id)
                                            })
                                    })
                            }
                        }
                    }
                } else if (!isLoading && searchQuery.isNotEmpty()) {
                    NothingFoundUIElement()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.DefaultAppBar(
    openDrawer: () -> Unit,
    isFavoriteShown: Boolean,
    openSearchBar: () -> Unit,
    onShowBookmarkClick: (Boolean) -> Unit,
    setLang: (String) -> Unit,
    animatedContentScope: AnimatedContentScope
) {
    val context = LocalContext.current

    TopAppBar(
        title = {
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
        navigationIcon = { NavigationOpenDrawerIcon(animatedContentScope, openDrawer) },
        actions = {
            AppIconButton(
                icon = Icons.Default.Search, title = stringResource(id = R.string.search)
            ) {
                openSearchBar()
            }
            AnimatedContent(targetState = isFavoriteShown, label = "bookmark") {
                AppIconButton(
                    icon = if (it) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    title = stringResource(id = R.string.favorite)
                ) { onShowBookmarkClick(!isFavoriteShown) }
            }

            ThreeDotsDropdownMenu(animatedContentScope) { closeMenu ->
                AppDropdownMenuCheckableItem(
                    text = stringResource(id = R.string.azkar_reminder),
                    isChecked = context.appPrefsLite.getBoolean(PREF_AZKAR_REINDER, false),
                    setChecked = {
                        context.appPrefsLite.edit { putBoolean(PREF_AZKAR_REINDER, it) }
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
                        AppDropdownMenuRadioItem(
                            text = lang.nativeName,
                            isSelected = lang.code == selectedLang,
                            setSelected = {
                                context.appPrefsLite.edit {
                                    putString(PREF_AZKAR_LANG, lang.code)
                                }
                                setLang(lang.code)
                                closeMenu()
                            })
                    }
                }
            }
        })
}
