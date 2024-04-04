package loch.golden.waytogo.routes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import loch.golden.waytogo.databinding.FragmentRoutesBinding

class RoutesFragment : Fragment() {

    private lateinit var binding: FragmentRoutesBinding
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
        binding.viewPager.adapter = RoutesViewPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Public Routes"
                1 -> tab.text = "My Routes"
            }
        }.attach()
    }

    private inner class RoutesViewPagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> PublicRoutesFragment()
                1 -> MyRoutesFragment()
                else -> PublicRoutesFragment()
            }
        }

        override fun getItemCount(): Int {
            return 2
        }
    }
}