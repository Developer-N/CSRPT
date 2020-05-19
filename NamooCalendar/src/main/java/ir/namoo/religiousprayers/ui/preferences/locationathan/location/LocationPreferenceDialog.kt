package ir.namoo.religiousprayers.ui.preferences.locationathan.location

import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.namoo.religiousprayers.utils.getAllCities

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
class LocationPreferenceDialog : PreferenceDialogFragmentCompat() {

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
        super.onPrepareDialogBuilder(builder)
        if (builder == null) return

        val recyclerView = RecyclerView(builder.context).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = LocationAdapter(
                this@LocationPreferenceDialog,
                getAllCities(context, true)
            )
        }

        builder.setView(recyclerView)

        builder.setPositiveButton("", null)
        builder.setNegativeButton("", null)
    }

    override fun onDialogClosed(positiveResult: Boolean) {}

    fun selectItem(city: String) {
        (preference as LocationPreference).setSelected(city)
        dismiss()
    }
}
