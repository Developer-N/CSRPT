package com.byagowi.persiancalendar.ui

import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.edit
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.lifecycleScope
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.bringMarketPage
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.applyLanguageToConfiguration
import com.byagowi.persiancalendar.utils.eventKey
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.jdnActionKey
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.readAndStoreDeviceCalendarEventsOfTheDay
import com.byagowi.persiancalendar.utils.startWorker
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import com.byagowi.persiancalendar.utils.update
import io.github.persiancalendar.calendar.PersianDate
import ir.namoo.commons.PREF_LAST_UPDATE_CHECK
import ir.namoo.commons.PREF_LAST_UPDATE_PRAY_TIMES_KEY
import ir.namoo.commons.model.UpdateModel
import ir.namoo.commons.repository.DataState
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.commons.utils.getDayNum
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.commons.utils.openUrlInCustomTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {

    private val prayTimeRepository: PrayTimeRepository = get()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Just to make sure we have an initial transparent system bars
        // System bars are tweaked later with project's with real values
        applyEdgeToEdge(isBackgroundColorLight = false, isSurfaceColorLight = true)

        setTheme(R.style.BaseTheme)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)

        intent.getLongExtra(eventKey, -1L).takeIf { it != -1L }?.let { eventId ->
            val intent = Intent(Intent.ACTION_VIEW).setData(
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            )
            runCatching { startActivity(intent) }.onFailure(logException)
            return finish()
        }

        initGlobal(this)

        startWorker(this)

        readAndStoreDeviceCalendarEventsOfTheDay(applicationContext)
        update(applicationContext, false)

        val initialJdn = run {
            // Follows https://github.com/FossifyOrg/Calendar/blob/fb56145d/app/src/main/kotlin/org/fossify/calendar/activities/MainActivity.kt#L531-L554
            // Receives content://com.android.calendar/time/1740774600000 or content://0@com.android.calendar/time/1740774600000
            intent?.data?.takeIf {
                when (CalendarContract.AUTHORITY) {
                    it.authority, it.authority?.substringAfter("@") -> true
                    else -> false
                } && when {
                    it.path?.startsWith("/time") == true -> true
                    intent?.extras?.getBoolean("DETAIL_VIEW", false) == true -> true
                    else -> false
                }
            }?.pathSegments?.last()?.toLongOrNull()?.let {
                Jdn(Date(it).toGregorianCalendar().toCivilDate())
            } ?: (intent.getLongExtra(jdnActionKey, -1L).takeIf { it != -1L }
                ?: intent.action?.takeIf {
                    it.startsWith(jdnActionKey)
                }?.replace(jdnActionKey, "")?.toLongOrNull())?.let(::Jdn)
        }
        setContent {
            //Check For Update
            var showUpdateDialog by remember { mutableStateOf(false) }
            var updateMessage by remember { mutableStateOf("") }
            LaunchedEffect(key1 = "Update") {
                val persianDate = PersianDate(Jdn.today().value)
                if (isNetworkConnected(this@MainActivity) && appPrefsLite.getInt(
                        PREF_LAST_UPDATE_CHECK, 1
                    ) != getDayNum(
                        persianDate.month, persianDate.dayOfMonth
                    )
                ) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        prayTimeRepository.getLastUpdateInfo().collectLatest { state ->
                            when (state) {
                                is DataState.Error -> {}
                                DataState.Loading -> {}
                                is DataState.Success -> {
                                    state.data as List<*>
                                    val serverLastUpdate = state.data.last() as UpdateModel
                                    val pInfo: PackageInfo =
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            packageManager.getPackageInfo(
                                                packageName, PackageManager.PackageInfoFlags.of(0)
                                            )
                                        } else {
                                            packageManager.getPackageInfo(packageName, 0)
                                        }
                                    val versionCode: Long =
                                        PackageInfoCompat.getLongVersionCode(pInfo)
                                    if (versionCode < serverLastUpdate.versionCode) withContext(
                                        Dispatchers.Main
                                    ) {
                                        updateMessage =
                                            serverLastUpdate.changes.replace("\n\n", "\n")
                                        showUpdateDialog = true
                                        appPrefsLite.edit {
                                            putInt(
                                                PREF_LAST_UPDATE_CHECK,
                                                getDayNum(persianDate.month, persianDate.dayOfMonth)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            AppTheme {
                val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
                val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
                LaunchedEffect(isBackgroundColorLight, isSurfaceColorLight) {
                    applyEdgeToEdge(isBackgroundColorLight, isSurfaceColorLight)
                }

                val view = LocalView.current
                LaunchedEffect(Unit) {
                    language.collect {
                        onConfigurationChanged(resources.configuration)
                        view.dispatchConfigurationChanged(resources.configuration)
                    }
                }

                App(intent?.action, initialJdn, ::finish)
                AnimatedVisibility(visible = showUpdateDialog) {
                    AlertDialog(onDismissRequest = { showUpdateDialog = false }, confirmButton = {
                        TextButton(onClick = {
                            showUpdateDialog = false
                            openUrlInCustomTab("https://namoodev.ir/pt")
                        }) {
                            Text(
                                text = stringResource(id = R.string.open_site),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }, dismissButton = {
                        TextButton(onClick = {
                            showUpdateDialog = false
                            bringMarketPage()
                        }) {
                            Text(
                                text = stringResource(id = R.string.markets),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }, title = {
                        Text(
                            text = stringResource(id = R.string.update),
                            fontWeight = FontWeight.SemiBold
                        )
                    }, text = {
                        Column {
                            Text(
                                text = stringResource(id = R.string.update_available),
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Justify
                            )
                            Text(
                                text = formatNumber(updateMessage), textAlign = TextAlign.Justify
                            )
                        }
                    }, icon = {
                        Icon(
                            imageVector = Icons.Default.Update, contentDescription = "Update"
                        )
                    })
                }
            }
        }

        applyAppLanguage(this)

        // There is a window:enforceNavigationBarContrast set to false in styles.xml as the following
        // isn't as effective in dark themes.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        //update downloaded times every week
        lifecycleScope.launch(Dispatchers.IO) {
            if (isNetworkConnected(this@MainActivity)) {
                val lastDownloadDay = Jdn(
                    appPrefsLite.getLong(
                        PREF_LAST_UPDATE_PRAY_TIMES_KEY, 0
                    )
                ).value
                val diff = Jdn.today().value - lastDownloadDay
                if (diff.milliseconds.inWholeDays > 7) {
                    val cityList = prayTimeRepository.getLocalCityList()
                    cityList.find { it.name == cityName.value }?.id?.let { cityID ->
                        prayTimeRepository.getTimesForCityAndSaveToLocalDB(cityID)
                            .collectLatest { state ->
                                when (state) {
                                    is DataState.Error -> {}
                                    DataState.Loading -> {}
                                    is DataState.Success -> {
                                        appPrefsLite.edit {
                                            putLong(
                                                PREF_LAST_UPDATE_PRAY_TIMES_KEY, Jdn.today().value
                                            )
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        }
    }

    private fun applyEdgeToEdge(isBackgroundColorLight: Boolean, isSurfaceColorLight: Boolean) {
        val statusBarStyle =
            if (isBackgroundColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) enableEdgeToEdge(
            statusBarStyle,
            if (isSurfaceColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
        ) else enableEdgeToEdge(
            statusBarStyle,
            // Just don't tweak navigation bar in older Android versions, leave it to default
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(applyLanguageToConfiguration(newConfig))
        applyAppLanguage(this)
    }

    override fun onResume() {
        super.onResume()
        applyAppLanguage(this)
        update(applicationContext, false)
        ++resumeToken_.value
    }
}

private val resumeToken_ = MutableStateFlow(0)
val resumeToken: StateFlow<Int> = resumeToken_
