package ir.namoo.religiousprayers.ui.preferences.agewidget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ir.namoo.religiousprayers.PREF_SELECTED_DATE_AGE_WIDGET
import ir.namoo.religiousprayers.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import ir.namoo.religiousprayers.PREF_SELECTED_WIDGET_TEXT_COLOR
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.ui.calendar.dialogs.showDayPickerDialog
import ir.namoo.religiousprayers.ui.preferences.shared.showColorPickerDialog
import ir.namoo.religiousprayers.utils.setOnClickListener

class WidgetAgeConfigureFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = context ?: return
        val appWidgetId = arguments
            ?.takeIf { it.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID) }
            ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0) ?: return

        val screen = preferenceManager.createPreferenceScreen(context)
        listOf(
            Preference(context).also {
                it.setTitle(R.string.select_date)
                it.setOnClickListener { showDayPickerDialog(PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId) }
            },
            Preference(context).also {
                it.setTitle(R.string.widget_text_color)
                it.setSummary(R.string.select_widgets_text_color)
                it.setOnClickListener {
                    showColorPickerDialog(false, PREF_SELECTED_WIDGET_TEXT_COLOR + appWidgetId)
                }
            },
            Preference(context).also {
                it.setTitle(R.string.widget_background_color)
                it.setSummary(R.string.select_widgets_background_color)
                it.setOnClickListener {
                    showColorPickerDialog(true, PREF_SELECTED_WIDGET_BACKGROUND_COLOR + appWidgetId)
                }
            }
        ).onEach { it.isIconSpaceReserved = false }.forEach(screen::addPreference)
        preferenceScreen = screen
    }
}
