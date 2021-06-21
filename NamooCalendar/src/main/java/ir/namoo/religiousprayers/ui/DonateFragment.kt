package ir.namoo.religiousprayers.ui

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.material.bottomsheet.BottomSheetDialog
import ir.namoo.religiousprayers.databinding.DonateLayoutBinding
import ir.namoo.religiousprayers.utils.isPackageInstalled

class DonateFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DonateLayoutBinding.inflate(requireActivity().layoutInflater)

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



        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            BottomSheetDialog(requireContext()).apply {
                setContentView(binding.root)
                create()
            }
        else AlertDialog.Builder(requireContext()).apply {
            setView(binding.root)
            setCustomTitle(null)
        }.create()
    }

    private fun openDonateUrl(url: String?) {
        CustomTabsIntent.Builder().build().apply {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (isPackageInstalled(
                    "com.android.chrome",
                    requireContext().packageManager
                )
            )
                intent.setPackage("com.android.chrome")
        }.launchUrl(requireActivity(), Uri.parse(url))
    }

}//end of class DonateFragment
