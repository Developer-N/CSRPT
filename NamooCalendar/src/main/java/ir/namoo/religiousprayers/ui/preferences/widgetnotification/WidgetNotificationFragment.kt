package ir.namoo.religiousprayers.ui.preferences.widgetnotification


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.preference.PreferenceFragmentCompat
import ir.namoo.religiousprayers.PREF_CENTER_ALIGN_WIDGETS
import ir.namoo.religiousprayers.PREF_IRAN_TIME
import ir.namoo.religiousprayers.PREF_NOTIFY_DATE
import ir.namoo.religiousprayers.PREF_NOTIFY_DATE_LOCK_SCREEN
import ir.namoo.religiousprayers.PREF_NUMERICAL_DATE_PREFERRED
import ir.namoo.religiousprayers.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import ir.namoo.religiousprayers.PREF_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR
import ir.namoo.religiousprayers.PREF_SELECTED_WIDGET_TEXT_COLOR
import ir.namoo.religiousprayers.PREF_WHAT_TO_SHOW_WIDGETS
import ir.namoo.religiousprayers.PREF_WIDGET_CLOCK
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.ui.preferences.build
import ir.namoo.religiousprayers.ui.preferences.clickable
import ir.namoo.religiousprayers.ui.preferences.dialogTitle
import ir.namoo.religiousprayers.ui.preferences.multiSelect
import ir.namoo.religiousprayers.ui.preferences.section
import ir.namoo.religiousprayers.ui.preferences.shared.showColorPickerDialog
import ir.namoo.religiousprayers.ui.preferences.summary
import ir.namoo.religiousprayers.ui.preferences.switch
import ir.namoo.religiousprayers.ui.preferences.title

// Consider that it is used both in MainActivity and WidgetConfigurationActivity
class WidgetNotificationFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val handler = Handler(Looper.getMainLooper())
        preferenceScreen = preferenceManager.createPreferenceScreen(context).build {
            section(R.string.pref_notification) {
                // Hide notification category if we are in widgets configuration
                if (arguments?.getBoolean(IS_WIDGETS_CONFIGURATION, false) == true)
                    isVisible = false
                switch(PREF_NOTIFY_DATE, true) {
                    title(R.string.notify_date)
                    summary(R.string.enable_notify)
                }
                switch(PREF_NOTIFY_DATE_LOCK_SCREEN, true) {
                    title(R.string.notify_date_lock_screen)
                    summary(R.string.notify_date_lock_screen_summary)
                    handler.post { dependency = PREF_NOTIFY_DATE } // deferred dependency wire up
                }
            }
            section(R.string.pref_widget) {
                // Mark the rest of options as advanced
                initialExpandedChildrenCount = 5
                clickable(onClick = {
                    showColorPickerDialog(false, PREF_SELECTED_WIDGET_TEXT_COLOR)
                }) {
                    title(R.string.widget_text_color)
                    summary(R.string.select_widgets_text_color)
                }
                clickable(onClick = {
                    showColorPickerDialog(false, PREF_SELECTED_WIDGET_NEXT_ATHAN_TEXT_COLOR)
                }) {
                    title(R.string.widget_next_athan_text_color)
                    summary(R.string.select_widgets_next_athan_text_color)
                }
                clickable(onClick = {
                    showColorPickerDialog(true, PREF_SELECTED_WIDGET_BACKGROUND_COLOR)
                }) {
                    title(R.string.widget_background_color)
                    summary(R.string.select_widgets_background_color)
                }
                switch(PREF_NUMERICAL_DATE_PREFERRED, false) {
                    title(R.string.prefer_linear_date)
                    summary(R.string.prefer_linear_date_summary)
                }
                switch(PREF_WIDGET_CLOCK, true) {
                    title(R.string.clock_on_widget)
                    summary(R.string.showing_clock_on_widget)
                }
                switch(PREF_CENTER_ALIGN_WIDGETS, false) {
                    title(R.string.center_align_widgets)
                    summary(R.string.center_align_widgets_summary)
                }
                switch(PREF_IRAN_TIME, false) {
                    title(R.string.iran_time)
                    summary(R.string.showing_iran_time)
                }
                multiSelect(
                    PREF_WHAT_TO_SHOW_WIDGETS,
                    resources.getStringArray(R.array.what_to_show).toList(),
                    resources.getStringArray(R.array.what_to_show_keys).toList(),
                    resources.getStringArray(R.array.what_to_show_default).toSet()
                ) {
                    title(R.string.customize_widget)
                    summary(R.string.customize_widget_summary)
                    dialogTitle(R.string.which_one_to_show)
                }
            }
        }
    }

    companion object {
        const val IS_WIDGETS_CONFIGURATION = "IS_WIDGETS_CONFIGURATION"
    }
}
