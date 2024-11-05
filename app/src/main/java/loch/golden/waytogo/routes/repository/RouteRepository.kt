package loch.golden.waytogo.routes.repository

import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import loch.golden.waytogo.audio.Audio
import loch.golden.waytogo.audio.AudioListResponse
import loch.golden.waytogo.routes.api.RetrofitInstance
import loch.golden.waytogo.user.model.auth.AuthRequest
import loch.golden.waytogo.user.model.auth.AuthResponse
import loch.golden.waytogo.routes.model.maplocation.MapLocation
import loch.golden.waytogo.routes.model.maplocation.MapLocationListResponse
import loch.golden.waytogo.routes.model.maplocation.MapLocationRequest
import loch.golden.waytogo.routes.model.relations.RouteWithMapLocations
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.model.route.RouteListResponse
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocation
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocationRequest
import loch.golden.waytogo.user.model.User
import loch.golden.waytogo.routes.room.dao.RouteDao
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException

class RouteRepository(private val routeDao: RouteDao) {

    val allRoutes: Flow<List<Route>> = routeDao.getAllRoutes()

//    @WorkerThread
//    suspend fun insertRouteWithMapLocations(routeWithMapLocations: RouteWithMapLocations) {
//        routeDao.insertRouteWithMapLocations(routeWithMapLocations)
//    }
//
//    @WorkerThread
//    suspend fun deleteRouteWithMapLocations(routeWithMapLocations: RouteWithMapLocations) {
//        routeDao.deleteRouteWithMapLocations(routeWithMapLocations)
//    }

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
        return routeDao.updateExternalId(routeUid,id,externalId)
    }

    suspend fun login(authRequest: AuthRequest): Response<AuthResponse> {
        val response = RetrofitInstance.apiService.login(authRequest)
        Log.d("Login", response.body().toString())
        return response

    }

    suspend fun register(user: User): Response<User> {
        val response = RetrofitInstance.apiService.register(user)
        Log.d("Register", response.body().toString())
        return response
    }

    suspend fun getAudioFile(audioId: String): Response<ByteArray> {
        val response = RetrofitInstance.apiService.getAudioFile(audioId)
        return Response.success(response.body()?.bytes())

    }

    suspend fun getAudioByMapLocationId(mapLocationId: String): Response<AudioListResponse> {
        return RetrofitInstance.apiService.getAudioByMapLocationId(mapLocationId)
    }

    suspend fun getRoutes(pageNumber: Int, pageSize: Int): Response<RouteListResponse> {
        return RetrofitInstance.apiService.getRoutes(pageNumber, pageSize)
    }

    suspend fun getRouteById(routeUid: String): Response<Route> {
        return RetrofitInstance.apiService.getRouteById(routeUid)
    }

    suspend fun getUserByUserId(userId: String): Response<User> {
        return RetrofitInstance.apiService.getUserByUserId(userId)
    }

    suspend fun putUserByUserId(userId: String, user: User): Response<Void> {
        return RetrofitInstance.apiService.putUserByUserId(userId, user)
    }

    suspend fun getMapLocationsByRouteId(routeId: String): Response<MapLocationListResponse> {
        return RetrofitInstance.apiService.getMapLocationsByRouteId(routeId)
    }

    suspend fun getRouteImage(routeId: String): Response<ByteArray> {
        val response = RetrofitInstance.apiService.getRouteImage(routeId)
        return Response.success(response.body()?.bytes())
    }

    suspend fun getMapLocationImage(mapLocationId: String): Response<ByteArray> {
        val response = RetrofitInstance.apiService.getMapLocationImage(mapLocationId)
        return Response.success(response.body()?.bytes())
    }

    suspend fun getMapLocationAudios(mapLocationId: String): Response<List<String>> {
        return RetrofitInstance.apiService.getMapLocationAudios(mapLocationId)
    }

    suspend fun postRoute(route: Route): Response<Route> {
        return RetrofitInstance.apiService.postRoute(route)
    }

    suspend fun putRouteById(routeId: String, route: Route): Response<Void> {
        return RetrofitInstance.apiService.putRouteById(routeId, route)
    }

    suspend fun postMapLocation(mapLocation: MapLocationRequest): Response<MapLocationRequest> {
        return RetrofitInstance.apiService.postMapLocation(mapLocation)
    }

    suspend fun putMapLocationById(mapLocationId: String, mapLocation: MapLocationRequest): Response<Void> {
        return RetrofitInstance.apiService.putMapLocationById(mapLocationId, mapLocation)
    }

    suspend fun postRouteMapLocation(routeMapLocation: RouteMapLocationRequest): Response<RouteMapLocationRequest> {
        return RetrofitInstance.apiService.postRouteMapLocation(routeMapLocation)
    }

    suspend fun putRouteMapLocationById(
        routeMapLocationId: String,
        routeMapLocation: RouteMapLocationRequest
    ): Response<Void> {
        return RetrofitInstance.apiService.putRouteMapLocationById(routeMapLocationId, routeMapLocation)
    }

    suspend fun postAudio(audio: Audio): Response<Audio> {
        return RetrofitInstance.apiService.postAudio(audio)
    }

    suspend fun putAudioById(audioId: String, audio: Audio): Response<Void> {
        return RetrofitInstance.apiService.putAudioById(audioId, audio)
    }

    suspend fun postAudioFile(audioId: String?, audioFile: MultipartBody.Part) {
        return RetrofitInstance.apiService.postAudioFile(audioId, audioFile)
    }

    suspend fun putImageToMapLocation(mapLocationId: String, imageFile: MultipartBody.Part) {
        return RetrofitInstance.apiService.putImageToMapLocation(mapLocationId, imageFile)
    }

    suspend fun putImageToRoute(routeId: String, imageFile: MultipartBody.Part) {
        return RetrofitInstance.apiService.putImageToRoute(routeId, imageFile)
    }

    suspend fun deleteRouteById(routeId: String): Response<Route> {
        return RetrofitInstance.apiService.deleteRouteById(routeId)
    }

    suspend fun deleteMapLocationById(mapLocationId: String): Response<MapLocation> {
        return RetrofitInstance.apiService.deleteMapLocationById(mapLocationId)
    }

    suspend fun deleteRouteMapLocationByIdApi(routeMapLocationId: String): Response<RouteMapLocation> {
        return RetrofitInstance.apiService.deleteRouteMapLocationByIdApi(routeMapLocationId)
    }

    suspend fun deleteAudioById(audioId: String): Response<Audio> {
        return RetrofitInstance.apiService.deleteAudioById(audioId)
    }


}

