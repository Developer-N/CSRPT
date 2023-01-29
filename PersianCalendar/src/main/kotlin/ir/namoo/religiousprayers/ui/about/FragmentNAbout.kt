package ir.namoo.religiousprayers.ui.about

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentNaboutBinding
import com.byagowi.persiancalendar.ui.utils.*
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import ir.namoo.commons.appLink
import ir.namoo.commons.utils.getAppFont
import ir.namoo.commons.utils.snackMessage
import java.io.File

class FragmentNAbout : Fragment() {

    private lateinit var binding: FragmentNaboutBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentNaboutBinding.inflate(layoutInflater, container, false)

        binding.appBar.toolbar.let { toolbar ->
            toolbar.setTitle(R.string.about)
            toolbar.setupMenuNavigation()

            toolbar.menu.add(R.string.share).also {
                it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_share)
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                it.onClick { shareApplication() }
            }

            toolbar.menu.add(R.string.device_information).also {
                it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_device_information)
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                it.onClick {
                    findNavController().navigateSafe(FragmentNAboutDirections.actionAboutToDeviceInformation())
                }
            }

            toolbar.menu.add(R.string.str_changes_title).also {
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                it.onClick {
                    MaterialAlertDialogBuilder(requireContext()).apply {
                        val txtView = MaterialTextView(requireContext()).apply {
                            movementMethod = ScrollingMovementMethod()
                            setPadding(30, 15, 30, 15)
                            textSize = 16f
                            setTextIsSelectable(true)
                            text = formatNumber(getString(R.string.changes))
                        }
                        setView(txtView)
                        setNegativeButton(R.string.close) { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                        show()
                    }
                }
            }

        }
        binding.appBar.root.hideToolbarBottomShadow()

        val version = programVersion(requireContext().packageManager, requireContext().packageName)
        val versionDescription = formatNumber(
            "${getString(R.string.app_name)}: ${
                String.format(getString(R.string.version, version))
            }\nنسخه مناسب سال 1401 - 1402"
        )

        binding.info.setContent {
            Mdc3Theme {
                val appFont = remember { getAppFont(requireContext()) }
                val normalTextColor =
                    remember { Color(requireContext().resolveColor(R.attr.colorTextNormal)) }
                val iconColor = remember { Color(requireContext().resolveColor(R.attr.colorIcon)) }
                val cardColor = remember { Color(requireContext().resolveColor(R.attr.colorCard)) }
                val scrollState = rememberScrollState()
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    InfoUIElement(appFont, normalTextColor, cardColor, versionDescription)
                    ContactUIElement(
                        normalTextColor = normalTextColor,
                        iconColor = iconColor,
                        cardColor = cardColor,
                        namooClick = { openTG(binding.root) },
                        developerNClick = { openTGDeveloper(binding.root) }
                    ) { mailTo() }
                }
            }
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
                    activity.applicationContext,
                    "${BuildConfig.APPLICATION_ID}.provider",
                    File(app.sourceDir).copyTo(
                        File(
                            "$cacheDir/" + getString(R.string.app_name).replace(
                                " ", "_"
                            ) + "-" + formatNumber(
                                programVersion(
                                    requireContext().packageManager, requireContext().packageName
                                ).split("-")[0]
                            ) + ".apk"
                        ), true
                    )
                ).apply {
                    activity.grantUriPermission(
                        "com.android.providers.media.MediaProvider",
                        this,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                putExtra(Intent.EXTRA_STREAM, uri)
                val text = "\n" + getString(R.string.app_name) + "\n$appLink"
                putExtra(Intent.EXTRA_TEXT, text)
                setDataAndType(uri, "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, getString(R.string.share)))
        }.onFailure(logException).getOrElse {
            requireActivity().bringMarketPage()
        }
    }

    private fun programVersion(packageManager: PackageManager, packageName: String): String =
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName, PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION") packageManager.getPackageInfo(
                    packageName, 0
                )
            }.versionName
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
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=namoo_ir"))
            startActivity(intent)
        }.onFailure(logException).getOrElse {
            snackMessage(view, getString(R.string.telegram_not_installed))
        }
    }

    private fun openTGDeveloper(view: View) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=developer_n"))
            startActivity(intent)
        }.onFailure(logException).getOrElse {
            snackMessage(view, getString(R.string.telegram_not_installed))
        }
    }
}//end of class FragmentNAbout
