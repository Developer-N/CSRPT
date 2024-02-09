package ir.namoo.religiousprayers.ui.downloadtimes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import ir.namoo.religiousprayers.ui.shared.LoadingUIElement
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import ir.namoo.religiousprayers.ui.shared.SearchAppBar
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DownloadPrayTimesScreen(
    openDrawer: () -> Unit, viewModel: DownloadPrayTimesViewModel = koinViewModel()
) {
    val context = LocalContext.current
    viewModel.loadData(context)
    val isLoading by viewModel.isLoading.collectAsState()
    val query by viewModel.query.collectAsState()
    val addedCities by viewModel.addedCities.collectAsState()
    val cityItemState by viewModel.cityIteState.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val isSearchBoxIsOpen by viewModel.isSearchBoxIsOpen.collectAsState()
    Scaffold(topBar = {
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
            DefaultDTTopAppBar(openDrawer = openDrawer, openSearch = { viewModel.openSearch() })
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
                Text(
                    text = stringResource(id = R.string.available_cities_list),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                if (addedCities.isNotEmpty()) Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 8.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .weight(4f)
                            .padding(horizontal = 8.dp),
                        text = stringResource(id = R.string.city)
                    )
                    Text(
                        modifier = Modifier.weight(5f),
                        text = stringResource(id = R.string.update_date)
                    )
                }

                AnimatedVisibility(visible = isLoading) {
                    LoadingUIElement()
                }
                val listState = rememberLazyListState()
                if (addedCities.isNotEmpty() && cityItemState.isNotEmpty()) {
                    LazyColumn(state = listState) {
                        items(items = addedCities, key = { it.id }) { city ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItemPlacement()
                            ) {
                                CityItemUIElement(city = city,
                                    searchText = query,
                                    cityItemState = cityItemState[addedCities.indexOf(city)].apply {
                                        isSelected = selectedCity == city.name
                                    },
                                    download = { viewModel.download(city, context) })
                            }
                        }
                    }
                } else if (!isLoading && query.isNotEmpty()) NothingFoundUIElement()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultDTTopAppBar(openDrawer: () -> Unit, openSearch: () -> Unit) {
    TopAppBar(title = { Text(text = stringResource(id = R.string.download_upload)) },
        colors = appTopAppBarColors(),
        navigationIcon = { NavigationOpenDrawerIcon(openDrawer) },
        actions = {
            AppIconButton(
                title = stringResource(id = R.string.search),
                onClick = { openSearch() },
                icon = Icons.Default.Search
            )
        })
}
