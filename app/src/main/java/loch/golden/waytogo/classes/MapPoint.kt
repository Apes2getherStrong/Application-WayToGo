package loch.golden.waytogo.classes

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import loch.golden.waytogo.routes.model.maplocation.MapLocation
import loch.golden.waytogo.routes.utils.Constants
import java.io.File

data class MapPoint(
    val id: String,
    var name: String,
    var description: String?,
    var position: LatLng,
    var audioPath: String? = null,
    var photoPath: String? = null,
) {

    constructor(mapLocation: MapLocation) :
            this(
                mapLocation.id,
                mapLocation.name,
                mapLocation.description,
                LatLng(
                    mapLocation.latitude,
                    mapLocation.longitude
                )
            )

    constructor(mapLocation: MapLocation, context: Context) :
            this(
                mapLocation.id,
                mapLocation.name,
                mapLocation.description,
                LatLng(
                    mapLocation.latitude,
                    mapLocation.longitude
                )
            ) {
        val imageFilePath = "${Constants.IMAGE_DIR}/$id${Constants.IMAGE_EXTENSION}"
        val imageFile = File(context.filesDir, imageFilePath)
        if (imageFile.exists())
            photoPath = imageFile.absolutePath

        val audioFilePath = "${Constants.AUDIO_DIR}/$id${Constants.AUDIO_EXTENSION}"
        val audioFile = File(context.filesDir, audioFilePath)
        if (audioFile.exists())
            audioPath = audioFile.absolutePath
    }

     constructor(mapLocationRequest: MapLocationRequest) :
            this(
                mapLocationRequest.id,
                mapLocationRequest.name,
                mapLocationRequest.description,
                LatLng(
                    mapLocationRequest.coordinates.coordinates[0],
                    mapLocationRequest.coordinates.coordinates[1]
                ),
                null,
                null
            )
}