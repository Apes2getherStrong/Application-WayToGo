package loch.golden.waytogo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import loch.golden.waytogo.databinding.ActivityMainBinding
import loch.golden.waytogo.fragments.map.views.PointMapFragment
import loch.golden.waytogo.fragments.route.views.RoutesFragment
import loch.golden.waytogo.fragments.user.components.TokenManager
import loch.golden.waytogo.fragments.user.views.LoginFragment
import loch.golden.waytogo.utils.Constants
import loch.golden.waytogo.utils.OnChangeFragmentListener
import loch.golden.waytogo.utils.Permissions
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity() : AppCompatActivity(), NavigationBarView.OnItemSelectedListener,
    ActivityCompat.OnRequestPermissionsResultCallback, OnChangeFragmentListener {

    @Inject
    lateinit var tokenManager: TokenManager
    private lateinit var binding: ActivityMainBinding
    private var dialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

            // Hide the bottom navigation bar when the keyboard is shown
            if (isKeyboardVisible) {
                binding.bottomNav.visibility = View.GONE
            } else {
                binding.bottomNav.visibility = View.VISIBLE
            }

            insets
        }
        initFolders()

        if (!isInternetAvailable(this)) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "No internet connection. Sprawdź swoje połaczenie przed korzystaniem z aplikacji.",
                Snackbar.LENGTH_LONG
            ).setAnchorView(binding.bottomNav)
                .show()
        }


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


    private fun initFolders() {
        val imageDir = File(this.filesDir, Constants.IMAGE_DIR)
        if (!imageDir.exists())
            imageDir.mkdirs()
        val audioDir = File(this.filesDir, Constants.AUDIO_DIR)
        if (!audioDir.exists())
            audioDir.mkdirs()
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

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
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

    override fun changeFragment(fragmentNo: Int, bundle: Bundle?) {
        val fragmentId = when (fragmentNo) {
            1 -> R.id.bottom_nav_map
            2 -> R.id.bottom_nav_routes
            3 -> R.id.bottom_nav_user
            else -> return
        }

        val fragment = when (fragmentId) {
            R.id.bottom_nav_map -> PointMapFragment()
            R.id.bottom_nav_routes -> RoutesFragment()
            R.id.bottom_nav_user -> LoginFragment()
            else -> return
        }

        bundle?.let {
            fragment.arguments = it
        }

        supportFragmentManager.commit {
            replace(R.id.fragment_container_main, fragment)
        }

        // Update the bottom navigation selected item
        binding.bottomNav.menu.findItem(fragmentId).isChecked = true
    }


    private fun setUpView() {
        supportFragmentManager.commit {
            replace(R.id.fragment_container_main, PointMapFragment())
        }
        binding.bottomNav.setOnItemSelectedListener(this)
    }

    private fun clearCache() {
        val cacheDir = this.cacheDir
        cacheDir.deleteRecursively()
    }

    override fun onDestroy() {
        clearCache()
        super.onDestroy()
    }

}
