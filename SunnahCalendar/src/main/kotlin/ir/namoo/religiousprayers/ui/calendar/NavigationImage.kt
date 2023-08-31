package ir.namoo.religiousprayers.ui.calendar

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn

enum class NavigationImage(
    @StringRes val nameStringId: Int, @DrawableRes val imageID: Int, @ColorInt val color: Int
) {

    DEFAULT(R.string.default_image, R.drawable.drawer_background, 0xcc5580aa.toInt()),
    RAMADAN(R.string.ramadan, R.drawable.ramadhan, 0xcc5580aa.toInt()),
    EID(R.string.eid, R.drawable.eid, 0xccbf8015.toInt()),
    FRIDAY(R.string.friday, R.drawable.friday, 0xcc5580aa.toInt());

    companion object {
        fun fromDate(jdn: Jdn): NavigationImage {
            val c = jdn.toIslamicDate()
            return when {
                jdn.dayOfWeek == 6 -> FRIDAY
                c.month == 9 -> RAMADAN
                (c.month == 10 || c.month == 12) && c.dayOfMonth in 1..3 -> EID
                else -> DEFAULT
            }
        }
    }
}
