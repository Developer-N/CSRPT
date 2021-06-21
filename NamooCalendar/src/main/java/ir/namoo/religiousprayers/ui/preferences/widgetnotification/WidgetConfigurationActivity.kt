package ir.namoo.religiousprayers.ui.preferences.widgetnotification

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.WidgetPreferenceLayoutBinding
import ir.namoo.religiousprayers.utils.appPrefs
import ir.namoo.religiousprayers.utils.applyAppLanguage
import ir.namoo.religiousprayers.utils.getThemeFromName
import ir.namoo.religiousprayers.utils.getThemeFromPreference
import ir.namoo.religiousprayers.utils.update
import ir.namoo.religiousprayers.utils.updateStoredPreference

class WidgetConfigurationActivity : AppCompatActivity() {

    private fun finishAndSuccess() {
        intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID).also { i ->
            setResult(Activity.RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, i))
        }
        updateStoredPreference(this)
        update(this, false)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAndSuccess()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromName(getThemeFromPreference(this, appPrefs)))

        applyAppLanguage(this)
        super.onCreate(savedInstanceState)
        val binding = WidgetPreferenceLayoutBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        supportFragmentManager.commit {
            add(
                R.id.preference_fragment_holder, WidgetNotificationFragment::class.java,
                bundleOf(WidgetNotificationFragment.IS_WIDGETS_CONFIGURATION to true), "TAG"
            )
        }
        binding.addWidgetButton.setOnClickListener { finishAndSuccess() }
    }
}
