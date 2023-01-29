package ir.namoo.commons.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.navigation.NavigationView
import ir.namoo.commons.PREF_APP_FONT
import ir.namoo.commons.SYSTEM_DEFAULT_FONT

fun overrideFont(defaultFontNameToOverride: String, face: Typeface): Unit = runCatching {
    val defaultFontTypefaceField =
        Typeface::class.java.getDeclaredField(defaultFontNameToOverride)
    defaultFontTypefaceField.isAccessible = true
    defaultFontTypefaceField.set(null, face)
}.getOrElse(logException)

fun getAppFont(context: Context): Typeface = runCatching {
    Typeface.createFromAsset(
        context.assets,
        context.appPrefs.getString(PREF_APP_FONT, SYSTEM_DEFAULT_FONT)
    )
}.onFailure(logException).getOrDefault(Typeface.create(Typeface.SERIF, Typeface.NORMAL))

fun changeNavigationItemTypeface(navigation: NavigationView) {
    val typeface = getAppFont(navigation.context)
    for (i in 0 until navigation.menu.size()) {
        val mi = navigation.menu.getItem(i)
        val s = SpannableString(mi.title)
        s.setSpan(
            CustomTypefaceSpan("", typeface), 0, s.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        mi.title = s
    }
}
