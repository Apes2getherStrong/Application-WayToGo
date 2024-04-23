package loch.golden.waytogo.classes

import android.media.MediaPlayer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

data class RoutePoint(
    val title : String,
    val position : LatLng,
    val audioPath: String,
    val photoPath: String

)