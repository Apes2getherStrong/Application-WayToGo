package loch.golden.waytogo.map

import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition
import loch.golden.waytogo.classes.MapPoint
import loch.golden.waytogo.classes.MapRoute
import loch.golden.waytogo.map.components.LocationManager

class MapViewModel : ViewModel() {
    var cameraPosition: CameraPosition? = null
    var route: MapRoute? = null
    var inCreationMode = false
    var mp: MediaPlayer? = null

    var currentSequenceNr = 1
    var currentPoint: MapPoint? = null

    var locationManager: LocationManager? = null


    fun updateCurrentSequenceNr(sequenceNr: Int): Boolean {
        if (sequenceNr > route!!.pointList.size)
            return false
        this.currentSequenceNr = sequenceNr
        this.currentPoint = route!!.pointList.values.find { it.sequenceNr == sequenceNr }
        return true
    }
}