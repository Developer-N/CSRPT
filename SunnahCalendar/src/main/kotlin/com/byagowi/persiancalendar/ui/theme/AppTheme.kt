package com.byagowi.persiancalendar.ui.theme

import android.content.Context
import android.os.PowerManager
import android.view.View
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
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
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.text.layoutDirection
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.isGradient
import com.byagowi.persiancalendar.global.isRedHolidays
import com.byagowi.persiancalendar.global.isVazirEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.systemDarkTheme
import com.byagowi.persiancalendar.global.systemLightTheme
import com.byagowi.persiancalendar.global.userSetTheme
import com.byagowi.persiancalendar.ui.calendar.calendarpager.MonthColors
import com.byagowi.persiancalendar.ui.calendar.times.SunViewColors
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.isDynamicGrayscale
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.variants.debugAssertNotNull

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val isVazirEnabled by isVazirEnabled.collectAsState()
    val language by language.collectAsState()
    val typography = if (isVazirEnabled && BuildConfig.DEVELOPMENT && language.isArabicScript) {
        val font = FontFamily(Font(R.font.vazirmatn))
        Typography(
            displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = font),
            displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = font),
            displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = font),

            headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = font),
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = font),
            headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = font),

            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = font),
            titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = font),
            titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = font),

            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = font),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = font),
            bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = font),

            labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = font),
            labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = font),
            labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = font)
        )
    } else MaterialTheme.typography
    MaterialTheme(colorScheme = appColorScheme(), typography = typography) {
        val contentColor by animateColor(MaterialTheme.colorScheme.onBackground)

        val isRtl =
            language.isLessKnownRtl || language.asSystemLocale().layoutDirection == View.LAYOUT_DIRECTION_RTL
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
        ) {
            Box(
                Modifier
                    // Don't draw behind sides insets in landscape, we don't have any plan to use it
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                    .clipToBounds()
                    // Don't move this upper to top of the chain so .clipToBounds can be applied to it
                    .background(appBackground()),
            ) { content() }
        }
    }
}

// The app's theme after custom dark/light theme is applied
@Composable
private fun effectiveTheme(): Theme {
    val explicitlySetTheme = userSetTheme.collectAsState().value
    if (explicitlySetTheme != Theme.SYSTEM_DEFAULT) return explicitlySetTheme
    return if (isSystemInDarkTheme()) {
        if (isPowerSaveMode(LocalContext.current)) Theme.BLACK
        else systemDarkTheme.collectAsState().value
    } else systemLightTheme.collectAsState().value
}

private fun isPowerSaveMode(context: Context): Boolean =
    context.getSystemService<PowerManager>()?.isPowerSaveMode == true

