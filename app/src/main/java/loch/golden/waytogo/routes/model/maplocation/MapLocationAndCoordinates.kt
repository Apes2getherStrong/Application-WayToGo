package loch.golden.waytogo.routes.model.maplocation

import androidx.room.Embedded
import androidx.room.Relation

data class MapLocationAndCoordinates(
    @Embedded val mapLocation: MapLocation,
    @Relation(
        parentColumn = "id",
        entityColumn = "coordId"
    )
    val coordinates: Coordinates
)
