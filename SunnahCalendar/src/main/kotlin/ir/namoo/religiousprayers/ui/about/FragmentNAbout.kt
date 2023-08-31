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
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentNaboutBinding
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import ir.namoo.commons.appLink
import ir.namoo.commons.utils.cardColor
import ir.namoo.commons.utils.snackMessage

class FragmentNAbout : Fragment() {

    private lateinit var binding: FragmentNaboutBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentNaboutBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        binding.appBar.toolbar.let { toolbar ->
            toolbar.setTitle(R.string.about)
            toolbar.setupMenuNavigation()

            toolbar.menu.add(R.string.share).also {
                it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_share)
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                it.onClick {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(
                        Intent.EXTRA_TEXT,
                        "نرم افزار تقویم + اوقات شرعی اهل سنت را از لینک زیر دانلود کنید. \n$appLink"
                    )
                    startActivity(
                        Intent.createChooser(intent, resources.getString(R.string.share))
                    )
                }
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.contentRoot.updatePadding(bottom = insets.bottom)
            binding.appBar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }

        val version = programVersion(requireContext().packageManager, requireContext().packageName)
        val versionDescription = formatNumber(
            "${getString(R.string.app_name)}: ${
                String.format(getString(R.string.version, version))
            }\nنسخه مناسب سال 1402"
        )

        binding.info.setContent {
            Mdc3Theme {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(cardColor)
                        .verticalScroll(scrollState)
                        .padding(4.dp, 10.dp)
                ) {
                    InfoUIElement(versionDescription)
                    Divider(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(), thickness = 2.dp
                    )
                    ContactUIElement(
                        namooClick = { openTG(binding.root) },
                        developerNClick = { openTGDeveloper(binding.root) },
                        mailTo = { mailTo() }
                    )

                }
            }
        }
        return binding.root
    }

    private fun programVersion(packageManager: PackageManager, packageName: String): String =
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName, PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                packageManager.getPackageInfo(packageName, 0)
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
