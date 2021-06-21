package ir.namoo.religiousprayers.ui.about

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textview.MaterialTextView
import ir.namoo.religiousprayers.BuildConfig
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.appLink
import ir.namoo.religiousprayers.databinding.FragmentAboutBinding
import ir.namoo.religiousprayers.utils.*
import java.io.File


class AboutFragment : Fragment() {
    private lateinit var binding: FragmentAboutBinding

    @SuppressLint("PrivateResource")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutBinding.inflate(inflater, container, false).apply {

            appBar.toolbar.let {
                it.setTitle(R.string.about)
                it.setupUpNavigation()
            }
        }

        // version
        val version = programVersion(requireContext())
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

        binding.appBar.let {
            it.toolbar.inflateMenu(R.menu.about_menu_buttons)
            it.toolbar.setOnMenuItemClickListener { clickedMenuItem ->
                when (clickedMenuItem?.itemId) {
                    R.id.deviceInformation -> findNavController().navigate(R.id.deviceInformation)
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
                true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                it.appbarLayout.outlineProvider = null
        }

        return binding.root
    }

    private fun shareApplication() {
        val activity = activity ?: return
        runCatching {
            val app = activity.applicationContext?.applicationInfo ?: return
            val cacheDir = requireContext().getExternalFilesDir("pic")?.absolutePath ?: return

            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                val uri = FileProvider.getUriForFile(
                    activity.applicationContext, "${BuildConfig.APPLICATION_ID}.provider",
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
        }.onFailure(logException).getOrElse {
            bringMarketPage(activity)
        }
    }

    private fun programVersion(context: Context): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.onFailure(logException).getOrDefault("")

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
        runCatching {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=namoo_ir"))
            startActivity(intent)
        }.onFailure(logException).getOrElse {
            snackMessage(view, getString(R.string.telegram_not_installed))
        }
    }

    private fun openTGDeveloper(view: View) {
        runCatching {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=developer_n"))
            startActivity(intent)
        }.onFailure(logException).getOrElse {
            snackMessage(view, getString(R.string.telegram_not_installed))
        }
    }
}//end of class FragmentAbout
