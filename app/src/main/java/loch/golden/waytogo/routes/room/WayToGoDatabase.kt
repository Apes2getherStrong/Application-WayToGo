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

@Database(entities = [Route::class, MapLocation::class, RouteMapLocation::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class WayToGoDatabase : RoomDatabase() {

    abstract fun getRouteDao() : RouteDao

    private class WayToGoDatabaseCallback (private val scope: CoroutineScope) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            DATABASE_INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.getRouteDao())
                }
            }
        }

        suspend fun populateDatabase(routeDao: RouteDao) {

            val routeMapLocations = listOf(
                RouteMapLocation("123eefs", "1"),
                RouteMapLocation("123eefs", "2"),
                RouteMapLocation("ddasa2312311", "2")
            )

            routeDao.insertListOfRouteMapLocations(routeMapLocations)

            val mapLocations = listOf(
                MapLocation("1", "Lokalizacja 1", "Opis lokalizacji 1", 12.345, 67.890),
                MapLocation("2", "Lokalizacja 2", "Opis lokalizacji 2", 34.567, 89.012)
            )
            routeDao.insertMapLocations(mapLocations)

            val route1 = Route("123eefs", "Pierwsza trasa", "Opis pierwszej trasy")
            val route2 = Route("ddasa2312311", "Druga trasa", "Opis drugiej trasy")
            routeDao.insertRoute(route1)
            routeDao.insertRoute(route2)

        }

    }

    companion object {

        @Volatile
        private var DATABASE_INSTANCE: WayToGoDatabase? = null
        fun getDatabase(context: Context,
                        scope: CoroutineScope) : WayToGoDatabase{

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