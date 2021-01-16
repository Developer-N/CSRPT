package ir.namoo.religiousprayers.ui.preferences.widgetnotification

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.WidgetPreferenceLayoutBinding
import ir.namoo.religiousprayers.utils.*

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
            add(R.id.preference_fragment_holder, WidgetNotificationFragment(), "TAG")
        }
        binding.addWidgetButton.setOnClickListener { finishAndSuccess() }
    }
}
