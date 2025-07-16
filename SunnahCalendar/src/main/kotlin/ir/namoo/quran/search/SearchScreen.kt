package ir.namoo.quran.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.quran.settings.MyBtnGroup
import ir.namoo.quran.utils.quranFont
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navigationUp: () -> Unit,
    navigateToVerse: (Int, Int) -> Unit,
    viewModel: SearchViewModel = koinViewModel()
) {
    viewModel.loadData()
    val isLoading by viewModel.isLoading.collectAsState()
    val isInSearch by viewModel.isInSearch.collectAsState()
    val quranList = viewModel.quranList
    val chapters = viewModel.chapters
    val query by viewModel.query.collectAsState()
    val focus = LocalFocusManager.current
    val searchInTranslates by viewModel.searchInTranslates.collectAsState()

    Scaffold(topBar = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            SearchBar(modifier = Modifier.fillMaxWidth(), inputField = {
                SearchBarDefaults.InputField(query = query,
                    onQueryChange = { viewModel.updateQuery(it) },
                    onSearch = {
                        focus.clearFocus()
                        viewModel.search()
                    },
                    expanded = false,
                    onExpandedChange = {},
                    placeholder = {
                        Text(text = stringResource(id = R.string.search))
                    },
                    leadingIcon = {
                        IconButton(onClick = {
                            focus.clearFocus()
                            viewModel.search()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(id = R.string.search),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            focus.clearFocus()
                            if (query.isNotEmpty()) viewModel.updateQuery("")
                            else navigationUp()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(id = R.string.close),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    })
            }, expanded = false, onExpandedChange = {}) { }
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
                val items = listOf(
                    stringResource(id = R.string.search_in_quran),
                    stringResource(id = R.string.search_in_quran_and_translates)
                )
                MyBtnGroup(title = "",
                    items = items,
                    checkedItem = if (searchInTranslates) items[1] else items[0],
                    onCheckChanged = { viewModel.updateSearchInTranslate() })
                AnimatedVisibility(visible = isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        strokeCap = StrokeCap.Round
                    )
                }
                AnimatedVisibility(
                    visible = quranList.isNotEmpty() && query.isNotEmpty() && isInSearch,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp), text = String.format(
                            stringResource(id = R.string.result_found), formatNumber(quranList.size)
                        ), textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (quranList.isNotEmpty() && chapters.isNotEmpty()) LazyColumn {
                    items(items = quranList, key = { it.id }) { quran ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .animateItem(
                                    fadeInSpec = null, fadeOutSpec = null, placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ), shape = MaterialTheme.shapes.extraLarge
                        ) {
                            var title =
                                chapters.find { it.sura == quran.surahID }?.nameArabic ?: " - "
                            title += " " + stringResource(id = R.string.aya) + " "
                            title += formatNumber(quran.verseID)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    modifier = Modifier.padding(4.dp),
                                    text = title,
                                    fontWeight = FontWeight.SemiBold
                                )
                                ElevatedButton(onClick = {
                                    navigateToVerse(quran.surahID, quran.verseID)
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.Reply,
                                        contentDescription = stringResource(id = R.string.go_to_aya)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(id = R.string.go_to_aya),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            HorizontalDivider()
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                text = quran.quranArabic,
                                fontFamily = FontFamily(quranFont)
                            )
                        }
                    }
                } else if (!isLoading && query.isNotEmpty() && isInSearch) NothingFoundUIElement()
            }
        }
    }
}