@Composable
private fun appColorScheme(): ColorScheme {
    val theme = effectiveTheme()
    val context = LocalContext.current
    val isDark = theme.isDark == true
    var colorScheme = if (theme.isDynamicColors) {
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (isDark) DefaultDarkColorScheme else DefaultLightColorScheme
    // Handle black theme which is useful for OLED screens
    if (theme == Theme.BLACK) colorScheme = colorScheme.copy(
        surface = Color.Black,
        surfaceContainerLow = colorScheme.surfaceContainerLowest,
        surfaceContainer = colorScheme.surfaceContainerLowest,
        surfaceContainerHigh = colorScheme.surfaceContainerLow,
        surfaceContainerHighest = colorScheme.surfaceContainer,
    )

    val backgroundColor = if (theme.isDynamicColors) when (theme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_600))
        Theme.DARK -> Color(context.getColor(android.R.color.system_neutral1_800))
        Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_1000))
        Theme.MODERN -> colorScheme.surface
        Theme.DARK_GREEN -> Color(0xFF00695c)
        Theme.CYAN -> Color(0xFF00ACC1)
        Theme.PURPLE -> Color(0xFF9C27B0)
        Theme.DEEP_PURPLE -> Color(0xFF673AB7)
        Theme.INDIGO -> Color(0xFF3F51B5)
        Theme.PINK -> Color(0xFFE91E63)
        Theme.GREEN -> Color(0xFF4CAF50)
        Theme.BROWN -> Color(0xFF795548)
        Theme.NEW_BLUE -> Color(0xFF2196F3)
        Theme.AQUA -> Color(0xFF1A237E)
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (theme) {
        Theme.DARK_GREEN -> Color(0xFF00695c)
        Theme.LIGHT, Theme.CYAN -> Color(0xFF00ACC1)
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

val appColorAnimationSpec = spring<Color>(stiffness = Spring.StiffnessMediumLow)

@Composable
fun animateColor(color: Color) = animateColorAsState(color, appColorAnimationSpec, "color")

/** This is similar to what [androidx.compose.animation.Crossfade] uses */
private val crossfadeSpec = fadeIn(tween()) togetherWith fadeOut(tween())

// Our own cross fade spec where AnimatedContent() has nicer effect
// than Crossfade() (usually on non binary changes) but we need a crossfade effect also
val appCrossfadeSpec: AnimatedContentTransitionScope<*>.() -> ContentTransform = { crossfadeSpec }

// This works better than (Enter|Exit)Transition.None in days screen switches for some reason
private val noTransition = fadeIn(snap()) togetherWith fadeOut(snap())
val noTransitionSpec: AnimatedContentTransitionScope<*>.() -> ContentTransform = { noTransition }

@Composable
fun isDynamicGrayscale(): Boolean {
    // Also track configuration changes
    LocalConfiguration.current.run {}
    return effectiveTheme().isDynamicColors && LocalContext.current.resources.isDynamicGrayscale
}

@Composable
private fun appBackground(): Brush {
    val backgroundColor = MaterialTheme.colorScheme.background
    val theme = effectiveTheme()
    val context = LocalContext.current
    val isGradient by isGradient.collectAsState()
    val backgroundGradientStart by animateColor(
        if (!isGradient) backgroundColor
        else if (theme.isDynamicColors) when (theme) {
            Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_500))
            Theme.DARK -> Color(context.getColor(android.R.color.system_neutral1_700))
            Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_1000))
            Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_0))
            Theme.DARK_GREEN -> Color(0xFF00796B)
            Theme.PURPLE -> Color(0xFF7B1FA2)
            Theme.CYAN -> Color(0xFF0097A7)
            Theme.DEEP_PURPLE -> Color(0xFF512DA8)
            Theme.INDIGO -> Color(0xFF303F9F)
            Theme.PINK -> Color(0xFFC2185B)
            Theme.GREEN -> Color(0xFF388E3C)
            Theme.BROWN -> Color(0xFF5d4037)
            Theme.NEW_BLUE -> Color(0xFF0288D1)
            Theme.AQUA -> Color(0xFF00838F)
            else -> null.debugAssertNotNull ?: Color.Transparent
        } else when (theme) {
            Theme.DARK_GREEN -> Color(0xFF00796B)
            Theme.LIGHT, Theme.CYAN -> Color(0xFF0097A7)
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
        }
    )
    val backgroundGradientEnd by animateColor(
        if (!isGradient) backgroundColor
        else if (theme.isDynamicColors) when (theme) {
            Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_900))
            Theme.DARK -> Color(context.getColor(android.R.color.system_neutral1_900))
            Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_1000))
            Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_100))
            Theme.DARK_GREEN -> Color(0xFF004D40)
            Theme.CYAN -> Color(0xFF006064)
            Theme.PURPLE -> Color(0xFF4A148C)
            Theme.DEEP_PURPLE -> Color(0xFF311B92)
            Theme.INDIGO -> Color(0xFF1A237E)
            Theme.PINK -> Color(0xFF880E4F)
            Theme.GREEN -> Color(0xFF1B5E20)
            Theme.BROWN -> Color(0xFF3e2723)
            Theme.NEW_BLUE -> Color(0xFF01579B)
            Theme.AQUA -> Color(0xFF1A237E)
            else -> null.debugAssertNotNull ?: Color.Transparent
        } else when (theme) {
            Theme.DARK_GREEN -> Color(0xFF004D40)
            Theme.LIGHT, Theme.CYAN -> Color(0xFF006064)
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
        }
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
fun appFabElevation(): FloatingActionButtonElevation {
    val isGradient by isGradient.collectAsState()
    return if (!isGradient) FloatingActionButtonDefaults.elevation(
        defaultElevation = 0.dp,
        pressedElevation = 0.dp,
        focusedElevation = 0.dp,
        hoveredElevation = 0.dp,
    ) else FloatingActionButtonDefaults.elevation()
}

