package com.byagowi.persiancalendar.ui.map

import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Matrix
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.SocialDistance
import androidx.compose.material.icons.filled._3dRotation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_SHOW_QIBLA_IN_COMPASS
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_MAP
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_TIME_BAR
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.common.DatePickerDialog
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ZoomableView
import com.byagowi.persiancalendar.ui.settings.locationathan.location.CoordinatesDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.GPSLocationDialog
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.utils.performLongPress
import com.byagowi.persiancalendar.utils.ONE_MINUTE_IN_MILLIS
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MapScreen(
    animatedContentScope: AnimatedContentScope,
    navigateUp: () -> Unit,
    fromSettings: Boolean,
    viewModel: MapViewModel,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val mapDraw = remember { MapDraw(context.resources) }

    LaunchedEffect(Unit) { coordinates.collect(viewModel::changeCurrentCoordinates) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(ONE_MINUTE_IN_MILLIS)
            viewModel.addOneMinute()
        }
    }

    var showGpsDialog by rememberSaveable { mutableStateOf(false) }
    if (showGpsDialog) GPSLocationDialog { showGpsDialog = false }

    var clickedCoordinates by remember { mutableStateOf<Coordinates?>(null) }
    var showCoordinatesDialog by rememberSaveable { mutableStateOf(false) }
    var saveCoordinates by rememberSaveable { mutableStateOf(fromSettings) }
    if (showCoordinatesDialog) CoordinatesDialog(
        inputCoordinates = clickedCoordinates,
        onDismissRequest = { showCoordinatesDialog = false },
        saveCoordinates = saveCoordinates,
        toggleSaveCoordinates = { saveCoordinates = !saveCoordinates },
        notifyChange = viewModel::changeCurrentCoordinates,
    )

    var showMapTypesDialog by rememberSaveable { mutableStateOf(false) }
    if (showMapTypesDialog) AppDialog(onDismissRequest = { showMapTypesDialog = false }) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            MapType.entries.drop(1) // Hide "None" option
                // Hide moon visibilities for now unless is a development build
                .filter { !it.isCrescentVisibility || BuildConfig.DEVELOPMENT }.forEach {
                    Text(
                        stringResource(it.title),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMapTypesDialog = false
                                viewModel.changeMapType(it)
                            }
                            .padding(vertical = 16.dp, horizontal = 24.dp),
                    )
                }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    class MenuItem(
        val icon: ImageVector,
        @StringRes val title: Int,
        val isEnabled: () -> Boolean = { false },
        val onClick: () -> Unit
    )

    val menu = listOf(
        MenuItem(Icons.Default._3dRotation, R.string.show_globe_view_label) onClick@{
            val textureSize = 2048
            val bitmap =
                runCatching { createBitmap(textureSize, textureSize) }.onFailure(logException)
                    .getOrNull() ?: return@onClick
            val matrix = Matrix()
            matrix.setScale(
                textureSize.toFloat() / mapDraw.mapWidth,
                textureSize.toFloat() / mapDraw.mapHeight,
            )
            mapDraw.draw(
                canvas = Canvas(bitmap),
                matrix = matrix,
                displayLocation = state.displayLocation,
                coordinates = state.coordinates,
                directPathDestination = state.directPathDestination,
                displayGrid = state.displayGrid
            )
            showGlobeDialog(context, bitmap, lifecycleOwner.lifecycle)
            // DO NOT use bitmap after this
        },
        MenuItem(Icons.Default.SocialDistance,
            R.string.show_direct_path_label,
            { state.isDirectPathMode }) {
            if (state.coordinates == null) showGpsDialog = true
            else viewModel.toggleDirectPathMode()
        },
        MenuItem(Icons.Default.Grid3x3, R.string.show_grid_label, { state.displayGrid }) {
            viewModel.toggleDisplayGrid()
        },
        MenuItem(Icons.Default.MyLocation, R.string.show_my_location_label) {
            showGpsDialog = true
        },
        MenuItem(Icons.Default.LocationOn,
            R.string.show_location_label,
            { state.coordinates != null && state.displayLocation }) {
            if (state.coordinates == null) showGpsDialog = true
            else viewModel.toggleDisplayLocation()
        },
        MenuItem(
            Icons.Default.NightlightRound, R.string.show_night_mask_label,
            { state.mapType != MapType.NONE },
        ) {
            if (viewModel.state.value.mapType == MapType.NONE) showMapTypesDialog = true
            else viewModel.changeMapType(MapType.NONE)
        },
    )
    val showKaaba = remember { context.preferences.getBoolean(PREF_SHOW_QIBLA_IN_COMPASS, true) }
    var formattedTime by remember { mutableStateOf("") }
    Box {
        // Best effort solution for landscape view till figuring out something better
        if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            Column {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
                Spacer(Modifier.height((16 + menuHeight + 16).dp))
                ScreenSurface(animatedContentScope) { Box(Modifier.fillMaxSize()) }
            }
        }
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .sharedBounds(
                    rememberSharedContentState(key = SHARED_CONTENT_KEY_MAP),
                    animatedVisibilityScope = animatedContentScope,
                ),
            factory = {
                val root = ZoomableView(it)
                root.contentWidth = mapDraw.mapWidth.toFloat()
                root.contentHeight = mapDraw.mapHeight.toFloat()
                root.maxScale = 512f
                root.contentDescription = it.getString(R.string.map)
                root.onClick = { x: Float, y: Float ->
                    val latitude = 90 - y / mapDraw.mapScaleFactor
                    val longitude = x / mapDraw.mapScaleFactor - 180
                    if (abs(latitude) < 90 && abs(longitude) < 180) {
                        // Easter egg like feature, bring sky renderer fragment
                        if (abs(latitude) < 2 && abs(longitude) < 2 && viewModel.state.value.displayGrid) {
                            Toast.makeText(context, "Null Island!", Toast.LENGTH_SHORT).show()
                        } else {
                            val coordinates =
                                Coordinates(latitude.toDouble(), longitude.toDouble(), 0.0)
                            if (viewModel.state.value.isDirectPathMode) viewModel.changeDirectPathDestination(
                                coordinates
                            )
                            else {
                                clickedCoordinates = coordinates
                                showCoordinatesDialog = true
                            }
                        }
                    }
                }
                root
            },
            update = {
                it.onDraw = { canvas, matrix ->
                    mapDraw.draw(
                        canvas = canvas,
                        matrix = matrix,
                        displayLocation = state.displayLocation,
                        coordinates = state.coordinates,
                        directPathDestination = state.directPathDestination,
                        displayGrid = state.displayGrid
                    )
                }
                mapDraw.drawKaaba = state.coordinates != null && state.displayLocation && showKaaba
                mapDraw.updateMap(state.time, state.mapType)
                formattedTime = mapDraw.maskFormattedTime
                it.invalidate()
            },
        )
    }

    Column {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
        Row(
            Modifier
                .alpha(.85f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraLarge)
                .height(menuHeight.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavigationRailItem(
                modifier = Modifier.weight(1f),
                selected = false,
                onClick = navigateUp,
                icon = {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_up),
                    )
                },
            )
            menu.forEach {
                NavigationRailItem(
                    modifier = Modifier.weight(1f),
                    onClick = it.onClick,
                    selected = false,
                    icon = {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = { PlainTooltip { Text(text = stringResource(it.title)) } },
                            state = rememberTooltipState()
                        ) {
                            val tint by animateColor(
                                if (it.isEnabled()) MaterialTheme.colorScheme.inversePrimary
                                else LocalContentColor.current
                            )
                            Icon(it.icon, stringResource(it.title), tint = tint)
                        }
                    },
                )
            }
        }
    }

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    if (showDatePickerDialog) {
        val currentJdn = Jdn(Date(state.time).toGregorianCalendar().toCivilDate())
        DatePickerDialog(currentJdn, { showDatePickerDialog = false }) { jdn ->
            viewModel.addDays(jdn - currentJdn)
        }
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = formattedTime.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(all = 16.dp)
                    .height(46.dp)
                    .fillMaxWidth(),
            ) {
                TimeArrow(mapDraw, viewModel, isPrevious = true)
                AnimatedContent(
                    modifier = Modifier.weight(1f, fill = false),
                    targetState = formattedTime,
                    label = "time",
                    transitionSpec = appCrossfadeSpec,
                ) { state ->
                    Text(
                        state,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { showDatePickerDialog = true },
                                onClickLabel = stringResource(R.string.select_date),
                                onLongClick = { viewModel.changeToTime(Date()) },
                                onLongClickLabel = stringResource(R.string.today),
                            )
                            .sharedElement(
                                rememberSharedContentState(key = SHARED_CONTENT_KEY_TIME_BAR),
                                animatedVisibilityScope = animatedContentScope,
                            ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                TimeArrow(mapDraw, viewModel, isPrevious = false)
            }
        }
    }
}

@Composable
private fun TimeArrow(mapDraw: MapDraw, viewModel: MapViewModel, isPrevious: Boolean) {
    val hapticFeedback = LocalHapticFeedback.current
    Icon(
        if (isPrevious) Icons.AutoMirrored.Default.KeyboardArrowLeft
        else Icons.AutoMirrored.Default.KeyboardArrowRight,
        contentDescription = stringResource(
            if (isPrevious) R.string.previous_x else R.string.next_x,
            stringResource(R.string.day),
        ),
        Modifier.combinedClickable(
            indication = ripple(bounded = false),
            interactionSource = null,
            onClick = {
                hapticFeedback.performLongPress()
                if (mapDraw.currentMapType.isCrescentVisibility) viewModel.addDays(if (isPrevious) -1 else 1)
                else {
                    if (isPrevious) viewModel.subtractOneHour() else viewModel.addOneHour()
                }
            },
            onClickLabel = stringResource(R.string.select_day),
            onLongClick = { viewModel.addDays(if (isPrevious) -10 else 10) },
        ),
        tint = MaterialTheme.colorScheme.primary,
    )
}

private const val menuHeight = 56
