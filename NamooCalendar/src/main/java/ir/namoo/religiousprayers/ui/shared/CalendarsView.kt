package ir.namoo.religiousprayers.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.praytimes.Clock
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.CalendarItemBinding
import ir.namoo.religiousprayers.databinding.CalendarsViewBinding
import ir.namoo.religiousprayers.utils.*
import java.util.*

class CalendarsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    private val changeBoundTransition = ChangeBounds()
    private val arrowRotationAnimationDuration =
        resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    private val calendarItemAdapter = CalendarItemAdapter(context)
    private val binding: CalendarsViewBinding =
        CalendarsViewBinding.inflate(context.layoutInflater, this, true).apply {
            root.setOnClickListener { expand(!calendarItemAdapter.isExpanded) }
            extraInformationContainer.visibility = View.GONE
            calendarsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context).apply {
                    orientation = RecyclerView.HORIZONTAL
                }
                adapter = calendarItemAdapter
            }
        }

    fun hideMoreIcon() {
        binding.moreCalendar.visibility = View.GONE
    }

    fun expand(expanded: Boolean) {
        calendarItemAdapter.isExpanded = expanded

        // Rotate expansion arrow
        binding.moreCalendar.animate()
            .rotation(if (expanded) 180f else 0f)
            .setDuration(arrowRotationAnimationDuration)
            .start()

        TransitionManager.beginDelayedTransition(binding.calendarsTabContent, changeBoundTransition)
        binding.extraInformationContainer.visibility = if (expanded) View.VISIBLE else View.GONE
    }

    fun showCalendars(
        jdn: Long, chosenCalendarType: CalendarType, calendarsToShow: List<CalendarType>
    ) {
        val context = context ?: return

        calendarItemAdapter.setDate(calendarsToShow, jdn)
        binding.weekDayName.text = getWeekDayName(CivilDate(jdn))

        binding.zodiac.apply {
            text = getZodiacInfo(context, jdn, withEmoji = true, short = false)
            visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
        }

        val isToday = getTodayJdn() == jdn

        if (isToday) {
            if (isForcedIranTimeEnabled) binding.weekDayName.text = "%s (%s)".format(
                getWeekDayName(CivilDate(jdn)),
                context.getString(R.string.iran_time)
            )
            binding.diffDate.visibility = View.GONE
        } else {
            binding.diffDate.visibility = View.VISIBLE
            binding.diffDate.text =
                calculateDaysDifference(jdn, context.getString(R.string.date_diff_text))
        }

        val mainDate = getDateFromJdnOfCalendar(chosenCalendarType, jdn)
        val startOfYear = getDateOfCalendar(
            chosenCalendarType,
            mainDate.year, 1, 1
        )
        val startOfNextYear = getDateOfCalendar(
            chosenCalendarType, mainDate.year + 1, 1, 1
        )
        val startOfYearJdn = startOfYear.toJdn()
        val endOfYearJdn = startOfNextYear.toJdn() - 1
        val currentWeek = calculateWeekOfYear(jdn, startOfYearJdn)
        val weeksCount = calculateWeekOfYear(endOfYearJdn, startOfYearJdn)

        val startOfYearText = context.getString(R.string.start_of_year_diff).format(
            formatNumber((jdn - startOfYearJdn + 1).toInt()),
            formatNumber(currentWeek),
            formatNumber(mainDate.month)
        )
        val endOfYearText = context.getString(R.string.end_of_year_diff).format(
            formatNumber((endOfYearJdn - jdn).toInt()),
            formatNumber(weeksCount - currentWeek),
            formatNumber(12 - mainDate.month)
        )
        binding.startAndEndOfYearDiff.text =
            listOf(startOfYearText, endOfYearText).joinToString("\n")

        var equinox = ""
        if (mainCalendar == chosenCalendarType && chosenCalendarType == CalendarType.SHAMSI) {
            if (mainDate.month == 12 && mainDate.dayOfMonth >= 20 || mainDate.month == 1 && mainDate.dayOfMonth == 1) {
                val addition = if (mainDate.month == 12) 1 else 0
                val springEquinox = getSpringEquinox(mainDate.toJdn())
                equinox = context.getString(R.string.spring_equinox).format(
                    formatNumber(mainDate.year + addition),
                    Clock(springEquinox[Calendar.HOUR_OF_DAY], springEquinox[Calendar.MINUTE])
                        .toFormattedString(forcedIn12 = true) + " " +
                            formatDate(
                                getDateFromJdnOfCalendar(
                                    mainCalendar,
                                    calendarToCivilDate(springEquinox).toJdn()
                                ),
                                forceNonNumerical = true
                            )
                )
            }
        }
        binding.equinox.apply {
            text = equinox
            visibility = if (equinox.isEmpty()) View.GONE else View.VISIBLE
        }

        binding.root.contentDescription = getA11yDaySummary(
            context, jdn, isToday, emptyEventsStore(),
            withZodiac = true, withOtherCalendars = true, withTitle = true
        )
    }

    class CalendarItemAdapter internal constructor(context: Context) :
        RecyclerView.Adapter<CalendarItemAdapter.ViewHolder>() {

        private val calendarFont = getCalendarFragmentFont(context)
        private var calendars = emptyList<CalendarType>()
        internal var isExpanded = false
            set(expanded) {
                field = expanded
                calendars.indices.forEach(::notifyItemChanged)
            }
        private var jdn = 0L

        internal fun setDate(calendars: List<CalendarType>, jdn: Long) {
            this.calendars = calendars
            this.jdn = jdn
            calendars.indices.forEach(::notifyItemChanged)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            CalendarItemBinding.inflate(parent.context.layoutInflater, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

        override fun getItemCount(): Int = calendars.size

        inner class ViewHolder(private val binding: CalendarItemBinding) :
            RecyclerView.ViewHolder(binding.root), OnClickListener {

            init {
                val applyLineMultiplier = !isCustomFontEnabled

                binding.monthYear.typeface = calendarFont
                binding.day.typeface = calendarFont
                if (applyLineMultiplier) binding.monthYear.setLineSpacing(0f, .8f)

                binding.container.setOnClickListener(this)
                binding.linear.setOnClickListener(this)
            }

            fun bind(position: Int) {
                val date = getDateFromJdnOfCalendar(calendars[position], jdn)

                binding.linear.text = toLinearDate(date)
                binding.linear.contentDescription = toLinearDate(date)
                val firstCalendarString = formatDate(date)
                binding.container.contentDescription = firstCalendarString
                binding.day.contentDescription = ""
                binding.day.text = formatNumber(date.dayOfMonth)
                binding.monthYear.contentDescription = ""
                binding.monthYear.text =
                    listOf(getMonthName(date), formatNumber(date.year)).joinToString("\n")
            }

            override fun onClick(view: View?) =
                copyToClipboard(view, "converted date", view?.contentDescription)
        }
    }
}