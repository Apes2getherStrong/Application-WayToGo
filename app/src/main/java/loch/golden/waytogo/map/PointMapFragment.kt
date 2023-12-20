package loch.golden.waytogo.map

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
    PanelSlideListener,
    IOnBackPressed {

    companion object {
        private const val CAMERA_POSITION_KEY = "camera_position"
        private const val MAP_BUNDLE_KEY = "map_state"
    }

    //viewmodel tied to parent activity - MainActivity
    private val mapViewModel by activityViewModels<MapViewModel>()
    private lateinit var binding: FragmentPointMapBinding
    private lateinit var googleMap: GoogleMap

    //TODO move media player to viewmodel, it breaks when switching fragments
    private var mp: MediaPlayer? = null
    private var currentTrack = R.raw.piosenka
    private var markerDetailsWindowVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPointMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMapView(savedInstanceState)
        setUpListeners()
        setUpSlidingUpPanel()
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

    override fun onMapReady(mMap: GoogleMap) {
        googleMap = mMap

        //this skips if camera position is null
        mapViewModel.cameraPosition?.let {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(it))
        }

        googleMap.setOnCameraMoveListener {
            mapViewModel.cameraPosition = googleMap.cameraPosition
        }
        googleMap.setInfoWindowAdapter(PointInfoWindowAdapter(requireContext()))
        googleMap.setOnInfoWindowClickListener(this)
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

    override fun onInfoWindowClick(marker: Marker) {
        //showMarkerDetailsWindow()
    }

    private fun setUpListeners() {
        binding.bottomPanelPlayButton.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Siema",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun setUpMediaPlayer(trackId: Int) {
        val playButtonClickListener = View.OnClickListener {
            if (mp == null) {
                mp = MediaPlayer.create(requireContext(), trackId)
                initSeekBar()
                mp?.start()
            } else if (mp!!.isPlaying)
                mp?.pause()
            else
                mp?.start()
            //TODO CHANGE THE ICON ON THE BUTTON
        }
        binding.bottomPanelPlayButton.setOnClickListener(playButtonClickListener)
        binding.expandedPanelPlayFab.setOnClickListener(playButtonClickListener)

    }

    private fun initSeekBar() {
        binding.expandedPanelSeekbar.max = mp!!.duration

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    val currentPosition = mp!!.currentPosition.toDouble()
                    val duration = mp!!.duration.toDouble()

                    // Update marker details seekbar
                    binding.expandedPanelSeekbar.progress = currentPosition.toInt()

                    // Update bottom custom seekbar
                    val trackPercentage = if (duration != 0.0) currentPosition / duration else 0.0
                    Log.d(
                        "trackCheck",
                        "track position $currentPosition track percent $trackPercentage"
                    )
                    val layoutParams = binding.bottomPanelCustomSeekbarProgress.layoutParams
                    layoutParams.width =
                        (resources.displayMetrics.widthPixels * trackPercentage).toInt()
                    binding.bottomPanelCustomSeekbarProgress.layoutParams = layoutParams


                    handler.postDelayed(this, 1000)
                } catch (e: Exception) {
                    Log.d("Warmbier", e.toString())
                }
            }
        }, 0)
        binding.expandedPanelSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    mp?.seekTo(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                // when start tracking
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                //when end tracking
            }
        })

        mp!!.setOnCompletionListener {
            Log.d("Warmbier", "MEDIA PLAYER HAS FINISHED")
            //TODO do something when the song ends
        }
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
        } else
            false
    }

    //Forwarding map functions
    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }


}
