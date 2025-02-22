package com.byagowi.persiancalendar.ui.settings.wallpaper

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.PREF_WALLPAPER_AUTOMATIC
import com.byagowi.persiancalendar.PREF_WALLPAPER_DARK
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.wallpaperAutomatic
import com.byagowi.persiancalendar.global.wallpaperDark
import com.byagowi.persiancalendar.ui.settings.SettingsSwitch
import com.byagowi.persiancalendar.ui.theme.SystemTheme
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.applyLanguageToConfiguration

class WallpaperSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        applyAppLanguage(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            BackHandler { finish() }
            SystemTheme {
                Column(modifier = Modifier.safeDrawingPadding()) {
                    Column(
                        Modifier
                            .alpha(AppBlendAlpha)
                            .verticalScroll(rememberScrollState())
                            .padding(all = 16.dp)
                            .background(
                                MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraLarge
                            )
                            .padding(vertical = 16.dp),
                    ) {
                        Button(
                            onClick = ::finish,
                            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                        ) {
                            Text(
                                stringResource(R.string.accept),
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                        }
                        val wallpaperAutomatic by wallpaperAutomatic.collectAsState()
                        SettingsSwitch(
                            PREF_WALLPAPER_AUTOMATIC,
                            wallpaperAutomatic,
                            stringResource(R.string.theme_default)
                        )
                        AnimatedVisibility(!wallpaperAutomatic) {
                            val wallpaperDark by wallpaperDark.collectAsState()
                            SettingsSwitch(
                                PREF_WALLPAPER_DARK,
                                wallpaperDark,
                                stringResource(R.string.theme_dark)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(applyLanguageToConfiguration(newConfig))
        applyAppLanguage(this)
    }
}
