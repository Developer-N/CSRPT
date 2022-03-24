package ir.namoo.religiousprayers.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.byagowi.persiancalendar.CHANGE_LANGUAGE_IS_PROMOTED_ONCE
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_ASR_HANAFI_JURISTIC
import com.byagowi.persiancalendar.PREF_ASTRONOMICAL_FEATURES
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.PREF_SHOW_WEEK_OF_YEAR_NUMBER
import com.byagowi.persiancalendar.PREF_WIDGET_IN_24
import com.byagowi.persiancalendar.databinding.ActivityIntroBinding
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import dagger.hilt.android.AndroidEntryPoint
import io.github.persiancalendar.praytimes.CalculationMethod
import ir.namoo.commons.PREF_APP_FONT
import ir.namoo.commons.PREF_FIRST_START
import ir.namoo.commons.PREF_FULL_SCREEN_METHOD
import ir.namoo.commons.PREF_NOTIFICATION_METHOD
import ir.namoo.commons.SYSTEM_DEFAULT_FONT
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.utils.getAppFont
import ir.namoo.commons.utils.getAthansDirectoryPath
import ir.namoo.commons.utils.overrideFont
import ir.namoo.religiousprayers.ui.intro.Intro0Fragment
import ir.namoo.religiousprayers.ui.intro.Intro1Fragment
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding

    @Inject
    lateinit var locationsDB: LocationsDB
    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)
        val font = appPrefs.getString(PREF_APP_FONT, SYSTEM_DEFAULT_FONT)
        if (!font.isNullOrEmpty() && font != SYSTEM_DEFAULT_FONT)
            appPrefs.edit { putString(PREF_APP_FONT, SYSTEM_DEFAULT_FONT) }
        if (appPrefs.getBoolean(PREF_FIRST_START, true)
            || appPrefs.getString(PREF_GEOCODED_CITYNAME, "").isNullOrEmpty()
            || appPrefs.getString(PREF_LATITUDE, "0.0") == "0.0"
            || appPrefs.getString(PREF_LONGITUDE, "0.0") == "0.0"
            || locationsDB.cityDAO().getAllCity().isNullOrEmpty()
        ) {
            appPrefs.edit {
                putString(PREF_APP_LANGUAGE, Language.FA.code)
                putString(PREF_PRAY_TIME_METHOD, CalculationMethod.Karachi.name)
                putBoolean(PREF_ASR_HANAFI_JURISTIC, false)
                putBoolean(PREF_SHOW_WEEK_OF_YEAR_NUMBER, true)
                putBoolean(PREF_ASTRONOMICAL_FEATURES, true)
                putBoolean(CHANGE_LANGUAGE_IS_PROMOTED_ONCE, true)
                putBoolean(PREF_WIDGET_IN_24, true)
                putInt(LAST_CHOSEN_TAB_KEY, 2)
                putInt(PREF_NOTIFICATION_METHOD, 1)
                putInt(PREF_FULL_SCREEN_METHOD, 1)
            }
            overrideFont("SANS_SERIF", getAppFont(applicationContext))

            binding = ActivityIntroBinding.inflate(layoutInflater).apply {
                setContentView(root)
            }

            binding.introPager.adapter = IntroPagerAdapter(this@IntroActivity)
            binding.introPager.isUserInputEnabled = false

            if (!File(getAthansDirectoryPath(this@IntroActivity)).exists())
                File(getAthansDirectoryPath(this@IntroActivity)).mkdirs()
        } else {
            startActivity(Intent(this@IntroActivity, MainActivity::class.java))
            finish()
        }

    }//end of onCreate

    fun goTo(position: Int) {
        binding.introPager.setCurrentItem(position, true)
    }

    class IntroPagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> Intro0Fragment()
                else -> Intro1Fragment()
            }
        }

    }

}//end of class IntroActivity
