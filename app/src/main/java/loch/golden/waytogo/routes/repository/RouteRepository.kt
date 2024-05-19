package loch.golden.waytogo.routes.repository

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import loch.golden.waytogo.routes.api.RetrofitInstance
import loch.golden.waytogo.routes.model.maplocation.MapLocation
import loch.golden.waytogo.routes.model.maplocation.MapLocationListResponse
import loch.golden.waytogo.routes.model.maplocation.MapLocationRequest
import loch.golden.waytogo.routes.model.realtions.RouteWithMapLocations
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.model.route.RouteListResponse
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocation
import loch.golden.waytogo.routes.room.dao.RouteDao
import retrofit2.Response

class RouteRepository(private val routeDao: RouteDao) {

    val allRoutes: Flow<List<Route>> = routeDao.getAllRoutes()

    @WorkerThread
    suspend fun insertRouteWithMapLocations(routeWithMapLocations: RouteWithMapLocations) {
        routeDao.insertRouteWithMapLocations(routeWithMapLocations)
    }

    @WorkerThread
    suspend fun deleteRouteWithMapLocations(routeWithMapLocations: RouteWithMapLocations) {
        routeDao.deleteRouteWithMapLocations(routeWithMapLocations)
    }

    @WorkerThread
    suspend fun getRouteFromDbById(routeUid: String) : Route {
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




    suspend fun getRoutes(pageNumber: Int, pageSize: Int): Response<RouteListResponse> {
        return RetrofitInstance.apiService.getRoutes(pageNumber, pageSize)
    }

    suspend fun getRouteById(routeUid: String): Response<Route> {
        return RetrofitInstance.apiService.getRouteById(routeUid)
    }

    suspend fun getMapLocationsByRouteId(routeId: String): Response<MapLocationListResponse> {
        return RetrofitInstance.apiService.getMapLocationsByRouteId(routeId)
    }

    suspend fun postRoute(route: Route): Response<Route> {
        return RetrofitInstance.apiService.postRoute(route)
    }

    suspend fun postMapLocations(mapLocations: List<MapLocationRequest>): Response<List<MapLocationRequest>> {
        return RetrofitInstance.apiService.postMapLocations(mapLocations)
    }
}

