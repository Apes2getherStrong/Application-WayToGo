package loch.golden.waytogo.fragments.map.components.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    private val _currentLocation = MutableLiveData<LatLng>()

    // Expose a LiveData variable to observe
    val currentLocation: LiveData<LatLng> get() = _currentLocation

    init {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
            setWaitForAccurateLocation(true)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setMinUpdateDistanceMeters(0.5f)
        }.build()
    }

    override fun onLocationResult(location: LocationResult) {
        _currentLocation.value =
            LatLng(location.lastLocation!!.latitude, location.lastLocation!!.longitude)

    }

    override fun onLocationAvailability(p0: LocationAvailability) {
        Log.d("Warmbier", "On Location availability")
    }

    fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, this, null)
    }
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(this)
    }

    fun awaitMyLocation(): LatLng {
        while (currentLocation.value == null)
            Thread.sleep(100)
        return currentLocation.value!!
    }

}