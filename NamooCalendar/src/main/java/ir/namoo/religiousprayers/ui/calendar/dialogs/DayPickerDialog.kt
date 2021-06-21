package ir.namoo.religiousprayers.ui.calendar.dialogs

import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.ui.shared.DayPickerView
import ir.namoo.religiousprayers.utils.Jdn
import ir.namoo.religiousprayers.utils.appPrefs
import ir.namoo.religiousprayers.utils.getJdnOrNull
import ir.namoo.religiousprayers.utils.putJdn

// Only one use but to match with showColorPickerDialog
fun Fragment.showDayPickerDialog(key: String) {
    val todayJdn = Jdn.today
    val jdn = activity?.appPrefs?.getJdnOrNull(key) ?: todayJdn
    showDayPickerDialog(jdn) { result -> activity?.appPrefs?.edit { putJdn(key, result) } }
}

fun Fragment.showDayPickerDialog(jdn: Jdn, onSuccess: (jdn: Jdn) -> Unit) {
    val activity = activity ?: return
    val dayPickerView = DayPickerView(activity).also { it.jdn = jdn }
    AlertDialog.Builder(activity)
        .setView(dayPickerView)
        .setCustomTitle(null)
        .setPositiveButton(R.string.go) { _, _ -> dayPickerView.jdn?.also(onSuccess) }
        .show()
}
