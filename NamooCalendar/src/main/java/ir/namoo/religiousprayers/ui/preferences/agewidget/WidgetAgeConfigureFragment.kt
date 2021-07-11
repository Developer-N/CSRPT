package ir.namoo.religiousprayers.ui.preferences.agewidget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.PreferenceFragmentCompat
import ir.namoo.religiousprayers.PREF_SELECTED_DATE_AGE_WIDGET
import ir.namoo.religiousprayers.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import ir.namoo.religiousprayers.PREF_SELECTED_WIDGET_TEXT_COLOR
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.ui.calendar.dialogs.showDayPickerDialog
import ir.namoo.religiousprayers.ui.preferences.build
import ir.namoo.religiousprayers.ui.preferences.clickable
import ir.namoo.religiousprayers.ui.preferences.section
import ir.namoo.religiousprayers.ui.preferences.shared.showColorPickerDialog
import ir.namoo.religiousprayers.ui.preferences.summary
import ir.namoo.religiousprayers.ui.preferences.title
import ir.namoo.religiousprayers.utils.Jdn
import ir.namoo.religiousprayers.utils.appPrefs
import ir.namoo.religiousprayers.utils.getJdnOrNull
import ir.namoo.religiousprayers.utils.putJdn

class WidgetAgeConfigureFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val appWidgetId = arguments
            ?.takeIf { it.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID) }
            ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0) ?: return

        preferenceScreen = preferenceManager.createPreferenceScreen(context).build {
            section(R.string.empty) {
                clickable(onClick = {
                    val key = PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId
                    val jdn = activity?.appPrefs?.getJdnOrNull(key) ?: Jdn.today
                    showDayPickerDialog(jdn, R.string.accept) { result ->
                        activity?.appPrefs?.edit { putJdn(key, result) }
                    }
                }) {
                    title(R.string.select_date)
                }
                clickable(onClick = {
                    showColorPickerDialog(false, PREF_SELECTED_WIDGET_TEXT_COLOR + appWidgetId)
                }) {
                    title(R.string.widget_text_color)
                    summary(R.string.select_widgets_text_color)
                }
                clickable(onClick = {
                    showColorPickerDialog(true, PREF_SELECTED_WIDGET_BACKGROUND_COLOR + appWidgetId)
                }) {
                    title(R.string.widget_background_color)
                    summary(R.string.select_widgets_background_color)
                }
            }
        }
    }
}
