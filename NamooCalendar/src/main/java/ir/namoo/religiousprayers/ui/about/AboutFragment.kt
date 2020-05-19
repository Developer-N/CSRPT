package ir.namoo.religiousprayers.ui.about

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.textview.MaterialTextView
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.FragmentAboutBinding
import ir.namoo.religiousprayers.ui.MainActivity
import ir.namoo.religiousprayers.utils.formatNumber
import ir.namoo.religiousprayers.utils.getAppFont
import ir.namoo.religiousprayers.utils.snackMessage


class AboutFragment : Fragment() {
    private lateinit var binding: FragmentAboutBinding

    @SuppressLint("PrivateResource")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val mainActivity = activity as MainActivity
        mainActivity.setTitleAndSubtitle(getString(R.string.about), "")
        setHasOptionsMenu(true)

        binding = FragmentAboutBinding.inflate(inflater, container, false)

        // version
        val version = programVersion(mainActivity)
        binding.textViewInfoVersion.text =
            formatNumber(String.format(getString(R.string.version, version)))

        binding.btnTgDeveloper.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    requireContext(),
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            openTGDeveloper(it)
        }
        binding.btnTg.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    requireContext(),
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            openTG(it)
        }
        binding.btnGmail.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    requireContext(),
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            mailTo()
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.about_menu_buttons, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.deviceInformation)
            (activity as MainActivity).navigateTo(R.id.deviceInformation)
        else if (item.itemId == R.id.mnu_changes) {
            AlertDialog.Builder(requireContext()).apply {
                val txtView = MaterialTextView(requireContext()).apply {
                    movementMethod = ScrollingMovementMethod()
                    typeface = getAppFont(requireContext())
                    setPadding(30, 15, 30, 15)
                    textSize = 16f
                    setTextIsSelectable(true)
                    text = getString(R.string.changes)
                }
                setView(txtView)
                setNegativeButton(R.string.close) { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }
            }.create().show()
        }
        return true
    }

    private fun programVersion(context: Context): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e(AboutFragment::class.java.name, "Name not found on PersianUtils.programVersion", e)
        ""
    }

    private fun mailTo() {
        val intent = Intent(
            Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "namoodev@gmail.com", null
            )
        )
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_subject))
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.mail_contect))
        startActivity(Intent.createChooser(intent, getString(R.string.send_mail)))
    }

    private fun openTG(view: View) {
        try {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=namoo_ir"))
            startActivity(intent)
        } catch (ex: Exception) {
            snackMessage(view, getString(R.string.telegram_not_installed))
        }
    }

    private fun openTGDeveloper(view: View) {
        try {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=developer_n"))
            startActivity(intent)
        } catch (ex: Exception) {
            snackMessage(view, getString(R.string.telegram_not_installed))
        }
    }
}//end of class FragmentAbout
