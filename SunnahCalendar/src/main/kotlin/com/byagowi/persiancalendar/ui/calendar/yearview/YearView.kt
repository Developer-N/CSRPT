package com.byagowi.persiancalendar.ui.calendar.yearview

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.core.util.lruCache
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.calendar.calendarpager.DayPainter
import com.byagowi.persiancalendar.ui.calendar.calendarpager.renderMonthWidget
import com.byagowi.persiancalendar.ui.calendar.detectZoom
import com.byagowi.persiancalendar.ui.theme.appMonthColors
import com.byagowi.persiancalendar.ui.utils.AnimatableFloatSaver
import com.byagowi.persiancalendar.ui.utils.LargeShapeCornerSize
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.otherCalendarFormat
import com.byagowi.persiancalendar.utils.readYearDeviceEvents
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor

@Composable
fun YearView(viewModel: CalendarViewModel, maxWidth: Dp, maxHeight: Dp, bottomPadding: Dp) {
    val today by viewModel.today.collectAsState()
    val todayDate = today on mainCalendar
    val selectedMonthOffset by viewModel.selectedMonthOffset.collectAsState()
    val yearOffsetInMonths = run {
        val selectedMonth = mainCalendar.getMonthStartFromMonthsDistance(today, selectedMonthOffset)
        selectedMonth.year - todayDate.year
    }

    val monthNames = mainCalendar.monthsNames
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val scale = rememberSaveable(saver = AnimatableFloatSaver) { Animatable(1f) }
    val horizontalDivisions = if (isLandscape) 4 else 3
    viewModel.yearViewIsInYearSelection(scale.value < yearSelectionModeMaxScale)

    val width = floor(maxWidth.value / horizontalDivisions * scale.value).coerceAtLeast(1f).dp
    val height = ((maxHeight - bottomPadding) / if (isLandscape) 3 else 4) * scale.value
    val shape = MaterialTheme.shapes.large.copy(CornerSize(LargeShapeCornerSize.dp * scale.value))

    val titleHeight = with(LocalDensity.current) {
        (height / 10).coerceAtLeast(20.dp).toSp() / 1.6f
    }
    val titleLineHeight = titleHeight * 1.6f
    val padding = 4.dp

    val widthInPx = with(LocalDensity.current) { width.toPx() }
    val paddingInPx = with(LocalDensity.current) { padding.toPx() }

    val context = LocalContext.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val monthColors = appMonthColors()
    val dayPainter = remember(monthColors, widthInPx) {
        lruCache(4, create = { height: Float ->
            DayPainter(
                resources = context.resources,
                width = (widthInPx - paddingInPx * 2f) / if (isShowWeekOfYearEnabled.value) 8 else 7,
                height = height / 7,/* rows count*/
                isRtl = isRtl,
                colors = monthColors,
                isYearView = true,
                selectedDayColor = monthColors.indicator.toArgb(),
            )
        })
    }

    val lazyListState = rememberLazyListState(halfPages + yearOffsetInMonths)
    val yearViewCommand by viewModel.yearViewCommand.collectAsState()
    LaunchedEffect(key1 = yearViewCommand) {
        when (yearViewCommand ?: return@LaunchedEffect) {
            YearViewCommand.ToggleYearSelection -> scale.snapTo(if (scale.value > .5f) 0.01f else 1f)

            YearViewCommand.PreviousMonth -> {
                lazyListState.animateScrollToItem(lazyListState.firstVisibleItemIndex - 1)
            }

            YearViewCommand.NextMonth -> {
                lazyListState.animateScrollToItem(lazyListState.firstVisibleItemIndex + 1)
            }

            YearViewCommand.TodayMonth -> {
                scale.animateTo(1f)
                if (abs(lazyListState.firstVisibleItemIndex - halfPages) > 2) {
                    lazyListState.scrollToItem(halfPages)
                } else lazyListState.animateScrollToItem(halfPages)
            }
        }
        viewModel.clearYearViewCommand()
    }

    val current by remember { derivedStateOf { lazyListState.firstVisibleItemIndex - halfPages } }
    LaunchedEffect(key1 = current) { viewModel.notifyYearViewOffset(current) }

    val coroutineScope = rememberCoroutineScope()

    val detectZoom = Modifier.detectZoom {
        coroutineScope.launch {
            val value = scale.value * it
            scale.snapTo(
                value.coerceIn(yearSelectionModeMaxScale, horizontalDivisions.toFloat())
            )
        }
    }

    LazyColumn(state = lazyListState, modifier = detectZoom) {
        items(halfPages * 2) {
            val yearOffset = it - halfPages

            Column(Modifier.fillMaxWidth()) {
                if (scale.value > yearSelectionModeMaxScale) {
                    val yearDeviceEvents: DeviceCalendarEventsStore =
                        remember(yearOffset, today) {
                            val yearStartJdn = Jdn(
                                mainCalendar.createDate(
                                    today.on(mainCalendar).year + yearOffset, 1, 1
                                )
                            )
                            if (isShowDeviceCalendarEvents.value) {
                                context.readYearDeviceEvents(yearStartJdn)
                            } else EventsStore.empty()
                        }
                    FlowRow(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        repeat(if (isLandscape) 3 else 4) { row ->
                            repeat(if (isLandscape) 4 else 3) { column ->
                                val month = 1 + column + row * if (isLandscape) 4 else 3
                                val offset = yearOffset * 12 + month - todayDate.month
                                val title = language.value.my.format(
                                    monthNames[month - 1],
                                    formatNumber(yearOffset + todayDate.year),
                                )
                                Column(
                                    Modifier
                                        .size(width, height)
                                        .padding(padding)
                                        .clip(shape)
                                        .then(detectZoom)
                                        .clickable(onClickLabel = stringResource(R.string.select_month)) {
                                            viewModel.closeYearView()
                                            viewModel.changeSelectedMonthOffsetCommand(offset)
                                        }
                                        .background(LocalContentColor.current.copy(alpha = .1f))
                                        .then(
                                            if (offset != selectedMonthOffset) Modifier else Modifier.border(
                                                width = 2.dp,
                                                color = LocalContentColor.current.copy(alpha = .15f),
                                                shape = shape
                                            )
                                        ),
                                ) {
                                    Text(
                                        title,
                                        Modifier.fillMaxWidth(),
                                        fontSize = titleHeight,
                                        textAlign = TextAlign.Center,
                                        lineHeight = titleLineHeight,
                                    )
                                    Canvas(Modifier.fillMaxSize()) {
                                        drawIntoCanvas { canvas ->
                                            renderMonthWidget(
                                                dayPainter = dayPainter[this.size.height],
                                                width = size.width,
                                                canvas = canvas.nativeCanvas,
                                                today = today,
                                                baseDate = mainCalendar.getMonthStartFromMonthsDistance(
                                                    today, offset
                                                ),
                                                deviceEvents = yearDeviceEvents,
                                                isRtl = isRtl,
                                                isShowWeekOfYearEnabled = isShowWeekOfYearEnabled.value,
                                                selectedDay = viewModel.selectedDay.value,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                val space = bottomPadding * scale.value.coerceIn(.4f, 1f)
                val alpha = (.15f * (1 - scale.value)).coerceIn(0f, .15f)
                Spacer(Modifier.height(space))
                if (yearOffset != halfPages - 1) Box(Modifier.align(Alignment.CenterHorizontally)) {
                    val year = yearOffset + todayDate.year + 1
                    val tooltip = enabledCalendars.let { if (it.size > 1) it.drop(1) else it }
                        .map { calendar ->
                            otherCalendarFormat(
                                year,
                                calendar
                            ) + " " + stringResource(calendar.title)
                        }.joinToString("\n")
                    @OptIn(ExperimentalMaterial3Api::class)
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = { PlainTooltip { Text(tooltip, textAlign = TextAlign.Center) } },
                        state = rememberTooltipState(),
                    ) {
                        Text(
                            formatNumber(year),
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .background(LocalContentColor.current.copy(alpha = alpha))
                                .padding((32 * alpha).dp)
                                .then(detectZoom)
                                .clickable(onClickLabel = stringResource(R.string.select_year)) {
                                    coroutineScope.launch {
                                        if (scale.value <= yearSelectionModeMaxScale) scale.snapTo(
                                            1f
                                        )
                                        lazyListState.animateScrollToItem(halfPages + yearOffset + 1)
                                    }
                                },
                        )
                    }
                }
            }
        }
    }
}

private const val yearSelectionModeMaxScale = .2f
private const val halfPages = 200
