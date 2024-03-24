package loch.golden.waytogo.map.creation

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.appolica.interactiveinfowindow.InfoWindow
import com.appolica.interactiveinfowindow.InfoWindowManager
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.model.Marker
import loch.golden.waytogo.databinding.FragmentMapBinding

class RouteCreationManager(
    private val context: Context,
    private val infoWindowManager: InfoWindowManager
) :
    OnMarkerDragListener {
    private val infoWindowMap: MutableMap<String, InfoWindow> = mutableMapOf()
    private val creationMarkerMap: MutableMap<String, Marker?> = mutableMapOf()
    private var markerId = 1


    fun addMarker(marker: Marker?, infoWindow: InfoWindow) {
        val id = marker?.snippet!!
        creationMarkerMap[id] = marker
        infoWindowMap[id] = infoWindow
    }

    fun removeMarker(marker: Marker?) {
        val id = marker?.snippet!!
        creationMarkerMap.remove(id)
        hideInfoWindow(id)
        infoWindowMap.remove(id)
        marker.remove()
    }

    fun getCurrentId() = markerId++

    fun getMarker(id: String) = creationMarkerMap[id]
    fun getInfoWindow(id: String) = infoWindowMap[id]!!

    fun hideInfoWindow(id: String) {
        if (infoWindowMap[id]!!.windowState in setOf(InfoWindow.State.SHOWN, InfoWindow.State.SHOWING))
            infoWindowManager.toggle(infoWindowMap[id]!!)
    }

    override fun onMarkerDrag(marker: Marker) {
        val id = marker.snippet!!
        hideInfoWindow(id)
    }

    override fun onMarkerDragEnd(marker: Marker) {
        val id = marker.snippet!!
        infoWindowMap[id]!!.position = marker.position
    }

    override fun onMarkerDragStart(marker: Marker) {
        val id = marker.snippet!!
        hideInfoWindow(id)
    }

}