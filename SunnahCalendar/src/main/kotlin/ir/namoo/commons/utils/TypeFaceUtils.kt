package ir.namoo.commons.utils

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.edit
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import ir.namoo.commons.PREF_APP_FONT
import ir.namoo.commons.SYSTEM_DEFAULT_FONT

fun overrideFont(defaultFontNameToOverride: String, face: Typeface): Unit = runCatching {
    val defaultFontTypefaceField =
        Typeface::class.java.getDeclaredField(defaultFontNameToOverride)
    defaultFontTypefaceField.isAccessible = true
    defaultFontTypefaceField.set(null, face)
}.getOrElse(logException)

fun getAppFont(context: Context): Typeface = runCatching {
    if (context.preferences.getString(PREF_APP_FONT, SYSTEM_DEFAULT_FONT)
            ?.contains("Vazir.ttf") == true
    ) context.preferences.edit { putString(PREF_APP_FONT, "fonts/Vazirmatn.ttf") }
    Typeface.createFromAsset(
        context.assets,
        context.preferences.getString(PREF_APP_FONT, SYSTEM_DEFAULT_FONT)
    )
}.onFailure(logException).getOrDefault(Typeface.create(Typeface.SERIF, Typeface.NORMAL))

