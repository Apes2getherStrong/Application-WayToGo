package loch.golden.waytogo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationBarView
import loch.golden.waytogo.databinding.ActivityMainBinding
import loch.golden.waytogo.map.MapViewModel
import loch.golden.waytogo.map.PointMapFragment

class MainActivity : AppCompatActivity(),NavigationBarView.OnItemSelectedListener {

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
            R.id.bottom_nav_map -> PointMapFragment()
//            R.id.bottom_nav_routes -> RoutesFragment()
//            R.id.bottom_nav_user -> UserFragment()
            R.id.bottom_nav_routes -> DummyFragment()
            else -> return false
        }
        supportFragmentManager.commit {
            replace(R.id.frame_content, fragment)
        }
        return true
    }


}