package com.byagowi.persiancalendar.ui.calendar.times

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import com.byagowi.persiancalendar.EXPANDED_TIME_STATE_KEY
import com.byagowi.persiancalendar.PREF_ATHAN_ALARM
import com.byagowi.persiancalendar.PREF_DISABLE_OWGHAT
import com.byagowi.persiancalendar.PREF_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_MOON
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.calendar.EncourageActionLayout
import com.byagowi.persiancalendar.ui.common.MoonView
import com.byagowi.persiancalendar.ui.theme.appSunViewColors
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.startAthan
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.APP_LINK
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import ir.namoo.religiousprayers.praytimeprovider.prayTimesFrom
import ir.namoo.religiousprayers.ui.calendar.NTimes
import ir.namoo.religiousprayers.ui.settings.location.ShowLocationDialog
import org.koin.compose.koinInject

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun SharedTransitionScope.TimesTab(
    navigateToSettingsLocationTab: () -> Unit,
    navigateToSettingsLocationTabSetAthanAlarm: () -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    viewModel: CalendarViewModel,
    animatedContentScope: AnimatedContentScope,
    interactionSource: MutableInteractionSource,
    minHeight: Dp,
    bottomPadding: Dp,
    navigateToAthanSetting: (Int) -> Unit,
    navigateToDownload: () -> Unit,
    navigateToEditTimes: () -> Unit,
    prayTimeProvider: PrayTimeProvider = koinInject()
) {
    val context = LocalContext.current
    val cityName by cityName.collectAsState()
    var showLocationDialog by remember { mutableStateOf(false) }
    val coordinates = coordinates.collectAsState().value ?: return Column(Modifier.fillMaxWidth()) {
        EncourageActionLayout(
            modifier = Modifier.padding(top = 24.dp),
            header = stringResource(R.string.ask_user_to_set_location),
            discardAction = {
                context.preferences.edit { putBoolean(PREF_DISABLE_OWGHAT, true) }
                viewModel.removeThirdTab()
            },
            acceptAction = navigateToSettingsLocationTab,
            hideOnAccept = false,
        )
        Spacer(Modifier.height(bottomPadding))
    }
    var isExpanded by rememberSaveable {
        mutableStateOf(context.preferences.getBoolean(EXPANDED_TIME_STATE_KEY, false))
    }
    DisposableEffect(Unit) {
        onDispose { context.preferences.edit { putBoolean(EXPANDED_TIME_STATE_KEY, isExpanded) } }
    }

    val jdn by viewModel.selectedDay.collectAsState()
    var prayTimes = coordinates.calculatePrayTimes(jdn.toGregorianCalendar())
    prayTimes = prayTimeProvider.replace(prayTimes, jdn)
    val now by viewModel.now.collectAsState()
    val today by viewModel.today.collectAsState()

    Column(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .clickable(
                indication = null,
                interactionSource = interactionSource,
                onClickLabel = stringResource(R.string.more),
                onClick = { isExpanded = !isExpanded },
            )
    ) {
        Spacer(Modifier.height(16.dp))
        val isToday = jdn == today
        AstronomicalOverview(
            viewModel, prayTimes, now, isToday, navigateToAstronomy, animatedContentScope
        )
        Spacer(Modifier.height(16.dp))
//        Times(isExpanded, prayTimes, now, isToday)
        NTimes(
            isToday = jdn == today,
            prayTimes = prayTimes,
            navigateToAthanSetting = navigateToAthanSetting,
            navigateToDownload = navigateToDownload
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (cityName != null) Text(
                modifier = Modifier.combinedClickable(
                    onClick = { showLocationDialog = true },
                    onLongClick = {
                        //val alarms = listOf("BFAJR", "AFAJR", "BDHUHR", "ADHUHR", "BASR", "AASR", "BMAGHRIB", "AMAGHRIB", "BISHA", "AISHA")
                        val alarms = PrayTime.athans
                        startAthan(context, alarms.random().name, null)
                    }), text = "$cityName ( ${
                    when (prayTimesFrom.value) {
                        0 -> stringResource(R.string.calculated_time)
                        1 -> stringResource(R.string.exact_time)
                        2 -> stringResource(R.string.edited_time)
                        else -> stringResource(R.string.calculated_asr)
                    }
                })", fontWeight = FontWeight.SemiBold, color = when (prayTimesFrom.value) {
                    2 -> MaterialTheme.colorScheme.secondary
                    0 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }
            )

            IconButton(onClick = navigateToEditTimes) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.edit_times),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { shareOwghat(jdn, context, prayTimeProvider) }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(id = R.string.share),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
