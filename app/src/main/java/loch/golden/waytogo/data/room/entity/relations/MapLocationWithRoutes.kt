package loch.golden.waytogo.data.room.entity.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import loch.golden.waytogo.data.room.entity.maplocation.MapLocation
import loch.golden.waytogo.data.room.entity.route.Route
import loch.golden.waytogo.data.room.entity.routemaplocation.RouteMapLocation

data class MapLocationWithRoutes(
    @Embedded val mapLocation: MapLocation,
    @Relation(
        parentColumn = "id",
        entityColumn = "route_uid",
        associateBy = Junction(RouteMapLocation::class)
    )
    val routes: List<Route>
)