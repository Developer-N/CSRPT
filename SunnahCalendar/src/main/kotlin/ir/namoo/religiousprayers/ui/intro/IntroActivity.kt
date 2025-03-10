package ir.namoo.religiousprayers.ui.intro

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_ASR_HANAFI_JURISTIC
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.PREF_SHOW_WEEK_OF_YEAR_NUMBER
import com.byagowi.persiancalendar.PREF_WIDGET_IN_24
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.preferences
import io.github.persiancalendar.praytimes.CalculationMethod
import ir.namoo.commons.PREF_APP_FONT
import ir.namoo.commons.PREF_FIRST_START
import ir.namoo.commons.SYSTEM_DEFAULT_FONT
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.utils.getAppFont
import ir.namoo.commons.utils.getAthansDirectoryPath
import ir.namoo.commons.utils.overrideFont
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import java.io.File

class IntroActivity : ComponentActivity() {
    private val prayTimeRepository: PrayTimeRepository = get()
    override fun onCreate(savedInstanceState: Bundle?) {
        // Just to make sure we have an initial transparent system bars
        // System bars are tweaked later with project's with real values
        applyEdgeToEdge(isBackgroundColorLight = false, isSurfaceColorLight = true)

        setTheme(R.style.BaseTheme)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val font = preferences.getString(PREF_APP_FONT, SYSTEM_DEFAULT_FONT)
            if (!font.isNullOrEmpty() && font != SYSTEM_DEFAULT_FONT) preferences.edit {
                putString(PREF_APP_FONT, SYSTEM_DEFAULT_FONT)
            }
            if (preferences.getBoolean(PREF_FIRST_START, true) ||
                preferences.getString(PREF_GEOCODED_CITYNAME, "").isNullOrEmpty() ||
                preferences.getString(PREF_LATITUDE, "0.0") == "0.0" ||
                preferences.getString(PREF_LONGITUDE, "0.0") == "0.0" ||
                prayTimeRepository.getLocalCityList().isEmpty()
            ) {
                preferences.edit {
                    putString(PREF_APP_LANGUAGE, Language.FA.code)
                    putString(PREF_PRAY_TIME_METHOD, CalculationMethod.Karachi.name)
                    putBoolean(PREF_ASR_HANAFI_JURISTIC, false)
                    putBoolean(PREF_SHOW_WEEK_OF_YEAR_NUMBER, true)
                    putBoolean(PREF_WIDGET_IN_24, true)
                    putInt(LAST_CHOSEN_TAB_KEY, 2)
                }
                overrideFont("SANS_SERIF", getAppFont(applicationContext))
                applyAppLanguage(this@IntroActivity)
                setContent {
                    AppTheme {
                        val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
                        val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
                        LaunchedEffect(isBackgroundColorLight, isSurfaceColorLight) {
                            applyEdgeToEdge(isBackgroundColorLight, isSurfaceColorLight)
                        }
                        IntroHomeScreen(modifier = Modifier.padding(0.dp)) {
                            startActivity(Intent(this@IntroActivity, MainActivity::class.java))
                            finish()
                        }
                    }
                }
                if (!File(getAthansDirectoryPath(this@IntroActivity)).exists()) File(
                    getAthansDirectoryPath(this@IntroActivity)
                ).mkdirs()
            } else {
                startActivity(Intent(this@IntroActivity, MainActivity::class.java))
                finish()
            }

        }
    }//end of onCreate

    private fun applyEdgeToEdge(isBackgroundColorLight: Boolean, isSurfaceColorLight: Boolean) {
        val statusBarStyle =
            if (isBackgroundColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) enableEdgeToEdge(
            statusBarStyle,
            if (isSurfaceColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
        ) else enableEdgeToEdge(
            statusBarStyle,
            // Just don't tweak navigation bar in older Android versions, leave it to default
        )
    }
}//end of class IntroActivity
