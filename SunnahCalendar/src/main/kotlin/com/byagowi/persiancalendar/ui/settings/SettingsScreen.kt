package com.byagowi.persiancalendar.ui.settings

import android.app.AlertDialog
import android.app.StatusBarManager
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.compose.LocalActivity
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.LOG_TAG
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.service.PersianCalendarTileService
import com.byagowi.persiancalendar.ui.about.ColorSchemeDemoDialog
import com.byagowi.persiancalendar.ui.about.DynamicColorsDialog
import com.byagowi.persiancalendar.ui.about.IconsDemoDialog
import com.byagowi.persiancalendar.ui.about.ScheduleAlarm
import com.byagowi.persiancalendar.ui.about.ShapesDemoDialog
import com.byagowi.persiancalendar.ui.about.TypographyDemoDialog
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.InterfaceCalendarSettings
import com.byagowi.persiancalendar.ui.settings.widgetnotification.AddWidgetDialog
import com.byagowi.persiancalendar.ui.settings.widgetnotification.WidgetNotificationSettings
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.shareTextFile
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.byagowi.persiancalendar.variants.debugLog
import ir.namoo.religiousprayers.ui.settings.NSettingsScreen
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
fun SharedTransitionScope.SettingsScreen(
    animatedContentScope: AnimatedContentScope,
    openDrawer: () -> Unit,
    navigateToMap: () -> Unit,
    navigateToAthanSettings: (Int) -> Unit,
    initialPage: Int,
    destination: String,
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = stringResource(R.string.settings),
                        label = "title",
                        transitionSpec = appCrossfadeSpec,
                    ) { state -> Text(state) }
                },
                colors = appTopAppBarColors(),
                navigationIcon = { NavigationOpenDrawerIcon(animatedContentScope, openDrawer) },
                actions = {
                    var showAddWidgetDialog by rememberSaveable { mutableStateOf(false) }
                    ThreeDotsDropdownMenu(animatedContentScope) { closeMenu ->
                        MenuItems(
                            openAddWidgetDialog = { closeMenu(); showAddWidgetDialog = true },
                            closeMenu = closeMenu,
                        )
                    }
                    if (showAddWidgetDialog) AddWidgetDialog { showAddWidgetDialog = false }
                },
            )
        },
    ) { paddingValues ->
        Column(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            val tabs = listOf(
                TabItem(
                    Icons.Outlined.Palette, Icons.Default.Palette,
                    R.string.pref_interface, R.string.calendar,
                ) { InterfaceCalendarSettings(destination) },
                TabItem(
                    Icons.Outlined.Widgets, Icons.Default.Widgets,
                    R.string.pref_notification, R.string.pref_widget,
                ) { WidgetNotificationSettings() },
                TabItem(
                    Icons.Outlined.LocationOn, Icons.Default.LocationOn,
                    R.string.location, R.string.athan,
                ) { NSettingsScreen(navigateToAthanSettings = navigateToAthanSettings) },
            )

            val pagerState = rememberPagerState(initialPage = initialPage, pageCount = tabs::size)
            val coroutineScope = rememberCoroutineScope()

            val selectedTabIndex = pagerState.currentPage
            TabRow(
                selectedTabIndex = selectedTabIndex,
                contentColor = LocalContentColor.current,
                containerColor = Color.Transparent,
                divider = {},
                indicator = @Composable { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        val isLandscape =
                            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                        TabRowDefaults.PrimaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            width = if (isLandscape) 92.dp else 64.dp,
                            color = LocalContentColor.current.copy(alpha = AppBlendAlpha)
                        )
                    }
                },
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isLandscape =
                        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                    if (isLandscape) Tab(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                tab.Icon(selectedTabIndex == index)
                                Spacer(modifier = Modifier.width(8.dp))
                                tab.Title()
                            }
                        },
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    ) else Tab(
                        icon = { tab.Icon(selectedTabIndex == index) },
                        text = { tab.Title() },
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    )
                }
            }

            ScreenSurface(animatedContentScope) {
                HorizontalPager(state = pagerState) { index ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        val scrollState = rememberScrollState()
                        Column(Modifier.verticalScroll(scrollState)) {
                            tabs[index].content(this)
                            Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
                        }
                        ScrollShadow(scrollState, top = true)
                        ScrollShadow(scrollState, top = false)
                    }
                }
            }
        }
    }
}

@Immutable
private data class TabItem(
    private val outlinedIcon: ImageVector,
    private val filledIcon: ImageVector,
    @StringRes private val firstTitle: Int,
    @StringRes private val secondTitle: Int,
    val content: @Composable ColumnScope.() -> Unit,
) {
    @Composable
    fun Title() {
        Text(
            stringResource(firstTitle) + stringResource(R.string.spaced_and) + stringResource(
                secondTitle
            )
        )
    }

    @Composable
    fun Icon(isSelected: Boolean) {
        Crossfade(isSelected, label = "icon") {
            Icon(if (it) filledIcon else outlinedIcon, contentDescription = null)
        }
    }
}

