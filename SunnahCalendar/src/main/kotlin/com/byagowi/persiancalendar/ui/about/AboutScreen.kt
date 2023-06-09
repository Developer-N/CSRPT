package com.byagowi.persiancalendar.ui.about

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.util.Linkify
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.AboutScreenBinding
import com.byagowi.persiancalendar.generated.faq
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.utils.bringMarketPage
import com.byagowi.persiancalendar.ui.utils.getAnimatedDrawable
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.isRtl
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.supportedYearOfIranCalendar
import com.google.android.material.chip.Chip

class AboutScreen : Fragment(R.layout.about_screen) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = AboutScreenBinding.bind(view)
        binding.appBar.toolbar.setTitle(R.string.about)
        binding.appBar.toolbar.setupMenuNavigation()
        binding.appBar.toolbar.menu.add(R.string.share).also {
            it.icon = binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_baseline_share)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.onClick { shareApplication() }
        }
        binding.appBar.toolbar.menu.add(R.string.device_information).also {
            it.icon =
                binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_device_information)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.onClick {
//                    findNavController().navigateSafe(AboutScreenDirections.actionAboutToDeviceInformation())
            }
        }
        binding.appBar.root.hideToolbarBottomShadow()

        // app
        val version = buildSpannedString {
            scale(1.5f) { bold { appendLine(getString(R.string.app_name)) } }
            scale(.8f) {
                val version =
                    // Don't formatNumber it if is multi-parted
                    if ("-" in BuildConfig.VERSION_NAME) BuildConfig.VERSION_NAME
                    else formatNumber(BuildConfig.VERSION_NAME)
                append(getString(R.string.version, version))
            }
            if (language.isUserAbleToReadPersian) {
                appendLine()
                scale(.8f) {
                    append(
                        getString(
                            R.string.about_help_subtitle,
                            formatNumber(supportedYearOfIranCalendar - 1),
                            formatNumber(supportedYearOfIranCalendar)
                        )
                    )
                }
            }
        }
        binding.aboutHeader.text = version
        binding.icon.also {
            val animation =
                context?.getAnimatedDrawable(R.drawable.splash_icon_animation) ?: return@also
            it.setImageDrawable(animation)
            animation.start()
            val clickHandlerDialog = createEasterEggClickHandler(::showPeriodicTableDialog)
            val clickHandlerIcon = createIconRandomEffects(it)
            it.setOnClickListener {
                animation.stop()
                animation.start()
                clickHandlerDialog(activity)
                clickHandlerIcon()
            }
        }

        fun TextView.putLineStartIcon(@DrawableRes icon: Int) {
            if (resources.isRtl) setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
            else setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
        }

        // licenses
        binding.licenses.setOnClickListener {
//            findNavController().navigateSafe(AboutScreenDirections.actionAboutToLicenses())
        }
        binding.licensesTitle.putLineStartIcon(R.drawable.ic_licences)

        // help
        binding.helpCard.isVisible = language.isUserAbleToReadPersian
        binding.helpTitle.putLineStartIcon(R.drawable.ic_help)
        binding.helpSectionsRecyclerView.apply {
            val sections = faq
                .split(Regex("^={4}$", RegexOption.MULTILINE))
                .map { it.trim().lines() }
                .map { lines ->
                    val title = lines.first()
                    val body = SpannableString(lines.drop(1).joinToString("\n").trim())
                    Linkify.addLinks(body, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
                    title to body
                }
            adapter = ExpandableItemsAdapter(sections)
            layoutManager = LinearLayoutManager(context)
        }

        // report bug
        binding.reportBug.setOnClickListener { launchReportIntent() }
        binding.reportBugTitle.putLineStartIcon(R.drawable.ic_bug)

        binding.email.setOnClickListener click@{ showEmailDialog(activity ?: return@click) }
        binding.emailTitle.putLineStartIcon(R.drawable.ic_email)

        setupContributorsList(binding)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.contentRoot.updatePadding(bottom = insets.bottom)
            binding.appBar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupContributorsList(binding: AboutScreenBinding) {
        val context = binding.root.context

        val chipsIconTintId = TypedValue().apply {
            context.theme.resolveAttribute(
                com.google.android.material.R.attr.colorAccent,
                this,
                true
            )
        }.resourceId

        val chipClick = View.OnClickListener {
            (it.tag as? String)?.also { user ->
                if (user == "ImanSoltanian") return@also // The only person without GitHub account
                runCatching {
                    val uri = "https://github.com/$user".toUri()
                    CustomTabsIntent.Builder().build().launchUrl(context, uri)
                }.onFailure(logException)
            }
        }


        // Chip view inflation crashes in Android 4 as lack RippleDrawable apparently and material's
        // internal bug so let's just hide it there
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            binding.developersSection.isVisible = false
            return
        }

        listOf(
            R.string.about_developers_list to R.drawable.ic_developer,
            R.string.about_designers_list to R.drawable.ic_designer,
            R.string.about_translators_list to R.drawable.ic_translator,
            R.string.about_contributors_list to R.drawable.ic_developer
        ).flatMap { (listId: Int, iconId: Int) ->
            val icon = context.getCompatDrawable(iconId)
            getString(listId).trim().split("\n").map {
                Chip(context).also { chip ->
                    chip.ensureAccessibleTouchTarget(0)
                    chip.setOnClickListener(chipClick)
                    val (username, displayName) = it.split(": ")
                    chip.tag = username
                    chip.text = displayName
                    chip.chipIcon = icon
                    chip.setChipIconTintResource(chipsIconTintId)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        chip.elevation = resources.getDimension(R.dimen.chip_elevation)
                    }
                }
            }
        }.shuffled().forEach(binding.developers::addView)
    }

    private fun launchReportIntent() {
        runCatching {
            val uri = "https://github.com/persian-calendar/persian-calendar/issues/new".toUri()
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }.onFailure(logException)
    }

    private fun shareApplication() {
        runCatching {
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                val textToShare = """${getString(R.string.app_name)}
https://github.com/persian-calendar/persian-calendar"""
                putExtra(Intent.EXTRA_TEXT, textToShare)
            }, getString(R.string.share)))
        }.onFailure(logException).onFailure { (activity ?: return).bringMarketPage() }
    }
}
