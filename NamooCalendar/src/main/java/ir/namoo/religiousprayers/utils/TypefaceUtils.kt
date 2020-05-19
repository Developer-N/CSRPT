package ir.namoo.religiousprayers.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import ir.namoo.religiousprayers.FONT_PATH
import ir.namoo.religiousprayers.PREF_APP_FONT

// https://gist.github.com/artem-zinnatullin/7749076
val isCustomFontEnabled: Boolean
    get() = isArabicDigitSelected() || isNonArabicScriptSelected()

/**
 * Using reflection to override default typeface
 * NOTICE: DO NOT FORGET TO SET TYPEFACE FOR APP THEME AS DEFAULT TYPEFACE WHICH WILL BE OVERRIDDEN
 */
fun overrideFont(defaultFontNameToOverride: String, face: Typeface) {
    try {
        val defaultFontTypefaceField =
            Typeface::class.java.getDeclaredField(defaultFontNameToOverride)
        defaultFontTypefaceField.isAccessible = true
        defaultFontTypefaceField.set(null, face)
    } catch (e: Exception) {
        Log.e(TAG, "Can not set custom font $face instead of $defaultFontNameToOverride", e)
    }
}

fun getAppFont(context: Context): Typeface =
    try {
        Typeface.createFromAsset(
            context.assets,
            context.appPrefs.getString(PREF_APP_FONT, FONT_PATH)
        )
    } catch (ex: Exception) {
        Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    }


fun getCalendarFragmentFont(context: Context): Typeface =
    if (isCustomFontEnabled) Typeface.create("sans-serif-light", Typeface.NORMAL)
    else getAppFont(context)

fun changeToolbarTypeface(toolbar: MaterialToolbar) {
    try {
        val field = Toolbar::class.java.getDeclaredField("mTitleTextView")
        val field2 = Toolbar::class.java.getDeclaredField("mSubtitleTextView")
        field.isAccessible = true
        field2.isAccessible = true
        (field.get(toolbar) as TextView).typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        (field2.get(toolbar) as TextView).typeface =
            Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    } catch (ex: Exception) {
        Log.e(TAG, "changeToolbarTypeface: $ex")
    }
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
