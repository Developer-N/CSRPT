package ir.namoo.religiousprayers.ui.intro

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class IntroPagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Intro0Fragment()
            else -> Intro1Fragment()
        }
    }

}
