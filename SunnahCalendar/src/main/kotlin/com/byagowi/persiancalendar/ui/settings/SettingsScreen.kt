package com.byagowi.persiancalendar.ui.settings

import android.app.StatusBarManager
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Px
import androidx.annotation.StyleRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.LOG_TAG
import com.byagowi.persiancalendar.PREF_HAS_EVER_VISITED
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.NumericBinding
import com.byagowi.persiancalendar.databinding.SettingsScreenBinding
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.service.AlarmWorker
import com.byagowi.persiancalendar.service.PersianCalendarTileService
import com.byagowi.persiancalendar.ui.about.showCarouselDialog
import com.byagowi.persiancalendar.ui.about.showIconsDemoDialog
import com.byagowi.persiancalendar.ui.about.showTypographyDemoDialog
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.InterfaceCalendarFragment
import com.byagowi.persiancalendar.ui.settings.widgetnotification.WidgetNotificationFragment
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.ui.utils.shareTextFile
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.byagowi.persiancalendar.variants.debugLog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import ir.namoo.religiousprayers.ui.preferences.NPreferenceFragment
import java.util.concurrent.TimeUnit

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */

class SettingsScreen : Fragment(R.layout.settings_screen) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = SettingsScreenBinding.bind(view)
        binding.appBar.root.hideToolbarBottomShadow()
        binding.appBar.toolbar.setTitle(R.string.settings)
        binding.appBar.toolbar.setupMenuNavigation()
        setupMenu(binding.appBar.toolbar, binding, layoutInflater)

        val args by navArgs<SettingsScreenArgs>()
        val viewModel by viewModels<SettingsViewModel>()
        if (viewModel.selectedTab.value == SettingsViewModel.DEFAULT_SELECTED_TAB)
            viewModel.changeSelectedTab(args.tab)
        val initiallySelectedTab = viewModel.selectedTab.value

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.changeSelectedTab(tab.position)
            }
        })
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = tabs.size
            override fun createFragment(position: Int) = tabs[position].first().also {
                if (position == args.tab && args.preferenceKey.isNotEmpty()) {
                    it.arguments = bundleOf(PREF_DESTINATION to args.preferenceKey)
                }
            }
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, i ->
            tab.text = tabs[i].second.joinToString(getString(R.string.spaced_and)) { getString(it) }
        }.attach()
        view.post {
            binding.viewPager.setCurrentItem(initiallySelectedTab, true)
            view.context.appPrefs.edit { putBoolean(PREF_HAS_EVER_VISITED, true) }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar.root) { appBar, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            appBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            windowInsets
        }

        @StyleRes var shape = R.style.shapeAppearanceTopCornerLarge
        @Px var pad = 0
        if (language.isArabicScript) {
            shape = R.style.shapeAppearanceTopCornerExtraLarge
            pad = (8 * resources.dp).toInt()
        }
        binding.viewPager.updatePadding(left = pad, right = pad)
        binding.roundnessFrame.shapeAppearanceModel =
            ShapeAppearanceModel.builder(binding.root.context, shape, 0).build()
    }

    companion object {
        fun insetsFix(recyclerView: RecyclerView): RecyclerView {
            ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { _, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                recyclerView.updatePadding(bottom = insets.bottom)
                windowInsets
            }
            return recyclerView
        }

        const val PREF_DESTINATION = "DESTINATION"
        const val INTERFACE_CALENDAR_TAB = 1
        const val WIDGET_NOTIFICATION_TAB = 2
        const val LOCATION_ATHAN_TAB = 0
    }

    private val tabs = listOf(
        ::NPreferenceFragment to listOf(R.string.location, R.string.athan),
        ::InterfaceCalendarFragment to listOf(R.string.pref_interface, R.string.calendar),
        ::WidgetNotificationFragment to listOf(R.string.pref_notification, R.string.pref_widget)
//        ::LocationAthanFragment to listOf(R.string.location, R.string.athan)
    )

    // Development only functionalities
    private fun setupMenu(
        toolbar: Toolbar, binding: SettingsScreenBinding, inflater: LayoutInflater
    ) {
        toolbar.menu.add(R.string.live_wallpaper_settings).onClick {
            runCatching { startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)) }
                .onFailure(logException).getOrNull().debugAssertNotNull
        }
        toolbar.menu.add(R.string.screensaver_settings).onClick {
            runCatching { startActivity(Intent(Settings.ACTION_DREAM_SETTINGS)) }
                .onFailure(logException).getOrNull().debugAssertNotNull
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            toolbar.menu.add(R.string.add_quick_settings_tile).onClick {
                val context = toolbar.context
                context.getSystemService<StatusBarManager>()?.requestAddTileService(
                    ComponentName(
                        context.packageName,
                        PersianCalendarTileService::class.qualifiedName ?: ""
                    ),
                    getString(R.string.app_name),
                    Icon.createWithResource(context, R.drawable.day19),
                    {},
                    {}
                )
            }
        }

        // Rest are development features
        if (!BuildConfig.DEVELOPMENT) return
        val activity = activity ?: return
        toolbar.menu.add("Static vs generated icons").onClick { showIconsDemoDialog(activity) }
        toolbar.menu.add("Typography").onClick { showTypographyDemoDialog(activity) }
        toolbar.menu.add("Clear preferences store and exit").onClick {
            activity.appPrefs.edit { clear() }
            activity.finish()
        }
        toolbar.menu.add("Schedule an alarm").onClick {
            val numericBinding = NumericBinding.inflate(inflater)
            numericBinding.edit.setText("5")
            MaterialAlertDialogBuilder(activity)
                .setTitle("Enter seconds to schedule alarm")
                .setView(numericBinding.root)
                .setPositiveButton(R.string.accept) { _, _ ->
                    val seconds = numericBinding.edit.text.toString().toLongOrNull() ?: 0L
                    val alarmWorker = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
                        .setInitialDelay(
                            TimeUnit.SECONDS.toMillis(seconds), TimeUnit.MILLISECONDS
                        )
                        .build()
                    WorkManager.getInstance(binding.root.context)
                        .beginUniqueWork(
                            "TestAlarm", ExistingWorkPolicy.REPLACE, alarmWorker
                        )
                        .enqueue()
                    Toast.makeText(context, "Alarm in ${seconds}s", Toast.LENGTH_SHORT)
                        .show()
                }
                .show()
        }
        fun viewCommandResult(command: String) {
            val dialogBuilder = MaterialAlertDialogBuilder(activity)
            val result = Runtime.getRuntime().exec(command).inputStream.bufferedReader().readText()
            val button = ImageButton(activity).also { button ->
                button.setImageDrawable(activity.getCompatDrawable(R.drawable.ic_baseline_share))
                button.setOnClickListener {
                    activity.shareTextFile(result, "log.txt", "text/plain")
                }
            }
            dialogBuilder.setCustomTitle(
                LinearLayout(activity).also {
                    it.layoutDirection = View.LAYOUT_DIRECTION_LTR
                    it.addView(button)
                }
            )
            dialogBuilder.setView(
                ScrollView(context).also { scrollView ->
                    scrollView.addView(TextView(context).also {
                        it.text = result
                        it.textDirection = View.TEXT_DIRECTION_LTR
                    })
                    // Scroll to bottom, https://stackoverflow.com/a/3080483
                    scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
                }
            )
            dialogBuilder.show()
        }
        toolbar.menu.addSubMenu("Log Viewer").also {
            it.add("Filtered").onClick {
                viewCommandResult("logcat -v raw -t 500 *:S $LOG_TAG:V AndroidRuntime:E")
            }
            it.add("Unfiltered").onClick { viewCommandResult("logcat -v raw -t 500") }
        }
        toolbar.menu.addSubMenu("Log").also {
            it.add("Log 'Hello'").onClick { debugLog("Hello!") }
            it.add("Handled Crash").onClick { logException(Exception("Logged Crash!")) }
            it.add("Crash!").onClick { error("Unhandled Crash!") }
        }
        toolbar.menu.add("Start Dream").onClick {
            // https://stackoverflow.com/a/23112947
            runCatching {
                startActivity(
                    Intent(Intent.ACTION_MAIN).setClassName(
                        "com.android.systemui",
                        "com.android.systemui.Somnambulator"
                    )
                )
            }.onFailure(logException).getOrNull().debugAssertNotNull
        }
        toolbar.menu.add("Start Carousel").onClick {
            showCarouselDialog(activity)
        }
    }
}
