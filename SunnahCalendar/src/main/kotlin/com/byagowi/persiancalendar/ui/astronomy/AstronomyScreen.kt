package com.byagowi.persiancalendar.ui.astronomy

import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.util.lruCache
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_MAP
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_MOON
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_TIME_BAR
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.DatePickerDialog
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.SolarDraw
import com.byagowi.persiancalendar.ui.common.SwitchWithLabel
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.common.TodayActionButton
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.theme.isDynamicGrayscale
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.ui.utils.performLongPress
import com.byagowi.persiancalendar.utils.TEN_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.isSouthernHemisphere
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import io.github.cosinekitty.astronomy.seasons
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.math.abs
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.AstronomyScreen(
    animatedContentScope: AnimatedContentScope,
    openDrawer: () -> Unit,
    navigateToMap: () -> Unit,
    viewModel: AstronomyViewModel,
) {
    LaunchedEffect(Unit) {
        // Default animation screen enter, only if minutes offset is at it's default
        if (viewModel.minutesOffset.value == AstronomyViewModel.DEFAULT_TIME) {
            viewModel.animateToAbsoluteMinutesOffset(0)
        }

        while (true) {
            delay(TEN_SECONDS_IN_MILLIS)
            // Ugly, just to make the offset
            viewModel.addMinutesOffset(1)
            viewModel.addMinutesOffset(-1)
        }
    }

    // Port SliderView to Compose maybe sometime
    var slider by remember { mutableStateOf<SliderView?>(null) }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.astronomy),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors = appTopAppBarColors(),
                navigationIcon = { NavigationOpenDrawerIcon(animatedContentScope, openDrawer) },
                actions = {
                    val minutesOffset by viewModel.minutesOffset.collectAsState()
                    val isTropical by viewModel.isTropical.collectAsState()
                    val mode by viewModel.mode.collectAsState()
                    TodayActionButton(visible = minutesOffset != 0) {
                        viewModel.animateToAbsoluteMinutesOffset(0)
                    }
                    AnimatedVisibility(visible = mode == AstronomyMode.EARTH) {
                        SwitchWithLabel(
                            label = stringResource(R.string.tropical),
                            checked = isTropical,
                            labelBeforeSwitch = true,
                            toggle = viewModel::toggleIsTropical,
                        )
                    }
                    ThreeDotsDropdownMenu(animatedContentScope) { closeMenu ->
                        AppDropdownMenuItem({ Text(stringResource(R.string.select_date)) }) {
                            closeMenu()
                            viewModel.showDatePickerDialog()
                        }
                        AppDropdownMenuItem({ Text(stringResource(R.string.map)) }) {
                            closeMenu()
                            navigateToMap()
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            ScreenSurface(animatedContentScope) {
                val bottomPadding = paddingValues.calculateBottomPadding()
                if (isLandscape) BoxWithConstraints(Modifier.fillMaxSize()) {
                    val maxHeight = maxHeight
                    val maxWidth = maxWidth
                    Row(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier
                                .width((maxWidth / 2).coerceAtMost(480.dp))
                                .fillMaxHeight()
                                .padding(
                                    top = 24.dp,
                                    start = 24.dp,
                                    bottom = bottomPadding + 16.dp,
                                ),
                        ) {
                            Header(Modifier, viewModel)
                            Spacer(Modifier.weight(1f))
                            SliderBar(animatedContentScope, slider, viewModel) { slider = it }
                        }
                        SolarDisplay(
                            Modifier
                                .weight(1f)
                                .padding(top = 16.dp, bottom = bottomPadding + 16.dp)
                                .height(maxHeight - bottomPadding),
                            animatedContentScope, viewModel, slider, navigateToMap,
                        )
                    }
                } else Column {
                    BoxWithConstraints(Modifier.weight(1f, fill = false)) {
                        val maxHeight = maxHeight
                        val maxWidth = maxWidth
                        var needsScroll by remember { mutableStateOf(false) }
                        // Puts content in middle of available space after the measured header
                        Layout(
                            modifier = if (needsScroll) Modifier.verticalScroll(rememberScrollState()) else Modifier,
                            content = {
                                // Header
                                Header(
                                    Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp),
                                    viewModel,
                                )
                                // Content
                                SolarDisplay(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(maxWidth - (56 * 2 + 8).dp),
                                    animatedContentScope, viewModel, slider, navigateToMap,
                                )
                            },
                        ) { (header, content), constraints ->
                            val placeableHeader = header.measure(constraints)
                            val placeableContent = content.measure(constraints)
                            layout(
                                width = constraints.maxWidth,
                                height = max(
                                    placeableHeader.height + placeableContent.height,
                                    maxHeight.roundToPx(),
                                ),
                            ) {
                                // Put the header at top
                                placeableHeader.placeRelative(0, 0)

                                val availableHeight = maxHeight.roundToPx() - placeableHeader.height
                                val space = availableHeight / 2 - placeableContent.height / 2
                                needsScroll = space <= 0
                                placeableContent.placeRelative(
                                    0, placeableHeader.height + space.coerceAtLeast(0)
                                )
                            }
                        }
                    }
                    SliderBar(animatedContentScope, slider, viewModel) { slider = it }
                    Spacer(Modifier.height(16.dp + bottomPadding))
                }
            }
        }
    }

    val isDatePickerDialogShown by viewModel.isDatePickerDialogShown.collectAsState()
    if (isDatePickerDialogShown) {
        val astronomyState by viewModel.astronomyState.collectAsState()
        DatePickerDialog(
            initialJdn = Jdn(astronomyState.date.toCivilDate()),
            onDismissRequest = viewModel::dismissDatePickerDialog,
        ) { jdn -> viewModel.animateToAbsoluteDayOffset(jdn - Jdn.today()) }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.SliderBar(
    animatedContentScope: AnimatedContentScope,
    slider: SliderView?,
    viewModel: AstronomyViewModel,
    setSlider: (SliderView) -> Unit,
) {
    val state by viewModel.astronomyState.collectAsState()
    var lastButtonClickTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    fun buttonScrollSlider(days: Int) {
        lastButtonClickTimestamp = System.currentTimeMillis()
        slider?.smoothScrollBy(250f * days * if (isRtl) 1 else -1, 0f)
        viewModel.animateToRelativeDayOffset(days)
    }

    Column(Modifier.fillMaxWidth()) {
        Text(
            state.date.formatDateAndTime(),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { viewModel.showDatePickerDialog() },
                    onClickLabel = stringResource(R.string.select_date),
                    onLongClick = { viewModel.animateToAbsoluteMinutesOffset(0) },
                    onLongClickLabel = stringResource(R.string.today),
                )
                .sharedElement(
                    rememberSharedContentState(key = SHARED_CONTENT_KEY_TIME_BAR),
                    animatedVisibilityScope = animatedContentScope,
                ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            TimeArrow(::buttonScrollSlider, isPrevious = true)
            val primary = MaterialTheme.colorScheme.primary
            AndroidView(
                factory = { context ->
                    val root = SliderView(context)
                    root.setBarsColor(primary.toArgb())
                    setSlider(root)
                    var latestVibration = 0L
                    root.smoothScrollBy(250f * if (isRtl) 1 else -1, 0f)
                    root.onScrollListener = { dx, _ ->
                        if (dx != 0f) {
                            val current = System.currentTimeMillis()
                            if (current - lastButtonClickTimestamp > 2000) {
                                if (current >= latestVibration + 25_000_000 / abs(dx)) {
                                    root.performHapticFeedbackVirtualKey()
                                    latestVibration = current
                                }
                                viewModel.addMinutesOffset(
                                    (dx * if (isRtl) 1 else -1).toInt()
                                )
                            }
                        }
                    }
                    root
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(46.dp)
                    .weight(1f, fill = false),
            )
            TimeArrow(::buttonScrollSlider, isPrevious = false)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimeArrow(buttonScrollSlider: (Int) -> Unit, isPrevious: Boolean) {
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
                buttonScrollSlider(if (isPrevious) -1 else 1)
            },
            onClickLabel = stringResource(R.string.select_day),
            onLongClick = { buttonScrollSlider(if (isPrevious) -365 else 365) },
            onLongClickLabel = stringResource(
                if (isPrevious) R.string.previous_x else R.string.next_x,
                stringResource(R.string.year)
            ),
        ),
        tint = MaterialTheme.colorScheme.primary,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.SolarDisplay(
    modifier: Modifier,
    animatedContentScope: AnimatedContentScope,
    viewModel: AstronomyViewModel,
    slider: SliderView?,
    navigateToMap: () -> Unit,
) {
    val state by viewModel.astronomyState.collectAsState()
    val isTropical by viewModel.isTropical.collectAsState()
    val mode by viewModel.mode.collectAsState()
    var showHoroscopeDialog by rememberSaveable { mutableStateOf(false) }
    if (showHoroscopeDialog) HoroscopesDialog(state.date.time) { showHoroscopeDialog = false }
    Box(modifier) {
        Column(Modifier.align(Alignment.CenterStart)) {
            AstronomyMode.entries.forEach {
                NavigationRailItem(
                    modifier = Modifier.size(56.dp),
                    selected = mode == it,
                    onClick = { viewModel.setMode(it) },
                    icon = {
                        if (it == AstronomyMode.MOON) MoonIcon(state, animatedContentScope)
                        else Icon(
                            ImageVector.vectorResource(it.icon),
                            modifier = Modifier.size(24.dp),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    },
                )
            }
        }
        val surfaceColor = MaterialTheme.colorScheme.surface
        val contentColor = LocalContentColor.current
        AndroidView(
            factory = {
                val solarView = SolarView(it)
                var clickCount = 0
                solarView.setOnClickListener {
                    if (++clickCount % 2 == 0) showHoroscopeDialog = true
                }
                solarView.rotationalMinutesChange = { offset ->
                    viewModel.addMinutesOffset(offset)
                    slider?.manualScrollBy(offset / 200f, 0f)
                }
                solarView
            },
            modifier = Modifier
                .padding(horizontal = 56.dp)
                .aspectRatio(1f)
                .align(Alignment.Center),
            update = {
                it.setSurfaceColor(surfaceColor.toArgb())
                it.setContentColor(contentColor.toArgb())
                it.isTropicalDegree = isTropical
                it.setTime(state)
                it.mode = mode
            },
        )
        val map = stringResource(R.string.map)
        NavigationRailItem(
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.CenterEnd)
                .sharedBounds(
                    rememberSharedContentState(key = SHARED_CONTENT_KEY_MAP),
                    animatedVisibilityScope = animatedContentScope,
                ),
            selected = false,
            onClick = navigateToMap,
            icon = {
                Text(
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) "m" else "🗺",
                    modifier = Modifier.semantics { this.contentDescription = map },
                )
            },
        )
    }
}

@Composable
private fun Header(modifier: Modifier, viewModel: AstronomyViewModel) {
    val isTropical by viewModel.isTropical.collectAsState()
    val mode by viewModel.mode.collectAsState()
    val state by viewModel.astronomyState.collectAsState()
    val sunZodiac = if (isTropical) Zodiac.fromTropical(state.sun.elon)
    else Zodiac.fromIau(state.sun.elon)
    val moonZodiac = if (isTropical) Zodiac.fromTropical(state.moon.lon)
    else Zodiac.fromIau(state.moon.lon)

    val context = LocalContext.current
    val headerCache = remember {
        lruCache(1024, create = { jdn: Jdn -> state.generateHeader(context.resources, jdn) })
    }

    Column(modifier) {
        val jdn by remember { derivedStateOf { Jdn(state.date.toCivilDate()) } }
        val contentColor = LocalContentColor.current
        headerCache[jdn].fastForEach { line ->
            AnimatedContent(targetState = line, label = "line", transitionSpec = appCrossfadeSpec) {
                SelectionContainer {
                    BasicText(
                        it,
                        color = { contentColor },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        softWrap = false,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 9.sp,
                            maxFontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        ),
                    )
                }
            }
        }
        Seasons(jdn)
        AnimatedVisibility(visible = mode == AstronomyMode.EARTH) {
            Row(Modifier.padding(top = 8.dp)) {
                Box(Modifier.weight(1f)) {
                    Cell(
                        Modifier.align(Alignment.Center),
                        Color(0xcceaaa00),
                        stringResource(R.string.sun),
                        sunZodiac.format(context.resources, true) // ☉☀️
                    )
                }
                Box(Modifier.weight(1f)) {
                    Cell(
                        Modifier.align(Alignment.Center),
                        Color(0xcc606060),
                        stringResource(R.string.moon),
                        moonZodiac.format(context.resources, true) // ☽it.moonPhaseEmoji
                    )
                }
            }
        }
    }
}

@Composable
private fun Seasons(jdn: Jdn) {
    val seasonsCache = remember { lruCache(1024, create = ::seasons) }
    val seasonsOrder = remember {
        if (coordinates.value?.isSouthernHemisphere == true) {
            listOf(Season.WINTER, Season.SPRING, Season.SUMMER, Season.AUTUMN)
        } else listOf(Season.SUMMER, Season.AUTUMN, Season.WINTER, Season.SPRING)
    }
    val equinoxes = (1..4).map { i ->
        Date(
            seasonsCache[CivilDate(
                PersianDate(jdn.toPersianDate().year, i * 3, 29)
            ).year].let {
                when (i) {
                    1 -> it.juneSolstice
                    2 -> it.septemberEquinox
                    3 -> it.decemberSolstice
                    else -> it.marchEquinox
                }
            }.toMillisecondsSince1970()
        ).toGregorianCalendar().formatDateAndTime()
    }
    repeat(2) { row ->
        Row(Modifier.padding(top = 8.dp)) {
            repeat(2) { cell ->
                Box(Modifier.weight(1f)) {
                    Cell(
                        Modifier,
                        seasonsOrder[cell + row * 2].color,
                        stringResource(seasonsOrder[cell + row * 2].nameStringId),
                        equinoxes[cell + row * 2],
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Stable
@Composable
private fun SharedTransitionScope.MoonIcon(
    astronomyState: AstronomyState,
    animatedContentScope: AnimatedContentScope,
) {
    val context = LocalContext.current
    val solarDraw = remember { SolarDraw(context.resources) }
    Box(
        modifier = Modifier
            .size(24.dp)
            .sharedBounds(
                rememberSharedContentState(key = SHARED_CONTENT_KEY_MOON),
                animatedVisibilityScope = animatedContentScope,
            )
            .drawBehind {
                drawIntoCanvas {
                    val radius = size.minDimension / 2f
                    val sun = astronomyState.sun
                    val moon = astronomyState.moon
                    solarDraw.moon(it.nativeCanvas, sun, moon, radius, radius, radius)
                }
            },
    )
}

@Stable
@Composable
private fun Cell(modifier: Modifier, color: Color, label: String, value: String) {
    Row(
        modifier.animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            modifier = Modifier
                .background(
                    if (isDynamicGrayscale()) Color(0xcc808080) else color,
                    MaterialTheme.shapes.small,
                )
                .align(alignment = Alignment.CenterVertically)
                .padding(vertical = 4.dp, horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
        )
        SelectionContainer {
            val contentColor = LocalContentColor.current
            BasicText(
                value,
                style = LocalTextStyle.current,
                color = { contentColor },
                modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 9.sp,
                    maxFontSize = MaterialTheme.typography.bodyMedium.fontSize,
                ),
            )
        }
    }
}
