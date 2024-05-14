package loch.golden.waytogo.routes.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import loch.golden.waytogo.routes.model.maplocation.MapLocation
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.model.realtions.RouteWithMapLocations
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocation

@Dao
interface RouteDao {

    @Query("SELECT * FROM route_table WHERE route_uid = :routeUid")
    suspend fun getRouteFromDbById(routeUid: String): Route

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: Route)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteMapLocation(routeMapLocation: RouteMapLocation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListOfRouteMapLocations(routeMapLocation: List<RouteMapLocation>)

    @Delete
    suspend fun deleteRouteMapLocation(routeMapLocation: RouteMapLocation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllRoutes(route: List<Route>)

    @Update
    suspend fun updateRoute(route: Route)

    @Delete
    suspend fun deleteRoute(route: Route)

    @Query("SELECT * FROM map_location_table WHERE id = :mapLocationId")
    suspend fun getMyLocationById(mapLocationId: String): MapLocation

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapLocations(mapLocations: List<MapLocation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapLocation(mapLocation: MapLocation)

    @Update
    suspend fun updateMapLocation(mapLocation: MapLocation)

    @Delete
    suspend fun deleteMapLocation(mapLocation: MapLocation)

    @Query("SELECT * FROM route_table")
    fun getAllRoutes(): Flow<List<Route>>

    @Query("DELETE FROM route_table")
    suspend fun clearRoutes()

    @Transaction
    @Query("SELECT * FROM route_table")
    fun getRoutesWithMapLocations(): List<RouteWithMapLocations>

    @Transaction
    @Query("SELECT * FROM route_table WHERE route_uid = :routeUid ")
    suspend fun getRouteWithMapLocations(routeUid: String): RouteWithMapLocations

    @Transaction
    suspend fun insertRouteWithMapLocations(routeWithMapLocations: RouteWithMapLocations) {
        insertRoute(routeWithMapLocations.route)
        routeWithMapLocations.mapLocations.forEach{ mapLocation ->
            insertMapLocation(mapLocation)
            val routeMapLocation = RouteMapLocation(routeWithMapLocations.route.routeUid, mapLocation.id)
            insertRouteMapLocation(routeMapLocation)
        }
    }

    @Transaction
    suspend fun deleteRouteWithMapLocations(routeWithMapLocations: RouteWithMapLocations) {
        val route = routeWithMapLocations.route
        val mapLocations = routeWithMapLocations.mapLocations

        mapLocations.forEach{mapLocation ->
            val routeMapLocation = RouteMapLocation(route.routeUid, mapLocation.id)
            deleteRouteMapLocation(routeMapLocation)
            deleteMapLocation(mapLocation)
        }
        deleteRoute(route)

    }

}