package com.byagowi.persiancalendar.ui.calendar.times

import android.content.Context
import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import com.byagowi.persiancalendar.ATHANS_LIST
import com.byagowi.persiancalendar.PREF_DISABLE_OWGHAT
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.calendar.EncourageActionLayout
import com.byagowi.persiancalendar.ui.common.MoonView
import com.byagowi.persiancalendar.ui.theme.appSunViewColors
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.startAthan
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.APP_LINK
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import ir.namoo.religiousprayers.praytimeprovider.prayTimesFrom
import ir.namoo.religiousprayers.ui.calendar.NTimes
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimesTab(
    navigateToSettingsLocationTab: () -> Unit,
    navigateToSettingsLocationTabSetAthanAlarm: () -> Unit,
    navigateToAstronomy: (Int) -> Unit,
    viewModel: CalendarViewModel,
    navigateToAthanSetting: (Int) -> Unit,
    navigateToDownload: () -> Unit,
    prayTimeProvider: PrayTimeProvider = koinInject()
) {
    val context = LocalContext.current
    val cityName by cityName.collectAsState()
    val coordinates = coordinates.collectAsState().value ?: return EncourageActionLayout(
        modifier = Modifier.padding(top = 24.dp),
        header = stringResource(R.string.ask_user_to_set_location),
        discardAction = {
            context.appPrefs.edit { putBoolean(PREF_DISABLE_OWGHAT, true) }
            viewModel.removeThirdTab()
        },
        acceptAction = navigateToSettingsLocationTab,
    )
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val today by viewModel.today.collectAsState()
    val jdn by viewModel.selectedDay.collectAsState()
    var prayTimes = coordinates.calculatePrayTimes(jdn.toGregorianCalendar())
    prayTimes = prayTimeProvider.replace(prayTimes, jdn) ?: return
    Column {
        Column(
            Modifier.clickable(
                indication = rememberRipple(bounded = false),
                interactionSource = remember { MutableInteractionSource() },
                onClickLabel = stringResource(R.string.more),
                onClick = { isExpanded = !isExpanded },
            ),
        ) {
            Spacer(Modifier.height(16.dp))
            AstronomicalOverview(viewModel, prayTimes, navigateToAstronomy)
            Spacer(Modifier.height(16.dp))
//            Times(isExpanded, prayTimes)
            NTimes(
                isToday = jdn == today,
                prayTimes = prayTimes,
                navigateToAthanSetting = navigateToAthanSetting,
                navigateToDownload = navigateToDownload
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (cityName != null) Text(
                    modifier = Modifier.combinedClickable(onClick = {}, onLongClick = {
                        startAthan(context, ATHANS_LIST.random()/*SUNRISE_KRY*/, null)
                    }),
                    text = "$cityName ( ${
                        when (prayTimesFrom.value) {
                            0 -> stringResource(R.string.calculated_time)
                            1 -> stringResource(R.string.exact_time)
                            2 -> stringResource(R.string.edited_time)
                            else -> stringResource(R.string.calculated_asr)
                        }
                    })", fontWeight = FontWeight.SemiBold,
                    color = when (prayTimesFrom.value) {
                        2 -> MaterialTheme.colorScheme.secondary
                        0 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
                IconButton(onClick = { shareOwghat(jdn, context, prayTimeProvider) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(id = R.string.share),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
//                ExpandArrow(isExpanded = isExpanded, tint = MaterialTheme.colorScheme.primary)
            }
        }

//        val language by language.collectAsState()
//        if ((language.isPersian || language.isDari) && PREF_ATHAN_ALARM !in context.appPrefs && PREF_NOTIFICATION_ATHAN !in context.appPrefs) {
//            EncourageActionLayout(
//                modifier = Modifier.padding(top = 16.dp),
//                header = "مایلید برنامه اذان پخش کند؟",
//                discardAction = { context.appPrefs.edit { putString(PREF_ATHAN_ALARM, "") } },
//                acceptAction = navigateToSettingsLocationTabSetAthanAlarm,
//            )
//        }
    }
}

@Composable
private fun AstronomicalOverview(
    viewModel: CalendarViewModel,
    prayTimes: PrayTimes,
    navigateToAstronomy: (Int) -> Unit,
) {
    val today by viewModel.today.collectAsState()
    val jdn by viewModel.selectedDay.collectAsState()
    val sunViewNeedsAnimation by viewModel.sunViewNeedsAnimation.collectAsState()
    val now by viewModel.now.collectAsState()
    LaunchedEffect(Unit) { viewModel.astronomicalOverviewLaunched() }

    Crossfade(
        jdn == today,
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
                update = { if (jdn != today) it.jdn = jdn.value.toFloat() },
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.Center)
                    .semantics { @OptIn(ExperimentalComposeUiApi::class) this.invisibleToUser() }
                    .clickable(
                        indication = rememberRipple(bounded = false),
                        interactionSource = remember { MutableInteractionSource() },
                    ) { navigateToAstronomy(jdn - Jdn.today()) },
            )
        }
    }
}

private fun shareOwghat(selectedDay: Jdn, context: Context, prayTimeProvider: PrayTimeProvider) {
    var prayTimes = coordinates.value?.calculatePrayTimes(selectedDay.toGregorianCalendar())
    prayTimes = prayTimeProvider.replace(prayTimes, selectedDay) ?: return
    val cityName = cityName.value
    val dayLength = Clock.fromMinutesCount(
        prayTimes.getFromStringId(R.string.maghrib)
            .toMinutes() - prayTimes.getFromStringId(R.string.fajr).toMinutes()
    )
    val text =
        "\uD83D\uDD4C ${context.getString(R.string.owghat)} $cityName  \uD83D\uDD4C \r\n" +
                "\uD83D\uDDD3 ${
                    dayTitleSummary(selectedDay, selectedDay.toCalendar(mainCalendar))
                } \r\n\uD83D\uDDD3 ${
                    formatDate(selectedDay.toCalendar(CalendarType.ISLAMIC))
                }" +
                "\r\n" +
                "${context.getString(R.string.fajr)} : ${
                    formatNumber(prayTimes.getFromStringId(R.string.fajr).toFormattedString())
                }\r\n" +
                "${context.getString(R.string.sunrise)} : ${
                    formatNumber(
                        prayTimes.getFromStringId(R.string.sunrise).toFormattedString()
                    )
                }\r\n" +
                "${context.getString(R.string.dhuhr)} : ${
                    formatNumber(prayTimes.getFromStringId(R.string.dhuhr).toFormattedString())
                }\r\n" +
                "${context.getString(R.string.asr)} : ${
                    formatNumber(prayTimes.getFromStringId(R.string.asr).toFormattedString())
                }\r\n" +
                "${context.getString(R.string.maghrib)} : ${
                    formatNumber(
                        prayTimes.getFromStringId(R.string.maghrib).toFormattedString()
                    )
                }\r\n" +
                "${context.getString(R.string.isha)} : ${
                    formatNumber(prayTimes.getFromStringId(R.string.isha).toFormattedString())
                }\r\n" +
                "${
                    context.getString(R.string.length_of_day) + spacedColon +
                            dayLength.asRemainingTime(context.resources, short = false)
                } \r\n\r\n" +
                APP_LINK

    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, text)
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
}//end of shareOwghat
