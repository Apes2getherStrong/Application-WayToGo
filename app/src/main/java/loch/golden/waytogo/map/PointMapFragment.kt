package loch.golden.waytogo.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import loch.golden.waytogo.R
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
import loch.golden.waytogo.map.navigation.NavigationManager
import loch.golden.waytogo.routes.RouteMainApplication
import loch.golden.waytogo.routes.RoutesFragment
import loch.golden.waytogo.routes.utils.Constants
import loch.golden.waytogo.routes.viewmodel.RouteViewModel
import loch.golden.waytogo.routes.viewmodel.RouteViewModelFactory
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class PointMapFragment() : Fragment(), OnMapReadyCallback,
    OnMarkerClickListener, GoogleMap.OnCameraMoveListener {

    //viewmodel tied to parent activity - MainActivity
    private lateinit var mapViewModel: MapViewModel
    private lateinit var binding: FragmentMapBinding
    private lateinit var googleMap: GoogleMap
    private val googleMapSetup = CompletableDeferred<Unit>()

    private var seekbarManager: SeekbarManagerV2? = null
    private lateinit var slidingUpPanelManager: SlidingUpPanelManager


    private var infoWindowManager: InfoWindowManager? = null
    private var routeCreationManager: RouteCreationManager? = null

    private var navigationManager: NavigationManager? = null
    private var currentPolyline: Polyline? = null

    private val markerList: MutableList<Marker?> = mutableListOf()

    private val routeViewModel: RouteViewModel by viewModels {
        RouteViewModelFactory((requireActivity().application as RouteMainApplication).repository)
    }

    private var changeFragmentListener: OnChangeFragmentListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnChangeFragmentListener) {
            changeFragmentListener = context
        } else {
            throw RuntimeException("$context must implement OnNavigateToMapListener")
        }
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
        if (mapViewModel.locationManager == null) {
            mapViewModel.locationManager = LocationManager(requireContext())
            mapViewModel.locationManager!!.startLocationUpdates() //todo move this to activity
        }

        initMapView(savedInstanceState)

        slidingUpPanelManager = SlidingUpPanelManager(binding, mapViewModel)
        setUpListeners()


        if (mapViewModel.route != null) {
            binding.bottomPanel.title.visibility = View.VISIBLE
            binding.bottomPanel.playButton.visibility = View.VISIBLE
            binding.bottomPanel.buttonChooseRoute.visibility = View.GONE
            if (mapViewModel.inCreationMode) {
                initCreation(savedInstanceState)
            } else {
                seekbarManager = SeekbarManagerV2(
                    mapViewModel,
                    binding.expandedPanel.seekbar,
                    listOf(binding.bottomPanel.playButton, binding.expandedPanel.normalPlayPause)
                )
                seekbarManager?.setCustomSeekbar(binding.bottomPanel.customSeekbarProgress, requireContext())
                binding.bottomPanel.title.text = mapViewModel.currentPoint?.name
                seekbarManager?.prepareAudio(mapViewModel.currentPoint!!.audioPath!!)
                navigationManager = NavigationManager(mapViewModel)
                observeLocation()
            }
        } else {
            binding.bottomPanel.title.visibility = View.GONE
            binding.bottomPanel.playButton.visibility = View.GONE
            binding.bottomPanel.buttonChooseRoute.visibility = View.VISIBLE

        }


    }

    private fun setUpListeners() {
        binding.bottomPanel.buttonChooseRoute.setOnClickListener {
            parentFragmentManager.commit {
                changeFragmentListener?.changeFragment(2)
            }
        }
        binding.bottomPanel.creationReturnToEdit.setOnClickListener {
            val bundle = Bundle().apply {
                putString("id", mapViewModel.route?.id)
            }
            parentFragmentManager.commit {
                changeFragmentListener?.changeFragment(2, bundle)
            }
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
            lifecycleScope.launch(Dispatchers.IO) {
                val myLocation = mapViewModel.locationManager!!.awaitMyLocation()
                withContext(Dispatchers.Main) {
                    val cameraPosition = CameraPosition.builder()
                        .target(myLocation)
                        .zoom(15.0f)
                        .build()
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            }
        }

        if (!mapViewModel.inCreationMode) {
            createPolylineToPoint()
        }


        googleMapSetup.complete(Unit)
    }

    private fun createPolylineToPoint() {
        lifecycleScope.launch {
            if (mapViewModel.route != null) {
                val myLocation = mapViewModel.locationManager!!.awaitMyLocation()
                try {
                    val polyPoints = navigationManager!!.getPolyline(myLocation, mapViewModel.currentPoint!!.position)
                    val polylineOptions = PolylineOptions().apply {
                        polyPoints.forEach() { polyPoint ->
                            add(polyPoint)
                        }
                        color(Color.BLUE)
                    }
                    val newPolyline = googleMap.addPolyline(polylineOptions)
                    currentPolyline?.remove()
                    currentPolyline = newPolyline
                } catch (e: Exception) {
                    e.printStackTrace().toString()
                    Toast.makeText(requireContext(), "Can't navigate to point", Toast.LENGTH_SHORT).show()
                }

            }
        }
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

    private fun initCreation(savedInstanceState: Bundle?) {
        infoWindowManager = InfoWindowManager(childFragmentManager)
        infoWindowManager?.setHideOnFling(true)
        infoWindowManager?.onParentViewCreated(binding.mapViewContainer, savedInstanceState)

        routeCreationManager =
            RouteCreationManager(binding, infoWindowManager!!, this, routeViewModel, mapViewModel)

        lifecycleScope.launch { // wait till googlemaps is initialized
            googleMapSetup.await()
            googleMap.setOnMarkerDragListener(routeCreationManager)
            routeCreationManager?.startExisting(mapViewModel.route!!.id, markerList)
        }

        slidingUpPanelManager.toggleCreation()

        binding.bottomPanel.creation.visibility = View.VISIBLE
        binding.bottomPanel.buttonAddMarker.setOnClickListener {
            if (mapViewModel.inCreationMode) {
                val markerId = routeCreationManager?.generateMarkerId()
                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(googleMap.projection.visibleRegion.latLngBounds.center)
                        .draggable(true)
                        .snippet(markerId)
                        .title("Point ${mapViewModel.route!!.pointList.size}")
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

        binding.bottomPanel.creationHelpButton.setOnClickListener {
            Snackbar.make(binding.root, "To move a point hold and drag it", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun observeLocation() {
        mapViewModel.locationManager!!.currentLocation.observe(viewLifecycleOwner) { newValue ->
            Log.d("Warmbier", "New location: $newValue")
            val distance = calculateDistance(
                newValue.latitude,
                newValue.longitude,
                mapViewModel.currentPoint!!.position.latitude,
                mapViewModel.currentPoint!!.position.longitude,
            )
//            Log.d("Warmbier", distance.toString())
            if (distance <= Constants.AUTOPLAY_DISTANCE) {
                currentPolyline?.remove()
                currentPolyline = null
                seekbarManager?.prepareAudio(mapViewModel.currentPoint!!.audioPath!!)
                seekbarManager?.setOnCompletionListener {
                    if (mapViewModel.updateCurrentSequenceNr(mapViewModel.currentSequenceNr + 1)) {
                        createPolylineToPoint()
                        seekbarManager?.prepareAudio(mapViewModel.currentPoint!!.audioPath!!)
                        slidingUpPanelManager.updateBottomPanel(mapViewModel.currentPoint)
                    } else
                        Log.d("Warmbier", "FINISH THE ROUTE")
                    Log.d("Warmbier", "The completion")
                }
                seekbarManager?.resumeAudio()
            } else {
                createPolylineToPoint()
            }


        }
    }

    private fun calculateDistance(lat_a: Double, lng_a: Double, lat_b: Double, lng_b: Double): Float {
        val earthRadius = 3958.75
        val latDiff = Math.toRadians((lat_b - lat_a))
        val lngDiff = Math.toRadians((lng_b - lng_a))
        val a =
            sin(latDiff / 2) * sin(latDiff / 2) + cos(Math.toRadians(lat_a)) * cos(
                Math.toRadians(lat_b)
            ) * sin(lngDiff / 2) * sin(lngDiff / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadius * c

        val meterConversion = 1609

        return (distance * meterConversion).toFloat()
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
        if (mapPoint == mapViewModel.currentPoint) {
            slidingUpPanelManager.openNormalPanel(mapPoint)
            if (mapPoint!!.audioPath != null)
                seekbarManager?.prepareAudio(mapPoint.audioPath!!)
        } else {
            slidingUpPanelManager.openDifferentPanel(mapPoint)
            binding.expandedPanel.buttonSelectMarker.setOnClickListener {
                mapViewModel.updateCurrentSequenceNr(mapPoint!!.sequenceNr)
                createPolylineToPoint()
                binding.expandedPanel.buttonSelectMarker.visibility = View.GONE
                binding.expandedPanel.seekbar.visibility = View.VISIBLE
                binding.expandedPanel.normalPlayPause.visibility = View.VISIBLE
                binding.bottomPanel.title.text = mapPoint.name
                val bitmap = BitmapFactory.decodeFile(mapViewModel.route!!.pointList[mapPoint.id]?.photoPath)
                if (bitmap != null) binding.expandedPanel.image.setImageBitmap(bitmap)
                else binding.expandedPanel.image.setImageResource(R.drawable.ic_no_photo_24)
                if (mapPoint.audioPath != null)
                    seekbarManager?.prepareAudio(mapPoint.audioPath!!)

                seekbarManager?.setOnCompletionListener {
                    if (mapViewModel.updateCurrentSequenceNr(mapPoint.sequenceNr + 1))
                        createPolylineToPoint()
                    else
                        Log.d("Warmbier", "FINISH THE ROUTE")
                    Log.d("Warmbier", "The completion")
                }
            }

        }
    }

    override fun onCameraMove() {
        mapViewModel.cameraPosition = googleMap.cameraPosition
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
