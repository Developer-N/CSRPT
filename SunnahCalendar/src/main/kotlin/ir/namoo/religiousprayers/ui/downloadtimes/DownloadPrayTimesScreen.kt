package ir.namoo.religiousprayers.ui.downloadtimes

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.NavigationOpenNavigationRailIcon
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import ir.namoo.religiousprayers.ui.shared.LoadingUIElement
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import ir.namoo.religiousprayers.ui.shared.SearchAppBar
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DownloadPrayTimesScreen(
    openNavigationRail: () -> Unit,
    animatedContentScope: AnimatedContentScope,
    viewModel: DownloadPrayTimesViewModel = koinViewModel()
) {
    val context = LocalContext.current
    viewModel.loadData(context)
    val isLoading by viewModel.isLoading.collectAsState()
    val isSearchBoxIsOpen by viewModel.isSearchBoxIsOpen.collectAsState()
    val cityItemState = viewModel.cityIteState
    var query by remember { mutableStateOf("") }
    val filteredList by remember {
        derivedStateOf {
            if (query.isNotEmpty())
                cityItemState.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            else cityItemState
        }
    }
    Scaffold(topBar = {
        AnimatedVisibility(
            visible = isSearchBoxIsOpen, enter = expandVertically(), exit = shrinkVertically()
        ) {
            SearchAppBar(
                query = query,
                updateQuery = { query = it },
                closeSearchBar = { viewModel.closeSearch() })
        }
        AnimatedVisibility(
            !isSearchBoxIsOpen, enter = expandVertically(), exit = shrinkVertically()
        ) {
            DefaultDTTopAppBar(
                openNavigationRail = openNavigationRail,
                animatedContentScope = animatedContentScope,
                openSearch = { viewModel.openSearch() })
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
                    .padding(bottom = paddingValues.calculateBottomPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                AnimatedVisibility(visible = isLoading) {
                    LoadingUIElement()
                }
                AnimatedVisibility(visible = !isLoading && cityItemState.isNotEmpty()) {
                    Text(
                        text = stringResource(id = R.string.available_cities_list),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                    )
                }
                AnimatedVisibility(visible = filteredList.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp)
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(4f)
                                .padding(horizontal = 8.dp),
                            text = stringResource(id = R.string.city),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize
                        )
                        Text(
                            modifier = Modifier.weight(5f),
                            text = stringResource(id = R.string.update_date),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize
                        )
                    }
                }
                if (filteredList.isNotEmpty()) {
                    LazyColumn {
                        items(items = filteredList, key = { it.id }) { city ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(
                                        fadeInSpec = null, fadeOutSpec = null,
                                        placementSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                            ) {
                                CityItemUIElement(
                                    city = city,
                                    searchText = query,
                                    download = { viewModel.download(city, context) })
                            }
                        }
                    }
                } else if (!isLoading && query.isNotEmpty()) NothingFoundUIElement()
                AnimatedVisibility(visible = cityItemState.isEmpty() && !isLoading) {
                    ElevatedButton(
                        modifier = Modifier.padding(16.dp),
                        onClick = { viewModel.loadData(context) }) {
                        Text(text = stringResource(id = R.string.str_retry))
                        Spacer(modifier = Modifier.padding(4.dp))
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DefaultDTTopAppBar(
    openNavigationRail: () -> Unit,
    animatedContentScope: AnimatedContentScope,
    openSearch: () -> Unit
) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.download_upload)) },
        colors = appTopAppBarColors(),
        navigationIcon = {
            NavigationOpenNavigationRailIcon(
                animatedContentScope,
                openNavigationRail
            )
        },
        actions = {
            AppIconButton(
                title = stringResource(id = R.string.search),
                onClick = { openSearch() },
                icon = Icons.Default.Search
            )
        })
}
