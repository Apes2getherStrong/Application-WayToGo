package loch.golden.waytogo.map

import android.annotation.SuppressLint
import android.location.Location
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener
import loch.golden.waytogo.IOnBackPressed
import loch.golden.waytogo.R
import loch.golden.waytogo.databinding.FragmentPointMapBinding
import java.lang.Exception

class PointMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
    PanelSlideListener, OnMarkerClickListener, IOnBackPressed {

    companion object {
        private const val CAMERA_POSITION_KEY = "camera_position"
        private const val MAP_BUNDLE_KEY = "map_state"
    }

    //viewmodel tied to parent activity - MainActivity
    private val mapViewModel by activityViewModels<MapViewModel>()
    private lateinit var binding: FragmentPointMapBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var mapMenuManager: MapMenuManager

    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }
    private val seekbarRunnable by lazy {
        object : Runnable {
            val percentList = mutableListOf<Float>()
            override fun run() {
                try {
                    val currentPosition = mapViewModel.mp!!.currentPosition.toFloat()
                    val duration = mapViewModel.mp!!.duration.toFloat()

                    // Update marker details seekbar
                    binding.expandedPanelSeekbar.progress = currentPosition.toInt()

                    // Update bottom custom seekbar
                    val trackPercentage = if (duration != 0.0f) currentPosition / duration else 0.0f
                    Log.d(
                        "trackCheck",
                        "track position $currentPosition track percent ${
                            String.format(
                                "%.2f",
                                trackPercentage * 100
                            )
                        }%"
                    )
                    percentList.add(trackPercentage)
                    val layoutParams = binding.bottomPanelCustomSeekbarProgress.layoutParams
                    layoutParams.width =
                        (resources.displayMetrics.widthPixels * trackPercentage).toInt()
                    binding.bottomPanelCustomSeekbarProgress.layoutParams = layoutParams


                    handler.postDelayed(this, 50)
                } catch (e: Exception) {
                    Log.d("Warmbier", e.toString())
                }
            }

        }
    }
    //TODO Move the logic to view model only leave updating the visuals

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPointMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMapView(savedInstanceState)
        setUpListeners()
        setUpSlidingUpPanel()
        locationManager = LocationManager(requireContext(), 600, 5.0f)
        locationManager.startLocationTracking()
        mapMenuManager = MapMenuManager(
            requireContext(),
            binding.mapMenu.fabMenu,
            arrayListOf(binding.mapMenu.addRouteFab, binding.mapMenu.stylesFab)
        )
        setUpMediaPlayer(R.raw.piosenka)
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

        //this skips if camera position is null
        mapViewModel.cameraPosition?.let {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(it))
        }

        googleMap.setOnCameraMoveListener {
            mapViewModel.cameraPosition = googleMap.cameraPosition
        }
        googleMap.isMyLocationEnabled = true


        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(locationManager.getLatLng()!!))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(7.7749, -122.4194)))
        googleMap.setInfoWindowAdapter(PointInfoWindowAdapter(requireContext()))
        googleMap.setOnInfoWindowClickListener(this)
        googleMap.setOnMarkerClickListener(this)
        populateMap()
    }

    private fun populateMap() {
        for ((index, latlng) in mapViewModel.markerList.withIndex()) {
            googleMap.addMarker(
                MarkerOptions()
                    .position(latlng)
                    .title("place $index")
            )
        }
    }


    private fun setUpListeners() {
        binding.bottomPanelPlayButton.setOnClickListener {
            Toast.makeText(requireContext(), "Siema", Toast.LENGTH_SHORT).show()
        }
        binding.mapMenu.addRouteFab.setOnClickListener() {
            Toast.makeText(requireContext(), "Add Route", Toast.LENGTH_SHORT).show()
        }
        binding.mapMenu.stylesFab.setOnClickListener() {
            Toast.makeText(requireContext(), "Styles", Toast.LENGTH_SHORT).show()
        }
//        binding.buttonCenterPos.setOnClickListener() {
//            locationManager.getLatLng()?.let {
//                googleMap.moveCamera(CameraUpdateFactory.newLatLng(it))
//                Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    private fun togglePlayPauseIcons() {
        if (mapViewModel.mp!!.isPlaying) {

            binding.bottomPanelPlayButton.setImageResource(R.drawable.ic_pause_24)
            binding.expandedPanelPlayFab.setImageResource(R.drawable.ic_pause_24)
        } else {
            binding.bottomPanelPlayButton.setImageResource(R.drawable.ic_play_arrow_24)
            binding.expandedPanelPlayFab.setImageResource(R.drawable.ic_play_arrow_24)
        }
    }

    private fun setUpMediaPlayer(trackId: Int) {
        initSeekBar()
        if (mapViewModel.mp != null)
            togglePlayPauseIcons()
        val playButtonClickListener = View.OnClickListener {
            if (mapViewModel.mp == null) {
                mapViewModel.mp = MediaPlayer.create(requireContext(), trackId)
                mapViewModel.mp?.start()
            } else if (mapViewModel.mp!!.isPlaying) mapViewModel.mp?.pause()
            else mapViewModel.mp?.start()
            togglePlayPauseIcons()
        }

        binding.bottomPanelPlayButton.setOnClickListener(playButtonClickListener)
        binding.expandedPanelPlayFab.setOnClickListener(playButtonClickListener)

    }

    private fun initSeekBar() {
        if (mapViewModel.mp == null)
            return
        binding.expandedPanelSeekbar.max = mapViewModel.mp!!.duration
        Log.d("lifecycleAlert", "initseekbar")
        handler.postDelayed(seekbarRunnable, 0)
        binding.expandedPanelSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mapViewModel.mp?.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekbar: SeekBar?) {
                // when start tracking
            }

            override fun onStopTrackingTouch(seekbar: SeekBar?) {
                //when end tracking
            }
        })

        mapViewModel.mp!!.setOnCompletionListener {
            Log.d("Warmbier", "MEDIA PLAYER HAS FINISHED")
            mapViewModel.mp?.seekTo(0)
            togglePlayPauseIcons()
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        binding.slideUpPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        return true
    }

    override fun onInfoWindowClick(marker: Marker) {
//        marker.hideInfoWindow()
//        binding.slideUpPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
    }

    override fun onPanelSlide(panel: View?, slideOffset: Float) {
        val maxVisibilitySlideOffset = 0.2f
        binding.bottomPanelContainer.alpha =
            1.0f - (slideOffset / maxVisibilitySlideOffset).coerceIn(0.0f, 1.0f)
        binding.expandedPanelContainer.alpha =
            (slideOffset / maxVisibilitySlideOffset).coerceIn(0.0f, 1.0f)
    }

    override fun onPanelStateChanged(
        panel: View?,
        previousState: SlidingUpPanelLayout.PanelState?,
        newState: SlidingUpPanelLayout.PanelState?
    ) {
        binding.bottomPanelPlayButton.isClickable =
            (binding.slideUpPanel.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED)
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
        handler.removeCallbacks(seekbarRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LifecycleAlert", "onDestroy")
        binding.mapView.onDestroy()
    }

    //TODO
    //center the titles
    //balance the opacity when expanding panels
    //decide between infoWindow approach and onMarkerClick approach
    //audio still behaves werid at the start
    //move Expanded panel to seperate class and View

}
