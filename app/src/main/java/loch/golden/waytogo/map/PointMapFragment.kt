package loch.golden.waytogo.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.appolica.interactiveinfowindow.InfoWindow
import com.appolica.interactiveinfowindow.InfoWindow.MarkerSpecification
import com.appolica.interactiveinfowindow.InfoWindowManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener
import loch.golden.waytogo.IOnBackPressed
import loch.golden.waytogo.Permissions
import loch.golden.waytogo.R
import loch.golden.waytogo.databinding.FragmentMapBinding
import loch.golden.waytogo.map.adapters.PointInfoWindowAdapter
import loch.golden.waytogo.map.components.LocationManager
import loch.golden.waytogo.map.components.MapMenuManager
import loch.golden.waytogo.map.creation.RouteCreationManager
import loch.golden.waytogo.map.components.SeekbarManager
import loch.golden.waytogo.map.creation.MarkerCreationFragment
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference


class PointMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
    PanelSlideListener, OnMarkerClickListener, IOnBackPressed {

    companion object {
        private const val CAMERA_POSITION_KEY = "camera_position"
        private const val MAP_BUNDLE_KEY = "map_state"
        private val markerSpec = MarkerSpecification(0, 100)
    }


    //viewmodel tied to parent activity - MainActivity
    private val mapViewModel by activityViewModels<MapViewModel>()
    private lateinit var binding: FragmentMapBinding
    private lateinit var googleMap: GoogleMap

    private lateinit var locationManager: LocationManager
    private lateinit var mapMenuManager: MapMenuManager
    private lateinit var seekbarManager: SeekbarManager
    private lateinit var infoWindowManager: InfoWindowManager
    private lateinit var routeCreationManager: RouteCreationManager // TODO do it by lazy or init it later (if possible)

    private var inCreationMode = false

    private val markerList: MutableList<Marker?> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMapView(savedInstanceState)
        setUpListeners()
        setUpSlidingUpPanel()
//        locationManager = LocationManager(requireContext(), 600, 5.0f)
//        locationManager.startLocationTracking()
        mapMenuManager = MapMenuManager(
            requireContext(),
            binding.mapMenu.fabMenu,
            arrayListOf(binding.mapMenu.addRouteFab, binding.mapMenu.stylesFab)
        )
        seekbarManager = SeekbarManager(
            requireContext(),
            mapViewModel,
            binding.expandedPanel.seekbar,
            binding.bottomPanel.customSeekbarProgress,
            arrayListOf(binding.bottomPanel.playButton, binding.expandedPanel.playFab)
        )

        infoWindowManager = InfoWindowManager(childFragmentManager)
        infoWindowManager.setHideOnFling(true)
        infoWindowManager.onParentViewCreated(binding.mapViewContainer, savedInstanceState)

        routeCreationManager = RouteCreationManager(binding, infoWindowManager, this)

    }

    private fun setUpSlidingUpPanel() {
        val slideUpPanel = binding.slideUpPanel
        slideUpPanel.addPanelSlideListener(this)
        slideUpPanel.setFadeOnClickListener {
            slideUpPanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        }
    }


    private fun initMapView(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(mMap: GoogleMap) {

        googleMap = mMap
        infoWindowManager.onMapReady(googleMap)

        //this skips if camera position is null
        mapViewModel.cameraPosition?.let {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(it))
        }

        googleMap.setOnCameraMoveListener {
            mapViewModel.cameraPosition = googleMap.cameraPosition
        }
        googleMap.isMyLocationEnabled = true


        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(locationManager.getLatLng()!!))
        googleMap.setInfoWindowAdapter(PointInfoWindowAdapter(requireContext()))
        googleMap.setOnInfoWindowClickListener(this)
        googleMap.setOnMarkerClickListener(this)

        populateMap()
    }

    private fun populateMap() {
        for ((index, latlng) in mapViewModel.markerList.withIndex()) {
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(latlng)
                    .title("place $index")
            )
            markerList.add(marker)
        }
    }

    private fun clearMap() {
        for (marker in markerList)
            marker?.remove()
    }

    private fun setUpListeners() {
        binding.mapMenu.addRouteFab.setOnClickListener {
            toggleRouteCreation()
        }
        binding.mapMenu.stylesFab.setOnClickListener {
            Toast.makeText(requireContext(), "Styles", Toast.LENGTH_SHORT).show()

        }
        binding.buttonAddMarker.setOnClickListener {
            if (inCreationMode) {
                val markerId = routeCreationManager.getCurrentId()
                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(googleMap.projection.visibleRegion.latLngBounds.center)
                        .draggable(true)
                        .snippet("$markerId")
                        .title("Point $markerId")
                )
                val markerSpec = MarkerSpecification(0, 100)
                val infoWindow = InfoWindow(
                    marker,
                    markerSpec,
                    MarkerCreationFragment(marker, routeCreationManager, binding)
                )
                routeCreationManager.addMarker(
                    marker, infoWindow

                )
            }
        }
    }

    private fun toggleRouteCreation() {
        val view = layoutInflater.inflate(R.layout.dialog_route_title, null)
        val editText = view.findViewById<EditText>(R.id.dialog_input)

        // Set up the MaterialAlertDialogBuilder
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Enter the the route title")
            .setView(R.layout.dialog_route_title)
            .setPositiveButton("OK") { dialog, _ ->
                val title = editText.text.toString()
                routeCreationManager.setRouteTitle(title)
                inCreationMode = !inCreationMode
                if (inCreationMode) {
                    googleMap.setOnMarkerDragListener(routeCreationManager)
                    clearMap()
                    binding.expandedPanel.normal.visibility = View.GONE
                    binding.expandedPanel.creation.visibility = View.VISIBLE
                } else {
                    googleMap.setOnMarkerDragListener(null)
                    populateMap()
                    binding.expandedPanel.normal.visibility = View.VISIBLE
                    binding.expandedPanel.creation.visibility = View.GONE
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                // Handle cancellation if needed
            }
        builder.show()


    }


    override fun onMarkerClick(marker: Marker): Boolean {
        if (inCreationMode) {
            infoWindowManager.toggle(routeCreationManager.getInfoWindow(marker.snippet!!))
        } else {
            binding.slideUpPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }
        return true
    }

    override fun onInfoWindowClick(marker: Marker) {
//        marker.hideInfoWindow()
//        binding.slideUpPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
    }

    override fun onPanelSlide(panel: View?, slideOffset: Float) {
        val maxVisibilitySlideOffset = 0.2f
        binding.bottomPanel.container.alpha =
            1.0f - (slideOffset / maxVisibilitySlideOffset).coerceIn(0.0f, 1.0f)
        binding.expandedPanel.container.alpha =
            (slideOffset / maxVisibilitySlideOffset).coerceIn(0.0f, 1.0f)
    }

    override fun onPanelStateChanged(
        panel: View?,
        previousState: SlidingUpPanelLayout.PanelState?,
        newState: SlidingUpPanelLayout.PanelState?
    ) {
        if (newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
            binding.bottomPanel.container.visibility = View.VISIBLE
            binding.expandedPanel.container.visibility = View.VISIBLE
        }

        if (previousState == SlidingUpPanelLayout.PanelState.DRAGGING) {
            if (newState == SlidingUpPanelLayout.PanelState.EXPANDED)
                binding.bottomPanel.container.visibility = View.GONE
            if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED)
                binding.expandedPanel.container.visibility = View.GONE
        }
    }

    override fun onBackPressed(): Boolean {
        return if (binding.slideUpPanel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            binding.slideUpPanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            true
        } else false
    }

    //Forwarding map functions
    override fun onStart() {
        super.onStart()
        Log.d("LifecycleAlert", "onStart")
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        Log.d("LifecycleAlert", "onResume")
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        Log.d("LifecycleAlert", "onPause")
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        Log.d("LifecycleAlert", "onStop")
        binding.mapView.onStop()
        seekbarManager.removeCallback()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LifecycleAlert", "onDestroy")
        routeCreationManager.onDestroy()
        infoWindowManager.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    //TODO
    //center the titles in expanded panel when scrolling
    //balance the opacity when expanding panels
    //decide between infoWindow approach and onMarkerClick approach
    //audio still behaves werid at the start
    //move Expanded panel to seperate class and View
    //fix Location Manager
    //create the route creation lol
    // add cropping from this lib https://github.com/CanHub/Android-Image-Cropper

}
