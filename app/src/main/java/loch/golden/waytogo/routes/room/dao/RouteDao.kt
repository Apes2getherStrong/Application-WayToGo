package loch.golden.waytogo.routes.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import loch.golden.waytogo.routes.model.maplocation.Coordinates
import loch.golden.waytogo.routes.model.maplocation.MapLocation
import loch.golden.waytogo.routes.model.maplocation.MapLocationAndCoordinates
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.model.route.RouteWithMapLocations
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocation

@Dao
interface RouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: Route)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapLocation(mapLocation: MapLocation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoordinates(coordinates: Coordinates)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteMapLocation(routeMapLocation: RouteMapLocation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllRoutes(route: List<Route>)

    @Update
    suspend fun updateRoute(route: Route)

    @Query("SELECT * FROM route_table")
    fun getAllRoutes(): Flow<List<Route>>

    @Delete
    fun deleteRoute(route: Route)

    @Query("DELETE FROM route_table")
    suspend fun clearRoutes()

    @Transaction
    @Query("SELECT * FROM route_table")
    fun getRoutesWithMapLocations(): List<RouteWithMapLocations>

    @Transaction
    @Query("SELECT * FROM route_table WHERE route_uid = :routeUid ")
    suspend fun getMapLocationsOfRoute(routeUid: String): List<RouteWithMapLocations>

    @Transaction
    suspend fun insertMapLocationAndCoordinates(mapLocationAndCoordinates: List<MapLocationAndCoordinates>) {
        mapLocationAndCoordinates.forEach { mapLocationAndCoordinates ->
            insertMapLocation(mapLocationAndCoordinates.mapLocation)
            insertCoordinates(mapLocationAndCoordinates.coordinates)
        }
    }

    @Transaction
    suspend fun insertRouteMapLocation(routeMapLocations: List<RouteMapLocation>) {
        routeMapLocations.forEach { routeMapLocation ->
            insertRouteMapLocation(routeMapLocation)
        }
    }

}