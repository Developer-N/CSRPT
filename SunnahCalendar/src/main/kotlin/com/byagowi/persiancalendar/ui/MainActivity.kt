package com.byagowi.persiancalendar.ui

import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.res.stringResource
import androidx.core.content.getSystemService
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.bringMarketPage
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.applyLanguageToConfiguration
import com.byagowi.persiancalendar.utils.readAndStoreDeviceCalendarEventsOfTheDay
import com.byagowi.persiancalendar.utils.startWorker
import com.byagowi.persiancalendar.utils.update
import io.github.persiancalendar.calendar.PersianDate
import ir.namoo.commons.BASE_API_URL
import ir.namoo.commons.PREF_LAST_UPDATE_CHECK
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.model.UpdateModel
import ir.namoo.commons.repository.DataState
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.repository.asDataState
import ir.namoo.commons.utils.getDayNum
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.commons.utils.openUrlInCustomTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {

    private val prayTimeRepository: PrayTimeRepository = get()
    private val locationsDB: LocationsDB = get()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Just to make sure we have an initial transparent system bars
        // System bars are tweaked later with project's with real values
        applyEdgeToEdge(isBackgroundColorLight = false, isSurfaceColorLight = true)

        setTheme(R.style.BaseTheme)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)

        initGlobal(this)

        startWorker(this)

        readAndStoreDeviceCalendarEventsOfTheDay(applicationContext)
        update(applicationContext, false)

        setContent {
            //AlarmManager Permission
            var showAlarmManagerDialog by remember { mutableStateOf(false) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = getSystemService<AlarmManager>()
                alarmManager?.let {
                    if (!it.canScheduleExactAlarms()) showAlarmManagerDialog = true
                }
            }
            //Check For Update
            var showUpdateDialog by remember { mutableStateOf(false) }
            var updateMessage by remember { mutableStateOf("") }
            var downloadLink by remember { mutableStateOf("") }
            val persianDate = PersianDate(Jdn.today().value)
            LaunchedEffect(key1 = "Update") {
                if (isNetworkConnected(this@MainActivity) && appPrefs.getInt(
                        PREF_LAST_UPDATE_CHECK,
                        1
                    ) != getDayNum(
                        persianDate.month, persianDate.dayOfMonth
                    )
                ) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        prayTimeRepository.getLastUpdateInfo().collect { result ->
                            when (result.asDataState()) {
                                is DataState.Error -> {}
                                DataState.Loading -> {}
                                is DataState.Success -> {
                                    val serverLastUpdate =
                                        (result.asDataState() as DataState.Success<List<UpdateModel>>).data.last()
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
                                            getString(R.string.update_available) + "\n" + serverLastUpdate.changes
                                        downloadLink =
                                            "$BASE_API_URL/app/downloadApp/${serverLastUpdate.id}"
                                        showUpdateDialog = true
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

                App(intent?.action, ::finish)

                AnimatedVisibility(visible = showAlarmManagerDialog) {
                    AlertDialog(onDismissRequest = { showAlarmManagerDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showAlarmManagerDialog = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) startActivity(
                                    Intent().apply {
                                        action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                                    })
                            }) {
                                Text(text = stringResource(id = R.string.ok))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAlarmManagerDialog = false }) {
                                Text(text = stringResource(id = R.string.cancel))
                            }
                        },
                        title = { Text(text = stringResource(id = R.string.requset_permision)) },
                        text = { Text(text = stringResource(id = R.string.schedule_permission_message)) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Alarm, contentDescription = "Alarm"
                            )
                        })

                }
                AnimatedVisibility(visible = showUpdateDialog) {
                    AlertDialog(
                        onDismissRequest = { showUpdateDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showUpdateDialog = false
                                openUrlInCustomTab(downloadLink)
                            }) {
                                Text(text = stringResource(id = R.string.download))
                            }
                        }, dismissButton = {
                            TextButton(onClick = {
                                showUpdateDialog = false
                                bringMarketPage()
                            }) {
                                Text(text = stringResource(id = R.string.markets))
                            }
                        }, title = { Text(text = stringResource(id = R.string.update)) },
                        text = { Text(text = updateMessage) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Update"
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                language.collect { applyAppLanguage(this@MainActivity) }
            }
        }
    }

    private fun applyEdgeToEdge(isBackgroundColorLight: Boolean, isSurfaceColorLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) enableEdgeToEdge(
            if (isBackgroundColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
            if (isSurfaceColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
        ) else enableEdgeToEdge( // Just don't tweak navigation bar in older Android versions
            if (isBackgroundColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT)
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
