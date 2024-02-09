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
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import io.github.persiancalendar.praytimes.CalculationMethod
import ir.namoo.commons.PREF_APP_FONT
import ir.namoo.commons.PREF_FIRST_START
import ir.namoo.commons.PREF_FULL_SCREEN_METHOD
import ir.namoo.commons.PREF_NOTIFICATION_METHOD
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
            val font = appPrefs.getString(PREF_APP_FONT, SYSTEM_DEFAULT_FONT)
            if (!font.isNullOrEmpty() && font != SYSTEM_DEFAULT_FONT) appPrefs.edit {
                putString(PREF_APP_FONT, SYSTEM_DEFAULT_FONT)
            }
            if (appPrefs.getBoolean(PREF_FIRST_START, true) ||
                appPrefs.getString(PREF_GEOCODED_CITYNAME, "").isNullOrEmpty() ||
                appPrefs.getString(PREF_LATITUDE, "0.0") == "0.0" ||
                appPrefs.getString(PREF_LONGITUDE, "0.0") == "0.0" ||
                prayTimeRepository.getLocalCityList().isEmpty()
            ) {
                appPrefs.edit {
                    putString(PREF_APP_LANGUAGE, Language.FA.code)
                    putString(PREF_PRAY_TIME_METHOD, CalculationMethod.Karachi.name)
                    putBoolean(PREF_ASR_HANAFI_JURISTIC, false)
                    putBoolean(PREF_SHOW_WEEK_OF_YEAR_NUMBER, true)
                    putBoolean(PREF_WIDGET_IN_24, true)
                    putInt(LAST_CHOSEN_TAB_KEY, 2)
                    putInt(PREF_NOTIFICATION_METHOD, 1)
                    putInt(PREF_FULL_SCREEN_METHOD, 1)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) enableEdgeToEdge(
            if (isBackgroundColorLight)
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
            if (isSurfaceColorLight)
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
        ) else enableEdgeToEdge( // Just don't tweak navigation bar in older Android versions
            if (isBackgroundColorLight)
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT)
        )
    }
}//end of class IntroActivity
