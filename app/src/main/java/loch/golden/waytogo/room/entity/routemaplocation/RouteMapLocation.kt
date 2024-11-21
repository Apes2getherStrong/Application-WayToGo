package loch.golden.waytogo.room.entity.routemaplocation

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.Expose

@Entity(
    tableName = "route_map_location",
    primaryKeys = ["route_uid", "id"]
)
data class RouteMapLocation(
    @ColumnInfo("route_uid")
    val routeUid: String,
    val id: String,
    val sequenceNr: Int,
    @Expose(serialize = false, deserialize = true) var externalId: String?
)
