package com.byagowi.persiancalendar.ui

import android.appwidget.AppWidgetManager
import android.os.Build
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.about.AboutScreen
import com.byagowi.persiancalendar.ui.about.LicensesScreen
import com.byagowi.persiancalendar.ui.calendar.CalendarScreen
import com.byagowi.persiancalendar.ui.compass.CompassScreen
import com.byagowi.persiancalendar.ui.converter.ConverterScreen
import com.byagowi.persiancalendar.ui.level.LevelScreen
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.settings.SettingsScreen.Companion.INTERFACE_CALENDAR_TAB
import com.byagowi.persiancalendar.ui.settings.SettingsScreen.Companion.LOCATION_ATHAN_TAB
import com.byagowi.persiancalendar.ui.settings.SettingsScreen.Companion.WIDGET_NOTIFICATION_TAB
import com.byagowi.persiancalendar.ui.settings.SettingsScreenArgs
import com.byagowi.persiancalendar.ui.settings.agewidget.AgeWidgetConfigureFragment
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.InterfaceCalendarFragment
import com.byagowi.persiancalendar.ui.settings.locationathan.LocationAthanFragment
import com.byagowi.persiancalendar.ui.settings.widgetnotification.WidgetNotificationFragment
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class FragmentsSmokeTest {

    @Test
    fun themesSmokeTest() {
        listOf(
            R.style.LightTheme, R.style.DarkTheme, R.style.ModernTheme, R.style.AquaTheme,
            R.style.BlackTheme
        ).let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) it + listOf(
                R.style.DynamicLightTheme, R.style.DynamicBlackTheme, R.style.DynamicDarkTheme,
                R.style.DynamicModernTheme
            ) else it
        }.forEach { launchFragmentInContainer<CalendarScreen>(themeResId = it) }
    }

    @Test
    fun fragmentsSmokeTest() {
        launchFragmentInContainer<CalendarScreen>(themeResId = R.style.LightTheme)
        launchFragmentInContainer<AboutScreen>(themeResId = R.style.LightTheme)
        // Doesn't go out for some reason, disable for now
        // launchFragmentInContainer<DeviceInformationScreen>(themeResId = R.style.LightTheme)
        launchFragmentInContainer<LicensesScreen>(themeResId = R.style.LightTheme)
        launchFragmentInContainer<CompassScreen>(themeResId = R.style.LightTheme)
        launchFragmentInContainer<ConverterScreen>(themeResId = R.style.LightTheme)
        launchFragmentInContainer<LevelScreen>(themeResId = R.style.LightTheme)
//      FIXME: These won't work as the use of navigation to retrieve their view model
//        launchFragmentInContainer<AstronomyScreen>(
//            themeResId = R.style.LightTheme,
//            fragmentArgs = AstronomyScreenArgs(0).toBundle()
//        )
//        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
//        navController.setViewModelStore(ViewModelStore())
//        launchFragmentInContainer<MapFragment>(themeResId = R.style.LightTheme).onFragment {
//            navController.setGraph(R.navigation.navigation_graph)
//            Navigation.setViewNavController(it.requireView(), navController)
//        }
//        launchFragmentInContainer<PanoRendoFragment>(themeResId = R.style.LightTheme).onFragment {
//            navController.setGraph(R.navigation.navigation_graph)
//            Navigation.setViewNavController(it.requireView(), navController)
//        }
        listOf(INTERFACE_CALENDAR_TAB, WIDGET_NOTIFICATION_TAB, LOCATION_ATHAN_TAB).forEach {
            launchFragmentInContainer<SettingsScreen>(
                themeResId = R.style.LightTheme,
                fragmentArgs = SettingsScreenArgs(it).toBundle()
            )
        }
        launchFragmentInContainer<AgeWidgetConfigureFragment>(
            themeResId = R.style.LightTheme,
            fragmentArgs = bundleOf(AppWidgetManager.EXTRA_APPWIDGET_ID to 1)
        )
        launchFragmentInContainer<InterfaceCalendarFragment>(themeResId = R.style.LightTheme)
        launchFragmentInContainer<LocationAthanFragment>(themeResId = R.style.LightTheme)
        launchFragmentInContainer<WidgetNotificationFragment>(themeResId = R.style.LightTheme)
    }
}
