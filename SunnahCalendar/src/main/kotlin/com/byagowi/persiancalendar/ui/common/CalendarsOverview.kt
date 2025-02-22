package com.byagowi.persiancalendar.ui.common

import android.content.ClipData
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.isAstronomicalExtraFeaturesEnabled
import com.byagowi.persiancalendar.global.isForcedIranTimeEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.utils.ItemWidth
import com.byagowi.persiancalendar.utils.calculateDaysDifference
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.generateZodiacInformation
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.isMoonInScorpio
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import com.byagowi.persiancalendar.utils.toLinearDate
import io.github.cosinekitty.astronomy.seasons
import io.github.persiancalendar.calendar.PersianDate
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun CalendarsOverview(
    jdn: Jdn,
    today: Jdn,
    selectedCalendar: Calendar,
    shownCalendars: List<Calendar>,
    isExpanded: Boolean,
) {
    val context = LocalContext.current
    val isToday = today == jdn
    Column(
        Modifier.semantics {
            if (isTalkBackEnabled) this.contentDescription = getA11yDaySummary(
                context.resources,
                jdn,
                isToday,
                EventsStore.empty(),
                withZodiac = true,
                withOtherCalendars = true,
                withTitle = true
            )
        },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            AnimatedVisibility(isAstronomicalExtraFeaturesEnabled && isExpanded) {
                AndroidView(
                    factory = ::MoonView,
                    update = { it.jdn = jdn.value.toFloat() },
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(20.dp)
                )
            }
            val isForcedIranTimeEnabled by isForcedIranTimeEnabled.collectAsState()
            val language by language.collectAsState()
            AnimatedContent(
                if (isToday && isForcedIranTimeEnabled) language.inParentheses.format(
                    jdn.weekDayName, stringResource(R.string.iran_time)
                ) else jdn.weekDayName,
                transitionSpec = appCrossfadeSpec,
                label = "weekday name",
            ) { SelectionContainer { Text(it, color = MaterialTheme.colorScheme.primary) } }
        }
        Spacer(Modifier.height(8.dp))
        CalendarsFlow(shownCalendars, jdn)
        Spacer(Modifier.height(4.dp))

        val date = jdn on selectedCalendar
        val equinox = remember(selectedCalendar, jdn) {
            if (date !is PersianDate) return@remember null
            if (date.month == 12 && date.dayOfMonth >= 20 || date.month == 1 && date.dayOfMonth == 1)
                equinoxTitle(date, jdn, context).first else null
        }
        val contextColor = LocalContentColor.current

        AnimatedVisibility(visible = equinox != null) {
            SelectionContainer {
                BasicText(
                    equinox ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = { contextColor },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    maxLines = 1,
                    softWrap = false,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = MaterialTheme.typography.labelSmall.fontSize,
                        maxFontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    ),
                )
            }
        }

        AnimatedVisibility(!isToday) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 4.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center,
            ) {
                SelectionContainer {
                    BasicText(
                        listOf(
                            stringResource(R.string.days_distance),
                            spacedColon,
                            calculateDaysDifference(context.resources, jdn, today)
                        ).joinToString(""),
                        color = { contextColor },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.animateContentSize(),
                        maxLines = 1,
                        softWrap = false,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = MaterialTheme.typography.labelSmall.fontSize,
                            maxFontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        ),
                    )
                }
            }
        }

        val showIsMoonInScorpio = isAstronomicalExtraFeaturesEnabled && isMoonInScorpio(jdn)
        AnimatedVisibility(showIsMoonInScorpio) {
            SelectionContainer {
                Text(
                    stringResource(R.string.moon_in_scorpio),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 4.dp),
                )
            }
        }

        AnimatedVisibility(isExpanded && isAstronomicalExtraFeaturesEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 4.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center,
            ) {
                SelectionContainer {
                    Text(
                        generateZodiacInformation(context.resources, jdn, withEmoji = true),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.animateContentSize(),
                    )
                }
            }
        }

        val startOfYearJdn = Jdn(selectedCalendar, date.year, 1, 1)
        val endOfYearJdn = Jdn(selectedCalendar, date.year + 1, 1, 1) - 1
        val currentWeek = jdn.getWeekOfYear(startOfYearJdn)
        val weeksCount = endOfYearJdn.getWeekOfYear(startOfYearJdn)
        val progresses = remember(jdn, selectedCalendar) {
            val (passedDaysInSeason, totalSeasonDays) = jdn.getPositionInSeason()
            val monthLength = selectedCalendar.getMonthLength(date.year, date.month)
            listOfNotNull(
                Triple(R.string.month, date.dayOfMonth, monthLength),
                Triple(R.string.season, passedDaysInSeason, totalSeasonDays),
                Triple(R.string.year, jdn - startOfYearJdn, endOfYearJdn - startOfYearJdn),
            )
        }

        var firstShow by rememberSaveable { mutableStateOf(true) }
        LaunchedEffect(Unit) { firstShow = false }
        val indicatorStrokeWidth by animateDpAsState(
            if (isExpanded && !firstShow) ProgressIndicatorDefaults.CircularStrokeWidth else 0.dp,
            animationSpec = tween(800),
            label = "stroke width",
        )

        AnimatedVisibility(isExpanded) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                progresses.forEach { (stringId, current, max) ->
                    val title = stringResource(stringId)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .semantics {
                                this.contentDescription = "$title$spacedColon$current / $max"
                            }
                            .padding(all = 8.dp),
                    ) {
                        val progress by animateFloatAsState(
                            current.toFloat() / max,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                            ),
                            label = "progress"
                        )
                        CircularProgressIndicator(
                            progress = { progress },
                            strokeWidth = indicatorStrokeWidth
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(title, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        AnimatedVisibility(isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 4.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center,
            ) {
                SelectionContainer {
                    BasicText(
                        stringResource(
                            R.string.start_of_year_diff,
                            formatNumber(jdn - startOfYearJdn + 1),
                            formatNumber(currentWeek),
                            formatNumber(date.month)
                        ),
                        color = { contextColor },
                        modifier = Modifier.animateContentSize(),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        softWrap = false,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = MaterialTheme.typography.labelSmall.fontSize,
                            maxFontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        ),
                    )
                }
            }
        }
        AnimatedVisibility(isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center,
            ) {
                SelectionContainer {
                    BasicText(
                        stringResource(
                            R.string.end_of_year_diff,
                            formatNumber(endOfYearJdn - jdn),
                            formatNumber(weeksCount - currentWeek),
                            formatNumber(12 - date.month)
                        ),
                        color = { contextColor },
                        modifier = Modifier.animateContentSize(),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        softWrap = false,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = MaterialTheme.typography.labelSmall.fontSize,
                            maxFontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        ),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        ExpandArrow(
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.CenterHorizontally),
            isExpanded = isExpanded,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

fun equinoxTitle(date: PersianDate, jdn: Jdn, context: Context): Pair<String, Long> {
    val gregorianYear = jdn.toCivilDate().year
    val timestamp = seasons(gregorianYear).marchEquinox.toMillisecondsSince1970()
    val equinoxYear = when (mainCalendar) {
        Calendar.SHAMSI -> date.year + if (date.month == 12) 1 else 0
        else -> gregorianYear
    }
    val calendar = Date(timestamp).toGregorianCalendar()
    return context.getString(
        R.string.spring_equinox, formatNumber(equinoxYear), calendar.formatDateAndTime()
    ) to timestamp
}

@Composable
private fun CalendarsFlow(calendarsToShow: List<Calendar>, jdn: Jdn) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        calendarsToShow.forEach { calendar ->
            val date = jdn on calendar
            Column(
                modifier = Modifier.defaultMinSize(minWidth = ItemWidth.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val clipboard = LocalClipboard.current
                val coroutineScope = rememberCoroutineScope()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(
                            interactionSource = null,
                            indication = ripple(bounded = false),
                        ) {
                            val entry = ClipData.newPlainText("date", formatDate(date))
                            coroutineScope.launch { clipboard.setClipEntry(entry.toClipEntry()) }
                        }
                        .semantics { this.contentDescription = formatDate(date) },
                ) {
                    Text(
                        formatNumber(date.dayOfMonth),
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.animateContentSize(),
                    )
                    Text(date.monthName, modifier = Modifier.animateContentSize())
                }
                SelectionContainer {
                    Text(date.toLinearDate(), modifier = Modifier.animateContentSize())
                }
            }
        }
    }
}
