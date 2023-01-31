package com.byagowi.persiancalendar.ui.settings.interfacecalendar.calendarsorder

import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.PREF_MAIN_CALENDAR_KEY
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.utils.appPrefs
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showCalendarPreferenceDialog(activity: FragmentActivity, onEmpty: () -> Unit) {
    val enabledCalendarTypes = enabledCalendars
    val orderedCalendarTypes =
        enabledCalendars + (enumValues<CalendarType>().toList() - enabledCalendars.toSet()) -
                // Don't show Nepali on default locales, at least for now.
                if (language.showNepaliCalendar) emptySet() else setOf(CalendarType.NEPALI)
    val adapter = RecyclerListAdapter(orderedCalendarTypes.map { calendarType ->
        RecyclerListAdapter.Item(
            activity.getString(calendarType.title), calendarType.name,
            calendarType in enabledCalendarTypes
        )
    })

    MaterialAlertDialogBuilder(activity)
        .setView(RecyclerView(activity).also {
            it.setHasFixedSize(true)
            it.layoutManager = LinearLayoutManager(activity)
            it.adapter = adapter
        })
        .setTitle(R.string.calendars_priority)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.accept) accept@{ _, _ ->
            val ordering = adapter.result.takeIf { it.isNotEmpty() } ?: return@accept onEmpty()
            activity.appPrefs.edit {
                putString(PREF_MAIN_CALENDAR_KEY, ordering.first())
                putString(PREF_OTHER_CALENDARS_KEY, ordering.drop(1).joinToString(","))
            }
        }
        .show()
}
