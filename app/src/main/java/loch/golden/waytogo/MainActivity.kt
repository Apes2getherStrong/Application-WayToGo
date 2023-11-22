package loch.golden.waytogo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.commit
import com.google.android.material.navigation.NavigationBarView
import loch.golden.waytogo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.bottomNav.setOnItemSelectedListener(this)
    }


    //Delete comments when adding fragments 
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment = when (item.itemId) {
//            R.id.bottom_nav_map -> MapFragment()
           R.id.bottom_nav_routes -> RoutesFragment()
//            R.id.bottom_nav_user -> UserFragment()
            else -> return false
        }
        supportFragmentManager.commit {
            replace(R.id.frame_content, fragment)
        }
        return true
    }


}