package com.byagowi.persiancalendar.ui.theme

import android.view.View
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.core.text.layoutDirection
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.isCyberpunk
import com.byagowi.persiancalendar.global.isGradient
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.theme
import com.byagowi.persiancalendar.ui.calendar.calendarpager.MonthColors
import com.byagowi.persiancalendar.ui.calendar.times.SunViewColors
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.variants.debugAssertNotNull

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = appColorScheme(), shapes = appShapes()) {
        val contentColor by animateColorAsState(
            MaterialTheme.colorScheme.onBackground,
            animationSpec = appColorAnimationSpec,
            label = "content color"
        )

        val language by language.collectAsState()
        val isRtl =
            language.isLessKnownRtl || language.asSystemLocale().layoutDirection == View.LAYOUT_DIRECTION_RTL
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
        ) {
            // Don't draw behind sides insets in landscape, we don't have any plan for using that space
            val sidesInsets =
                WindowInsets.systemBars.only(WindowInsetsSides.Start + WindowInsetsSides.End)
            Box(
                Modifier
                    .windowInsetsPadding(sidesInsets)
                    .clipToBounds()
                    // Don't move this upper to top of the chain so clip to bound can be applied to it
                    .background(appBackground()),
            ) { content() }
        }
    }
}

@Composable
private fun appColorScheme(): ColorScheme {
    val theme by theme.collectAsState()
    val context = LocalContext.current
    val systemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = theme.isDark || (theme == Theme.SYSTEM_DEFAULT && systemInDarkTheme)
    var colorScheme = if (theme.isDynamicColors()) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (darkTheme) DefaultDarkColorScheme else DefaultLightColorScheme
    // Handle black theme which is useful for OLED screens
    if (theme == Theme.BLACK) colorScheme = colorScheme.copy(surface = Color.Black)

    val resolvedTheme =
        if (theme != Theme.SYSTEM_DEFAULT) theme else if (isSystemInDarkTheme()) Theme.DARK else Theme.LIGHT
    val backgroundColor = if (theme.isDynamicColors()) when (resolvedTheme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_600))
        Theme.DARK -> Color(context.getColor(android.R.color.system_neutral1_800))
        Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_1000))
        Theme.MODERN -> colorScheme.surface
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (resolvedTheme) {
        Theme.DARK_GREEN -> Color(0xFF00695c)
        Theme.LIGHT -> Color(0xFF00ACC1)
        Theme.PURPLE -> Color(0xFF9C27B0)
        Theme.DEEP_PURPLE -> Color(0xFF673AB7)
        Theme.INDIGO -> Color(0xFF3F51B5)
        Theme.PINK -> Color(0xFFE91E63)
        Theme.GREEN -> Color(0xFF4CAF50)
        Theme.BROWN -> Color(0xFF795548)
        Theme.NEW_BLUE -> Color(0xFF2196F3)
        Theme.DARK -> Color(0xFF2F3133)
        Theme.BLACK -> Color.Black
        Theme.AQUA -> Color(0xFF1A237E)
        Theme.MODERN -> Color(0xFFFAFAFA)
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    val listOfOldThemes = listOf(
        Theme.DARK_GREEN,
        Theme.LIGHT,
        Theme.PURPLE,
        Theme.DEEP_PURPLE,
        Theme.INDIGO,
        Theme.PINK,
        Theme.GREEN,
        Theme.BROWN,
        Theme.NEW_BLUE
    )
    if (!theme.isDynamicColors() && listOfOldThemes.contains(theme))
        colorScheme = colorScheme.copy(
            primary = when (theme) {
                Theme.DARK_GREEN -> Color(0xFF00695c)
                Theme.LIGHT -> Color(0xFF00ACC1)
                Theme.PURPLE -> Color(0xFF9C27B0)
                Theme.DEEP_PURPLE -> Color(0xFF673AB7)
                Theme.INDIGO -> Color(0xFF3F51B5)
                Theme.PINK -> Color(0xFFE91E63)
                Theme.GREEN -> Color(0xFF4CAF50)
                Theme.BROWN -> Color(0xFF795548)
                Theme.NEW_BLUE -> Color(0xFF2196F3)
                else -> Color(0xFF00ACC1)
            }
        )
    return colorScheme.copy(
        background = backgroundColor,
        onBackground = if (backgroundColor.isLight) DefaultLightColorScheme.onBackground
        else DefaultDarkColorScheme.onBackground,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun appTopAppBarColors(): TopAppBarColors {
    return TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
        navigationIconContentColor = LocalContentColor.current,
        actionIconContentColor = LocalContentColor.current,
        titleContentColor = LocalContentColor.current,
    )
}

@Composable
private fun appShapes(): Shapes {
    if (!BuildConfig.DEVELOPMENT) return MaterialTheme.shapes
    val isCyberpunk by isCyberpunk.collectAsState()
    return if (isCyberpunk) Shapes(
        extraSmall = CutCornerShape(MaterialTheme.shapes.extraSmall.topStart),
        small = CutCornerShape(MaterialTheme.shapes.small.topStart),
        medium = CutCornerShape(MaterialTheme.shapes.medium.topStart),
        large = CutCornerShape(MaterialTheme.shapes.large.topStart),
        extraLarge = CutCornerShape(MaterialTheme.shapes.extraLarge.topStart),
    ) else MaterialTheme.shapes
}

val appColorAnimationSpec = spring<Color>(stiffness = Spring.StiffnessMediumLow)

/** This is similar to what [androidx.compose.animation.Crossfade] uses */
private val crossfadeSpec = fadeIn(tween()).togetherWith(fadeOut(tween()))

// Our own cross fade spec where AnimatedContent() has nicer effect
// than Crossfade() (usually on non binary changes) but we need a crossfade effect also
val appCrossfadeSpec: AnimatedContentTransitionScope<*>.() -> ContentTransform = { crossfadeSpec }

@Composable
private fun appBackground(): Brush {
    val backgroundColor = MaterialTheme.colorScheme.background
    val theme by theme.collectAsState()
    val context = LocalContext.current
    val resolvedTheme =
        if (theme != Theme.SYSTEM_DEFAULT) theme else if (isSystemInDarkTheme()) Theme.DARK else Theme.LIGHT
    val isGradient by isGradient.collectAsState()
    val backgroundGradientStart by animateColorAsState(
        if (!isGradient) backgroundColor
        else if (theme.isDynamicColors()) when (resolvedTheme) {
            Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_500))
            Theme.DARK -> Color(context.getColor(android.R.color.system_neutral1_700))
            Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_1000))
            Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_0))
            else -> null.debugAssertNotNull ?: Color.Transparent
        } else when (resolvedTheme) {
            Theme.DARK_GREEN -> Color(0xFF00796B)
            Theme.LIGHT -> Color(0xFF0097A7)
            Theme.PURPLE -> Color(0xFF7B1FA2)
            Theme.DEEP_PURPLE -> Color(0xFF512DA8)
            Theme.INDIGO -> Color(0xFF303F9F)
            Theme.PINK -> Color(0xFFC2185B)
            Theme.GREEN -> Color(0xFF388E3C)
            Theme.BROWN -> Color(0xFF5d4037)
            Theme.NEW_BLUE -> Color(0xFF0288D1)
            Theme.DARK -> Color(0xFF3E4042)
            Theme.BLACK -> Color.Black
            Theme.AQUA -> Color(0xFF00838F)
            Theme.MODERN -> Color.White
            else -> null.debugAssertNotNull ?: Color.Transparent
        },
        label = "gradient start color",
        animationSpec = appColorAnimationSpec,
    )
    val backgroundGradientEnd by animateColorAsState(
        if (!isGradient) backgroundColor
        else if (theme.isDynamicColors()) when (resolvedTheme) {
            Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_900))
            Theme.DARK -> Color(context.getColor(android.R.color.system_neutral1_900))
            Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_1000))
            Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_100))
            else -> null.debugAssertNotNull ?: Color.Transparent
        } else when (resolvedTheme) {
            Theme.DARK_GREEN -> Color(0xFF004D40)
            Theme.LIGHT -> Color(0xFF006064)
            Theme.PURPLE -> Color(0xFF4A148C)
            Theme.DEEP_PURPLE -> Color(0xFF311B92)
            Theme.INDIGO -> Color(0xFF1A237E)
            Theme.PINK -> Color(0xFF880E4F)
            Theme.GREEN -> Color(0xFF1B5E20)
            Theme.BROWN -> Color(0xFF3e2723)
            Theme.NEW_BLUE -> Color(0xFF01579B)
            Theme.DARK -> Color(0xFF191C1E)
            Theme.BLACK -> Color.Black
            Theme.AQUA -> Color(0xFF1A237E)
            Theme.MODERN -> Color(0xFFE1E3E5)
            else -> null.debugAssertNotNull ?: Color.Transparent
        },
        label = "gradient end color",
        animationSpec = appColorAnimationSpec,
    )
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    return Brush.linearGradient(
        0f to backgroundGradientStart,
        1f to backgroundGradientEnd,
        start = Offset(if (isRtl) Float.POSITIVE_INFINITY else 0f, 0f),
        end = Offset(if (isRtl) 0f else Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )
}

