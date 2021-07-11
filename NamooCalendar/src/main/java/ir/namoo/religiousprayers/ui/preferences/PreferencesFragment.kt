package ir.namoo.religiousprayers.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.FragmentSettingsBinding
import ir.namoo.religiousprayers.ui.preferences.interfacecalendar.InterfaceCalendarFragment
import ir.namoo.religiousprayers.ui.preferences.widgetnotification.WidgetNotificationFragment
import ir.namoo.religiousprayers.utils.setupUpNavigation

class PreferencesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentSettingsBinding.inflate(inflater, container, false).also { binding ->
        binding.appBar.toolbar.let {
            it.setTitle(R.string.settings)
            it.setupUpNavigation()
        }

        val tabs = listOf(
            R.string.pref_header_nsetting to NSettingFragment::class.java,
            R.string.pref_header_interface_calendar to InterfaceCalendarFragment::class.java,
            R.string.pref_header_widget_location to WidgetNotificationFragment::class.java
        )
        val args: PreferencesFragmentArgs by navArgs()
        binding.viewPager.adapter = object : FragmentStateAdapter(this@PreferencesFragment) {
            override fun getItemCount() = tabs.size
            override fun createFragment(position: Int) = tabs[position].second.newInstance().also {
                if (position == args.tab && args.preferenceKey.isNotEmpty()) {
                    it.arguments = bundleOf(PREF_DESTINATION to args.preferenceKey)
                }
            }
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, i ->
            tab.setText(tabs[i].first)
            tab.setIcon(
                when (i) {
                    0 -> R.drawable.ic_settings
                    1 -> R.drawable.ic_calendar
                    2 -> R.drawable.ic_widgets
                    else -> R.drawable.ic_settings
                }
            )
        }.attach()
        binding.viewPager.currentItem = args.tab
    }.root
}

val PREF_DESTINATION = "DESTINATION"

val LOCATION_ATHAN_TAB = 0
val INTERFACE_CALENDAR_TAB = 1
val WIDGET_NOTIFICATION_TAB = 2
