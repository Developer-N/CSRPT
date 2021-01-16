package ir.namoo.religiousprayers.ui.preferences.interfacecalendar.calendarsorder

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.edit
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.namoo.religiousprayers.PREF_MAIN_CALENDAR_KEY
import ir.namoo.religiousprayers.PREF_OTHER_CALENDARS_KEY
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.ui.MainActivity
import ir.namoo.religiousprayers.utils.appPrefs
import ir.namoo.religiousprayers.utils.getEnabledCalendarTypes
import ir.namoo.religiousprayers.utils.getOrderedCalendarEntities
import ir.namoo.religiousprayers.utils.updateStoredPreference

class CalendarPreferenceDialog : AppCompatDialogFragment() {

    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity as MainActivity

        updateStoredPreference(activity)
        val enabledCalendarTypes = getEnabledCalendarTypes()
        val adapter = RecyclerListAdapter(this, getOrderedCalendarEntities(activity).map {
            RecyclerListAdapter.Item(
                it.toString(),
                it.type.toString(),
                it.type in enabledCalendarTypes
            )
        })
        val recyclerView = RecyclerView(activity).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            this.adapter = adapter
        }

        val callback = SimpleItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback).apply {
            attachToRecyclerView(recyclerView)
        }

        return AlertDialog.Builder(activity).apply {
            setView(recyclerView)
            setTitle(R.string.calendars_priority)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.accept) { _, _ ->
                val ordering = adapter.result
                activity.appPrefs.edit {
                    if (ordering.isNotEmpty()) {
                        putString(PREF_MAIN_CALENDAR_KEY, ordering[0])
                        putString(
                            PREF_OTHER_CALENDARS_KEY,
                            ordering.subList(1, ordering.size).joinToString(",")
                        )
                    }
                }
            }
        }.create()
    }

    fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
    }
}
