package loch.golden.waytogo.routes.room

import androidx.room.Database
import androidx.room.RoomDatabase
import loch.golden.waytogo.routes.model.Route

@Database(entities = [Route::class], version = 1)
abstract class WayToGoDatabase : RoomDatabase() {

    abstract fun getRouteDao() : RouteDao

}