package ir.namoo.religiousprayers.ui.about

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.google.android.material.textview.MaterialTextView
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.appLink
import ir.namoo.religiousprayers.databinding.FragmentAboutBinding
import ir.namoo.religiousprayers.ui.MainActivity
import ir.namoo.religiousprayers.utils.*
import java.io.File


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

        binding.licenses.setOnClickListener {
            AlertDialog.Builder(
                mainActivity
            )
                .setTitle(resources.getString(R.string.about_license_title))
                .setView(ScrollView(mainActivity).apply {
                    addView(TextView(mainActivity).apply {
                        text = readRawResource(mainActivity, R.raw.credits)
                        setPadding(20)
                        typeface = Typeface.MONOSPACE
                        Linkify.addLinks(this, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
                        setTextIsSelectable(true)
                    })
                })
                .setCancelable(true)
                .setNegativeButton(R.string.about_license_dialog_close, null)
                .show()
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.about_menu_buttons, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.deviceInformation -> (activity as MainActivity).navigateTo(R.id.deviceInformation)
            R.id.mnu_changes -> {
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
            R.id.share -> shareApplication()
        }
        return true
    }

    private fun shareApplication() {
        val activity = activity ?: return
        try {
            val app = activity.applicationContext?.applicationInfo ?: return
            val cacheDir = requireContext().getExternalFilesDir("pic")?.absolutePath ?: return

            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                val uri = FileProvider.getUriForFile(
                    activity.applicationContext, "ir.namoo.religiousprayers.fileprovider",
                    File(app.sourceDir).copyTo(
                        File(
                            "$cacheDir/" +
                                    getString(R.string.app_name).replace(" ", "_") +
                                    "-" + formatNumber(programVersion(activity).split("-")[0]) + ".apk"
                        ), true
                    )
                ).apply {
                    activity.grantUriPermission(
                        "com.android.providers.media.MediaProvider", this,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                putExtra(Intent.EXTRA_STREAM, uri)
                val text = "\n" + getString(R.string.app_name) +
                        "\n$appLink"
                putExtra(Intent.EXTRA_TEXT, text)
                setDataAndType(uri, "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, getString(R.string.share)))
        } catch (ex: Exception) {
            ex.printStackTrace()
            bringMarketPage(activity)
        }
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
