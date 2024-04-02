package loch.golden.waytogo.routes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import loch.golden.waytogo.databinding.FragmentRoutesBinding
import loch.golden.waytogo.routes.adapter.RoutesViewPagerAdapter

class RoutesFragment : Fragment(), OnTabSelectedListener {

    private lateinit var binding: FragmentRoutesBinding
    private lateinit var routesViewPagerAdapter: RoutesViewPagerAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoutesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        routesViewPagerAdapter = RoutesViewPagerAdapter(this)
        binding.viewPager.adapter = routesViewPagerAdapter
        binding.tabLayout.addOnTabSelectedListener(this)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabLayout.getTabAt(position)?.select()
            }
        })
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        binding.viewPager.currentItem = tab!!.position
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {}

    override fun onTabReselected(tab: TabLayout.Tab?) {}

}