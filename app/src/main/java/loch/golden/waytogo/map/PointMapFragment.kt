package loch.golden.waytogo.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.*
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import loch.golden.waytogo.databinding.FragmentPointMapBinding


class PointMapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentPointMapBinding? = null
    private val binding get() =_binding!!
    private lateinit var googleMap: GoogleMap
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPointMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMapView(savedInstanceState)
        setUpClickListeners()

    }

    private fun setUpClickListeners() {
        binding.buttonAddPoint.setOnClickListener { buttonAddPointListener() }
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

    private fun setUpMapView(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        //binding.mapView.onResume() needed to get the map to display immediately from stack overflow - DOESNT SEEM TO BE TRUE

        try {
            MapsInitializer.initialize(requireActivity().applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.mapView.getMapAsync(this)
    }

    override fun onMapReady(mMap: GoogleMap) {
        googleMap=mMap
        val sydney = LatLng(-33.852, 151.211)
        googleMap.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney")
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }


    //region according to various sources needed for the map to load properly, works without it
    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }


    override fun onLowMemory() {
        super.onLowMemory()
        _binding?.mapView?.onLowMemory()
    }
    //endregion

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }
}
