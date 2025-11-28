package ir.namoo.quran.bookmarks

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.ui.common.NavigationOpenNavigationRailIcon
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import ir.namoo.quran.utils.quranFont
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.BookmarksScreen(
    animatedContentScope: AnimatedContentScope,
    openDrawer: () -> Unit,
    navigateToVerse: (Int, Int) -> Unit,
    viewModel: BookmarkViewModel = koinViewModel()
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.loadData()
    }
    val isLoading by viewModel.isLoading.collectAsState()
    val bookmarks = viewModel.bookmarks
    val chapters = viewModel.chapters
    val numeral by numeral.collectAsState()

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = stringResource(id = R.string.bookmarks))
        }, navigationIcon = {
            NavigationOpenNavigationRailIcon(animatedContentScope, openDrawer)
        }, colors = appTopAppBarColors(), actions = {
            if (bookmarks.isNotEmpty()) AnimatedContent(
                targetState = bookmarks.size, label = "Size"
            ) {
                IconButton(onClick = {}) {
                    Text(text = numeral.format(it), fontWeight = FontWeight.SemiBold)
                }
            }
        })
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
                            .padding(8.dp),
                        strokeCap = StrokeCap.Round
                    )
                }
                if (bookmarks.isNotEmpty() && chapters.isNotEmpty()) LazyColumn {
                    items(items = bookmarks, key = { it.id }) { quran ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .animateItem(
                                    fadeInSpec = null, fadeOutSpec = null, placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ), shape = MaterialTheme.shapes.large
                        ) {
                            var title =
                                chapters.find { it.sura == quran.surahID }?.nameArabic ?: " - "
                            title += " " + stringResource(id = R.string.aya) + " "
                            title += numeral.format(quran.verseID)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = title, fontWeight = FontWeight.SemiBold)
                                IconButton(onClick = { viewModel.removeBookmark(quran) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Bookmark,
                                        contentDescription = stringResource(id = R.string.bookmarks),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                ElevatedButton(onClick = {
                                    navigateToVerse(
                                        quran.surahID, quran.verseID
                                    )
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
