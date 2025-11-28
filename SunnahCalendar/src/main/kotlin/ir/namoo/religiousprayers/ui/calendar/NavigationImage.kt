package ir.namoo.religiousprayers.ui.calendar

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.WeekDay

enum class NavigationImage(
    @param:StringRes val nameStringId: Int,
    @param:DrawableRes val imageID: Int,
    @param:ColorInt val color: Int
) {

    DEFAULT(
        R.string.default_image,
        R.drawable.drawer_background,
        0xcc5580aa.toInt()
    ),
    RAMADAN(R.string.ramadan, R.drawable.ramadhan, 0xcc5580aa.toInt()), EID(
        R.string.eid,
        R.drawable.eid,
        0xccbf8015.toInt()
    ),
    FRIDAY(R.string.friday, R.drawable.friday, 0xcc5580aa.toInt());

    companion object {
        fun fromDate(jdn: Jdn): NavigationImage {
            val c = jdn.toIslamicDate()
            return when {
                (c.month == 10 && c.dayOfMonth in 1..3) || (c.month == 12 && c.dayOfMonth in 10..13) -> EID
                jdn.weekDay == WeekDay.FRIDAY -> FRIDAY
                c.month == 9 -> RAMADAN
                else -> DEFAULT
            }
        }
    }
}
