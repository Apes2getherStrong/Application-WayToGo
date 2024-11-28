package loch.golden.waytogo.access.repositories

import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import loch.golden.waytogo.data.room.dao.RouteDao
import loch.golden.waytogo.data.room.entity.maplocation.MapLocation
import loch.golden.waytogo.data.room.entity.relations.RouteWithMapLocations
import loch.golden.waytogo.data.room.entity.route.Route
import loch.golden.waytogo.data.room.entity.routemaplocation.RouteMapLocation
import javax.inject.Inject

class LocalRepository @Inject constructor(
    private val routeDao: RouteDao
) {
    val allRoutes: Flow<List<Route>> = routeDao.getAllRoutes()

    @WorkerThread
    suspend fun getRouteFromDbById(routeUid: String): Route {
        return routeDao.getRouteFromDbById(routeUid)
    }

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
        Log.d("Gogo", "DaoWeszlo")
        return routeDao.updateExternalIdRouteMapLocation(routeUid, id, externalId)
    }

    @WorkerThread
    suspend fun updateRouteExternalId(routeUid: String, externalId: String?) {
        return routeDao.updateRouteExternalId(routeUid, externalId)
    }
}