@Composable
fun appMonthColors(): MonthColors {
    val contentColor = LocalContentColor.current
    val theme = effectiveTheme()
    val isRedHolidays by isRedHolidays.collectAsState()
    val context = LocalContext.current
    val colorAppointments = if (theme.isDynamicColors) when (theme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_200))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_accent1_200))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_400))
        Theme.DARK_GREEN, Theme.CYAN -> Color(0xFF74BBEF)
        Theme.PURPLE -> Color(0xFF9C27B0)
        Theme.DEEP_PURPLE -> Color(0xFF673AB7)
        Theme.INDIGO -> Color(0xFF3F51B5)
        Theme.PINK -> Color(0xFFE91E63)
        Theme.GREEN -> Color(0xFF4CAF50)
        Theme.BROWN -> Color(0xFF795548)
        Theme.NEW_BLUE -> Color(0xFF2196F3)
        Theme.AQUA -> Color(0xFF74BBEF)
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (theme) {
        Theme.DARK_GREEN -> Color(0xFF74BBEF)
        Theme.LIGHT, Theme.CYAN -> Color(0xFF00ACC1)
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
    val colorHolidays = if (theme.isDynamicColors && !isRedHolidays) when (theme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_200))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_accent1_200))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_400))
        Theme.DARK_GREEN, Theme.CYAN -> Color(0xFFFF8A65)
        Theme.PURPLE, Theme.DEEP_PURPLE, Theme.INDIGO, Theme.GREEN, Theme.BROWN -> Color(0xFFFF9800)
        Theme.PINK -> Color(0xFFFFB74D)
        Theme.NEW_BLUE -> Color(0xFFFFD740)
        Theme.AQUA -> Color(0xFFFF8A65)
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (theme) {
        Theme.DARK_GREEN -> Color(0xFFFF8A65)
        Theme.LIGHT, Theme.CYAN -> Color(0xFFFFCA28)
        Theme.PURPLE, Theme.DEEP_PURPLE, Theme.INDIGO, Theme.GREEN, Theme.BROWN -> Color(0xFFFF9800)
        Theme.PINK -> Color(0xFFFFB74D)
        Theme.NEW_BLUE -> Color(0xFFFFD740)
        Theme.DARK, Theme.BLACK -> Color(0xFFE65100)
        Theme.AQUA -> Color(0xFFFF8A65)
        Theme.MODERN -> Color(0xFFE51C23)
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    val colorCurrentDay = if (theme.isDynamicColors) when (theme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent1_400))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_accent1_200))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_600))
        Theme.DARK_GREEN, Theme.CYAN -> Color(0xFFFF7043)
        Theme.PURPLE, Theme.DEEP_PURPLE, Theme.INDIGO, Theme.GREEN, Theme.BROWN -> Color(0xFFFF9800)
        Theme.PINK -> Color(0xFFFFB74D)
        Theme.NEW_BLUE -> Color(0xFFFFD740)
        Theme.AQUA -> Color(0xFFFF7043)
        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (theme) {
        Theme.DARK_GREEN -> Color(0xFFFF7043)
        Theme.LIGHT, Theme.CYAN -> Color(0xFFFFD180)
        Theme.PURPLE, Theme.DEEP_PURPLE, Theme.INDIGO, Theme.GREEN, Theme.BROWN -> Color(0xFFFF9800)
        Theme.PINK -> Color(0xFFFFB74D)
        Theme.NEW_BLUE -> Color(0xFFFFD740)
        Theme.DARK, Theme.BLACK -> Color(0xFF82B1FF)
        Theme.AQUA -> Color(0xFFFF7043)
        Theme.MODERN -> Color(0xFF42AFBF)
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    val colorEventIndicator = if (theme.isDynamicColors) when (theme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_neutral1_0))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_100))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_neutral1_1000))
        Theme.DARK_GREEN, Theme.CYAN, Theme.PURPLE, Theme.DEEP_PURPLE, Theme.INDIGO, Theme.PINK, Theme.GREEN, Theme.BROWN, Theme.NEW_BLUE, Theme.AQUA
        -> Color(0xFFEFF2F1)

        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (theme) {
        Theme.LIGHT, Theme.CYAN, Theme.DARK_GREEN, Theme.PURPLE, Theme.DEEP_PURPLE, Theme.INDIGO, Theme.PINK, Theme.GREEN, Theme.BROWN, Theme.NEW_BLUE -> Color(
            0xFFEFF2F1
        )

        Theme.DARK, Theme.BLACK -> Color(0xFFE0E0E0)
        Theme.AQUA -> Color(0xFFEFF2F1)
        Theme.MODERN -> Color.Black
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    val colorTextDaySelected = if (theme.isDynamicColors) when (theme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_accent2_0))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_accent2_0))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent2_900))
        Theme.DARK_GREEN, Theme.CYAN, Theme.PURPLE, Theme.DEEP_PURPLE, Theme.INDIGO, Theme.PINK, Theme.GREEN, Theme.BROWN, Theme.NEW_BLUE -> Color(
            0xFF2F3133
        )

        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (theme) {
        Theme.LIGHT, Theme.CYAN, Theme.DARK_GREEN, Theme.PURPLE, Theme.DEEP_PURPLE, Theme.INDIGO, Theme.PINK, Theme.GREEN, Theme.BROWN, Theme.NEW_BLUE -> Color(
            0xFF2F3133
        )

        Theme.DARK -> Color(0xFF2F3133)
        Theme.BLACK -> Color.Black
        Theme.AQUA -> Color(0xFF2F3133)
        Theme.MODERN -> Color.Black
        else -> null.debugAssertNotNull ?: Color.Transparent
    }
    val indicator = if (theme.isDynamicColors) when (theme) {
        Theme.LIGHT -> Color(context.getColor(android.R.color.system_neutral1_800))
        Theme.DARK, Theme.BLACK -> Color(context.getColor(android.R.color.system_neutral1_600))
        Theme.MODERN -> Color(context.getColor(android.R.color.system_accent2_100))
        Theme.DARK_GREEN, Theme.CYAN, Theme.PURPLE, Theme.DEEP_PURPLE, Theme.INDIGO, Theme.PINK, Theme.GREEN, Theme.BROWN, Theme.NEW_BLUE -> Color(
            0xFFEFF2F1
        )

        else -> null.debugAssertNotNull ?: Color.Transparent
    } else when (theme) {
        Theme.LIGHT, Theme.CYAN, Theme.DARK_GREEN, Theme.PURPLE, Theme.DEEP_PURPLE, Theme.INDIGO, Theme.PINK, Theme.GREEN, Theme.BROWN, Theme.NEW_BLUE -> Color(
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
fun nextTimeColor(): Color {
    val theme = effectiveTheme()
    val context = LocalContext.current
    return if (theme.isDynamicColors) when (theme) {
        Theme.LIGHT, Theme.MODERN -> Color(context.getColor(android.R.color.system_accent1_500))
        else -> MaterialTheme.colorScheme.primary
    } else MaterialTheme.colorScheme.primary
}

@Composable
fun scrollShadowColor(): Color =
    animateColor(Color(if (effectiveTheme().isDark == true) 0x38FFFFFF else 0x38000000)).value

@Composable
fun appSunViewColors(): SunViewColors {
    val theme = effectiveTheme()
    val context = LocalContext.current
    var nightColor = ContextCompat.getColor(
        context,
        if (theme.isDynamicColors) R.color.sun_view_dynamic_night_color else R.color.sun_view_night_color
    )
    var dayColor = ContextCompat.getColor(
        context,
        if (theme.isDynamicColors) R.color.sun_view_dynamic_day_color else R.color.sun_view_day_color
    )
    var midDayColor = ContextCompat.getColor(
        context,
        if (theme.isDynamicColors) R.color.sun_view_dynamic_midday_color else R.color.sun_view_midday_color
    )
    if (theme == Theme.BLACK && theme.isDynamicColors) {
        nightColor = ContextCompat.getColor(context, android.R.color.system_accent1_900)
        dayColor = ContextCompat.getColor(context, android.R.color.system_accent1_800)
        midDayColor = ContextCompat.getColor(context, android.R.color.system_accent1_600)
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
