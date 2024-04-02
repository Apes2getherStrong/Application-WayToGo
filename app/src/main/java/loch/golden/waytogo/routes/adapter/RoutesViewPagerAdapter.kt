package loch.golden.waytogo.routes.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import loch.golden.waytogo.routes.MyRoutesFragment
import loch.golden.waytogo.routes.PublicRoutesFragment


class RoutesViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {


    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 -> PublicRoutesFragment()
            1 -> MyRoutesFragment()
            else -> PublicRoutesFragment()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }


}