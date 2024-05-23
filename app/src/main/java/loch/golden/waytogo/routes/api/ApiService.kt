package loch.golden.waytogo.routes.api

import loch.golden.waytogo.routes.model.maplocation.MapLocation
import loch.golden.waytogo.routes.model.maplocation.MapLocationListResponse
import loch.golden.waytogo.routes.model.maplocation.MapLocationRequest
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.model.route.RouteListResponse
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocation
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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

    @GET("routes/{routeId}/image")
    suspend fun getRouteImage(
        @Path("routeId") routeId: String
    ): Response<ResponseBody>

    @GET("mapLocations/{mapLocationId}/image")
    suspend fun getMapLocationImage(
        @Path("mapLocationId") mapLocationId: String
    ): Response<ResponseBody>

    @GET("mapLocations/{mapLocationId}/audio")
    suspend fun getMapLocationAudios(
        @Path("mapLocationId") maplocationId: String
    ):Response<List<String>>


    @POST("routes")
    suspend fun postRoute(
        @Body route: Route
    ): Response<Route>

    @POST("mapLocations")
    suspend fun postMapLocation(
        @Body mapLocation: MapLocationRequest
    ) : Response<MapLocationRequest>

    @POST("routeMapLocations")
    suspend fun postRouteMapLocation(
        @Body routeMapLocation: RouteMapLocation
    ): Response<RouteMapLocation>
}