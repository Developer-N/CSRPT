package ir.namoo.religiousprayers.ui.preferences.locationathan.numeric

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference
import ir.namoo.religiousprayers.utils.formatNumber

class NumericPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    EditTextPreference(context, attrs) {

    private val defaultSummary: CharSequence = summary ?: ""

    private var value = .0

    override fun getText(): String = value.toString()

    // http://stackoverflow.com/a/10848393
    override fun setText(text: String?) {
        val wasBlocking = shouldDisableDependents()
        value = text?.toDoubleOrNull() ?: .0
        persistString(value.toString())
        val isBlocking = shouldDisableDependents()
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking)
        summary = if (value == 0.0) defaultSummary else formatNumber(value)
    }
}
