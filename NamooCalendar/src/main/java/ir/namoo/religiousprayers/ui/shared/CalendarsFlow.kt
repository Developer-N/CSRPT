package ir.namoo.religiousprayers.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.helper.widget.Flow
import ir.namoo.religiousprayers.databinding.CalendarItemBinding
import ir.namoo.religiousprayers.utils.CalendarType
import ir.namoo.religiousprayers.utils.Jdn
import ir.namoo.religiousprayers.utils.addViewsToFlow
import ir.namoo.religiousprayers.utils.copyToClipboard
import ir.namoo.religiousprayers.utils.formatDate
import ir.namoo.religiousprayers.utils.formatNumber
import ir.namoo.religiousprayers.utils.getCalendarFragmentFont
import ir.namoo.religiousprayers.utils.isCustomFontEnabled
import ir.namoo.religiousprayers.utils.layoutInflater
import ir.namoo.religiousprayers.utils.monthName
import ir.namoo.religiousprayers.utils.toLinearDate

class CalendarsFlow(context: Context, attrs: AttributeSet?) : Flow(context, attrs),
    View.OnClickListener {

    private val calendarFont = getCalendarFragmentFont(context)
    private var bindings = emptyList<CalendarItemBinding>()

    fun update(calendarsToShow: List<CalendarType>, jdn: Jdn) {
        // It implicitly expects the number of calendarsToShow items to not be changed during
        // the view lifecycle
        if (bindings.isEmpty()) {
            bindings = calendarsToShow.map { CalendarItemBinding.inflate(context.layoutInflater) }
            val applyLineMultiplier = !isCustomFontEnabled
            addViewsToFlow(bindings.map {
                it.monthYear.typeface = calendarFont
                it.day.typeface = calendarFont
                if (applyLineMultiplier) it.monthYear.setLineSpacing(0f, .8f)
                it.container.setOnClickListener(this)
                it.linear.setOnClickListener(this)
                it.root
            })
        }
        bindings.zip(calendarsToShow) { binding, calendarType ->
            val date = jdn.toCalendar(calendarType)
            val firstCalendarString = formatDate(date)
            binding.linear.text = toLinearDate(date)
            binding.linear.contentDescription = toLinearDate(date)
            binding.container.contentDescription = firstCalendarString
            binding.day.contentDescription = ""
            binding.day.text = formatNumber(date.dayOfMonth)
            binding.monthYear.contentDescription = ""
            binding.monthYear.text =
                listOf(date.monthName, formatNumber(date.year)).joinToString("\n")
        }
    }

    override fun onClick(view: View?) =
        copyToClipboard(view, "converted date", view?.contentDescription)
}
