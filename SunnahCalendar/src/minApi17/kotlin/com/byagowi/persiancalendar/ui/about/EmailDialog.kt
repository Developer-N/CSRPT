package com.byagowi.persiancalendar.ui.about

import android.content.DialogInterface
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.EmailDialogBinding
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showEmailDialog(activity: FragmentActivity) {
    val emailBinding = EmailDialogBinding.inflate(activity.layoutInflater)
    val dialog = MaterialAlertDialogBuilder(activity)
        .setView(emailBinding.root)
        .setTitle(R.string.about_email_sum)
        .setPositiveButton(R.string.continue_button) { _, _ ->
            launchEmailIntent(activity, emailBinding.inputText.text?.toString() ?: "")
        }
        .setNegativeButton(R.string.cancel, null)
        .show()

    val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE).debugAssertNotNull
    positiveButton?.isEnabled = false
    emailBinding.inputText.doAfterTextChanged {
        positiveButton?.isEnabled = (it?.toString() ?: "").isNotBlank()
    }
}