//            ExpandArrow(
//                modifier = Modifier.size(20.dp),
//                isExpanded = isExpanded,
//                tint = MaterialTheme.colorScheme.primary
//            )
        }
//        if (showEnableAthanForPersianUsers()) EncourageActionLayout(
//            header = "مایلید برنامه اذان پخش کند؟",
//            discardAction = { context.preferences.edit { putString(PREF_ATHAN_ALARM, "") } },
//            acceptAction = navigateToSettingsLocationTabSetAthanAlarm,
//        )
        Spacer(Modifier.height(bottomPadding))
        AnimatedVisibility(visible = showLocationDialog) {
            ShowLocationDialog(closeDialog = { showLocationDialog = false })
        }
    }
}

@Composable
private fun showEnableAthanForPersianUsers(): Boolean {
    val language by language.collectAsState()
    // As the message is only translated in Persian
    if (!(language.isPersian || language.isDari)) return false
    val context = LocalContext.current
    return PREF_ATHAN_ALARM !in context.preferences && PREF_NOTIFICATION_ATHAN !in context.preferences
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.AstronomicalOverview(
    viewModel: CalendarViewModel,
    prayTimes: PrayTimes,
    now: Long,
    isToday: Boolean,
    navigateToAstronomy: (Jdn) -> Unit,
    animatedContentScope: AnimatedContentScope,
) {
    val jdn by viewModel.selectedDay.collectAsState()
    val sunViewNeedsAnimation by viewModel.sunViewNeedsAnimation.collectAsState()
    LaunchedEffect(Unit) { viewModel.astronomicalOverviewLaunched() }

    Crossfade(
        targetState = isToday,
        label = "heading",
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
    ) { state ->
        val sunViewColors = appSunViewColors()
        if (state) AndroidView(
            factory = ::SunView,
            update = {
                it.colors = sunViewColors
                it.prayTimes = prayTimes
                it.setTime(now)
                if (sunViewNeedsAnimation) {
                    it.startAnimate()
                    viewModel.clearNeedsAnimation()
                } else it.initiate()
            },
            modifier = Modifier.fillMaxHeight(),
        ) else Box(Modifier.fillMaxSize()) {
            AndroidView(
                factory = ::MoonView,
                update = { if (!isToday) it.jdn = jdn.value.toFloat() },
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.Center)
                    .semantics { this.hideFromAccessibility() }
                    .sharedBounds(
                        rememberSharedContentState(key = SHARED_CONTENT_KEY_MOON),
                        animatedVisibilityScope = animatedContentScope,
                    )
                    .clickable(
                        indication = ripple(bounded = false),
                        interactionSource = null,
                    ) { navigateToAstronomy(jdn) },
            )
        }
    }
}

private fun shareOwghat(selectedDay: Jdn, context: Context, prayTimeProvider: PrayTimeProvider) {
    var prayTimes =
        coordinates.value?.calculatePrayTimes(selectedDay.toGregorianCalendar()) ?: return
    prayTimes = prayTimeProvider.replace(prayTimes, selectedDay)
    val cityName = cityName.value
    val dayLength = Clock(prayTimes.maghrib - prayTimes.fajr)
    val text =
        "\uD83D\uDD4C ${context.getString(R.string.owghat)} $cityName  \uD83D\uDD4C \r\n" + "\uD83D\uDDD3 ${
            dayTitleSummary(selectedDay, selectedDay on mainCalendar)
        } \r\n\uD83D\uDDD3 ${
            formatDate(selectedDay on Calendar.ISLAMIC)
        }" + "\r\n" + "${context.getString(R.string.fajr)} : ${
            Clock(prayTimes.fajr).toFormattedString()
        }\r\n" + "${context.getString(R.string.sunrise)} : ${
            Clock(prayTimes.sunrise).toFormattedString()
        }\r\n" + "${context.getString(R.string.dhuhr)} : ${
            Clock(prayTimes.dhuhr).toFormattedString()
        }\r\n" + "${context.getString(R.string.asr)} : ${
            Clock(prayTimes.asr).toFormattedString()
        }\r\n" + "${context.getString(R.string.maghrib)} : ${
            Clock(prayTimes.maghrib).toFormattedString()
        }\r\n" + "${context.getString(R.string.isha)} : ${
            Clock(prayTimes.isha).toFormattedString()
        }\r\n" + "${
            context.getString(R.string.length_of_day) + spacedColon + dayLength.asRemainingTime(
                context.resources, short = false
            )
        } \r\n\r\n" + APP_LINK

    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, text)
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
}//end of shareOwghat
