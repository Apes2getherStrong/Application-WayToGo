package loch.golden.waytogo.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import loch.golden.waytogo.room.dao.RouteDao
import loch.golden.waytogo.room.entity.maplocation.MapLocation
import loch.golden.waytogo.room.entity.route.Route
import loch.golden.waytogo.room.entity.routemaplocation.RouteMapLocation
import loch.golden.waytogo.services.components.Converters
import java.util.UUID

@Database(entities = [Route::class, MapLocation::class, RouteMapLocation::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class WayToGoDatabase : RoomDatabase() {

    abstract fun getRouteDao(): RouteDao

    suspend fun populateDatabase(routeDao: RouteDao) {

        val route1 = Route(UUID.randomUUID().toString(), "Pierwsza trasa", "Opis pierwszej trasy", null)
        val route2 = Route(UUID.randomUUID().toString(), "Druga trasa", "Opis drugiej trasy", null)
        val route3NOwa = Route(UUID.randomUUID().toString(), "Trasa na posta", "Opis dzikiej trasy", null)
        routeDao.insertRoute(route1)
        routeDao.insertRoute(route2)
        routeDao.insertRoute(route3NOwa)


        val mapLocations = listOf(
            MapLocation(UUID.randomUUID().toString(), "Lokalizacja 1", "Opis lokalizacji 1", 12.345, 67.890, null),
            MapLocation(UUID.randomUUID().toString(), "Lokalizacja 2", "Opis lokalizacji 2", 34.567, 89.012, null)

        )
        routeDao.insertMapLocations(mapLocations)

        val routeMapLocations = listOf(
            RouteMapLocation("123eefs", "324esczxc", 1, null),
            RouteMapLocation("123eefs", "123eas", 2, null),
            RouteMapLocation("ddasa2312311", "123eas", 3, null),


            )

        routeDao.insertListOfRouteMapLocations(routeMapLocations)

        val route4 = Route(
            UUID.randomUUID().toString(),
            "Nowa trasa do posta",
            "Opis nowej trasy",
            null
        )
        routeDao.insertRoute(route4)

        val mapLocationsForRoute4 = listOf(
            MapLocation(UUID.randomUUID().toString(), "Punkt 1", "Opis punktu 1", 45.678, 23.456, null),
            MapLocation(UUID.randomUUID().toString(), "Punkt 2", "Opis punktu 2", 56.789, 34.567, null)
        )
        routeDao.insertMapLocations(mapLocationsForRoute4)

        val routeMapLocationsForRoute4 = listOf(
            RouteMapLocation(route4.routeUid, mapLocationsForRoute4[0].id, 1, null),
            RouteMapLocation(route4.routeUid, mapLocationsForRoute4[1].id, 2, null)
        )
        routeDao.insertListOfRouteMapLocations(routeMapLocationsForRoute4)
    }

}


