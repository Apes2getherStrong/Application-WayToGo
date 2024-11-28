package loch.golden.waytogo.access.repositories

import android.util.Log
import loch.golden.waytogo.access.services.dto.audio.AudioDTO
import loch.golden.waytogo.access.services.dto.audio.AudioListResponse
import loch.golden.waytogo.access.services.dto.auth.AuthRequest
import loch.golden.waytogo.access.services.dto.auth.AuthResponse
import loch.golden.waytogo.access.services.dto.maplocation.MapLocationListResponse
import loch.golden.waytogo.access.services.dto.maplocation.MapLocationRequest
import loch.golden.waytogo.access.services.dto.route.RouteListResponse
import loch.golden.waytogo.access.services.dto.routemaplocation.RouteMapLocationRequest
import loch.golden.waytogo.access.services.dto.user.UserDTO
import loch.golden.waytogo.access.services.services.ApiService
import loch.golden.waytogo.data.room.entity.maplocation.MapLocation
import loch.golden.waytogo.data.room.entity.route.Route
import loch.golden.waytogo.data.room.entity.routemaplocation.RouteMapLocation
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

class BackendRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun login(authRequest: AuthRequest): Response<AuthResponse> {
        val response = apiService.login(authRequest)
        Log.d("Login", response.body().toString())
        return response

    }

    suspend fun register(userDTO: UserDTO): Response<UserDTO> {
        val response = apiService.register(userDTO)
        Log.d("Register", response.body().toString())
        return response
    }

    suspend fun getAudioFile(audioId: String): Response<ByteArray> {
        val response = apiService.getAudioFile(audioId)
        return Response.success(response.body()?.bytes())

    }

    suspend fun getAudioByMapLocationId(mapLocationId: String): Response<AudioListResponse> {
        return apiService.getAudioByMapLocationId(mapLocationId)
    }

    suspend fun getRoutes(pageNumber: Int, pageSize: Int): Response<RouteListResponse> {
        return apiService.getRoutes(pageNumber, pageSize)
    }

    suspend fun getRouteById(routeUid: String): Response<Route> {
        return apiService.getRouteById(routeUid)
    }

    suspend fun getUserByUserId(userId: String): Response<UserDTO> {
        return apiService.getUserByUserId(userId)
    }

    suspend fun putUserByUserId(userId: String, userDTO: UserDTO): Response<Void> {
        return apiService.putUserByUserId(userId, userDTO)
    }

    suspend fun getMapLocationsByRouteId(routeId: String): Response<MapLocationListResponse> {
        return apiService.getMapLocationsByRouteId(routeId)
    }

    suspend fun getRouteImage(routeId: String): Response<ByteArray> {
        val response = apiService.getRouteImage(routeId)
        return Response.success(response.body()?.bytes())
    }

    suspend fun getMapLocationImage(mapLocationId: String): Response<ByteArray> {
        val response = apiService.getMapLocationImage(mapLocationId)
        return Response.success(response.body()?.bytes())
    }

    suspend fun getMapLocationAudios(mapLocationId: String): Response<List<String>> {
        return apiService.getMapLocationAudios(mapLocationId)
    }

    suspend fun postRoute(route: Route): Response<Route> {
        return apiService.postRoute(route)
    }

    suspend fun putRouteById(routeId: String, route: Route): Response<Void> {
        return apiService.putRouteById(routeId, route)
    }

    suspend fun postMapLocation(mapLocation: MapLocationRequest): Response<MapLocationRequest> {
        return apiService.postMapLocation(mapLocation)
    }

    suspend fun putMapLocationById(mapLocationId: String, mapLocation: MapLocationRequest): Response<Void> {
        return apiService.putMapLocationById(mapLocationId, mapLocation)
    }

    suspend fun postRouteMapLocation(routeMapLocation: RouteMapLocationRequest): Response<RouteMapLocationRequest> {
        return apiService.postRouteMapLocation(routeMapLocation)
    }

    suspend fun putRouteMapLocationById(
        routeMapLocationId: String,
        routeMapLocation: RouteMapLocationRequest
    ): Response<Void> {
        return apiService.putRouteMapLocationById(routeMapLocationId, routeMapLocation)
    }

    suspend fun postAudio(audioDTO: AudioDTO): Response<AudioDTO> {
        return apiService.postAudio(audioDTO)
    }

    suspend fun putAudioById(audioId: String, audioDTO: AudioDTO): Response<Void> {
        return apiService.putAudioById(audioId, audioDTO)
    }

    suspend fun postAudioFile(audioId: String?, audioFile: MultipartBody.Part) {
        return apiService.postAudioFile(audioId, audioFile)
    }

    suspend fun putImageToMapLocation(mapLocationId: String, imageFile: MultipartBody.Part) {
        return apiService.putImageToMapLocation(mapLocationId, imageFile)
    }

    suspend fun putImageToRoute(routeId: String, imageFile: MultipartBody.Part) {
        return apiService.putImageToRoute(routeId, imageFile)
    }

    suspend fun deleteRouteById(routeId: String): Response<Route> {
        return apiService.deleteRouteById(routeId)
    }

    suspend fun deleteMapLocationById(mapLocationId: String): Response<MapLocation> {
        return apiService.deleteMapLocationById(mapLocationId)
    }

    suspend fun deleteRouteMapLocationByIdApi(routeMapLocationId: String): Response<RouteMapLocation> {
        return apiService.deleteRouteMapLocationByIdApi(routeMapLocationId)
    }

    suspend fun deleteAudioById(audioId: String): Response<AudioDTO> {
        return apiService.deleteAudioById(audioId)
    }


}

