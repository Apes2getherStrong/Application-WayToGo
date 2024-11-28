package loch.golden.waytogo.access.services.dto.routemaplocation

import com.google.gson.annotations.SerializedName
import loch.golden.waytogo.access.services.dto.maplocation.MapLocationRequest
import loch.golden.waytogo.data.room.entity.route.Route

data class RouteMapLocationRequest(
    val id: String,
    @SerializedName("mapLocation")
    val mapLocationRequest: MapLocationRequest,
    val route: Route,
    val sequenceNr: Int)
