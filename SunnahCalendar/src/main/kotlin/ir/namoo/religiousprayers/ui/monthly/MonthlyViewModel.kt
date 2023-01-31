package ir.namoo.religiousprayers.ui.monthly

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.persianMonths
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import kotlinx.coroutines.launch

class MonthlyViewModel : ViewModel() {

    var times by mutableStateOf(mutableListOf<PrayTimes>())
        private set
    var selectedMonthName by mutableStateOf(persianMonths[0])
        private set
    var isLoading by mutableStateOf(false)
        private set

    fun loadTimeFor(context: Context, monthName: String) {
        viewModelScope.launch {
            isLoading = true
            times.clear()
            selectedMonthName = monthName
            val monthNumber = persianMonths.indexOf(monthName) + 1
            val monthDays = if (monthNumber in 1..6) 31 else 30
            val civilDate = Jdn.today().toGregorianCalendar()
            for (day in 1..monthDays) {
                val persianDate = PersianDate(PersianDate(civilDate.toJdn()).year, monthNumber, day)
                val date = CivilDate(persianDate.toJdn())
                var prayTimes: PrayTimes? =
                    coordinates?.calculatePrayTimes(Jdn(date).toJavaCalendar())
                prayTimes = PrayTimeProvider(context).nReplace(prayTimes, Jdn(date))
                prayTimes?.let { times.add(it) }
            }
            isLoading = false
        }
    }
}
