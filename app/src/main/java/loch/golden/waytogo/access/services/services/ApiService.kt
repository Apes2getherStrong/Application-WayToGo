package loch.golden.waytogo.access.services.services


import loch.golden.waytogo.access.services.dto.audio.AudioDTO
import loch.golden.waytogo.access.services.dto.audio.AudioListResponse
import loch.golden.waytogo.access.services.dto.auth.AuthRequest
import loch.golden.waytogo.access.services.dto.auth.AuthResponse
import loch.golden.waytogo.access.services.dto.maplocation.MapLocationListResponse
import loch.golden.waytogo.access.services.dto.maplocation.MapLocationRequest
import loch.golden.waytogo.access.services.dto.route.RouteListResponse
import loch.golden.waytogo.access.services.dto.routemaplocation.RouteMapLocationRequest
import loch.golden.waytogo.access.services.dto.user.UserDTO
import loch.golden.waytogo.data.room.entity.maplocation.MapLocation
import loch.golden.waytogo.data.room.entity.route.Route
import loch.golden.waytogo.data.room.entity.routemaplocation.RouteMapLocation
import loch.golden.waytogo.utils.Constants
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST(Constants.LOGIN_URL)
    suspend fun login(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST(Constants.REGISTER_URL)
    suspend fun register(@Body userDTO: UserDTO): Response<UserDTO>

    @GET("users/{userId}")
    suspend fun getUserByUserId(
        @Path("userId") userId: String
    ): Response<UserDTO>

    @PUT("users/{userId}")
    suspend fun putUserByUserId(
        @Path("userId") userId: String,
        @Body userDTO: UserDTO
    ): Response<Void>

//USER MAPLOCATION ROUTE - routemaplocation



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
        @Path("routeId") routeId: String
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
    ): Response<List<String>>

    @GET("audios/{audioId}/audio")
    suspend fun getAudioFile(
        @Path("audioId") audioId: String
    ): Response<ResponseBody>

    @GET("mapLocations/{mapLocationId}/audios")
    suspend fun getAudioByMapLocationId(
        @Path("mapLocationId") mapLocationId: String
    ): Response<AudioListResponse>

    @POST("routes")
    suspend fun postRoute(
        @Body route: Route
    ): Response<Route>

    @PUT("routes/{routeId}")
    suspend fun putRouteById(
        @Path("routeId") routeId: String,
        @Body route: Route
    ): Response<Void>

    @POST("mapLocations")
    suspend fun postMapLocation(
        @Body mapLocation: MapLocationRequest
    ): Response<MapLocationRequest>

    @PUT("mapLocations/{mapLocationId}")
    suspend fun putMapLocationById(
        @Path("mapLocationId") mapLocationId: String,
        @Body mapLocation: MapLocationRequest
    ): Response<Void>

    @POST("routeMapLocations")
    suspend fun postRouteMapLocation(
        @Body routeMapLocation: RouteMapLocationRequest
    ): Response<RouteMapLocationRequest>

    @PUT("routeMapLocations/{routeMapLocationId}")
    suspend fun putRouteMapLocationById(
        @Path("routeMapLocationId") routeMapLocationId: String,
        @Body routeMapLocation: RouteMapLocationRequest
    ): Response<Void>

    @POST("audios")
    suspend fun postAudio(@Body audioDTO: AudioDTO): Response<AudioDTO>

    @PUT("audios/{audioId}")
    suspend fun putAudioById(
        @Path("audioId") audioId: String,
        @Body audioDTO: AudioDTO
    ): Response<Void>

    @Multipart
    @POST("audios/{audioId}/audio")
    suspend fun postAudioFile(
        @Path("audioId") audioId: String?,
        @Part file: MultipartBody.Part,
    )

    @Multipart
    @PUT("mapLocations/{mapLocationsId}/image")
    suspend fun putImageToMapLocation(
        @Path("mapLocationsId") mapLocationId: String,
        @Part imageFile: MultipartBody.Part
    )

    @Multipart
    @PUT("routes/{routeId}/image")
    suspend fun putImageToRoute(
        @Path("routeId") routeId: String,
        @Part imageFile: MultipartBody.Part
    )

    @DELETE("routes/{routeId}")
    suspend fun deleteRouteById(
        @Path("routeId") routeId: String
    ): Response<Route>

    @DELETE("mapLocations/{mapLocationId}")
    suspend fun deleteMapLocationById(
        @Path("mapLocationId") mapLocationId: String
    ): Response<MapLocation>

    @DELETE("routeMapLocations/{routeMapLocationId}")
    suspend fun deleteRouteMapLocationByIdApi(
        @Path("routeMapLocationId") routeMapLocationId: String
    ) : Response<RouteMapLocation>

    @DELETE("audios/{audioId}")
    suspend fun deleteAudioById(
        @Path("audioId") audioId: String
    ): Response<AudioDTO>
}