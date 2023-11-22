package loch.golden.waytogo.map

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

class MapViewModel : ViewModel() {
    var cameraPosition: CameraPosition? = null
    var markerList : List<LatLng> = listOf(
        LatLng(54.3520, 18.6466),
        LatLng(34.3520, 0.6466),
        LatLng(18.3520, 36.6466)
    )
}