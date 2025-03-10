package com.byagowi.persiancalendar.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVerticalCircle
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ripple
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.byagowi.persiancalendar.PREF_ATHAN_ALARM
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_SYSTEM_DARK_THEME
import com.byagowi.persiancalendar.PREF_SYSTEM_LIGHT_THEME
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.systemDarkTheme
import com.byagowi.persiancalendar.global.systemLightTheme
import com.byagowi.persiancalendar.global.userSetTheme
import com.byagowi.persiancalendar.ui.about.AboutScreen
import com.byagowi.persiancalendar.ui.about.DeviceInformationScreen
import com.byagowi.persiancalendar.ui.about.LicensesScreen
import com.byagowi.persiancalendar.ui.astronomy.AstronomyScreen
import com.byagowi.persiancalendar.ui.astronomy.AstronomyViewModel
import com.byagowi.persiancalendar.ui.calendar.CalendarScreen
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.calendar.DaysScreen
import com.byagowi.persiancalendar.ui.calendar.ScheduleScreen
import com.byagowi.persiancalendar.ui.common.ScrollShadow
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
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.isDynamicGrayscale
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.preferences
import ir.namoo.hadeeth.ui.chapter.HadeethChapterScreen
import ir.namoo.hadeeth.ui.hadeeth.HadeethScreen
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
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun App(intentStartDestination: String?, initialJdn: Jdn? = null, finish: () -> Unit) {
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
    var appInitialJdn by remember { mutableStateOf(initialJdn) }
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    if (drawerState.isOpen) {
        BackHandler(enabled = true) { coroutineScope.launch { drawerState.close() } }
    }

    val selectedDayKey = "SELECTED_DAY"
    val isWeekKey = "IS_WEEK"
    val tabKey = "TAB"
    val settingsKey = "SETTINGS"
    val daysOffsetKey = "DAYS_OFFSET"

    SharedTransitionLayout {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    drawerContainerColor = animateColor(MaterialTheme.colorScheme.surface).value,
                    drawerContentColor = animateColor(MaterialTheme.colorScheme.onSurface).value,
                    modifier = Modifier.renderInSharedTransitionScopeOverlay(),
                ) {
                    Box {
                        val scrollState = rememberScrollState()
                        DrawerContent(drawerState, navController, finish, scrollState)
                        DrawerTopGradient()
                        ScrollShadow(scrollState, top = true)
                        ScrollShadow(scrollState, top = false)
                    }
                }
            },
        ) {
            NavHost(navController = navController, startDestination = startDestination) {

                fun navigateToSettingsLocationTab() {
                    navController.graph.findNode(settingsRoute)?.let { destination ->
                        navController.navigate(
                            destination.id, bundleOf(tabKey to LOCATION_ATHAN_TAB)
                        )
                    }
                }

                fun isCurrentDestination(route: String) =
                    navController.currentDestination?.route == route

                fun navigateUp(currentRoute: String) {
                    // If we aren't in the screen that this wasn't supposed to be called, just ignore, happens while transition
                    if (!isCurrentDestination(currentRoute)) return
                    // if there wasn't anything to pop, just exit the app, happens if the app is entered from the map widget
                    if (!navController.popBackStack()) finish()
                }

                composable(calendarRoute) {
                    val viewModel = viewModel<CalendarViewModel>()
                    appInitialJdn?.let {
                        viewModel.changeSelectedMonthOffsetCommand(
                            mainCalendar.getMonthsDistance(Jdn.today(), it)
                        )
                        viewModel.changeSelectedDay(it)
                        appInitialJdn = null
                    }
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
                        navigateToSchedule = { navController.navigate(scheduleRoute) },
                        navigateToDays = { jdn, isWeek ->
                            navController.graph.findNode(daysRoute)?.let { dst ->
                                navController.navigate(
                                    dst.id, bundleOf(
                                        selectedDayKey to jdn.value,
                                        isWeekKey to isWeek
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
                        viewModel = viewModel,
                        animatedContentScope = this,
                        isCurrentDestination = isCurrentDestination(calendarRoute),
                        navigateToAthanSetting = { athanId -> navController.navigate("$athanSettingsRoute?athanId=$athanId") },
                        navigateToDownload = { navController.navigate(downloadRoute) },
                        navigateToEditTimes = { navController.navigate(editRoute) }
                    )
                }

                composable(scheduleRoute) { backStackEntry ->
                    val previousEntry = navController.previousBackStackEntry
                    val previousRoute = previousEntry?.destination?.route
                    val viewModel = if (previousRoute == calendarRoute) {
                        viewModel<CalendarViewModel>(previousEntry)
                    } else viewModel<CalendarViewModel>()

                    val jdn =
                        backStackEntry.arguments?.getLong(selectedDayKey, 0)?.takeIf { it != 0L }
                            ?.let(::Jdn) ?: remember { viewModel.selectedDay.value }
                    ScheduleScreen(
                        calendarViewModel = viewModel,
                        animatedContentScope = this,
                        navigateUp = { navigateUp(scheduleRoute) },
                        initiallySelectedDay = jdn,
                    )
                }

                composable(daysRoute) { backStackEntry ->
                    val previousEntry = navController.previousBackStackEntry
                    val previousRoute = previousEntry?.destination?.route
                    val viewModel = if (previousRoute == calendarRoute) {
                        viewModel<CalendarViewModel>(previousEntry)
                    } else viewModel<CalendarViewModel>()
                    val arguments = backStackEntry.arguments
                    val isWeek = arguments?.getBoolean(isWeekKey) ?: false
                    val jdn = arguments?.getLong(selectedDayKey, 0)?.takeIf { it != 0L }?.let(::Jdn)
                        ?: Jdn.today()
                    DaysScreen(
                        calendarViewModel = viewModel,
                        initiallySelectedDay = jdn,
                        appAnimatedContentScope = this,
                        isInitiallyWeek = isWeek,
                        navigateUp = { navigateUp(daysRoute) },
                    )
                }

                composable(converterRoute) {
                    ConverterScreen(
                        animatedContentScope = this,
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        viewModel = viewModel<ConverterViewModel>()
                    )
                }

                composable(compassRoute) {
                    CompassScreen(
                        animatedContentScope = this,
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        navigateToLevel = { navController.navigate(levelRoute) },
                        navigateToMap = { navController.navigate(mapRoute) },
                        navigateToSettingsLocationTab = ::navigateToSettingsLocationTab,
                    )
                }

                composable(levelRoute) {
                    LevelScreen(
                        animatedContentScope = this,
                        navigateUp = { navigateUp(levelRoute) },
                        navigateToCompass = { navController.navigate(compassRoute) },
                    )
                }

                composable(astronomyRoute) { backStackEntry ->
                    val viewModel = viewModel<AstronomyViewModel>()
                    backStackEntry.arguments?.getInt(daysOffsetKey, 0)?.takeIf { it != 0 }?.let {
                        viewModel.changeToTime((Jdn.today() + it).toGregorianCalendar().timeInMillis)
                    }
                    AstronomyScreen(
                        animatedContentScope = this,
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
                        animatedContentScope = this,
                        navigateUp = { navigateUp(mapRoute) },
                        fromSettings = previousRoute == settingsRoute,
                        viewModel = viewModel,
                    )
                }

                composable(settingsRoute) { backStackEntry ->
                    SettingsScreen(
                        animatedContentScope = this,
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        navigateToMap = { navController.navigate(mapRoute) },
                        navigateToAthanSettings = { athanId -> navController.navigate("$athanSettingsRoute?athanId=$athanId") },
                        initialPage = backStackEntry.arguments?.getInt(tabKey, 0) ?: 0,
                        destination = backStackEntry.arguments?.getString(settingsKey) ?: ""
                    )
                }
                composable(azkarRoute) {
                    AzkarScreen(
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        animatedContentScope = this
                    )
                }
                composable(hadeethChaptersRoute) {
                    HadeethChapterScreen(
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        animatedContentScope = this,
                        navigateToHadeeth = {
                            navController.navigate("$hadeethRoute/$it")
                        }
                    )
                }
                composable(
                    route = "$hadeethRoute/{hadeethID}",
                    arguments = listOf(navArgument("hadeethID") {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val hadeethID = backStackEntry.arguments?.getString("hadeethID") ?: "-1"
                    HadeethScreen(
                        hadeethID = hadeethID,
                        animatedContentScope = this,
                        openDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
                composable(downloadRoute) {
                    DownloadPrayTimesScreen(
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        animatedContentScope = this
                    )
                }
                composable(nAboutRoute) {
                    NAboutScreen(
                        navigateToPersianCalendar = { navController.navigate(aboutRoute) },
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        animatedContentScope = this
                    )
                }
                composable(
                    route = "$athanSettingsRoute?athanId={athanId}",
                    arguments = listOf(navArgument("athanId") {
                        type = NavType.IntType
                    })
                ) { navBackStackEntry ->
                    val athanId = navBackStackEntry.arguments?.getInt("athanId") ?: 1
                    AthanSettingsScreen(
                        athanId = athanId,
                        navigateUp = { navController.navigateUp() })
                }
                composable(editRoute) {
                    PrayTimesEditScreen(
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        animatedContentScope = this
                    )
                }

                composable(aboutRoute) {
                    AboutScreen(
                        animatedContentScope = this,
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        navigateToLicenses = { navController.navigate(licensesRoute) },
                        navigateToDeviceInformation = {
                            navController.navigate(deviceInformationRoute)
                        },
                    )
                }

                composable(licensesRoute) {
                    LicensesScreen(animatedContentScope = this) { navigateUp(licensesRoute) }
                }

                composable(deviceInformationRoute) {
                    DeviceInformationScreen(
                        { navigateUp(deviceInformationRoute) },
                        animatedContentScope = this,
                    )
                }
            }
        }
    }
}

// Make system status bar icons visible when surface is light but theme has dark background
@Composable
private fun DrawerTopGradient() {
    val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
    val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
    val needsVisibleStatusBarPlaceHolder = !isBackgroundColorLight && isSurfaceColorLight
    val topColor by animateColor(
        if (needsVisibleStatusBarPlaceHolder) Color(0x70000000) else Color.Transparent
    )
    Spacer(
        Modifier
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.systemBars)
            .background(Brush.verticalGradient(listOf(topColor, Color.Transparent))),
    )
}

@Composable
private fun DrawerContent(
    drawerState: DrawerState,
    navController: NavHostController,
    finish: () -> Unit,
    scrollState: ScrollState,
) {
    val context = LocalContext.current
    var showDonateDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Box {
            NDrawerPager(drawerState)
            DrawerDarkModeToggle()
        }
        val onSecondaryContainerColor by animateColor(MaterialTheme.colorScheme.onSecondaryContainer)
        val navItemColors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            unselectedTextColor = LocalContentColor.current,
            unselectedIconColor = LocalContentColor.current,
            selectedContainerColor = animateColor(MaterialTheme.colorScheme.secondaryContainer).value,
            selectedTextColor = onSecondaryContainerColor,
            selectedIconColor = onSecondaryContainerColor,
        )
        val coroutineScope = rememberCoroutineScope()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        navItems.forEach { (id, icon, title) ->
            NavigationDrawerItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                icon = {
                    Icon(
                        icon, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                colors = navItemColors,
                label = {
                    // Apparently language strings isn't applied immediately in drawer
                    // items titles but only when drawer opens so let's animate it!
                    AnimatedContent(
                        targetState = stringResource(title),
                        label = "title",
                        transitionSpec = appCrossfadeSpec,
                    ) { state -> Text(state) }
                },
                selected =
                if (navBackStackEntry?.destination?.route?.startsWith(
                        athanSettingsRoute
                    ) == true
                ) {
                    id == settingsRoute
                } else if (navBackStackEntry?.destination?.route?.startsWith(
                        hadeethRoute
                    ) == true
                ) {
                    id == hadeethChaptersRoute
                } else when (val route = navBackStackEntry?.destination?.route) {
                    levelRoute -> compassRoute
                    mapRoute -> astronomyRoute
                    deviceInformationRoute, licensesRoute -> aboutRoute
                    else -> route ?: calendarRoute
                } == id,
                onClick = onClick@{
                    if (id == null) return@onClick finish()
                    if (id == quranRoute) {
                        context.startActivity(
                            Intent(context, QuranActivity::class.java)
                        )
                        coroutineScope.launch { drawerState.close() }
                        return@onClick
                    }
                    if ((id == donateRoute)) {
                        coroutineScope.launch { drawerState.close() }
                        showDonateDialog = true
                        return@onClick
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
    }
    if (showDonateDialog) DonateDialog { showDonateDialog = false }
}


private const val calendarRoute = "calendar"
private const val quranRoute = "quran"
private const val azkarRoute = "azkar"
private const val hadeethChaptersRoute = "hadeethChapters"
private const val hadeethRoute = "hadeeth"
private const val editRoute = "edit"
private const val downloadRoute = "download"
private const val compassRoute = "compass"
private const val levelRoute = "level"
private const val mapRoute = "map"
private const val converterRoute = "converter"
private const val scheduleRoute = "schedule"
private const val daysRoute = "days"
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
    Triple(hadeethChaptersRoute, Icons.AutoMirrored.Default.MenuBook, R.string.hadeeth),
    Triple(azkarRoute, Icons.AutoMirrored.Default.LibraryBooks, R.string.azkar),
    Triple(downloadRoute, Icons.Default.CloudDownload, R.string.download_upload),
    Triple(converterRoute, Icons.Default.SwapVerticalCircle, R.string.date_converter),
    Triple(compassRoute, Icons.Default.Explore, R.string.compass),
    Triple(astronomyRoute, AstrologyIcon, R.string.astronomy),
    Triple(settingsRoute, Icons.Default.Settings, R.string.settings),
    Triple(nAboutRoute, Icons.Default.Info, R.string.about),
    Triple(donateRoute, Icons.Default.CardGiftcard, R.string.donate),
    Triple(null, Icons.Default.Cancel, R.string.exit),
)

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
                delay(30.seconds)
                val seasonIndex = Season.fromDate(Date(), coordinates.value).ordinal
                if (seasonIndex != actualSeason) {
                    actualSeason = seasonIndex
                    pagerState.animateScrollToPage(pageSize / 2 + actualSeason)
                }
            }
        }
    }

    val isDynamicGrayscale = isDynamicGrayscale()
    val imageFilter = remember(isDynamicGrayscale) {
        if (!isDynamicGrayscale) null
        // Consider gray scale themes of Android 14
        // And apply a gray scale filter https://stackoverflow.com/a/75698731
        else ColorFilter.colorMatrix(ColorMatrix().also { it.setToSaturation(0f) })
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
            .height(196.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .semantics { this.hideFromAccessibility() },
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

@Composable
private fun BoxScope.DrawerDarkModeToggle() {
    val userSetTheme by userSetTheme.collectAsState()
    // If current theme is default theme, isDark is null so no toggle is shown also
    val isDark = userSetTheme.isDark ?: return
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    Crossfade(
        label = "dark mode toggle",
        targetState = if (isDark) Icons.Outlined.LightMode else Icons.Default.ModeNight,
        modifier = Modifier
            .semantics { this.hideFromAccessibility() }
            .padding(32.dp)
            .align(Alignment.BottomEnd)
            .clickable(
                indication = ripple(bounded = false),
                interactionSource = null,
                onClick = {
                    val systemTheme = if (isDark) systemLightTheme else systemDarkTheme
                    context.preferences.edit {
                        putString(PREF_THEME, systemTheme.value.key)
                        putString(
                            if (isDark) PREF_SYSTEM_DARK_THEME else PREF_SYSTEM_LIGHT_THEME,
                            userSetTheme.key
                        )
                    }
                },
            )
            .background(
                animateColor(MaterialTheme.colorScheme.surface.copy(alpha = .5f)).value,
                MaterialTheme.shapes.extraLarge
            )
            .padding(8.dp),
    ) {
        val tint by animateColor(MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) .9f else .6f))
        Icon(
            it,
            stringResource(if (isDark) R.string.theme_dark else R.string.theme_light),
            tint = tint,
        )
    }
}