const val INTERFACE_CALENDAR_TAB = 0
const val WIDGET_NOTIFICATION_TAB = 1
const val LOCATION_ATHAN_TAB = 2

@Composable
private fun MenuItems(openAddWidgetDialog: () -> Unit, closeMenu: () -> Unit) {
    val context = LocalContext.current
    AppDropdownMenuItem({ Text(stringResource(R.string.live_wallpaper_settings)) }) {
        closeMenu()
        runCatching {
            context.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
        }.onFailure(logException).getOrNull().debugAssertNotNull
    }
    AppDropdownMenuItem({ Text(stringResource(R.string.screensaver_settings)) }) {
        closeMenu()
        runCatching { context.startActivity(Intent(Settings.ACTION_DREAM_SETTINGS)) }.onFailure(
            logException
        ).getOrNull().debugAssertNotNull
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AppDropdownMenuItem({ Text(stringResource(R.string.add_quick_settings_tile)) }) {
            closeMenu()
            context.getSystemService<StatusBarManager>()?.requestAddTileService(
                ComponentName(
                    context.packageName, PersianCalendarTileService::class.qualifiedName ?: "",
                ),
                context.getString(R.string.app_name),
                Icon.createWithResource(context, R.drawable.day19),
                {},
                {},
            )
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && runCatching {
            AppWidgetManager.getInstance(
                context
            ).isRequestPinAppWidgetSupported
        }.getOrNull() == true) {
        AppDropdownMenuItem(
            text = { Text(stringResource(R.string.add_widget)) },
            onClick = openAddWidgetDialog,
        )
    }

    if (!BuildConfig.DEVELOPMENT) return // Rest are development only functionalities
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        AppDropdownMenuItem({ Text("Static vs generated icons") }) { showDialog = true }
        if (showDialog) IconsDemoDialog { showDialog = false }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        AppDropdownMenuItem({ Text("Dynamic Colors") }) { showDialog = true }
        if (showDialog) DynamicColorsDialog { showDialog = false }
    }
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        AppDropdownMenuItem({ Text("Color Scheme") }) { showDialog = true }
        if (showDialog) ColorSchemeDemoDialog { showDialog = false }
    }
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        AppDropdownMenuItem({ Text("Typography") }) { showDialog = true }
        if (showDialog) TypographyDemoDialog { showDialog = false }
    }
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        AppDropdownMenuItem({ Text("Shapes") }) { showDialog = true }
        if (showDialog) ShapesDemoDialog { showDialog = false }
    }
    val activity = LocalActivity.current
    AppDropdownMenuItem({ Text("Clear preferences store and exit") }) {
        context.preferences.edit { clear() }
        activity?.finish()
    }
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        AppDropdownMenuItem({ Text("Schedule an alarm") }) { showDialog = true }
        if (showDialog) ScheduleAlarm { showDialog = false }
    }

    HorizontalDivider()

    fun viewCommandResult(command: String) {
        val dialogBuilder = AlertDialog.Builder(context)
        val result = Runtime.getRuntime().exec(command).inputStream.bufferedReader().readText()
        val button = Button(context).also { button ->
            button.text = "Share"
            button.setOnClickListener {
                context.shareTextFile(result, "log.txt", "text/plain")
            }
        }
        dialogBuilder.setCustomTitle(LinearLayout(context).also {
            it.layoutDirection = View.LAYOUT_DIRECTION_LTR
            it.addView(button)
        })
        dialogBuilder.setView(ScrollView(context).also { scrollView ->
            scrollView.addView(TextView(context).also {
                it.text = result
                it.textDirection = View.TEXT_DIRECTION_LTR
            })
            // Scroll to bottom, https://stackoverflow.com/a/3080483
            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
        })
        dialogBuilder.show()
    }
    listOf(
        "Filtered Log Viewer" to "logcat -v raw -t 500 *:S $LOG_TAG:V AndroidRuntime:E",
        "Unfiltered Log Viewer" to "logcat -v raw -t 500",
    ).forEach { (title, command) ->
        AppDropdownMenuItem({ Text(title) }) { viewCommandResult(command) }
    }

    HorizontalDivider()

    listOf(
        "Log 'Hello'" to { debugLog("Hello!") },
        "Handled Crash" to { logException(Exception("Logged Crash!")) },
        // "Log 'Hello'" to { error("Unhandled Crash!") }
    ).forEach { (text, action) -> AppDropdownMenuItem(text = { Text(text) }, onClick = action) }

    HorizontalDivider()

    AppDropdownMenuItem({ Text("Start Dream") }) {
        // https://stackoverflow.com/a/23112947
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_MAIN).setClassName(
                    "com.android.systemui", "com.android.systemui.Somnambulator"
                )
            )
        }.onFailure(logException).getOrNull().debugAssertNotNull
    }
}