@Composable
fun appMonthColors(): MonthColors {
    val contentColor = LocalContentColor.current
    val theme by theme.collectAsState()
    val resolvedTheme =
        if (theme != Theme.SYSTEM_DEFAULT) theme else if (isSystemInDarkTheme()) Theme.DARK else Theme.LIGHT
    val context = LocalContext.current
    val colorAppointments = if (theme.isDynamicColors()) when (resolvedTheme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_200))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_accent1_200))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_400))
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (resolvedTheme) {
        Theme.DARK_GREEN -> Color(0xFF74BBEF)
        Theme.LIGHT -> Color(0xFF00ACC1)
        Theme.PURPLE -> Color(0xFF9C27B0)
        Theme.DEEP_PURPLE -> Color(0xFF673AB7)
        Theme.INDIGO -> Color(0xFF3F51B5)
        Theme.PINK -> Color(0xFFE91E63)
        Theme.GREEN -> Color(0xFF4CAF50)
        Theme.BROWN -> Color(0xFF795548)
        Theme.NEW_BLUE -> Color(0xFF2196F3)
        Theme.DARK, Theme.BLACK -> Color(0xFF74BBEF)
        Theme.AQUA -> Color(0xFF74BBEF)
        Theme.MODERN -> Color(0xFF376E9F)
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    val colorHolidays = if (theme.isDynamicColors()) when (resolvedTheme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_200))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_accent1_200))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_400))
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (resolvedTheme) {
        Theme.DARK_GREEN -> Color(0xFFFF8A65)
        Theme.LIGHT -> Color(0xFFFFCA28)
        Theme.PURPLE, Theme.DEEP_PURPLE, Theme.INDIGO, Theme.GREEN, Theme.BROWN -> Color(0xFFFF9800)
        Theme.PINK -> Color(0xFFFFB74D)
        Theme.NEW_BLUE -> Color(0xFFFFD740)
        Theme.DARK, Theme.BLACK -> Color(0xFFE65100)
        Theme.AQUA -> Color(0xFFFF8A65)
        Theme.MODERN -> Color(0xFFE51C23)
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    val colorCurrentDay = if (theme.isDynamicColors()) when (resolvedTheme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_400))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_accent1_200))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_600))
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (resolvedTheme) {
        Theme.DARK_GREEN -> Color(0xFFFF7043)
        Theme.LIGHT -> Color(0xFFFFD180)
        Theme.PURPLE, Theme.DEEP_PURPLE, Theme.INDIGO, Theme.GREEN, Theme.BROWN -> Color(0xFFFF9800)
        Theme.PINK -> Color(0xFFFFB74D)
        Theme.NEW_BLUE -> Color(0xFFFFD740)
        Theme.DARK, Theme.BLACK -> Color(0xFF82B1FF)
        Theme.AQUA -> Color(0xFFFF7043)
        Theme.MODERN -> Color(0xFF42AFBF)
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    val colorEventIndicator = if (theme.isDynamicColors()) when (resolvedTheme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_neutral1_0))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_100))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_neutral1_1000))
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (resolvedTheme) {
        Theme.LIGHT, Theme.DARK_GREEN, Theme.PURPLE, Theme.DEEP_PURPLE, Theme.INDIGO, Theme.PINK, Theme.GREEN, Theme.BROWN, Theme.NEW_BLUE -> Color(
            0xFFEFF2F1
        )

        Theme.DARK, Theme.BLACK -> Color(0xFFE0E0E0)
        Theme.AQUA -> Color(0xFFEFF2F1)
        Theme.MODERN -> Color(0xFF000000)
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    val colorTextDaySelected = if (theme.isDynamicColors()) when (resolvedTheme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent2_0))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_accent2_0))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent2_900))
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (resolvedTheme) {
        Theme.LIGHT, Theme.DARK_GREEN, Theme.PURPLE, Theme.DEEP_PURPLE, Theme.INDIGO, Theme.PINK, Theme.GREEN, Theme.BROWN, Theme.NEW_BLUE -> Color(
            0xFF2F3133
        )

        Theme.DARK -> Color(0xFF2F3133)
        Theme.BLACK -> Color.Black
        Theme.AQUA -> Color(0xFF2F3133)
        Theme.MODERN -> Color.Black
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    val indicator = if (theme.isDynamicColors()) when (resolvedTheme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_neutral1_800))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_600))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent2_100))
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (resolvedTheme) {
        Theme.LIGHT, Theme.DARK_GREEN, Theme.PURPLE, Theme.DEEP_PURPLE, Theme.INDIGO, Theme.PINK, Theme.GREEN, Theme.BROWN, Theme.NEW_BLUE -> Color(
            0xFFEFF2F1
        )

        Theme.DARK, Theme.BLACK -> Color(0xFFE0E0E0)
        Theme.AQUA -> Color(0xFFF5F5F5)
        Theme.MODERN -> Color(0xFFDDDEE2)
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    return MonthColors(
        contentColor = contentColor,
        appointments = colorAppointments,
        holidays = colorHolidays,
        currentDay = colorCurrentDay,
        eventIndicator = colorEventIndicator,
        textDaySelected = colorTextDaySelected,
        indicator = indicator,
    )
}

