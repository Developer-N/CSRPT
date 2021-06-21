package ir.namoo.religiousprayers.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import ir.namoo.religiousprayers.SYSTEM_DEFAULT_FONT
import ir.namoo.religiousprayers.PREF_APP_FONT

// https://gist.github.com/artem-zinnatullin/7749076
val isCustomFontEnabled: Boolean
    get() = isArabicDigitSelected() || isNonArabicScriptSelected()

/**
 * Using reflection to override default typeface
 * NOTICE: DO NOT FORGET TO SET TYPEFACE FOR APP THEME AS DEFAULT TYPEFACE WHICH WILL BE OVERRIDDEN
 */
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


fun getCalendarFragmentFont(context: Context): Typeface =
    if (isCustomFontEnabled) Typeface.create("sans-serif-light", Typeface.NORMAL)
    else getAppFont(context)

fun changeToolbarTypeface(toolbar: MaterialToolbar) {
    runCatching {
        val field = Toolbar::class.java.getDeclaredField("mTitleTextView")
        field.isAccessible = true
        (field.get(toolbar) as TextView).typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }.onFailure(logException)
    runCatching {
        val field2 = Toolbar::class.java.getDeclaredField("mSubtitleTextView")
        field2.isAccessible = true
        (field2.get(toolbar) as TextView?)?.typeface =
            Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    }.onFailure(logException)
}

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
