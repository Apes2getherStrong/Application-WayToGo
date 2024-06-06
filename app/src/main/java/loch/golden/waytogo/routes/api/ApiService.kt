package loch.golden.waytogo.routes.api

import loch.golden.waytogo.user.model.auth.AuthRequest
import loch.golden.waytogo.user.model.auth.AuthResponse
import loch.golden.waytogo.routes.model.maplocation.MapLocationListResponse
import loch.golden.waytogo.routes.model.maplocation.MapLocationRequest
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.model.route.RouteListResponse
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocation
import loch.golden.waytogo.user.model.User
import loch.golden.waytogo.routes.utils.Constants
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST(Constants.LOGIN_URL)
    suspend fun login(
        @Body request: AuthRequest
    ) : Response<AuthResponse>

    @POST(Constants.REGISTER_URL)
    suspend fun register(@Body user: User): Response<Void>

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