@Composable
fun appSunViewColors(): SunViewColors {
    val theme by theme.collectAsState()
    val context = LocalContext.current
    var nightColor = ContextCompat.getColor(
        context,
        if (theme.isDynamicColors()) R.color.sun_view_dynamic_night_color else R.color.sun_view_night_color
    )
    var dayColor = ContextCompat.getColor(
        context,
        if (theme.isDynamicColors()) R.color.sun_view_dynamic_day_color else R.color.sun_view_day_color
    )
    var midDayColor = ContextCompat.getColor(
        context,
        if (theme.isDynamicColors()) R.color.sun_view_dynamic_midday_color else R.color.sun_view_midday_color
    )
    if (theme == Theme.BLACK && theme.isDynamicColors()) {
        nightColor = ContextCompat.getColor(context, android.R.color.system_accent1_900)
        dayColor = ContextCompat.getColor(context, android.R.color.system_accent1_800)
        midDayColor = ContextCompat.getColor(context, android.R.color.system_accent1_600)
    }
    val listOfOldThemes = listOf(
        Theme.DARK_GREEN,
        Theme.LIGHT,
        Theme.PURPLE,
        Theme.DEEP_PURPLE,
        Theme.INDIGO,
        Theme.PINK,
        Theme.GREEN,
        Theme.BROWN,
        Theme.NEW_BLUE
    )
    if (!theme.isDynamicColors() && listOfOldThemes.contains(theme)) {
        nightColor = when (theme) {
            Theme.DARK_GREEN -> 0xFF004D40.toInt()
            Theme.LIGHT -> 0xFF006064.toInt()
            Theme.PURPLE -> 0xFF4A148C.toInt()
            Theme.DEEP_PURPLE -> 0xFF311B92.toInt()
            Theme.INDIGO -> 0xFF1A237E.toInt()
            Theme.PINK -> 0xFF880E4F.toInt()
            Theme.GREEN -> 0xFF1B5E20.toInt()
            Theme.BROWN -> 0xFF3e2723.toInt()
            Theme.NEW_BLUE -> 0xFF0d47a1.toInt()
            else -> 0xFF006064.toInt()
        }
        dayColor = when (theme) {
            Theme.DARK_GREEN -> 0xFF009688.toInt()
            Theme.LIGHT -> 0xFF00BCD4.toInt()
            Theme.PURPLE -> 0xFF9C27B0.toInt()
            Theme.DEEP_PURPLE -> 0xFF673AB7.toInt()
            Theme.INDIGO -> 0xFF3F51B5.toInt()
            Theme.PINK -> 0xFFE91E63.toInt()
            Theme.GREEN -> 0xFF4CAF50.toInt()
            Theme.BROWN -> 0xFF795548.toInt()
            Theme.NEW_BLUE -> 0xFF2196F3.toInt()
            else -> 0xFF00BCD4.toInt()
        }
        midDayColor = when (theme) {
            Theme.DARK_GREEN -> 0xFF4DB6AC.toInt()
            Theme.LIGHT -> 0xFF4DD0E1.toInt()
            Theme.PURPLE -> 0xFFBA68C8.toInt()
            Theme.DEEP_PURPLE -> 0xFF9575CD.toInt()
            Theme.INDIGO -> 0xFF7986CB.toInt()
            Theme.PINK -> 0xFFF06292.toInt()
            Theme.GREEN -> 0xFF81C784.toInt()
            Theme.BROWN -> 0xFFa1887f.toInt()
            Theme.NEW_BLUE -> 0xFF64B5F6.toInt()
            else -> 0xFF4DD0E1.toInt()
        }
    }
    return SunViewColors(
        nightColor = nightColor,
        dayColor = dayColor,
        middayColor = midDayColor,
        sunriseTextColor = 0xFFFF9800.toInt(),
        middayTextColor = 0xFFFFC107.toInt(),
        sunsetTextColor = 0xFFF22424.toInt(),
        textColorSecondary = LocalContentColor.current.copy(alpha = AppBlendAlpha).toArgb(),
        linesColor = 0x60888888,
    )
}
