package loch.golden.waytogo.map

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.appolica.interactiveinfowindow.InfoWindow
import com.appolica.interactiveinfowindow.InfoWindow.MarkerSpecification
import com.appolica.interactiveinfowindow.InfoWindowManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import loch.golden.waytogo.classes.MapPoint
import loch.golden.waytogo.classes.MapRoute
import loch.golden.waytogo.databinding.FragmentMapBinding
import loch.golden.waytogo.map.adapters.PointInfoWindowAdapter
import loch.golden.waytogo.map.components.LocationManager
import loch.golden.waytogo.map.components.MapMenuManager
import loch.golden.waytogo.map.components.SeekbarManagerV2
import loch.golden.waytogo.map.components.SlidingUpPanelManager
import loch.golden.waytogo.map.creation.MarkerCreationFragment
import loch.golden.waytogo.map.creation.RouteCreationManager
import loch.golden.waytogo.routes.RouteMainApplication
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory


class PointMapFragment(val currentRoute: MapRoute? = null) : Fragment(), OnMapReadyCallback,
    OnMarkerClickListener, GoogleMap.OnCameraMoveListener {

    //viewmodel tied to parent activity - MainActivity
    private lateinit var mapViewModel: MapViewModel
    private lateinit var binding: FragmentMapBinding
    private lateinit var googleMap: GoogleMap
    private val googleMapSetup = CompletableDeferred<Unit>()

    private lateinit var locationManager: LocationManager
    private lateinit var mapMenuManager: MapMenuManager

    private var seekbarManager: SeekbarManagerV2? = null
    private lateinit var slidingUpPanelManager: SlidingUpPanelManager


    private var infoWindowManager: InfoWindowManager? = null
    private var routeCreationManager: RouteCreationManager? = null

    private val markerList: MutableList<Marker?> = mutableListOf()

    private val routeViewModel: RouteViewModel by viewModels {
        RouteViewModelFactory((requireActivity().application as RouteMainApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        initMapView(savedInstanceState)

        setUpListeners()

        slidingUpPanelManager = SlidingUpPanelManager(binding, mapViewModel)
        locationManager = LocationManager(requireContext())
        locationManager.startLocationUpdates()




        if (mapViewModel.inCreationMode) {
            initCreation(savedInstanceState)
        } else {
            seekbarManager = SeekbarManagerV2(
                mapViewModel,
                binding.expandedPanel.seekbar,
                listOf(binding.bottomPanel.playButton, binding.expandedPanel.normalPlayPause)
            )
            seekbarManager?.setCustomSeekbar(binding.bottomPanel.customSeekbarProgress, requireContext())
        }

    }


    private fun initMapView(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(mMap: GoogleMap) {

        googleMap = mMap
        infoWindowManager?.onMapReady(googleMap)


        googleMap.isMyLocationEnabled = true
//        googleMap.uiSettings.isMyLocationButtonEnabled = false
        googleMap.setInfoWindowAdapter(PointInfoWindowAdapter(requireContext()))
        googleMap.setOnMarkerClickListener(this)
        googleMap.setOnCameraMoveListener(this)

        mapViewModel.route?.let {
            populateMap(it.pointList)
        }

        mapViewModel.cameraPosition?.let {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(mapViewModel.cameraPosition!!));
        } ?: run {
            //TODO change to first point
            val cameraPosition = CameraPosition.builder()
                .target(locationManager.getCurrentLocation() ?: LatLng(0.0, 0.0))
                .zoom(4.0f)
                .build()
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }


        googleMapSetup.complete(Unit)

    }

    private fun populateMap(mapPoints: Map<String, MapPoint>) {
        for ((id, mapPoint) in mapPoints) {
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
        binding.mapMenu.stylesFab.setOnClickListener {
            Toast.makeText(requireContext(), "Styles", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initCreation(savedInstanceState: Bundle?) {
        infoWindowManager = InfoWindowManager(childFragmentManager)
        infoWindowManager?.setHideOnFling(true)
        infoWindowManager?.onParentViewCreated(binding.mapViewContainer, savedInstanceState)

        routeCreationManager =
            RouteCreationManager(binding, infoWindowManager!!, this, routeViewModel, mapViewModel)
//        routeCreationManager?.startNew(mapViewModel.route!!.name)

        lifecycleScope.launch { // wait till googlemaps is initialized
            googleMapSetup.await()
            googleMap.setOnMarkerDragListener(routeCreationManager)
            routeCreationManager?.startExisting(mapViewModel.route!!.id, markerList)
        }

        slidingUpPanelManager.toggleCreation()


        binding.buttonAddMarker.setOnClickListener {
            if (mapViewModel.inCreationMode) {
                val markerId = routeCreationManager?.generateMarkerId()
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
                    MarkerCreationFragment(marker, routeCreationManager!!, binding)
                )
                routeCreationManager?.addMarker(
                    marker, infoWindow
                )
            }
        }
    }


    override fun onMarkerClick(marker: Marker): Boolean {
        if (mapViewModel.inCreationMode) {
            infoWindowManager?.toggle(routeCreationManager!!.getInfoWindow(marker.snippet!!))
        } else {
            openNormalPanel(mapViewModel.route?.pointList?.get(marker.snippet))
        }
        return true
    }

    private fun openNormalPanel(mapPoint: MapPoint?) {
        binding.expandedPanel.title.text = mapPoint?.name
        binding.bottomPanel.title.text = mapPoint?.name
        binding.expandedPanel.description.text = mapPoint?.description
        binding.slideUpPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        val bitmap = BitmapFactory.decodeFile(mapViewModel.route!!.pointList[mapPoint?.id]?.photoPath)
        binding.expandedPanel.image.setImageBitmap(bitmap)
        seekbarManager?.prepareAudio(mapViewModel.route!!.pointList[mapPoint?.id]?.audioPath!!)


//        binding.expandedPanel.image.setImageResource(mapPoint.image)
    }

    override fun onCameraMove() {
        mapViewModel.cameraPosition = googleMap.cameraPosition
    }

    private fun handleBackPress() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.slideUpPanel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                        binding.slideUpPanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                    } else {
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
        routeCreationManager?.onDestroy()
        infoWindowManager?.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }


}
