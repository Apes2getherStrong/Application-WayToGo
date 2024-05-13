package loch.golden.waytogo.classes

import com.google.android.gms.maps.model.LatLng
import loch.golden.waytogo.routes.model.maplocation.MapLocation

data class MapPoint(
    val id: String,
    val name: String,
    val description: String?,
    val position: LatLng,
    val audioPath: String?,
    val photoPath: String?

) {
    constructor(mapLocation: MapLocation) :
            this(
                mapLocation.id,
                mapLocation.name,
                mapLocation.description,
                LatLng(
                    mapLocation.latitude,
                    mapLocation.longitude
                ),
                null,
                null
            )

}