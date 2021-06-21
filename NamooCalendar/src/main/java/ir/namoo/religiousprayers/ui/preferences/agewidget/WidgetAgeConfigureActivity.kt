package ir.namoo.religiousprayers.ui.preferences.agewidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import ir.namoo.religiousprayers.PREF_SELECTED_DATE_AGE_WIDGET
import ir.namoo.religiousprayers.PREF_TITLE_AGE_WIDGET
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.ActivityAgeWidgetConfigureBinding
import ir.namoo.religiousprayers.updateAgeWidget
import ir.namoo.religiousprayers.utils.*

class AgeWidgetConfigureActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private fun confirm(title: String) {
        val context = this@AgeWidgetConfigureActivity

        val selectedJdn = appPrefs.getJdnOrNull(PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId)
        appPrefs.edit {
            if (selectedJdn == null) {
                // Put today's jdn if nothing was set
                putJdn(PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId, Jdn.today)
            }
            putString(PREF_TITLE_AGE_WIDGET + appWidgetId, title)
        }

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateAgeWidget(context, appWidgetManager, appWidgetId)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    override fun onCreate(icicle: Bundle?) {
        setTheme(getThemeFromName(getThemeFromPreference(this, appPrefs)))
        applyAppLanguage(this)

        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        val binding = ActivityAgeWidgetConfigureBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        val intent = intent
        val extras = intent?.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        supportFragmentManager.commit {
            add(
                R.id.preference_fragment_holder, WidgetAgeConfigureFragment::class.java,
                bundleOf(AppWidgetManager.EXTRA_APPWIDGET_ID to appWidgetId), "TAG"
            )
        }

        val title = appPrefs.getString(PREF_TITLE_AGE_WIDGET + appWidgetId, "")
        binding.editWidgetTitle.text = SpannableStringBuilder(title)
        binding.addWidgetButton.setOnClickListener {
            confirm(binding.editWidgetTitle.text.toString())
        }
    }
}
