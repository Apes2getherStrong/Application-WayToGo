package loch.golden.waytogo.routes.api

import loch.golden.waytogo.routes.model.MapLocation
import loch.golden.waytogo.routes.model.MapLocationListResponse
import loch.golden.waytogo.routes.model.Route
import loch.golden.waytogo.routes.model.RouteListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("routes")
    suspend fun getRoutes(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Response<RouteListResponse>

    @GET("routes/{routeId}")
    suspend fun getRouteById(
        @Path("routeId") routeId: String
    ): Response<Route>

    @GET("routes/{routeId}/mapLocations")
    suspend fun getMapLocationsByRouteId(
        @Path("routeId") routeId : String
    ): Response<MapLocationListResponse>
}