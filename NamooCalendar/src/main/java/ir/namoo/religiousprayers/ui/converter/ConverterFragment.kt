package ir.namoo.religiousprayers.ui.converter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.FragmentConverterBinding
import ir.namoo.religiousprayers.utils.Jdn
import ir.namoo.religiousprayers.utils.getOrderedCalendarTypes
import ir.namoo.religiousprayers.utils.setupUpNavigation

class ConverterFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentConverterBinding.inflate(inflater, container, false).also { binding ->
        binding.appBar.toolbar.let {
            it.setupUpNavigation()
            it.setTitle(R.string.date_converter)
        }

        binding.calendarsView.toggle()
        binding.calendarsView.hideMoreIcon()

        val todayJdn = Jdn.today

        binding.todayButton.setOnClickListener { binding.dayPickerView.jdn = todayJdn }

        binding.dayPickerView.also {
            it.selectedDayListener = fun(jdn) {
                if (jdn == null) {
                    binding.resultCard.isVisible = false
                } else {
                    if (jdn == todayJdn) binding.todayButton.hide() else binding.todayButton.show()

                    binding.resultCard.isVisible = true
                    val selectedCalendarType = binding.dayPickerView.selectedCalendarType
                    binding.calendarsView.showCalendars(
                        jdn, selectedCalendarType, getOrderedCalendarTypes() - selectedCalendarType
                    )
                }
            }
            it.jdn = Jdn.today
            it.anchorView = binding.todayButton
        }
    }.root
}
