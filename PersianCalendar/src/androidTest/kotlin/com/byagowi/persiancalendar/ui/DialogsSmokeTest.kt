package com.byagowi.persiancalendar.ui

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.about.showEmailDialog
import com.byagowi.persiancalendar.ui.about.showIconsDemoDialog
import com.byagowi.persiancalendar.ui.about.showTypographyDemoDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.showDayPickerDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.showMonthOverviewDialog
import com.byagowi.persiancalendar.ui.calendar.shiftwork.showShiftWorkDialog
import com.byagowi.persiancalendar.ui.settings.common.showColorPickerDialog
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.calendarsorder.showCalendarPreferenceDialog
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.showHolidaysTypesDialog
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.showLanguagePreferenceDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showAthanGapDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showAthanSelectDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showAthanVolumeDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showPrayerSelectDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.athan.showPrayerSelectPreviewDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showCoordinatesDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showDistrictsDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showGPSLocationDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showLocationPreferenceDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showProvinceDialog
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class DialogsSmokeTest {
    @Test
    fun testDialog() {
        listOf<(MainActivity) -> Unit>(
            { it.showChangeLanguageSnackbar() },
            { it.showAppIsOutDatedSnackbar() },
            { showDayPickerDialog(it, Jdn.today(), R.string.accept) {} },
            { showMonthOverviewDialog(it, Jdn.today().toCalendar(mainCalendar)) },
            { showShiftWorkDialog(it, Jdn.today()) },
            { showIconsDemoDialog(it) },
            { showTypographyDemoDialog(it) },
//            { showColorPickerDialog(it, true, "ABC") },
//            { showColorPickerDialog(it, false, "ABC") },
            { showHolidaysTypesDialog(it) },
            { showCalendarPreferenceDialog(it) {} },
            { showAthanGapDialog(it) },
            {
                showAthanSelectDialog(it, object : ActivityResultLauncher<Unit>() {
                    override fun launch(input: Unit?, options: ActivityOptionsCompat?) = Unit
                    override fun unregister() = Unit
                    override fun getContract(): ActivityResultContract<Unit, *> = TODO()
                })
            },
            { showAthanVolumeDialog(it) },
            { showPrayerSelectDialog(it) },
            { showPrayerSelectPreviewDialog(it) },
            { showCoordinatesDialog(it, it) },
            { showProvinceDialog(it) },
            { showDistrictsDialog(it, listOf("a", "b", "c")) },
            { showGPSLocationDialog(it, it) },
            { showGPSLocationDialog(it, it) },
            { showEmailDialog(it) },
            { showLanguagePreferenceDialog(it) },
            { showLocationPreferenceDialog(it) },
        ).forEach { launcher ->
            ActivityScenario.launch<MainActivity>(
                Intent(
                    ApplicationProvider.getApplicationContext(), MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ).onActivity { activity -> launcher(activity) }
        }
    }
}
