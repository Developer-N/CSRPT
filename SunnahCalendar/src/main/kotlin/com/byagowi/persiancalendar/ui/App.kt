package com.byagowi.persiancalendar.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVerticalCircle
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.byagowi.persiancalendar.PREF_ATHAN_ALARM
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.theme
import com.byagowi.persiancalendar.ui.about.AboutScreen
import com.byagowi.persiancalendar.ui.about.DeviceInformationScreen
import com.byagowi.persiancalendar.ui.about.LicensesScreen
import com.byagowi.persiancalendar.ui.astronomy.AstronomyScreen
import com.byagowi.persiancalendar.ui.astronomy.AstronomyViewModel
import com.byagowi.persiancalendar.ui.calendar.CalendarScreen
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.compass.CompassScreen
import com.byagowi.persiancalendar.ui.converter.ConverterScreen
import com.byagowi.persiancalendar.ui.converter.ConverterViewModel
import com.byagowi.persiancalendar.ui.icons.AstrologyIcon
import com.byagowi.persiancalendar.ui.level.LevelScreen
import com.byagowi.persiancalendar.ui.map.MapScreen
import com.byagowi.persiancalendar.ui.map.MapViewModel
import com.byagowi.persiancalendar.ui.settings.INTERFACE_CALENDAR_TAB
import com.byagowi.persiancalendar.ui.settings.LOCATION_ATHAN_TAB
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.utils.isDynamicGrayscale
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.THIRTY_SECONDS_IN_MILLIS
import ir.namoo.quran.QuranActivity
import ir.namoo.religiousprayers.ui.about.NAboutScreen
import ir.namoo.religiousprayers.ui.azkar.AzkarScreen
import ir.namoo.religiousprayers.ui.calendar.NDrawerPager
import ir.namoo.religiousprayers.ui.donate.DonateDialog
import ir.namoo.religiousprayers.ui.downloadtimes.DownloadPrayTimesScreen
import ir.namoo.religiousprayers.ui.edit.PrayTimesEditScreen
import ir.namoo.religiousprayers.ui.settings.athan.AthanSettingsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun App(intentStartDestination: String?, finish: () -> Unit) {
    val context = LocalContext.current
    var showDonateDialog by remember { mutableStateOf(false) }
    // See xml/shortcuts.xml
    val startDestination = when (intentStartDestination) {
        "COMPASS" -> compassRoute
        "LEVEL" -> levelRoute
        "CONVERTER" -> converterRoute
        "ASTRONOMY" -> astronomyRoute
        "MAP" -> mapRoute
        "AZKAR" -> azkarRoute
        else -> calendarRoute
    }
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) { coroutineScope.launch { drawerState.close() } }

    val navBackStackEntry by navController.currentBackStackEntryAsState()

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
                    NDrawerPager(drawerState)
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
                                // Apparently language strings isn't applied immediately in drawer
                                // items titles but only when drawer opens so let's animate it!
                                AnimatedContent(
                                    targetState = stringResource(title),
                                    label = "title",
                                    transitionSpec = appCrossfadeSpec,
                                ) { state -> Text(state) }
                            },
                            selected = if (navBackStackEntry?.destination?.route?.startsWith(
                                    athanSettingsRoute
                                ) == true
                            ) {
                                id == settingsRoute
                            } else when (val route = navBackStackEntry?.destination?.route) {
                                levelRoute -> compassRoute
                                mapRoute -> astronomyRoute
                                deviceInformationRoute, licensesRoute -> aboutRoute
                                else -> route ?: calendarRoute
                            } == id,
                            onClick = {
                                if (id == null) return@NavigationDrawerItem finish()
                                if (id == quranRoute) {
                                    context.startActivity(
                                        Intent(context, QuranActivity::class.java)
                                    )
                                    coroutineScope.launch { drawerState.close() }
                                    return@NavigationDrawerItem
                                }
                                if ((id == donateRoute)) {
                                    coroutineScope.launch { drawerState.close() }
                                    showDonateDialog = true
                                    return@NavigationDrawerItem
                                }
                                coroutineScope.launch {
                                    drawerState.close()
                                    if (navBackStackEntry?.destination?.route != id) {
                                        navController.navigate(id)
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
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            val tabKey = "TAB"
            val settingsKey = "SETTINGS"
            val daysOffsetKey = "DAYS_OFFSET"

            fun navigateToSettingsLocationTab() {
                navController.graph.findNode(settingsRoute)?.let { destination ->
                    navController.navigate(
                        destination.id, bundleOf(tabKey to LOCATION_ATHAN_TAB)
                    )
                }
            }

            fun navigateUp(currentRoute: String) {
                // If we aren't in the screen that this was supposed to be called, just ignore, happens while transition
                if (navController.currentDestination?.route != currentRoute) return
                // if there wasn't anything to pop, just exit the app, happens if the app is entered from the map widget
                if (!navController.popBackStack()) finish()
            }

            composable(calendarRoute) {
                CalendarScreen(
                    openDrawer = { coroutineScope.launch { drawerState.open() } },
                    navigateToHolidaysSettings = {
                        navController.graph.findNode(settingsRoute)?.let { destination ->
                            navController.navigate(
                                destination.id, bundleOf(
                                    tabKey to INTERFACE_CALENDAR_TAB,
                                    settingsKey to PREF_HOLIDAY_TYPES,
                                )
                            )
                        }
                    },
                    navigateToSettingsLocationTabSetAthanAlarm = {
                        navController.graph.findNode(settingsRoute)?.let { destination ->
                            navController.navigate(
                                destination.id, bundleOf(
                                    tabKey to LOCATION_ATHAN_TAB,
                                    settingsKey to PREF_ATHAN_ALARM,
                                )
                            )
                        }
                    },
                    navigateToSettingsLocationTab = ::navigateToSettingsLocationTab,
                    navigateToAstronomy = { daysOffset ->
                        navController.graph.findNode(astronomyRoute)?.let { destination ->
                            navController.navigate(
                                destination.id, bundleOf(daysOffsetKey to daysOffset)
                            )
                        }
                    },
                    viewModel = viewModel<CalendarViewModel>(),
                    navigateToAthanSetting = { athanId -> navController.navigate("$athanSettingsRoute?athanId=$athanId") },
                    navigateToDownload = { navController.navigate(downloadRoute) })
            }

            composable(converterRoute) {
                ConverterScreen(
                    openDrawer = { coroutineScope.launch { drawerState.open() } },
                    viewModel = viewModel<ConverterViewModel>()
                )
            }

            composable(compassRoute) {
                CompassScreen(
                    openDrawer = { coroutineScope.launch { drawerState.open() } },
                    navigateToLevel = { navController.navigate(levelRoute) },
                    navigateToMap = { navController.navigate(mapRoute) },
                    navigateToSettingsLocationTab = ::navigateToSettingsLocationTab,
                )
            }

            composable(levelRoute) {
                LevelScreen(
                    navigateUp = { navigateUp(levelRoute) },
                    navigateToCompass = {
                        // If compass wasn't in backstack (level is brought from shortcut), navigate to it
                        if (!navController.popBackStack(compassRoute, false)) {
                            navController.navigate(levelRoute)
                        }
                    },
                )
            }

            composable(astronomyRoute) {
                val viewModel = viewModel<AstronomyViewModel>()
                it.arguments?.getInt(daysOffsetKey, 0)?.takeIf { it != 0 }?.let {
                    viewModel.changeToTime((Jdn.today() + it).toGregorianCalendar().timeInMillis)
                }
                AstronomyScreen(
                    openDrawer = { coroutineScope.launch { drawerState.open() } },
                    navigateToMap = { navController.navigate(mapRoute) },
                    viewModel = viewModel,
                )
            }

            composable(mapRoute) {
                val viewModel = viewModel<MapViewModel>()
                val previousEntry = navController.previousBackStackEntry
                val previousRoute = previousEntry?.destination?.route
                if (previousRoute == astronomyRoute) {
                    val astronomyViewModel = viewModel<AstronomyViewModel>(previousEntry)
                    LaunchedEffect(Unit) {
                        viewModel.changeToTime(astronomyViewModel.astronomyState.value.date.time)
                        viewModel.state.collectLatest { astronomyViewModel.changeToTime(it.time) }
                    }
                }
                MapScreen(
                    navigateUp = { navigateUp(mapRoute) },
                    fromSettings = previousRoute == settingsRoute,
                    viewModel = viewModel,
                )
            }

            composable(settingsRoute) {
                SettingsScreen(
                    openDrawer = { coroutineScope.launch { drawerState.open() } },
                    navigateToMap = { navController.navigate(mapRoute) },
                    navigateToAthanSettings = { athanId -> navController.navigate("$athanSettingsRoute?athanId=$athanId") },
                    initialPage = it.arguments?.getInt(tabKey, 0) ?: 0,
                    destination = it.arguments?.getString(settingsKey) ?: ""
                )
            }

            composable(aboutRoute) {
                AboutScreen(
                    openDrawer = { coroutineScope.launch { drawerState.open() } },
                    navigateToLicenses = { navController.navigate(licensesRoute) },
                    navigateToDeviceInformation = {
                        navController.navigate(deviceInformationRoute)
                    },
                )
            }

            composable(licensesRoute) {
                LicensesScreen { navigateUp(licensesRoute) }
            }

            composable(deviceInformationRoute) {
                DeviceInformationScreen { navigateUp(deviceInformationRoute) }
            }
            composable(azkarRoute) {
                AzkarScreen(openDrawer = { coroutineScope.launch { drawerState.open() } })
            }
            composable(downloadRoute) {
                DownloadPrayTimesScreen(openDrawer = { coroutineScope.launch { drawerState.open() } })
            }
            composable(nAboutRoute) {
                NAboutScreen(navigateToPersianCalendar = { navController.navigate(aboutRoute) },
                    openDrawer = { coroutineScope.launch { drawerState.open() } })
            }
            composable(
                route = "$athanSettingsRoute?athanId={athanId}",
                arguments = listOf(navArgument("athanId") {
                    type = NavType.IntType
                })
            ) { navBackStackEntry ->
                val athanId = navBackStackEntry.arguments?.getInt("athanId") ?: 1
                AthanSettingsScreen(athanId = athanId, navigateUp = { navController.navigateUp() })
            }
            composable(editRoute) {
                PrayTimesEditScreen(openDrawer = { coroutineScope.launch { drawerState.open() } })
            }
        }
    }
    if (showDonateDialog) DonateDialog { showDonateDialog = false }
}


