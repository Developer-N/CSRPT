package com.byagowi.persiancalendar.ui.theme

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R

enum class Theme(
    val key: String,
    @get:StringRes val title: Int,
    val hasGradient: Boolean = true,
    private val lackDynamicColors: Boolean = false,
    // This is null in system default, if that's needed, use effectiveTheme()
    val isDark: Boolean? = false,
) {
    SYSTEM_DEFAULT("SystemDefault", R.string.theme_default, isDark = null),
    LIGHT("LightTheme", R.string.theme_light),
    CYAN("CYAN", R.string.theme_cyan, lackDynamicColors = true),
    DARK_GREEN("DarkGreenTheme", R.string.theme_dark_green, lackDynamicColors = true),
    PURPLE("PurpleTheme", R.string.theme_purple, lackDynamicColors = true),
    DEEP_PURPLE("DeepPurpleTheme", R.string.theme_deep_purple, lackDynamicColors = true),
    INDIGO("IndigoTheme", R.string.theme_indigo, lackDynamicColors = true),
    PINK("PinkTheme", R.string.theme_pink, lackDynamicColors = true),
    GREEN("GreenTheme", R.string.theme_green, lackDynamicColors = true),
    BROWN("BrownTheme", R.string.theme_brown, lackDynamicColors = true),
    NEW_BLUE("NewBlueTheme", R.string.theme_nblue, lackDynamicColors = true),
    AQUA("BlueTheme"/*legacy*/, R.string.theme_aqua, lackDynamicColors = true),
    DARK("DarkTheme", R.string.theme_dark, isDark = true),
    BLACK("BlackTheme", R.string.theme_black, hasGradient = false, isDark = true),
    MODERN("ClassicTheme",/*legacy*/R.string.theme_modern);

    val isDynamicColors
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S) get() =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !lackDynamicColors
}
