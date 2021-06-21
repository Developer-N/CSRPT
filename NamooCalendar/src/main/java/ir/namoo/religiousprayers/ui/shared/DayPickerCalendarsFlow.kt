package ir.namoo.religiousprayers.ui.shared

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.constraintlayout.helper.widget.Flow
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.SingleChipLayoutBinding
import ir.namoo.religiousprayers.entities.CalendarTypeItem
import ir.namoo.religiousprayers.utils.CalendarType
import ir.namoo.religiousprayers.utils.addViewsToFlow
import ir.namoo.religiousprayers.utils.layoutInflater

class DayPickerCalendarsFlow(context: Context, attrs: AttributeSet?) : Flow(context, attrs) {
    fun setup(calendarTypes: List<CalendarTypeItem>, onItemClick: (CalendarType) -> Unit) {
        val chips = calendarTypes.map { calendarTypeItem ->
            SingleChipLayoutBinding.inflate(context.layoutInflater).also {
                it.chip.text = calendarTypeItem.title
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                        it.chip.elevation = resources.getDimension(R.dimen.chip_elevation)
                    }
                }
            }.root
        }
        addViewsToFlow(chips.mapIndexed { i, chip ->
            chip.setOnClickListener {
                onItemClick(calendarTypes[i].type)
                chips.forEachIndexed { j, chipView ->
                    chipView.isClickable = i != j
                    chipView.isSelected = i == j
                }
            }
            chip.isClickable = i != 0
            chip.isSelected = i == 0
            chip.isCheckable = false
            chip
        })
    }
}
