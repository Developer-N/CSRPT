package ir.namoo.quran.bookmarks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.quran.utils.quranFont
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BookmarksScreen(
    drawerState: DrawerState,
    navController: NavHostController,
    viewModel: BookmarkViewModel = koinViewModel()
) {
    viewModel.loadData()
    val scope = rememberCoroutineScope()
    val isLoading by viewModel.isLoading.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val chapters by viewModel.chapters.collectAsState()

    Scaffold(topBar = {
        TopAppBar(title = {
            Column {
                Text(
                    text = stringResource(id = R.string.bookmarks),
                    fontSize = 16.sp
                )
                AnimatedVisibility(visible = !isLoading) {
                    Text(
                        text = formatNumber(bookmarks.size),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
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
        }, colors = appTopAppBarColors())
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
                            .padding(8.dp, 4.dp)
                            .height(2.dp),
                        strokeCap = StrokeCap.Round
                    )
                }
                if (bookmarks.isNotEmpty() && chapters.isNotEmpty()) LazyColumn(state = rememberLazyListState()) {
                    items(items = bookmarks, key = { it.id }) { quran ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .animateItemPlacement()
                        ) {
                            var title =
                                chapters.find { it.sura == quran.surahID }?.nameArabic ?: " - "
                            title += " " + stringResource(id = R.string.aya) + " "
                            title += formatNumber(quran.verseID)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = title,
                                    fontSize = 16.sp
                                )
                                IconButton(onClick = { viewModel.removeBookmark(quran) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Bookmark,
                                        contentDescription = stringResource(id = R.string.bookmarks),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                ElevatedAssistChip(modifier = Modifier,
                                    onClick = { navController.navigate("sura/${quran.surahID}/${quran.verseID}") },
                                    label = {
                                        Text(
                                            text = stringResource(
                                                id = R.string.go_to_aya
                                            )
                                        )
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
                                text = quran.quranArabic,
                                fontFamily = FontFamily(quranFont)
                            )
                        }
                    }
                }
                else if (!isLoading) NothingFoundUIElement()

            }
        }
    }
}
