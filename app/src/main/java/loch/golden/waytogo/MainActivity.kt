package loch.golden.waytogo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.google.android.material.navigation.NavigationBarView
import loch.golden.waytogo.databinding.ActivityMainBinding
import loch.golden.waytogo.map.PointMapFragment
import loch.golden.waytogo.routes.RoutesFragment


class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handlePermissions()
        binding.bottomNav.setOnItemSelectedListener(this)
    }

    private fun handlePermissions() {
        if (Permissions.isPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION))
            addMap()
        else {
            Permissions.requestPermission(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                Permissions.LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Permissions.LOCATION_PERMISSION_REQUEST_CODE) {
            // Check if the user granted the location permissions
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with map setup
                Toast.makeText(this, "Location permission good", Toast.LENGTH_SHORT).show()

            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }




    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (binding.bottomNav.selectedItemId == item.itemId)
            return true

        val fragment = when (item.itemId) {
            R.id.bottom_nav_map -> PointMapFragment()
            R.id.bottom_nav_routes -> RoutesFragment()
//            R.id.bottom_nav_user -> UserFragment()
            else -> return false
        }
        supportFragmentManager.commit {
            replace(R.id.fragment_container_main, fragment)
        }
        return true
    }


    private fun addMap() {
        supportFragmentManager.commit {
            replace(R.id.fragment_container_main, PointMapFragment())
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_main)
        val backPressHandler = (fragment as? IOnBackPressed)

        if (backPressHandler?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }
//TODO on permission decline make the screen tell u need to enable it
}
