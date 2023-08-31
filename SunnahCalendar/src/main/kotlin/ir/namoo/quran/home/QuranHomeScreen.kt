package ir.namoo.quran.home

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shortcut
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.byagowi.persiancalendar.R
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.iconColor
import ir.namoo.quran.bookmarks.BookmarksScreen
import ir.namoo.quran.chapters.ChaptersScreen
import ir.namoo.quran.download.DownloadScreen
import ir.namoo.quran.notes.NotesScreen
import ir.namoo.quran.search.SearchScreen
import ir.namoo.quran.settings.SettingsScreen
import ir.namoo.quran.sura.SuraScreen
import kotlinx.coroutines.launch

@Composable
fun QuranHomeScreen(exit: () -> Unit, createShortcut: () -> Unit, checkFiles: () -> Unit) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedPage by remember { mutableIntStateOf(1) }

    ModalNavigationDrawer(
        modifier = Modifier.fillMaxSize(),
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier.padding(4.dp, 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        modifier = Modifier.clip(ShapeDefaults.ExtraLarge),
                        painter = painterResource(id = R.drawable.quran_drawer),
                        contentDescription = stringResource(
                            id = R.string.quran
                        )
                    )
                }
                // Chapter
                NavigationDrawerItem(modifier = Modifier.padding(horizontal = 8.dp), label = {
                    Text(
                        text = stringResource(id = R.string.chapter),
                        fontFamily = FontFamily(appFont),
                        fontWeight = if (selectedPage == 1) FontWeight.SemiBold else FontWeight.Normal
                    )
                }, selected = selectedPage == 1, icon = {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = stringResource(id = R.string.chapter),
                        tint = iconColor
                    )
                }, onClick = {
                    navController.navigate("chapters")
                    scope.launch {
                        drawerState.close()
                    }
                })
                //Search
                NavigationDrawerItem(modifier = Modifier.padding(horizontal = 8.dp), label = {
                    Text(
                        text = stringResource(id = R.string.search_the_whole_quran),
                        fontFamily = FontFamily(appFont)
                    )
                }, selected = selectedPage == 2, icon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = stringResource(id = R.string.search_the_whole_quran),
                        tint = iconColor
                    )
                }, onClick = {
                    navController.navigate("search")
                    scope.launch {
                        drawerState.close()
                    }
                })
                //Notes
                NavigationDrawerItem(modifier = Modifier.padding(horizontal = 8.dp), label = {
                    Text(
                        text = stringResource(id = R.string.notes), fontFamily = FontFamily(appFont)
                    )
                }, selected = selectedPage == 3, icon = {
                    Icon(
                        imageVector = Icons.Filled.Notes,
                        contentDescription = stringResource(id = R.string.notes),
                        tint = iconColor
                    )
                }, onClick = {
                    navController.navigate("notes")
                    scope.launch {
                        drawerState.close()
                    }
                })
                //Bookmarks
                NavigationDrawerItem(modifier = Modifier.padding(horizontal = 8.dp), label = {
                    Text(
                        text = stringResource(id = R.string.bookmarks),
                        fontFamily = FontFamily(appFont)
                    )
                }, selected = selectedPage == 4, icon = {
                    Icon(
                        imageVector = Icons.Filled.Bookmarks,
                        contentDescription = stringResource(id = R.string.bookmarks),
                        tint = iconColor
                    )
                }, onClick = {
                    navController.navigate("bookmarks")
                    scope.launch {
                        drawerState.close()
                    }
                })
                //Download
                NavigationDrawerItem(modifier = Modifier.padding(horizontal = 8.dp), label = {
                    Text(
                        text = stringResource(id = R.string.download_audios),
                        fontFamily = FontFamily(appFont)
                    )
                }, selected = selectedPage == 5, icon = {
                    Icon(
                        imageVector = Icons.Filled.CloudDownload,
                        contentDescription = stringResource(id = R.string.download_audios),
                        tint = iconColor
                    )
                }, onClick = {
                    navController.navigate("download")
                    scope.launch {
                        drawerState.close()
                    }
                })
                //Settings
                NavigationDrawerItem(modifier = Modifier.padding(horizontal = 8.dp), label = {
                    Text(
                        text = stringResource(id = R.string.settings),
                        fontFamily = FontFamily(appFont)
                    )
                }, selected = selectedPage == 6, icon = {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(id = R.string.settings),
                        tint = iconColor
                    )
                }, onClick = {
                    navController.navigate("setting")
                    scope.launch {
                        drawerState.close()
                    }
                })
                //Shortcut
                NavigationDrawerItem(modifier = Modifier.padding(horizontal = 8.dp), label = {
                    Text(
                        text = stringResource(id = R.string.create_shortcut),
                        fontFamily = FontFamily(appFont)
                    )
                }, selected = selectedPage == 7, icon = {
                    Icon(
                        imageVector = Icons.Filled.Shortcut,
                        contentDescription = stringResource(id = R.string.create_shortcut),
                        tint = iconColor
                    )
                }, onClick = {
                    createShortcut()
                    scope.launch {
                        drawerState.close()
                    }
                })
                //Exit
                NavigationDrawerItem(modifier = Modifier.padding(horizontal = 8.dp), label = {
                    Text(
                        text = stringResource(id = R.string.exit), fontFamily = FontFamily(appFont)
                    )
                }, selected = selectedPage == 8, icon = {
                    Icon(
                        imageVector = Icons.Rounded.ExitToApp,
                        contentDescription = stringResource(id = R.string.exit),
                        tint = iconColor
                    )
                }, onClick = {
                    exit()
                })
            }
        }) {
        NavHost(navController = navController, startDestination = "chapters",
            enterTransition = {
                fadeIn(animationSpec = tween()) + expandVertically(animationSpec = tween())
            },
            exitTransition = {
                fadeOut(animationSpec = tween()) + shrinkVertically(animationSpec = tween())
            }) {
            //Chapters Screen
            composable(route = "chapters") {
                selectedPage = 1
                ChaptersScreen(drawerState, navController, checkFiles)
            }
            //Sura Screen
            composable(
                route = "sura/{sura}/{aya}",
                arguments = listOf(navArgument("sura") { type = NavType.IntType },
                    navArgument("aya") { type = NavType.IntType })
            ) { backStackEntry ->
                selectedPage = 1
                SuraScreen(
                    sura = backStackEntry.arguments?.getInt("sura") ?: 1,
                    aya = backStackEntry.arguments?.getInt("aya") ?: 1,
                    drawerState = drawerState,
                    navController = navController
                )
            }
            //Search Screen
            composable(route = "search") {
                selectedPage = 2
                SearchScreen(navController = navController)
            }
            //Notes Screen
            composable(route = "notes") {
                selectedPage = 3
                NotesScreen(drawerState = drawerState, navController = navController)
            }
            //Bookmarks Screen
            composable(route = "bookmarks") {
                selectedPage = 4
                BookmarksScreen(drawerState = drawerState, navController = navController)
            }
            //Download Screen
            composable(route = "download") {
                selectedPage = 5
                DownloadScreen(drawerState = drawerState)
            }
            //Setting Screen
            composable(route = "setting") {
                selectedPage = 6
                SettingsScreen(drawerState)
            }
        }
        BackHandler(enabled = drawerState.isOpen) {
            scope.launch {
                drawerState.close()
            }
        }
    }
}

@Preview(name = "day", showBackground = true, locale = "fa")
@Preview(name = "night", showBackground = true, locale = "fa", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun HomePrev() {
    Mdc3Theme {
        QuranHomeScreen(exit = {}, createShortcut = {}, checkFiles = {})
    }
}
