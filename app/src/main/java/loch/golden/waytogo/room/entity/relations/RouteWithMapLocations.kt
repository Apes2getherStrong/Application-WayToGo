package loch.golden.waytogo.room.entity.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import loch.golden.waytogo.room.entity.maplocation.MapLocation
import loch.golden.waytogo.room.entity.route.Route
import loch.golden.waytogo.room.entity.routemaplocation.RouteMapLocation

data class RouteWithMapLocations(
    @Embedded val route: Route,
    @Relation(
        parentColumn = "route_uid",
        entityColumn = "id",
        associateBy = Junction(RouteMapLocation::class)
    )
    val mapLocations: List<MapLocation>
)
