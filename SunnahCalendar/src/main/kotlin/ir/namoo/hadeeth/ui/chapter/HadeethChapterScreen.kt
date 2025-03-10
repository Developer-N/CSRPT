package ir.namoo.hadeeth.ui.chapter

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.AppDropdownMenu
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.isOnCI
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.commons.utils.openUrlInCustomTab
import ir.namoo.hadeeth.repository.LanguageEntity
import ir.namoo.religiousprayers.ui.shared.LoadingUIElement
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.HadeethChapterScreen(
    openDrawer: () -> Unit,
    animatedContentScope: AnimatedContentScope,
    navigateToHadeeth: (String) -> Unit,
    viewModel: HadeethChapterViewModel = koinViewModel()
) {
    viewModel.loadData()

    val isLoading by viewModel.isLoading.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val route by viewModel.route.collectAsState()
    val totalItems by viewModel.totalItems.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val perPage by viewModel.perPage.collectAsState()
    val lastPage by viewModel.lastPage.collectAsState()
    val isChapterLoading by viewModel.isChapterLoading.collectAsState()
    val showRetry by viewModel.showRetry.collectAsState()
    val languages = viewModel.languages
    val selectedParent = viewModel.selectedParent
    val categories = viewModel.categories
    val rootsCategories by remember {
        derivedStateOf {
            categories.filter {
                it.parentID == if (selectedParent.isEmpty()) null else selectedParent.last()
            }
        }
    }

    BackHandler(enabled = selectedParent.isNotEmpty()) {
        viewModel.removeSelectedPatent()
    }

    Scaffold(topBar = {
        HadeethTopBar(
            languages,
            selectedLanguage,
            animatedContentScope,
            openDrawer,
            viewModel::onLanguageSelected,
            viewModel::clearCachedData
        )
    }) { paddingValues ->
        Surface(
            shape = materialCornerExtraLargeTop(),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(visible = isLoading) {
                    LoadingUIElement()
                }
                AnimatedVisibility(visible = showRetry) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ElevatedButton(onClick = viewModel::loadData) {
                            Text(
                                text = stringResource(R.string.str_retry),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = stringResource(R.string.str_retry)
                            )
                        }
                    }
                }
                AnimatedVisibility(visible = selectedParent.isNotEmpty()) {
                    ElevatedButton(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                        onClick = { viewModel.removeSelectedPatent() },
                        colors = ButtonDefaults.elevatedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = null
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                AnimatedContent(targetState = route) {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp), text = it, fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
                LazyColumn(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())) {
                    if (rootsCategories.isNotEmpty()) {
                        stickyHeader {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = MaterialTheme.shapes.extraLarge
                                    )
                                    .padding(8.dp),
                                text = stringResource(R.string.categories),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(items = rootsCategories, key = { it.id }) { category ->
                            HadeethCategoryItem(
                                category,
                                onItemClicked = { viewModel.addSelectedPatent(it) },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = null, fadeOutSpec = null, placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            )
                        }
                    }
                    if (chapters == null && isChapterLoading)
                        item {
                            LoadingUIElement()
                        }
                    chapters?.let { chapters ->
                        stickyHeader {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = MaterialTheme.shapes.extraLarge
                                    )
                                    .padding(vertical = 2.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                FilledIconButton(
                                    onClick = viewModel::loadPreviousPage,
                                    enabled = currentPage > 1,
                                    colors = IconButtonDefaults.filledIconButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.NavigateBefore,
                                        contentDescription = null
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(4.dp),
                                        text = stringResource(R.string.hadeeth_chapters) + formatNumber(
                                            " ($totalItems)"
                                        ),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    LazyRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(2.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                                shape = MaterialTheme.shapes.extraLarge
                                            )
                                            .padding(2.dp)
                                            .animateContentSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        items(
                                            items = (1..lastPage).toList(),
                                            key = { it }) { page ->
                                            Text(
                                                modifier = Modifier
                                                    .clickable(
                                                        onClick = { viewModel.updateCurrentPage(page) }
                                                    )
                                                    .border(
                                                        width = if (currentPage == page) 1.dp else 0.dp,
                                                        color = if (currentPage == page) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                        shape = MaterialTheme.shapes.extraLarge
                                                    )
                                                    .padding(horizontal = 10.dp),
                                                text = formatNumber(page),
                                                fontWeight = if (currentPage == page) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = if (currentPage == page) 16.sp else 14.sp,
                                                color = if (currentPage == page) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                FilledIconButton(
                                    onClick = viewModel::loadNextPage,
                                    enabled = currentPage < lastPage,
                                    colors = IconButtonDefaults.filledIconButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.NavigateNext,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        if (!isChapterLoading)
                            itemsIndexed(items = chapters.data,
                                key = { _, item -> item.id }) { index, chapter ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp, horizontal = 4.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .padding(vertical = 2.dp, horizontal = 4.dp)
                                        .animateItem(
                                            fadeInSpec = null,
                                            fadeOutSpec = null,
                                            placementSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                        .clickable(onClick = { navigateToHadeeth(chapter.id) })
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(4.dp),
                                        text = formatNumber("${index + 1 + (currentPage - 1) * perPage}. ${chapter.title}"),
                                        textAlign = TextAlign.Justify
                                    )
                                }
                            }
                        else
                            item {
                                LoadingUIElement()
                            }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SharedTransitionScope.HadeethTopBar(
    languages: List<LanguageEntity>,
    selectedLanguage: String,
    animatedContentScope: AnimatedContentScope,
    openDrawer: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    clearCachedData: () -> Unit
) {
    val activity = LocalActivity.current
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLanguages by remember { mutableStateOf(false) }

    TopAppBar(title = {
        Text(text = stringResource(R.string.hadeeth))
    }, navigationIcon = {
        NavigationOpenDrawerIcon(animatedContentScope, openDrawer)
    }, colors = appTopAppBarColors(), actions = {
        IconButton(
            modifier = if (LocalContext.current.isOnCI()) Modifier else Modifier.sharedElement(
                rememberSharedContentState(key = "key_language"),
                animatedVisibilityScope = animatedContentScope
            ), onClick = { showLanguages = true }) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = stringResource(R.string.language)
            )
            AppDropdownMenu(expanded = showLanguages,
                onDismissRequest = { showLanguages = false },
                content = {
                    languages.forEach { language ->
                        AppDropdownMenuItem(text = {
                            Text(
                                text = language.native,
                                fontWeight = if (language.code == selectedLanguage) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }, onClick = {
                            onLanguageSelected(language.code)
                            showLanguages = false
                        }, trailingIcon = {
                            if (language.code == selectedLanguage) Icon(
                                imageVector = Icons.Default.Check, contentDescription = null
                            )
                        })
                    }
                })
        }
        IconButton(onClick = { showAboutDialog = true }) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.about)
            )
        }
    })

    AnimatedVisibility(visible = showAboutDialog) {
        AlertDialog(onDismissRequest = { showAboutDialog = false }, icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }, title = {
            Text(
                text = stringResource(R.string.about), fontWeight = FontWeight.SemiBold
            )
        }, text = {
            Column {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    text = stringResource(R.string.about_hadeeth),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = {
                    activity?.openUrlInCustomTab("https://hadeethenc.com")
                }) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        text = "https://hadeethenc.com",
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }, confirmButton = {
            TextButton(onClick = { showAboutDialog = false }) {
                Text(text = stringResource(R.string.close), fontWeight = FontWeight.SemiBold)
            }
        }, dismissButton = {
            TextButton(
                onClick = {
                    showAboutDialog = false
                    clearCachedData()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(
                    text = stringResource(R.string.clear_cached_data),
                    fontWeight = FontWeight.SemiBold
                )
            }
        })
    }
}
