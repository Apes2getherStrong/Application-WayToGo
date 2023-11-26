package loch.golden.waytogo.map

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import loch.golden.waytogo.IOnBackPressed
import loch.golden.waytogo.R
import loch.golden.waytogo.databinding.FragmentPointMapBinding
import java.lang.Exception


class PointMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
    IOnBackPressed {

    companion object {
        private const val CAMERA_POSITION_KEY = "camera_position"
        private const val MAP_BUNDLE_KEY = "map_state"
    }

    //viewmodel tied to parent activity - MainActivity
    private val mapViewModel by activityViewModels<MapViewModel>()
    private lateinit var binding: FragmentPointMapBinding
    private lateinit var googleMap: GoogleMap

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
        showMarkerDetailsWindow()
    }

    private fun setUpListeners() {
        binding.buttonAddPoint.setOnClickListener { buttonAddPointListener() }
        binding.markerDetailsWindowButtonBack.setOnClickListener { hideMarkerDetailsWindow() }

    }

    private fun showMarkerDetailsWindow() {
        markerDetailsWindowVisible = true
        binding.markerDetailsWindowContainer.visibility = View.VISIBLE
        binding.mapView.alpha = 0.5f
        setUpMediaPlayer(currentTrack)
        //TODO lock the map maybe add listener that disables this
    }

    private fun setUpMediaPlayer(trackId: Int) {
        binding.markerDetailsWindowFabPlay.setOnClickListener {
            if (mp == null) {
                mp = MediaPlayer.create(requireContext(), trackId)
                initSeekBar()
            }
            mp?.start()
        }

        binding.markerDetailsWindowSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    mp?.seekTo(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                TODO("Not yet implemented")
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun initSeekBar() {
        binding.markerDetailsWindowSeekbar.max = mp!!.duration

        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    binding.markerDetailsWindowSeekbar.progress = mp!!.currentPosition
                    handler.postDelayed(this, 1000)
                } catch (e: Exception) {
                    binding.markerDetailsWindowSeekbar.progress = 0
                }
            }
        }, 0)
    }


    private fun hideMarkerDetailsWindow() {
        markerDetailsWindowVisible = false
        binding.markerDetailsWindowContainer.visibility = View.GONE
        binding.mapView.alpha = 1.0f
        //TODO make the audio bar slide to the bottom of the map
    }

    private fun buttonAddPointListener() {
        val gdansk = LatLng(54.3520, 18.6466)
        googleMap.addMarker(
            MarkerOptions()
                .position(gdansk)
                .title("gdansk")
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(gdansk))
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

    fun OnBackPressed() {
        if (markerDetailsWindowVisible)
            markerDetailsWindowVisible = false
    }

    override fun onBackPressed(): Boolean {
        return if (markerDetailsWindowVisible) {
            hideMarkerDetailsWindow()
            true
        } else
            false
    }


}
