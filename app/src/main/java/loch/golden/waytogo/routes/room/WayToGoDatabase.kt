package loch.golden.waytogo.routes.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import loch.golden.waytogo.routes.model.Converters
import loch.golden.waytogo.routes.model.maplocation.MapLocation
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocation
import loch.golden.waytogo.routes.room.dao.RouteDao
import java.util.UUID

@Database(entities = [Route::class, MapLocation::class, RouteMapLocation::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class WayToGoDatabase : RoomDatabase() {

    abstract fun getRouteDao(): RouteDao

    private class WayToGoDatabaseCallback(private val scope: CoroutineScope) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            DATABASE_INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.getRouteDao())
                }
            }
        }

        suspend fun populateDatabase(routeDao: RouteDao) {

            val route1 = Route(UUID.randomUUID().toString(), "Pierwsza trasa", "Opis pierwszej trasy")
            val route2 = Route(UUID.randomUUID().toString(), "Druga trasa", "Opis drugiej trasy")
            val route3NOwa = Route(UUID.randomUUID().toString(), "Trasa na posta", "Opis dzikiej trasy")
            routeDao.insertRoute(route1)
            routeDao.insertRoute(route2)
            routeDao.insertRoute(route3NOwa)


            val mapLocations = listOf(
                MapLocation(UUID.randomUUID().toString(), "Lokalizacja 1", "Opis lokalizacji 1", 12.345, 67.890),
                MapLocation(UUID.randomUUID().toString(), "Lokalizacja 2", "Opis lokalizacji 2", 34.567, 89.012)

            )
            routeDao.insertMapLocations(mapLocations)

            val routeMapLocations = listOf(
                RouteMapLocation("123eefs", "324esczxc", 1),
                RouteMapLocation("123eefs", "123eas", 2),
                RouteMapLocation("ddasa2312311", "123eas", 3),


                )

            routeDao.insertListOfRouteMapLocations(routeMapLocations)

            val route4 = Route(
                UUID.randomUUID().toString(),
                "Nowa trasa do posta",
                "Opis nowej trasy"
            )
            routeDao.insertRoute(route4)

            val mapLocationsForRoute4 = listOf(
                MapLocation(UUID.randomUUID().toString(), "Punkt 1", "Opis punktu 1", 45.678, 23.456),
                MapLocation(UUID.randomUUID().toString(), "Punkt 2", "Opis punktu 2", 56.789, 34.567)
            )
            routeDao.insertMapLocations(mapLocationsForRoute4)

            val routeMapLocationsForRoute4 = listOf(
                RouteMapLocation(route4.routeUid, mapLocationsForRoute4[0].id, 1),
                RouteMapLocation(route4.routeUid, mapLocationsForRoute4[1].id, 2)
            )
            routeDao.insertListOfRouteMapLocations(routeMapLocationsForRoute4)
        }

    }

    companion object {

        @Volatile
        private var DATABASE_INSTANCE: WayToGoDatabase? = null
        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): WayToGoDatabase {

            return DATABASE_INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WayToGoDatabase::class.java,
                    "way-to-go-database"
                )
                    .addCallback(WayToGoDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                DATABASE_INSTANCE = instance
                instance
            }
        }
    }

}