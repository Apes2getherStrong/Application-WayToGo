package loch.golden.waytogo.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.appolica.interactiveinfowindow.InfoWindow
import com.appolica.interactiveinfowindow.InfoWindow.MarkerSpecification
import com.appolica.interactiveinfowindow.InfoWindowManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import loch.golden.waytogo.classes.MapPoint
import loch.golden.waytogo.classes.MapRoute
import loch.golden.waytogo.databinding.DialogRouteTitleBinding
import loch.golden.waytogo.databinding.FragmentMapBinding
import loch.golden.waytogo.map.adapters.PointInfoWindowAdapter
import loch.golden.waytogo.map.components.LocationManager
import loch.golden.waytogo.map.components.MapMenuManager
import loch.golden.waytogo.map.components.SlidingUpPanelManager
import loch.golden.waytogo.map.creation.MarkerCreationFragment
import loch.golden.waytogo.map.creation.RouteCreationManager


class PointMapFragment(val currentRoute: MapRoute? = null) : Fragment(), OnMapReadyCallback,
    OnMarkerClickListener {

    //viewmodel tied to parent activity - MainActivity
    private lateinit var mapViewModel: MapViewModel
    private lateinit var binding: FragmentMapBinding
    private lateinit var googleMap: GoogleMap

    private lateinit var locationManager: LocationManager
    private lateinit var mapMenuManager: MapMenuManager
    //private lateinit var seekbarManager: SeekbarManager
    private lateinit var infoWindowManager: InfoWindowManager
    private lateinit var slidingUpPanelManager: SlidingUpPanelManager
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
        mapViewModel = ViewModelProvider(requireActivity()).get(loch.golden.waytogo.map.MapViewModel::class.java)
        initMapView(savedInstanceState)
        setUpListeners()

        slidingUpPanelManager = SlidingUpPanelManager(binding)
        locationManager = LocationManager(requireContext())
        locationManager.startLocationUpdates()
        mapMenuManager = MapMenuManager(
            requireContext(),
            binding.mapMenu.fabMenu,
            arrayListOf(binding.mapMenu.addRouteFab, binding.mapMenu.stylesFab)
        )
//        seekbarManager = SeekbarManager(
//            requireContext(),
//            mapViewModel,
//            binding.expandedPanel.seekbar,
//            binding.bottomPanel.customSeekbarProgress,
//            arrayListOf(binding.bottomPanel.playButton, binding.expandedPanel.playFab)
//        )

        infoWindowManager = InfoWindowManager(childFragmentManager)
        infoWindowManager.setHideOnFling(true)
        infoWindowManager.onParentViewCreated(binding.mapViewContainer, savedInstanceState)

        routeCreationManager = RouteCreationManager(binding, infoWindowManager, this)

    }


    private fun initMapView(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(mMap: GoogleMap) {

        googleMap = mMap
        infoWindowManager.onMapReady(googleMap)


        googleMap.isMyLocationEnabled = true
        googleMap.setInfoWindowAdapter(PointInfoWindowAdapter(requireContext()))
        googleMap.setOnMarkerClickListener(this)
        mapViewModel.route?.let{
            populateMap(it.pointList)
        }
    }

    private fun populateMap(mapPoints : List<MapPoint>) {
        for (mapPoint in mapPoints) {
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(mapPoint.position)
                    .title(mapPoint.name)
                    .snippet(mapPoint.id)
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
                val markerId = routeCreationManager.generateMarkerId()
                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(googleMap.projection.visibleRegion.latLngBounds.center)
                        .draggable(true)
                        .snippet(markerId)
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

        if (!inCreationMode) {
            val inflater = LayoutInflater.from(requireContext())
            val dialogBinding = DialogRouteTitleBinding.inflate(inflater)


            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Enter the route title")
                .setView(dialogBinding.root)
                .setPositiveButton("OK") { dialog, _ ->
                    val title = dialogBinding.editText.text.toString().trim()
                    routeCreationManager.startNew(title)
                    inCreationMode = true
                    googleMap.setOnMarkerDragListener(routeCreationManager)
                    clearMap()
                    slidingUpPanelManager.toggleCreation()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

        } else {
            routeCreationManager.clearCreationMarkers()
            googleMap.setOnMarkerDragListener(null)
            slidingUpPanelManager.toggleCreation()
            inCreationMode = false
        }
    }


    override fun onMarkerClick(marker: Marker): Boolean {
        if (inCreationMode) {
            infoWindowManager.toggle(routeCreationManager.getInfoWindow(marker.snippet!!))
        } else {
            binding.slideUpPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }
        return true
    }

    private fun handleBackPress() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.slideUpPanel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED){
                    binding.slideUpPanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                }else{
                    isEnabled = false
                    activity?.onBackPressed()
                }
            }
        })
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
        //seekbarManager.removeCallback()
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



}
