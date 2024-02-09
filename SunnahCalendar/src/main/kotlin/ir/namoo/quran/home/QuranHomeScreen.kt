package ir.namoo.quran.home

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NoteAlt
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.utils.isLight
import ir.namoo.quran.bookmarks.BookmarksScreen
import ir.namoo.quran.chapters.ChaptersScreen
import ir.namoo.quran.download.DownloadScreen
import ir.namoo.quran.notes.NotesScreen
import ir.namoo.quran.search.SearchScreen
import ir.namoo.quran.settings.SettingsScreen
import ir.namoo.quran.sura.SuraScreen
import kotlinx.coroutines.launch

@Composable
fun QuranHomeScreen(
    startSura: Int = -1,
    startAya: Int = -1,
    exit: () -> Unit,
    createShortcut: () -> Unit,
    checkFiles: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    ModalNavigationDrawer(
        modifier = Modifier.fillMaxSize(),
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
                            ), contentScale = ContentScale.FillWidth

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
        }) {
        NavHost(navController = navController, startDestination = chapterRoute,
            enterTransition = {
                fadeIn(animationSpec = tween()) + expandVertically(animationSpec = tween())
            },
            exitTransition = {
                fadeOut(animationSpec = tween()) + shrinkVertically(animationSpec = tween())
            }) {
            //Chapters Screen
            composable(route = chapterRoute) {
                ChaptersScreen(drawerState, navController, checkFiles)
            }
            //Sura Screen
            composable(
                route = "sura/{sura}/{aya}?play={play}",
                arguments = listOf(navArgument("sura") { type = NavType.IntType },
                    navArgument("aya") { type = NavType.IntType },
                    navArgument("play") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                SuraScreen(
                    startSura = backStackEntry.arguments?.getInt("sura") ?: 1,
                    aya = backStackEntry.arguments?.getInt("aya") ?: 1,
                    play = backStackEntry.arguments?.getBoolean("play") ?: false,
                    drawerState = drawerState,
                    navController = navController
                )
            }
            //Search Screen
            composable(route = searchRoute) {
                SearchScreen(navController = navController)
            }
            //Notes Screen
            composable(route = notesRoute) {
                NotesScreen(drawerState = drawerState, navController = navController)
            }
            //Bookmarks Screen
            composable(route = bookmarksRoute) {
                BookmarksScreen(drawerState = drawerState, navController = navController)
            }
            //Download Screen
            composable(route = downloadRoute) {
                DownloadScreen(drawerState = drawerState)
            }
            //Setting Screen
            composable(route = settingsRoute) {
                SettingsScreen(drawerState)
            }
        }
        BackHandler(enabled = drawerState.isOpen) {
            scope.launch {
                drawerState.close()
            }
        }
    }

    LaunchedEffect(key1 = "startFromNotification") {
        if (startSura != -1 && startAya != -1)
            navController.navigate("sura/$startSura/$startAya") {
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
    Triple(chapterRoute, Icons.Filled.Menu, R.string.chapter),
    Triple(searchRoute, Icons.Filled.Search, R.string.search_the_whole_quran),
    Triple(notesRoute, Icons.Filled.NoteAlt, R.string.notes),
    Triple(bookmarksRoute, Icons.Filled.Bookmarks, R.string.bookmarks),
    Triple(downloadRoute, Icons.Filled.CloudDownload, R.string.download_audios),
    Triple(settingsRoute, Icons.Filled.Settings, R.string.settings),
    Triple(shortcutRoute, Icons.Filled.AppShortcut, R.string.create_shortcut),
    Triple(exitRoute, Icons.Default.Cancel, R.string.exit),
)

@Preview(name = "day", showBackground = true, locale = "fa")
@Preview(name = "night", showBackground = true, locale = "fa", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun HomePrev() {
    AppTheme {
        QuranHomeScreen(exit = {}, createShortcut = {}, checkFiles = {})
    }
}
