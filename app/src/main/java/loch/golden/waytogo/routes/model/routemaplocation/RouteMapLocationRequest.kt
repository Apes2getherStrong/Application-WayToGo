package loch.golden.waytogo.routes.model.routemaplocation

import com.google.gson.annotations.SerializedName
import loch.golden.waytogo.routes.model.maplocation.MapLocationRequest
import loch.golden.waytogo.routes.model.route.Route

data class RouteMapLocationRequest(
    val id: String,
    @SerializedName("mapLocation")
    val mapLocationRequest: MapLocationRequest,
    val route: Route,
    val sequenceNr: Int)
