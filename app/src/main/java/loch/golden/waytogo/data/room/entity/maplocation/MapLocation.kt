package loch.golden.waytogo.data.room.entity.maplocation

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import loch.golden.waytogo.access.services.dto.maplocation.MapLocationRequest
import loch.golden.waytogo.logic.viewmodels.classes.MapPoint

@Entity(tableName = "map_location_table")
data class MapLocation(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    @Expose(serialize = false, deserialize = true) var externalId: String?
//    val createdDate: Any?,
//    val updateDate: Any?
){
    constructor(mapPoint: MapPoint) :this(
        mapPoint.id,
        mapPoint.name,
        mapPoint.description!!,
        mapPoint.position.latitude,
        mapPoint.position.longitude,
        null
    )

    constructor(mapLocationRequest: MapLocationRequest) :this(
        mapLocationRequest.id,
        mapLocationRequest.name,
        mapLocationRequest.description,
        mapLocationRequest.coordinates.coordinates[0],
        mapLocationRequest.coordinates.coordinates[1],
        null
    )
}