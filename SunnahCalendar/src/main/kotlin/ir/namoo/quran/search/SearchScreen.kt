package ir.namoo.quran.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.quran.settings.MyBtnGroup
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = koinViewModel()
) {
    viewModel.loadData()
    val isLoading by viewModel.isLoading.collectAsState()
    val isInSearch by viewModel.isInSearch.collectAsState()
    val quranList by viewModel.quranList.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val query by viewModel.query.collectAsState()
    val focus = LocalFocusManager.current
    val searchInTranslates by viewModel.searchInTranslates.collectAsState()

    Scaffold(topBar = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            SearchBar(modifier = Modifier.fillMaxWidth(),
                query = query,
                onQueryChange = { viewModel.updateQuery(it) },
                onSearch = {
                    focus.clearFocus()
                    viewModel.search()
                },
                active = false,
                onActiveChange = {},
                placeholder = {
                    Text(text = stringResource(id = R.string.search))
                },
                leadingIcon = {
                    IconButton(onClick = {
                        focus.clearFocus()
                        viewModel.search()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(id = R.string.search),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                trailingIcon = {
                    IconButton(onClick = {
                        focus.clearFocus()
                        if (query.isNotEmpty()) viewModel.updateQuery("")
                        else navController.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(id = R.string.close),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }) { }
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
                val items = listOf(
                    stringResource(id = R.string.search_in_quran),
                    stringResource(id = R.string.search_in_quran_and_translates)
                )
                MyBtnGroup(
                    title = "",
                    items = items,
                    checkedItem = if (searchInTranslates) items[1] else items[0],
                    onCheckChanged = { viewModel.updateSearchInTranslate() }
                )
                AnimatedVisibility(visible = isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp, 4.dp)
                            .height(4.dp),
                        strokeCap = StrokeCap.Round
                    )
                }
                AnimatedVisibility(
                    visible = !isLoading && query.isNotEmpty() && isInSearch,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        text = String.format(
                            stringResource(id = R.string.result_found),
                            formatNumber(quranList.size)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
                if (quranList.isNotEmpty() && chapters.isNotEmpty()) LazyColumn(
                    state = rememberLazyListState()
                ) {
                    items(items = quranList, key = { it.id }) { quran ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .animateItemPlacement(),
                            colors = CardDefaults.elevatedCardColors(),
                            elevation = CardDefaults.elevatedCardElevation()
                        ) {
                            var title =
                                chapters.find { it.sura == quran.surahID }?.nameArabic ?: " - "
                            title += " " + stringResource(id = R.string.aya) + " "
                            title += formatNumber(quran.verseID)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier.padding(8.dp, 0.dp),
                                    text = title,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                ElevatedAssistChip(modifier = Modifier.padding(8.dp, 0.dp),
                                    onClick = { navController.navigate("sura/${quran.surahID}/${quran.verseID}") },
                                    label = {
                                        Text(text = stringResource(id = R.string.go_to_aya))
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowCircleRight,
                                            contentDescription = stringResource(id = R.string.go_to_aya)
                                        )
                                    })
                            }
                            HorizontalDivider()
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                text = quran.quranArabic
                            )
                        }
                    }
                } else if (!isLoading && query.isNotEmpty() && isInSearch)
                    NothingFoundUIElement()
            }
        }
    }
}
