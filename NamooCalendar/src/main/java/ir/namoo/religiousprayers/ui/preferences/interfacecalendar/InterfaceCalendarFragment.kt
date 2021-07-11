package ir.namoo.religiousprayers.ui.preferences.interfacecalendar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import ir.namoo.religiousprayers.AppLocalesData
import ir.namoo.religiousprayers.DEFAULT_ISLAMIC_OFFSET
import ir.namoo.religiousprayers.DEFAULT_WEEK_ENDS
import ir.namoo.religiousprayers.DEFAULT_WEEK_START
import ir.namoo.religiousprayers.LANG_AR
import ir.namoo.religiousprayers.LANG_EN_US
import ir.namoo.religiousprayers.LANG_ES
import ir.namoo.religiousprayers.LANG_FR
import ir.namoo.religiousprayers.LANG_JA
import ir.namoo.religiousprayers.PREF_APP_FONT
import ir.namoo.religiousprayers.PREF_APP_LANGUAGE
import ir.namoo.religiousprayers.PREF_ASTRONOMICAL_FEATURES
import ir.namoo.religiousprayers.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import ir.namoo.religiousprayers.PREF_HOLIDAY_TYPES
import ir.namoo.religiousprayers.PREF_ISLAMIC_OFFSET
import ir.namoo.religiousprayers.PREF_PERSIAN_DIGITS
import ir.namoo.religiousprayers.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import ir.namoo.religiousprayers.PREF_SHOW_WEEK_OF_YEAR_NUMBER
import ir.namoo.religiousprayers.PREF_THEME
import ir.namoo.religiousprayers.PREF_WEEK_ENDS
import ir.namoo.religiousprayers.PREF_WEEK_START
import ir.namoo.religiousprayers.PREF_WIDGET_IN_24
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.SYSTEM_DEFAULT_FONT
import ir.namoo.religiousprayers.SYSTEM_DEFAULT_THEME
import ir.namoo.religiousprayers.ui.preferences.PREF_DESTINATION
import ir.namoo.religiousprayers.ui.preferences.build
import ir.namoo.religiousprayers.ui.preferences.clickable
import ir.namoo.religiousprayers.ui.preferences.dialogTitle
import ir.namoo.religiousprayers.ui.preferences.interfacecalendar.calendarsorder.showCalendarPreferenceDialog
import ir.namoo.religiousprayers.ui.preferences.multiSelect
import ir.namoo.religiousprayers.ui.preferences.section
import ir.namoo.religiousprayers.ui.preferences.singleSelect
import ir.namoo.religiousprayers.ui.preferences.summary
import ir.namoo.religiousprayers.ui.preferences.switch
import ir.namoo.religiousprayers.ui.preferences.title
import ir.namoo.religiousprayers.utils.askForCalendarPermission
import ir.namoo.religiousprayers.utils.formatNumber
import ir.namoo.religiousprayers.utils.language

class InterfaceCalendarFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val destination = arguments?.getString(PREF_DESTINATION)
        if (destination == PREF_HOLIDAY_TYPES) showHolidaysTypesDialog()

        preferenceScreen = preferenceManager.createPreferenceScreen(context).build {
            section(R.string.pref_interface) {
                clickable(onClick = { showLanguagePreferenceDialog() }) {
                    if (destination == PREF_APP_LANGUAGE)
                        title = "Language"
                    else
                        title(R.string.language)
                }
                singleSelect(
                    PREF_THEME,
                    resources.getStringArray(R.array.themeNames).toList(),
                    resources.getStringArray(R.array.themeKeys).toList(),
                    SYSTEM_DEFAULT_THEME
                ) {
                    title(R.string.select_skin)
                    dialogTitle(R.string.select_skin)
                    summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                }
                singleSelect(
                    PREF_APP_FONT,
                    resources.getStringArray(R.array.fontNames).toList(),
                    resources.getStringArray(R.array.fontKeys).toList(),
                    SYSTEM_DEFAULT_FONT
                ) {
                    title(R.string.fonts)
                    dialogTitle(R.string.fonts)
                    summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                }
                switch(PREF_EASTERN_GREGORIAN_ARABIC_MONTHS, false) {
                    if (language == LANG_AR) {
                        title = "السنة الميلادية بالاسماء الشرقية"
                        summary = "كانون الثاني، شباط، آذار، …"
                    } else isVisible = false
                }
                switch(PREF_PERSIAN_DIGITS, true) {
                    title(R.string.persian_digits)
                    summary(R.string.enable_persian_digits)
                    when (language) {
                        LANG_EN_US, LANG_JA, LANG_FR, LANG_ES -> isVisible = false
                    }
                }
            }
            section(R.string.calendar) {
                // Mark the rest of options as advanced
                initialExpandedChildrenCount = 6
                clickable(onClick = { showHolidaysTypesDialog() }) {
                    title(R.string.events)
                    summary(R.string.events_summary)
                }
                switch(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false) {
                    title(R.string.show_device_calendar_events)
                    summary(R.string.show_device_calendar_events_summary)
                    setOnPreferenceChangeListener { _, _ ->
                        val activity = activity ?: return@setOnPreferenceChangeListener false
                        isChecked = if (ActivityCompat.checkSelfPermission(
                                activity, Manifest.permission.READ_CALENDAR
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            askForCalendarPermission(activity)
                            false
                        } else {
                            !isChecked
                        }
                        false
                    }
                }
                clickable(onClick = { showCalendarPreferenceDialog() }) {
                    title(R.string.calendars_priority)
                    summary(R.string.calendars_priority_summary)
                }
                switch(PREF_ASTRONOMICAL_FEATURES, false) {
                    title(R.string.astronomical_info)
                    summary(R.string.astronomical_info_summary)
                }
                switch(PREF_SHOW_WEEK_OF_YEAR_NUMBER, false) {
                    title(R.string.week_of_year)
                    summary(R.string.week_of_year_summary)
                }
                switch(PREF_WIDGET_IN_24, true) {
                    title(R.string.clock_in_24)
                    summary(R.string.showing_clock_in_24)
                }
                singleSelect(
                    PREF_ISLAMIC_OFFSET,
                    // One is formatted with locale's numerals and the other used for keys isn't
                    (-2..2).map { formatNumber(it.toString()) }, (-2..2).map { it.toString() },
                    DEFAULT_ISLAMIC_OFFSET
                ) {
                    title(R.string.islamic_offset)
                    summary(R.string.islamic_offset_summary)
                    dialogTitle(R.string.islamic_offset)
                }
                val weekDays = AppLocalesData.getWeekDays(language)
                val weekDaysValues = (0..6).map { it.toString() }
                singleSelect(PREF_WEEK_START, weekDays, weekDaysValues, DEFAULT_WEEK_START) {
                    title(R.string.week_start)
                    dialogTitle(R.string.week_start_summary)
                    summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                }
                multiSelect(PREF_WEEK_ENDS, weekDays, weekDaysValues, DEFAULT_WEEK_ENDS) {
                    title(R.string.week_ends)
                    summary(R.string.week_ends_summary)
                    dialogTitle(R.string.week_ends_summary)
                }
            }
        }
    }
}
