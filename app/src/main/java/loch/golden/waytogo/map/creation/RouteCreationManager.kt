package loch.golden.waytogo.map.creation

import android.content.Context
import android.widget.Toast
import com.appolica.interactiveinfowindow.InfoWindow
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.model.Marker

class RouteCreationManager(private val context: Context) : OnMarkerDragListener {
    private val infoWindowMap: MutableMap<String, InfoWindow> = mutableMapOf()
    private val creationMarkerMap: MutableMap<String, Marker?> = mutableMapOf()
    private var markerId = 1


    fun addMarker(marker: Marker?, infoWindow: InfoWindow) {
        val id = marker?.snippet!!
        creationMarkerMap[id] = marker
        infoWindowMap[id] = infoWindow
    }

    fun removeMarker(marker: Marker?){
        val id = marker?.snippet!!
        creationMarkerMap.remove(id)
        infoWindowMap[id]?.windowState = InfoWindow.State.HIDDEN
        marker.remove()
    }

    fun getCurrentId() = markerId++

    fun getMarker(id: String) = creationMarkerMap[id]
    fun getInfoWindow(id: String) = infoWindowMap[id]!!

    override fun onMarkerDrag(marker: Marker) {}

    override fun onMarkerDragEnd(marker: Marker) {
        Toast.makeText(context, "OnMarkerDragEnd", Toast.LENGTH_SHORT).show()
        val id = marker.snippet!!
        infoWindowMap[id]!!.position = marker.position
    }

    override fun onMarkerDragStart(marker: Marker) {}

}