package loch.golden.waytogo.repositories

import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import loch.golden.waytogo.room.dao.RouteDao
import loch.golden.waytogo.room.entity.maplocation.MapLocation
import loch.golden.waytogo.room.entity.relations.RouteWithMapLocations
import loch.golden.waytogo.room.entity.route.Route
import loch.golden.waytogo.room.entity.routemaplocation.RouteMapLocation
import loch.golden.waytogo.services.dto.audio.AudioDTO
import loch.golden.waytogo.services.dto.audio.AudioListResponse
import loch.golden.waytogo.services.dto.auth.AuthRequest
import loch.golden.waytogo.services.dto.auth.AuthResponse
import loch.golden.waytogo.services.dto.maplocation.MapLocationListResponse
import loch.golden.waytogo.services.dto.maplocation.MapLocationRequest
import loch.golden.waytogo.services.dto.route.RouteListResponse
import loch.golden.waytogo.services.dto.routemaplocation.RouteMapLocationRequest
import loch.golden.waytogo.services.dto.user.UserDTO
import loch.golden.waytogo.services.services.ApiService
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

class RouteRepository @Inject constructor(
    private val routeDao: RouteDao,
    private val apiService: ApiService
) {

    val allRoutes: Flow<List<Route>> = routeDao.getAllRoutes()

    @WorkerThread
    suspend fun getRouteFromDbById(routeUid: String): Route {
        return routeDao.getRouteFromDbById(routeUid)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(route: Route) {
        routeDao.insertRoute(route)
    }

    @WorkerThread
    suspend fun updateRoute(route: Route) {
        routeDao.updateRoute(route)
    }

    @WorkerThread
    suspend fun deleteRoute(route: Route) {
        routeDao.deleteRoute(route)
    }

    @WorkerThread
    suspend fun deleteRouteWithMapLocations(routeUid: String) {
        routeDao.deleteRouteWithMapLocations(routeUid)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertMapLocation(mapLocation: MapLocation) {
        routeDao.insertMapLocation(mapLocation)
    }

    @WorkerThread
    suspend fun insertRouteMapLocation(routeMapLocation: RouteMapLocation) {
        routeDao.insertRouteMapLocation(routeMapLocation)
    }

    @WorkerThread
    suspend fun deleteRouteMapLocation(routeMapLocation: RouteMapLocation) {
        routeDao.deleteRouteMapLocation(routeMapLocation)
    }

    @WorkerThread
    suspend fun deleteRouteMapLocationById(mapLocationId: String) {
        routeDao.deleteRouteMapLocationById(mapLocationId)
    }


    @WorkerThread
    suspend fun getRouteWithMapLocations(routeUid: String): RouteWithMapLocations {
        return routeDao.getRouteWithMapLocations(routeUid)
    }

    @WorkerThread
    suspend fun updateMapLocation(mapLocation: MapLocation) {
        routeDao.updateMapLocation(mapLocation)
    }

    @WorkerThread
    suspend fun deleteMapLocation(mapLocation: MapLocation) {
        routeDao.deleteMapLocation(mapLocation)
    }

    @WorkerThread
    suspend fun getSequenceNrByMapLocationId(mapLocationId: String): Int {
        return routeDao.getSequenceNrByMapLocationId(mapLocationId)
    }

    @WorkerThread
    suspend fun updateRouteMapLocationSequenceNrById(mapLocationId: String, newSequenceNr: Int) {
        routeDao.updateRouteMapLocationSequenceNrById(mapLocationId, newSequenceNr)
    }

    @WorkerThread
    suspend fun getRouteMapLocationByMapLocationId(mapLocationId: String): RouteMapLocation {
        return routeDao.getRouteMapLocationByMapLocationId(mapLocationId)
    }

    @WorkerThread
    suspend fun updateRouteMapLocation(routeMapLocation: RouteMapLocation) {
        return routeDao.updateRouteMapLocation(routeMapLocation)
    }

    @WorkerThread
    suspend fun updateExternalId(routeUid: String, id: String, externalId: String) {
        Log.d("Gogo","DaoWeszlo")
        return routeDao.updateExternalIdRouteMapLocation(routeUid,id,externalId)
    }

    @WorkerThread
    suspend fun updateRouteExternalId(routeUid: String, externalId: String?) {
        return routeDao.updateRouteExternalId(routeUid,externalId)
    }

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

