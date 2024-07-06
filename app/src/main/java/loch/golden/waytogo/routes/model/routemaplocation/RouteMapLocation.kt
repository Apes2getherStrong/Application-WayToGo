package loch.golden.waytogo.routes.model.routemaplocation

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "route_map_location",
    primaryKeys = ["route_uid", "id"]
)
data class RouteMapLocation(
    @ColumnInfo("route_uid")
    val routeUid: String,
    val id: String,
    val sequenceNr: Int
)
