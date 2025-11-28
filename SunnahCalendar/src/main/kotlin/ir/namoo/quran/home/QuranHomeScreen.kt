package ir.namoo.quran.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.utils.isLight
import ir.namoo.quran.bookmarks.BookmarksScreen
import ir.namoo.quran.chapters.ChaptersScreen
import ir.namoo.quran.download.DownloadScreen
import ir.namoo.quran.mushaf.MushafScreen
import ir.namoo.quran.notes.NotesScreen
import ir.namoo.quran.search.SearchScreen
import ir.namoo.quran.settings.SettingsScreen
import ir.namoo.quran.sura.SuraScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun QuranHomeScreen(
    startSura: Int = -1,
    startAya: Int = -1,
    pageType: Int = 1,
    reload: () -> Unit,
    exit: () -> Unit,
    createShortcut: () -> Unit,
    checkFiles: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    SharedTransitionLayout {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(windowInsets = WindowInsets(0, 0, 0, 0)) {
                    run {
                        val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
                        val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
                        val needsVisibleStatusBarPlaceHolder =
                            !isBackgroundColorLight && isSurfaceColorLight
                        Spacer(
                            Modifier
                                .fillMaxWidth()
                                .then(
                                    if (needsVisibleStatusBarPlaceHolder) Modifier.background(
                                        Brush.verticalGradient(
                                            0f to Color(0x70000000), 1f to Color.Transparent
                                        )
                                    ) else Modifier
                                )
                                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
                        )
                    }

                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        //Header
                        Row(
                            modifier = Modifier.padding(8.dp, 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(ShapeDefaults.ExtraLarge),
                                painter = painterResource(id = R.drawable.quran_drawer),
                                contentDescription = stringResource(
                                    id = R.string.quran
                                ),
                                contentScale = ContentScale.FillWidth

                            )
                        }
                        // Items
                        navItems.forEach { (id, icon, title) ->
                            NavigationDrawerItem(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                label = {
                                    AnimatedContent(
                                        targetState = stringResource(title),
                                        label = "title",
                                        transitionSpec = appCrossfadeSpec,
                                    ) { state -> Text(state) }
                                },
                                selected = navBackStackEntry?.destination?.route == id,
                                onClick = {
                                    when (id) {
                                        exitRoute -> exit()
                                        shortcutRoute -> {
                                            createShortcut()
                                            scope.launch { drawerState.close() }
                                        }

                                        else -> scope.launch {
                                            drawerState.close()
                                            if (navBackStackEntry?.destination?.route != id) {
                                                navController.navigate(id)
                                            }
                                        }
                                    }
                                },
                            )
                        }
                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
                    }
                }
            },
        ) {
            NavHost(navController = navController, startDestination = chapterRoute) {
                //Chapters Screen
                composable(route = chapterRoute) {
                    ChaptersScreen(
                        this, { scope.launch { drawerState.open() } }, navController, checkFiles
                    )
                }
                //Sura Screen
                composable(
                    route = "sura/{sura}/{aya}?play={play}",
                    arguments = listOf(
                        navArgument("sura") { type = NavType.IntType },
                        navArgument("aya") { type = NavType.IntType },
                        navArgument("play") {
                            type = NavType.BoolType
                            defaultValue = false
                        })
                ) { backStackEntry ->
                    val sura = backStackEntry.arguments?.getInt("sura") ?: 1
                    val aya = backStackEntry.arguments?.getInt("aya") ?: 1
                    if (pageType == 0) {
                        SuraScreen(
                            startSura = sura,
                            aya = aya,
                            animatedContentScope = this,
                            openDrawer = { scope.launch { drawerState.open() } }
                        )
                    } else {
                        MushafScreen(
                            sura = sura,
                            aya = aya,
                            animatedContentScope = this,
                            openDrawer = { scope.launch { drawerState.open() } })
                    }
                }
                //Search Screen
                composable(route = searchRoute) {
                    SearchScreen(
                        navigationUp = { navController.navigateUp() },
                        navigateToVerse = { sura, verse ->
                            navController.navigate("sura/$sura/$verse")
                        })
                }
                //Notes Screen
                composable(route = notesRoute) {
                    NotesScreen(
                        animatedContentScope = this,
                        openDrawer = { scope.launch { drawerState.open() } },
                        navigateToVerse = { sura, verse ->
                            navController.navigate("sura/$sura/$verse")
                        })
                }
                //Bookmarks Screen
                composable(route = bookmarksRoute) {
                    BookmarksScreen(
                        animatedContentScope = this,
                        openDrawer = { scope.launch { drawerState.open() } },
                        navigateToVerse = { sura, verse ->
                            navController.navigate("sura/$sura/$verse")
                        })
                }
                //Download Screen
                composable(route = downloadRoute) {
                    DownloadScreen(
                        animatedContentScope = this,
                        openDrawer = { scope.launch { drawerState.open() } })
                }
                //Setting Screen
                composable(route = settingsRoute) {
                    SettingsScreen(
                        animatedContentScope = this,
                        reload = reload,
                        openDrawer = { scope.launch { drawerState.open() } })
                }
            }
            BackHandler(enabled = drawerState.isOpen) {
                scope.launch {
                    drawerState.close()
                }
            }
        }
    }

    LaunchedEffect(key1 = "startFromNotification") {
        if (startSura != -1 && startAya != -1) navController.navigate("sura/$startSura/$startAya") {
            popUpTo(chapterRoute)
        }
    }
}

private const val chapterRoute = "chapters"
private const val searchRoute = "search"
private const val notesRoute = "notes"
private const val bookmarksRoute = "bookmarks"
private const val downloadRoute = "download"
private const val settingsRoute = "setting"
private const val shortcutRoute = "shortcut"
private const val exitRoute = "exit"

@Stable
private val navItems: List<Triple<String, ImageVector, Int>> = listOf(
    Triple(chapterRoute, Icons.Default.Menu, R.string.chapter),
    Triple(searchRoute, Icons.Default.Search, R.string.search_the_whole_quran),
    Triple(notesRoute, Icons.Default.EditNote, R.string.notes),
    Triple(bookmarksRoute, Icons.Default.Bookmarks, R.string.bookmarks),
    Triple(downloadRoute, Icons.Default.CloudDownload, R.string.download_audios),
    Triple(settingsRoute, Icons.Default.Settings, R.string.settings),
    Triple(shortcutRoute, Icons.Default.AppShortcut, R.string.create_shortcut),
    Triple(exitRoute, Icons.Default.Cancel, R.string.exit),
)
