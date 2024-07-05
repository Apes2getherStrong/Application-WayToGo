package loch.golden.waytogo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.commit
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import loch.golden.waytogo.databinding.ActivityMainBinding
import loch.golden.waytogo.user.LoginFragment
import loch.golden.waytogo.map.OnNavigateToMapListener
import loch.golden.waytogo.map.PointMapFragment
import loch.golden.waytogo.routes.RoutesFragment
import loch.golden.waytogo.routes.api.RetrofitInstance
import loch.golden.waytogo.user.tokenmanager.TokenManager


class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener,
    ActivityCompat.OnRequestPermissionsResultCallback, OnNavigateToMapListener {

    private lateinit var tokenManager: TokenManager
    private lateinit var binding: ActivityMainBinding
    private var dialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        RetrofitInstance.getTokenManager(tokenManager)



    }

    override fun onStart() {
        super.onStart()
        handlePermissions(true)
    }

    private fun handlePermissions(requestPerm: Boolean) {
        dialog?.dismiss()
        if (Permissions.isPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION))
            setUpView()
        else {
            if (requestPerm) {
                Permissions.requestPermission(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    Permissions.LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                dialog = MaterialAlertDialogBuilder(this).apply {
                    setTitle("Requesting Permissions")
                    setMessage("You need to allow WayToGo to access this device's location")
                    setPositiveButton("Retry") { dialog, _ ->
                        handlePermissions(false)
                        dialog.dismiss()
                    }
                    setNeutralButton("More info") { _, _ ->
                        intent = Intent(Intent.ACTION_VIEW)
                        intent.data =
                            Uri.parse("https://support.google.com/android/answer/9431959?hl=en")
                        startActivity(intent)

                    }
                    setOnDismissListener {
                        Log.d("Warmbier", "Im being dismissed :(")
                    }
                }.create()
                dialog?.show()
            }

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
                setUpView()

            } else {
                handlePermissions(false)
            }
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (binding.bottomNav.selectedItemId == item.itemId)
            return true

        val fragment = when (item.itemId) {
            R.id.bottom_nav_map -> PointMapFragment()
            R.id.bottom_nav_routes -> RoutesFragment()
            R.id.bottom_nav_user -> LoginFragment()
            else -> return false
        }
        supportFragmentManager.commit {
            replace(R.id.fragment_container_main, fragment)
        }
        return true
    }
    override fun navigateToMap() {
        supportFragmentManager.commit {
            replace(R.id.fragment_container_main, PointMapFragment())
        }

        binding.bottomNav.menu.findItem(R.id.bottom_nav_map).isChecked = true
    }


    private fun setUpView() {
        supportFragmentManager.commit {
            replace(R.id.fragment_container_main, PointMapFragment())
        }
        binding.bottomNav.setOnItemSelectedListener(this)
    }


}
