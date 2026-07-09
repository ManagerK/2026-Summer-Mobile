package ai.hnu.kr.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    private val pages = listOf(
        PageFragment.newInstance("#FF5722", "Page 1"),
        PageFragment.newInstance("#4CAF50", "Page 2"),
        PageFragment.newInstance("#2196F3", "Page 3")
    )

    override fun getItemCount(): Int = pages.size
    override fun createFragment(position: Int): Fragment = pages[position]
}