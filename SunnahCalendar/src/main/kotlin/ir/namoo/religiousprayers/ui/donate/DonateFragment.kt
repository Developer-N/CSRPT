package ir.namoo.religiousprayers.ui.donate

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DonateLayoutBinding
import com.byagowi.persiancalendar.utils.formatNumber
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.bottomsheet.BottomSheetDialog
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.iconColor
import ir.namoo.commons.utils.isPackageInstalled

class DonateFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DonateLayoutBinding.inflate(requireActivity().layoutInflater)

        binding.itemDonateMsg.text = formatNumber(getString(R.string.donate_msg))

        binding.bankNumber.setContent {
            Mdc3Theme {
                val clipboardManager = LocalClipboardManager.current

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Center
                ) {
                    ElevatedAssistChip(
                        modifier = Modifier.padding(8.dp),
                        onClick = {
                            clipboardManager.setText(AnnotatedString("5859831016333741"))
                            Toast.makeText(
                                requireContext(), "شماره کارت کپی شد! 😃", Toast.LENGTH_SHORT
                            ).show()
                        },
                        label = {
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = stringResource(id = R.string.bank_number),
                                fontFamily = FontFamily(appFont),
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                fontWeight = FontWeight.Bold,
                                color = iconColor
                            )
                        },
                        colors = AssistChipDefaults.elevatedAssistChipColors(),
                        elevation = AssistChipDefaults.elevatedAssistChipElevation(elevation = 4.dp),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = stringResource(id = R.string.copy),
                                tint = iconColor
                            )
                        })
                }
            }
        }

        binding.donateCustom.setOnClickListener {
            val url = "https://zarinp.al/namoo"
            openDonateUrl(url)
        }
        binding.donate5000.setOnClickListener {
            val url = "https://zarinp.al/371439"
            openDonateUrl(url)
        }
        binding.donate10000.setOnClickListener {
            val url = "https://zarinp.al/371438"
            openDonateUrl(url)
        }
        binding.donate20000.setOnClickListener {
            val url = "https://zarinp.al/371440"
            openDonateUrl(url)
        }
        binding.donate50000.setOnClickListener {
            val url = "https://zarinp.al/371441"
            openDonateUrl(url)
        }

        return BottomSheetDialog(requireContext()).apply {
            setContentView(binding.root)
            create()
        }
    }

    private fun openDonateUrl(url: String?) {
        CustomTabsIntent.Builder().build().apply {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (isPackageInstalled(
                    "com.android.chrome", requireContext().packageManager
                )
            ) intent.setPackage("com.android.chrome")
        }.launchUrl(requireActivity(), Uri.parse(url))
    }

}//end of class DonateFragment