private const val calendarRoute = "calendar"
private const val quranRoute = "quran"
private const val azkarRoute = "azkar"
private const val editRoute = "edit"
private const val downloadRoute = "download"
private const val compassRoute = "compass"
private const val levelRoute = "level"
private const val mapRoute = "map"
private const val converterRoute = "converter"
private const val astronomyRoute = "astronomy"
private const val settingsRoute = "settings"
private const val aboutRoute = "about"
private const val licensesRoute = "license"
private const val deviceInformationRoute = "device"
private const val donateRoute = "donate"
private const val nAboutRoute = "nAbout"
private const val athanSettingsRoute = "athanSettings"

@Stable
private val navItems: List<Triple<String?, ImageVector, Int>> = listOf(
    Triple(calendarRoute, Icons.Default.DateRange, R.string.calendar),
    Triple(quranRoute, Icons.AutoMirrored.Default.MenuBook, R.string.quran),
    Triple(azkarRoute, Icons.Default.Mosque, R.string.azkar),
    Triple(editRoute, Icons.Default.EditCalendar, R.string.edit_times),
    Triple(downloadRoute, Icons.Default.CloudDownload, R.string.download_upload),
    Triple(converterRoute, Icons.Default.SwapVerticalCircle, R.string.date_converter),
    Triple(compassRoute, Icons.Default.Explore, R.string.compass),
    Triple(astronomyRoute, AstrologyIcon, R.string.astronomy),
    Triple(settingsRoute, Icons.Default.Settings, R.string.settings),
    Triple(nAboutRoute, Icons.Default.Info, R.string.about),
    Triple(donateRoute, Icons.Default.MonetizationOn, R.string.donate),
    Triple(null, Icons.Default.Cancel, R.string.exit),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerSeasonsPager(drawerState: DrawerState) {
    var actualSeason by remember {
        mutableIntStateOf(Season.fromDate(Date(), coordinates.value).ordinal)
    }
    val pageSize = 200
    val pagerState = rememberPagerState(
        initialPage = pageSize / 2 + actualSeason - 3, // minus 3 so it does an initial animation
        pageCount = { pageSize },
    )
    if (drawerState.isOpen) {
        LaunchedEffect(Unit) {
            pagerState.animateScrollToPage(pageSize / 2 + actualSeason)
            while (true) {
                delay(THIRTY_SECONDS_IN_MILLIS)
                val seasonIndex = Season.fromDate(Date(), coordinates.value).ordinal
                if (seasonIndex != actualSeason) {
                    actualSeason = seasonIndex
                    pagerState.animateScrollToPage(pageSize / 2 + actualSeason)
                }
            }
        }
    }

    val context = LocalContext.current
    val theme by theme.collectAsState()
    val imageFilter = remember(LocalConfiguration.current, theme) {
        // Consider gray scale themes of Android 14
        // And apply a gray scale filter https://stackoverflow.com/a/75698731
        if (theme.isDynamicColors() && context.resources.isDynamicGrayscale) {
            ColorFilter.colorMatrix(ColorMatrix().also { it.setToSaturation(0f) })
        } else null
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
            .height(196.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .semantics { @OptIn(ExperimentalComposeUiApi::class) this.invisibleToUser() },
        pageSpacing = 8.dp,
    ) {
        val season = Season.entries[it % 4]
        Image(
            ImageBitmap.imageResource(season.imageId),
            contentScale = ContentScale.FillWidth,
            contentDescription = """${stringResource(R.string.season)}$spacedColon${
                stringResource(season.nameStringId)
            }""",
            colorFilter = imageFilter,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.extraLarge),
        )
    }
}
