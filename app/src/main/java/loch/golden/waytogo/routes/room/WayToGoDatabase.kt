package loch.golden.waytogo.routes.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import loch.golden.waytogo.routes.model.Route
import loch.golden.waytogo.routes.room.dao.RouteDao

@Database(entities = [Route::class], version = 1, exportSchema = false)
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
            routeDao.clearRoutes()

            //sprawdzenie czy insert do bazy dziala, sprawdzic w app inspection , database inspector
//            val route1 = Route(123,"Test trasy","test")
//            routeDao.insertRoute(route1)
//            val route2 = Route(32,"Drugi test trasy","dsadas")
//            routeDao.insertRoute(route2)
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