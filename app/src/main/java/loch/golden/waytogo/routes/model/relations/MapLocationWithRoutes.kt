package loch.golden.waytogo.routes.model.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import loch.golden.waytogo.routes.model.maplocation.MapLocation
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocation

data class MapLocationWithRoutes(
    @Embedded val mapLocation: MapLocation,
    @Relation(
        parentColumn = "id",
        entityColumn = "route_uid",
        associateBy = Junction(RouteMapLocation::class)
    )
    val routes: List<Route>
)
