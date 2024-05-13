package loch.golden.waytogo.routes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import loch.golden.waytogo.databinding.FragmentRoutesBinding
import kotlin.random.Random

class RoutesFragment : Fragment() {

    private lateinit var binding: FragmentRoutesBinding
    private lateinit var pagerAdapter: RoutesViewPagerAdapter
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
        pagerAdapter = RoutesViewPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Public Routes"
                1 -> tab.text = "My Routes"
            }
        }.attach()
    }

    fun replaceFragment(position: Int, fragment: Fragment) {
        pagerAdapter.replaceFragment(position, fragment)
    }
    private inner class RoutesViewPagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {
        val fragments: Array<Fragment> = arrayOf(PublicRoutesFragment(), MyRoutesFragment())
        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }

        override fun getItemCount(): Int {
            return 2
        }

        fun replaceFragment(position: Int, fragment: Fragment) {
            fragments[position] = fragment
            notifyItemChanged(position)
            createFragment(position)
        }

        override fun getItemId(position: Int): Long {
            val fragment = fragments[position]
            return getIDForFragment(fragment)

        }

        override fun containsItem(itemId: Long): Boolean {
            for (fragment in fragments){
                if (getIDForFragment(fragment) == itemId)
                    return true
            }
            return false
        }

        fun getIDForFragment(fragment: Fragment): Long{
            return fragment.hashCode().toLong()
        }
    }


}

