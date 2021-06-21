package ir.namoo.religiousprayers.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.NumberPicker
import com.google.android.material.snackbar.Snackbar
import ir.namoo.religiousprayers.LANG_AR
import ir.namoo.religiousprayers.LANG_EN_US
import ir.namoo.religiousprayers.LANG_JA
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.DayPickerViewBinding
import ir.namoo.religiousprayers.utils.CalendarType
import ir.namoo.religiousprayers.utils.Jdn
import ir.namoo.religiousprayers.utils.calendarType
import ir.namoo.religiousprayers.utils.formatNumber
import ir.namoo.religiousprayers.utils.getMonthLength
import ir.namoo.religiousprayers.utils.getOrderedCalendarEntities
import ir.namoo.religiousprayers.utils.language
import ir.namoo.religiousprayers.utils.layoutInflater
import ir.namoo.religiousprayers.utils.monthsNames

class DayPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    private var mJdn: Jdn? = null

    var selectedDayListener = fun(_: Jdn?) {}

    var selectedCalendarType: CalendarType = CalendarType.SHAMSI

    var anchorView: View? = null

    private val inflater = context.layoutInflater
    val binding = DayPickerViewBinding.inflate(inflater, this, true).also { binding ->
        val calendarTypes = getOrderedCalendarEntities(
            context, abbreviation = when (language) {
                LANG_EN_US, LANG_JA, LANG_AR -> true
                else -> false
            }
        ).also { selectedCalendarType = it[0].type }
        binding.calendarsFlow.setup(calendarTypes) {
            selectedCalendarType = it
            jdn = mJdn
            selectedDayListener(mJdn)
        }

        val onDaySelected = NumberPicker.OnValueChangeListener { _, _, _ ->
            mJdn = jdn
            selectedDayListener(mJdn)
        }
        binding.yearPicker.setOnValueChangedListener(onDaySelected)
        binding.monthPicker.setOnValueChangedListener(onDaySelected)
        binding.dayPicker.setOnValueChangedListener(onDaySelected)
    }

    var jdn: Jdn?
        get() {
            val year = binding.yearPicker.value
            val month = binding.monthPicker.value
            val day = binding.dayPicker.value
            return if (day > selectedCalendarType.getMonthLength(year, month)) {
                Snackbar.make(rootView, R.string.date_exception, Snackbar.LENGTH_SHORT)
                    .setAnchorView(anchorView)
                    .show()
                null
            } else Jdn(selectedCalendarType, year, month, day)
        }
        set(value) {
            val jdn = value ?: Jdn.today
            mJdn = jdn
            val date = jdn.toCalendar(selectedCalendarType)
            binding.yearPicker.also {
                it.minValue = date.year - 100
                it.maxValue = date.year + 100
                it.value = date.year
                it.setFormatter(::formatNumber)
                it.isVerticalScrollBarEnabled = false
            }
            binding.monthPicker.also {
                it.minValue = 1
                it.maxValue = 12
                it.value = date.month
                val months = date.calendarType.monthsNames
                it.setFormatter { x -> months[x - 1] + " / " + formatNumber(x) }
                it.isVerticalScrollBarEnabled = false
            }
            binding.dayPicker.also {
                it.minValue = 1
                it.maxValue = 31
                it.value = date.dayOfMonth
                it.setFormatter(::formatNumber)
                it.isVerticalScrollBarEnabled = false
            }
            selectedDayListener(value)
        }
}
