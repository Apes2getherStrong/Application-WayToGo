package loch.golden.waytogo.map

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition
import loch.golden.waytogo.classes.MapRoute

class MapViewModel : ViewModel() {
    var cameraPosition: CameraPosition? = null
    var route: MapRoute? = null
    var inCreationMode = false

}