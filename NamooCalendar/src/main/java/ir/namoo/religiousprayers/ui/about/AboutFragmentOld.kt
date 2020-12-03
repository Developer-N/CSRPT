package ir.namoo.religiousprayers.ui.about

//import android.content.ActivityNotFoundException
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.Typeface
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.text.util.Linkify
//import android.util.Log
//import android.util.TypedValue
//import android.view.*
//import android.widget.LinearLayout
//import android.widget.ScrollView
//import android.widget.TextView
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.content.res.AppCompatResources
//import androidx.browser.customtabs.CustomTabsIntent
//import androidx.core.net.toUri
//import androidx.core.view.setMargins
//import androidx.core.view.setPadding
//import androidx.fragment.app.Fragment
//import ir.namoo.religiousprayers.*
//import ir.namoo.religiousprayers.databinding.DialogEmailBinding
//import ir.namoo.religiousprayers.databinding.FragmentAboutBinding
//import ir.namoo.religiousprayers.ui.MainActivity
//import ir.namoo.religiousprayers.utils.formatNumber
//import ir.namoo.religiousprayers.utils.language
//import ir.namoo.religiousprayers.utils.readRawResource
//import ir.namoo.religiousprayers.utils.supportedYearOfIranCalendar
//import com.google.android.material.chip.Chip
//import com.google.android.material.snackbar.Snackbar
//import ir.namoo.religiousprayers.databinding.FragmentAboutOldBinding
//
//class AboutFragmentOld : Fragment() {
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View? {
//        val mainActivity = activity as MainActivity
//        mainActivity.setTitleAndSubtitle(getString(R.string.about), "")
//        setHasOptionsMenu(true)
//
//        val binding = FragmentAboutOldBinding.inflate(inflater, container, false)
//
//        // version
//        val version = programVersion(mainActivity).split("-")
//            .mapIndexed { i, x -> if (i == 0) formatNumber(x) else x }
//        binding.version.text =
//            getString(R.string.version).format(version.joinToString("\n"))
//
//        // licenses
//        binding.licenses.setOnClickListener {
//            AlertDialog.Builder(
//                mainActivity,
//                com.google.android.material.R.style.Widget_MaterialComponents_MaterialCalendar_Fullscreen
//            )
//                .setTitle(resources.getString(R.string.about_license_title))
//                .setView(ScrollView(mainActivity).apply {
//                    addView(TextView(mainActivity).apply {
//                        text = readRawResource(mainActivity, R.raw.credits)
//                        setPadding(20)
//                        typeface = Typeface.MONOSPACE
//                        Linkify.addLinks(this, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
//                        setTextIsSelectable(true)
//                    })
//                })
//                .setCancelable(true)
//                .setNegativeButton(R.string.about_license_dialog_close, null)
//                .show()
//        }
//
//        // help
//        binding.aboutTitle.text = getString(R.string.about_help_subtitle).format(
//            formatNumber(supportedYearOfIranCalendar - 1),
//            formatNumber(supportedYearOfIranCalendar)
//        )
//        binding.helpCard.visibility = when (language) {
//            LANG_FA, LANG_GLK, LANG_AZB, LANG_FA_AF, LANG_EN_IR -> View.VISIBLE
//            else -> View.GONE
//        }
//
//        Linkify.addLinks(binding.helpSummary, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
//
//        // report bug
//        binding.reportBug.setOnClickListener {
//            try {
//                startActivity(
//                    Intent(
//                        Intent.ACTION_VIEW,
//                        "https://github.com/persian-calendar/DroidPersianCalendar/issues/new".toUri()
//                    )
//                )
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//
//        binding.email.setOnClickListener {
//            val emailBinding = DialogEmailBinding.inflate(inflater, container, false)
//            AlertDialog.Builder(mainActivity)
//                .setView(emailBinding.root)
//                .setTitle(R.string.about_email_sum)
//                .setPositiveButton(R.string.continue_button) { _, _ ->
//                    val emailIntent = Intent(
//                        Intent.ACTION_SENDTO,
//                        Uri.fromParts("mailto", "persian-calendar-admin@googlegroups.com", null)
//                    )
//                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
//                    try {
//                        emailIntent.putExtra(
//                            Intent.EXTRA_TEXT, """${emailBinding.inputText.text?.toString()}
//
//
//
//
//
//===Device Information===
//Manufacturer: ${Build.MANUFACTURER}
//Model: ${Build.MODEL}
//Android Version: ${Build.VERSION.RELEASE}
//App Version Code: ${version[0]}"""
//                        )
//                        startActivity(
//                            Intent.createChooser(
//                                emailIntent,
//                                getString(R.string.about_sendMail)
//                            )
//                        )
//                    } catch (e: ActivityNotFoundException) {
//                        e.printStackTrace()
//                        Snackbar.make(binding.root, R.string.about_noClient, Snackbar.LENGTH_SHORT)
//                            .show()
//                    }
//                }
//                .setNegativeButton(R.string.cancel, null).show()
//        }
//
//        val developerIcon = AppCompatResources.getDrawable(mainActivity, R.drawable.ic_developer)
//        val translatorIcon = AppCompatResources.getDrawable(mainActivity, R.drawable.ic_translator)
//        val designerIcon = AppCompatResources.getDrawable(mainActivity, R.drawable.ic_designer)
//        val chipsIconsColor = TypedValue().apply {
//            mainActivity.theme.resolveAttribute(R.attr.colorDrawerIcon, this, true)
//        }.resourceId
//
//        val chipsLayoutParams = LinearLayout.LayoutParams(
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        ).apply { setMargins(8) }
//
//        val chipClick = View.OnClickListener {
//            try {
//                CustomTabsIntent.Builder().build().launchUrl(
//                    mainActivity,
//                    ("https://github.com/" + (it as Chip).text.toString()
//                        .split("@")[1].split(")")[0]).toUri()
//                )
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//
//        getString(R.string.about_developers_list)
//            .trim().split("\n").shuffled().map {
//                Chip(mainActivity).apply {
//                    layoutParams = chipsLayoutParams
//                    setOnClickListener(chipClick)
//                    text = it
//                    chipIcon = developerIcon
//                    setChipIconTintResource(chipsIconsColor)
//                }
//            }.forEach(binding.developers::addView)
//
//        getString(R.string.about_designers_list)
//            .trim().split("\n").shuffled().map {
//                Chip(mainActivity).apply {
//                    layoutParams = chipsLayoutParams
//                    text = it
//                    chipIcon = designerIcon
//                    setChipIconTintResource(chipsIconsColor)
//                }
//            }.forEach(binding.developers::addView)
//
//        getString(R.string.about_translators_list)
//            .trim().split("\n").shuffled().map {
//                Chip(mainActivity).apply {
//                    layoutParams = chipsLayoutParams
//                    setOnClickListener(chipClick)
//                    text = it
//                    chipIcon = translatorIcon
//                    setChipIconTintResource(chipsIconsColor)
//                }
//            }.forEach(binding.developers::addView)
//
//        getString(R.string.about_contributors_list)
//            .trim().split("\n").shuffled().map {
//                Chip(mainActivity).apply {
//                    layoutParams = chipsLayoutParams
//                    setOnClickListener(chipClick)
//                    text = it
//                    chipIcon = developerIcon
//                    setChipIconTintResource(chipsIconsColor)
//                }
//            }.forEach(binding.developers::addView)
//
//        return binding.root
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        menu.clear()
//        inflater.inflate(R.menu.about_menu_buttons, menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.deviceInformation)
//            (activity as MainActivity).navigateTo(R.id.deviceInformation)
//        return true
//    }
//
//    private fun programVersion(context: Context): String = try {
//        context.packageManager.getPackageInfo(context.packageName, 0).versionName
//    } catch (e: PackageManager.NameNotFoundException) {
//        Log.e(AboutFragment::class.java.name, "Name not found on PersianUtils.programVersion", e)
//        ""
//    }
//}
