package ir.namoo.religiousprayers.ui.converter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.FragmentConverterBinding
import ir.namoo.religiousprayers.ui.MainActivity
import ir.namoo.religiousprayers.utils.getOrderedCalendarTypes
import ir.namoo.religiousprayers.utils.getTodayJdn

class ConverterFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentConverterBinding.inflate(inflater, container, false).apply {
        (activity as? MainActivity)?.setTitleAndSubtitle(
            getString(R.string.date_converter), ""
        )

        calendarsView.expand(true)
        calendarsView.hideMoreIcon()

        val todayJdn = getTodayJdn()

        todayButton.setOnClickListener { dayPickerView.setDayJdnOnView(todayJdn) }

        dayPickerView.selectedDayListener = fun(jdn) {
            if (jdn == -1L) {
                calendarsView.visibility = View.GONE
            } else {
                if (jdn == todayJdn) todayButton.hide() else todayButton.show()

                calendarsView.visibility = View.VISIBLE
                val selectedCalendarType = dayPickerView.selectedCalendarType
                calendarsView.showCalendars(
                    jdn, selectedCalendarType,
                    getOrderedCalendarTypes() - selectedCalendarType
                )
            }
        }
        dayPickerView.setDayJdnOnView(getTodayJdn())
    }.root
}
