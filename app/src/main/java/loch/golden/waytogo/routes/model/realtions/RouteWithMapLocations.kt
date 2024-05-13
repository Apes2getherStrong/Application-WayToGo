package loch.golden.waytogo.routes.model.realtions

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import loch.golden.waytogo.routes.model.maplocation.MapLocation
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocation

data class RouteWithMapLocations(
    @Embedded val route: Route,
    @Relation(
        parentColumn = "route_uid",
        entityColumn = "id",
        associateBy = Junction(RouteMapLocation::class)
    )
    val mapLocations: List<MapLocation>
)
