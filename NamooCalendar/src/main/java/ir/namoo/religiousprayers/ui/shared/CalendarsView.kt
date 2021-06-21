package ir.namoo.religiousprayers.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.CalendarsViewBinding
import ir.namoo.religiousprayers.utils.CalendarType
import ir.namoo.religiousprayers.utils.Jdn
import ir.namoo.religiousprayers.utils.calculateDaysDifference
import ir.namoo.religiousprayers.utils.dayOfWeekName
import ir.namoo.religiousprayers.utils.emptyEventsStore
import ir.namoo.religiousprayers.utils.formatDate
import ir.namoo.religiousprayers.utils.formatNumber
import ir.namoo.religiousprayers.utils.getA11yDaySummary
import ir.namoo.religiousprayers.utils.getSpringEquinox
import ir.namoo.religiousprayers.utils.getWeekOfYear
import ir.namoo.religiousprayers.utils.getZodiacInfo
import ir.namoo.religiousprayers.utils.isForcedIranTimeEnabled
import ir.namoo.religiousprayers.utils.layoutInflater
import ir.namoo.religiousprayers.utils.mainCalendar
import ir.namoo.religiousprayers.utils.toCivilDate
import ir.namoo.religiousprayers.utils.toFormattedString
import io.github.persiancalendar.praytimes.Clock
import java.util.*

class CalendarsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    private val changeBoundTransition = ChangeBounds()
    private val arrowRotationAnimationDuration =
        resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    private val binding = CalendarsViewBinding.inflate(context.layoutInflater, this, true).also {
        it.root.setOnClickListener { toggle() }
        it.extraInformationContainer.isVisible = false
    }
    private var isExpanded = false

    fun toggle() {
        isExpanded = !isExpanded

        binding.moreCalendar.contentDescription = context.getString(
            if (isExpanded) R.string.close else R.string.open
        )

        // Rotate expansion arrow
        binding.moreCalendar.animate()
            .rotation(if (isExpanded) 180f else 0f)
            .setDuration(arrowRotationAnimationDuration)
            .start()

        TransitionManager.beginDelayedTransition(binding.calendarsTabContent, changeBoundTransition)
        binding.extraInformationContainer.isVisible = isExpanded
    }

    fun hideMoreIcon() {
        binding.moreCalendar.isVisible = false
    }

    fun showCalendars(
        jdn: Jdn, chosenCalendarType: CalendarType, calendarsToShow: List<CalendarType>
    ) {
        val context = context ?: return

        binding.calendarsFlow.update(calendarsToShow, jdn)
        binding.weekDayName.text = jdn.dayOfWeekName

        binding.zodiac.also {
            it.text = getZodiacInfo(context, jdn, withEmoji = true, short = false)
            it.isVisible = it.text.isNotEmpty()
        }

        val isToday = Jdn.today == jdn
        if (isToday) {
            if (isForcedIranTimeEnabled) binding.weekDayName.text = "%s (%s)".format(
                jdn.dayOfWeekName, context.getString(R.string.iran_time)
            )
            binding.diffDate.isVisible = false
        } else {
            binding.also {
                it.diffDate.isVisible = true
                it.diffDate.text =
                    calculateDaysDifference(jdn, context.getString(R.string.date_diff_text))
            }
        }

        val mainDate = jdn.toCalendar(chosenCalendarType)
        val startOfYearJdn = Jdn(chosenCalendarType, mainDate.year, 1, 1)
        val endOfYearJdn = Jdn(chosenCalendarType, mainDate.year + 1, 1, 1) - 1
        val currentWeek = jdn.getWeekOfYear(startOfYearJdn)
        val weeksCount = endOfYearJdn.getWeekOfYear(startOfYearJdn)

        val startOfYearText = context.getString(R.string.start_of_year_diff).format(
            formatNumber(jdn - startOfYearJdn + 1),
            formatNumber(currentWeek),
            formatNumber(mainDate.month)
        )
        val endOfYearText = context.getString(R.string.end_of_year_diff).format(
            formatNumber(endOfYearJdn - jdn),
            formatNumber(weeksCount - currentWeek),
            formatNumber(12 - mainDate.month)
        )
        binding.startAndEndOfYearDiff.text =
            listOf(startOfYearText, endOfYearText).joinToString("\n")

        var equinox = ""
        if (mainCalendar == chosenCalendarType && chosenCalendarType == CalendarType.SHAMSI) {
            if (mainDate.month == 12 && mainDate.dayOfMonth >= 20 || mainDate.month == 1 && mainDate.dayOfMonth == 1) {
                val addition = if (mainDate.month == 12) 1 else 0
                val springEquinox = jdn.toGregorianCalendar().getSpringEquinox()
                equinox = context.getString(R.string.spring_equinox).format(
                    formatNumber(mainDate.year + addition),
                    Clock(springEquinox[Calendar.HOUR_OF_DAY], springEquinox[Calendar.MINUTE])
                        .toFormattedString(forcedIn12 = true) + " " +
                            formatDate(
                                Jdn(springEquinox.toCivilDate()).toCalendar(mainCalendar),
                                forceNonNumerical = true
                            )
                )
            }
        }
        binding.equinox.also {
            it.text = equinox
            it.isVisible = equinox.isNotEmpty()
        }

        binding.root.contentDescription = getA11yDaySummary(
            context, jdn, isToday, emptyEventsStore(),
            withZodiac = true, withOtherCalendars = true, withTitle = true
        )
    }
}
