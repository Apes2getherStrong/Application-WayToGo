package loch.golden.waytogo.logic.viewmodels.classes

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import loch.golden.waytogo.access.services.dto.maplocation.MapLocationRequest
import loch.golden.waytogo.data.room.entity.maplocation.MapLocation
import loch.golden.waytogo.utils.Constants
import java.io.File


data class MapPoint(
    val id: String,
    var name: String,
    var description: String?,
    var position: LatLng,
    var sequenceNr : Int,
    var audioPath: String? = null,
    var photoPath: String? = null,

) {

    constructor(mapLocation: MapLocation, sequenceNr: Int) :
            this(
                mapLocation.id,
                mapLocation.name,
                mapLocation.description,
                LatLng(
                    mapLocation.latitude,
                    mapLocation.longitude
                ),
                sequenceNr
            )

    constructor(mapLocation: MapLocation, sequenceNr: Int, context: Context) :
            this(
                mapLocation.id,
                mapLocation.name,
                mapLocation.description,
                LatLng(
                    mapLocation.latitude,
                    mapLocation.longitude
                ),
                sequenceNr
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

    constructor(mapLocationRequest: MapLocationRequest, sequenceNr: Int) :
            this(
                mapLocationRequest.id,
                mapLocationRequest.name,
                mapLocationRequest.description,
                LatLng(
                    mapLocationRequest.coordinates.coordinates[0],
                    mapLocationRequest.coordinates.coordinates[1]
                ),
                sequenceNr,
                null,
                null
            )
}