package loch.golden.waytogo.map.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

@SuppressLint("MissingPermission")
class LocationManager(context: Context) : LocationCallback() {
    private val fusedLocationClient: FusedLocationProviderClient
    private val locationRequest: LocationRequest
    private var currentLocation: LatLng? = null

    init {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
            setWaitForAccurateLocation(true)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setMinUpdateDistanceMeters(0.5f)
        }.build()
    }

    override fun onLocationResult(location: LocationResult) {
        currentLocation =
            LatLng(location.lastLocation!!.latitude, location.lastLocation!!.longitude)
//        Log.d("Warmbier", "On location result: $currentLocation")

    }

    override fun onLocationAvailability(p0: LocationAvailability) {
        Log.d("Warmbier", "On Location availability")
    }

    fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, this, null)
    }

    fun stopLocationUpdates(){
        fusedLocationClient.removeLocationUpdates(this)
    }

}