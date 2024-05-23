package loch.golden.waytogo.routes.model.maplocation

import androidx.room.Entity
import androidx.room.PrimaryKey
import loch.golden.waytogo.classes.MapPoint

@Entity(tableName = "map_location_table")
data class MapLocation(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
//    val createdDate: Any?,
//    val updateDate: Any?
){
    constructor(mapPoint: MapPoint) :this(
        mapPoint.id,
        mapPoint.name,
        mapPoint.description!!,
        mapPoint.position.latitude,
        mapPoint.position.longitude,
    )

    constructor(mapLocationRequest: MapLocationRequest) :this(
        mapLocationRequest.id,
        mapLocationRequest.name,
        mapLocationRequest.description,
        mapLocationRequest.coordinates.coordinates[0],
        mapLocationRequest.coordinates.coordinates[1]
    )
}