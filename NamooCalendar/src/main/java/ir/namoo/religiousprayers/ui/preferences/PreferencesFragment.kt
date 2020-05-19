package ir.namoo.religiousprayers.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.FragmentSettingsBinding
import ir.namoo.religiousprayers.ui.MainActivity
import ir.namoo.religiousprayers.ui.preferences.interfacecalendar.InterfaceCalendarFragment
import ir.namoo.religiousprayers.ui.preferences.widgetnotification.WidgetNotificationFragment

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class PreferencesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentSettingsBinding.inflate(inflater, container, false).apply {
        val mainActivity = activity as MainActivity
        mainActivity.setTitleAndSubtitle(getString(R.string.settings), "")
        val tabs = listOf(
            R.string.pref_header_nsetting to NSettingFragment::class.java,
            R.string.pref_header_interface_calendar to InterfaceCalendarFragment::class.java,
            R.string.pref_header_widget_location to WidgetNotificationFragment::class.java
        )
        viewPager.adapter = object : FragmentStateAdapter(this@PreferencesFragment) {
            override fun getItemCount() = tabs.size
            override fun createFragment(position: Int) = tabs[position].second.newInstance()
        }
        TabLayoutMediator(tabLayout, viewPager) { tab, i ->
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
    }.root
}
