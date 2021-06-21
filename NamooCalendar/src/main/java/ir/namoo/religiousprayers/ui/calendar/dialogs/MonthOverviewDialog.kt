package ir.namoo.religiousprayers.ui.calendar.dialogs

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.MonthOverviewDialogBinding
import ir.namoo.religiousprayers.databinding.MonthOverviewItemBinding
import ir.namoo.religiousprayers.utils.Jdn
import ir.namoo.religiousprayers.utils.copyToClipboard
import ir.namoo.religiousprayers.utils.dayTitleSummary
import ir.namoo.religiousprayers.utils.dp
import ir.namoo.religiousprayers.utils.getEvents
import ir.namoo.religiousprayers.utils.getEventsTitle
import ir.namoo.religiousprayers.utils.getMonthLength
import ir.namoo.religiousprayers.utils.isHighTextContrastEnabled
import ir.namoo.religiousprayers.utils.layoutInflater
import ir.namoo.religiousprayers.utils.mainCalendar
import ir.namoo.religiousprayers.utils.readMonthDeviceEvents
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.persiancalendar.calendar.AbstractDate

fun Fragment.showMonthOverviewDialog(date: AbstractDate) {
    val activity = activity ?: return
    val baseJdn = Jdn(date)
    val deviceEvents = baseJdn.readMonthDeviceEvents(activity)
    val events = (0 until mainCalendar.getMonthLength(date.year, date.month)).mapNotNull {
        val jdn = baseJdn + it
        val events = jdn.getEvents(deviceEvents)
        val holidays = getEventsTitle(
            events, holiday = true, compact = false, showDeviceCalendarEvents = false,
            insertRLM = false, addIsHoliday = isHighTextContrastEnabled
        )
        val nonHolidays = getEventsTitle(
            events, holiday = false, compact = false, showDeviceCalendarEvents = true,
            insertRLM = false, addIsHoliday = false
        )
        if (holidays.isEmpty() && nonHolidays.isEmpty()) null
        else MonthOverviewRecord(
            dayTitleSummary(jdn, jdn.toCalendar(mainCalendar)), holidays, nonHolidays
        )
    }.takeIf { it.isNotEmpty() } ?: listOf(
        MonthOverviewRecord(getString(R.string.warn_if_events_not_set), "", "")
    )

    BottomSheetDialog(activity).also { dialog ->
        dialog.setContentView(
            MonthOverviewDialogBinding.inflate(
                activity.layoutInflater, null, false
            ).also { binding ->
                binding.recyclerView.also {
                    it.layoutManager = LinearLayoutManager(context)
                    it.adapter = MonthOverviewItemAdapter(events)
                    it.setPadding(0, 4.dp, 0, 0)
                }
            }.root
        )
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
    }.show()
}

private class MonthOverviewRecord(
    val title: String, val holidays: String, val nonHolidays: String
) {
    override fun toString() = listOf(title, holidays, nonHolidays)
        .filter { it.isNotEmpty() }.joinToString("\n")
}

private class MonthOverviewItemAdapter(private val rows: List<MonthOverviewRecord>) :
    RecyclerView.Adapter<MonthOverviewItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        MonthOverviewItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount(): Int = rows.size

    inner class ViewHolder(private val binding: MonthOverviewItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
        }

        fun bind(position: Int) = binding.let {
            val record = rows[position]
            it.title.text = record.title
            it.holidays.text = record.holidays
            it.holidays.isVisible = record.holidays.isNotEmpty()
            it.nonHolidays.text = record.nonHolidays
            it.nonHolidays.isVisible =
                record.nonHolidays.isNotEmpty()
        }

        override fun onClick(v: View?) = copyToClipboard(
            binding.root, "Events", rows[bindingAdapterPosition].toString(),
            showToastInstead = true
        )
    }
}
