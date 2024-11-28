package loch.golden.waytogo.logic.viewmodels

import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import loch.golden.waytogo.logic.viewmodels.classes.MapPoint
import loch.golden.waytogo.logic.viewmodels.classes.MapRoute
import loch.golden.waytogo.presentation.fragments.map.components.navigation.LocationManager
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    val locationManager: LocationManager
) : ViewModel() {
    var cameraPosition: CameraPosition? = null
    var route: MapRoute? = null
    var inCreationMode = false
    var mp: MediaPlayer? = null

    var currentSequenceNr = 1
    var currentPoint: MapPoint? = null


    fun updateCurrentSequenceNr(sequenceNr: Int): Boolean {
        if (sequenceNr > route!!.pointList.size)
            return false
        this.currentSequenceNr = sequenceNr
        this.currentPoint = route!!.pointList.values.find { it.sequenceNr == sequenceNr }
        return true
    }
}