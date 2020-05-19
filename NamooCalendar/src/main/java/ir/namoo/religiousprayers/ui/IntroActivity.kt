package ir.namoo.religiousprayers.ui

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import ir.namoo.religiousprayers.*
import ir.namoo.religiousprayers.databinding.ActivityIntroBinding
import ir.namoo.religiousprayers.ui.calendar.anims.ZoomOutPageTransformer
import ir.namoo.religiousprayers.ui.intro.IntroPagerAdapter
import ir.namoo.religiousprayers.utils.*
import java.io.File

class IntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(getThemeFromName(getThemeFromPreference(this, appPrefs)))
        applyAppLanguage(this)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (language !in listOf(LANG_EN_US, LANG_JA))
            overrideFont("SANS_SERIF", getAppFont(applicationContext))
        appPrefs.edit {
            putString(PREF_APP_LANGUAGE, LANG_FA)
            putString(PREF_PRAY_TIME_METHOD, "Karachi")
            putInt(LAST_CHOSEN_TAB_KEY, 2)
            putBoolean("showWeekOfYearNumber", true)
            putBoolean("astronomicalFeatures", true)
        }
        binding = ActivityIntroBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        binding.introPager.adapter = IntroPagerAdapter(this)
        binding.introPager.setPageTransformer(ZoomOutPageTransformer())
        binding.introPager.isUserInputEnabled = false

        if (!File(getTimesDirectoryPath(this)).exists())
            File(getTimesDirectoryPath(this)).mkdirs()
        if (!File(getAthansDirectoryPath(this)).exists())
            File(getAthansDirectoryPath(this)).mkdirs()

    }//end of onCreate

    fun goTo(position: Int) {
        binding.introPager.setCurrentItem(position, true)
    }
}//end of class