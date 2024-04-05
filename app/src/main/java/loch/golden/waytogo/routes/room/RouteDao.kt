package loch.golden.waytogo.routes.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import loch.golden.waytogo.routes.model.Route
@Dao
interface RouteDao {
    @Insert
    fun createRoute(route: Route)

    @Query("SELECT * FROM route")
    fun getAllRoutes(): List<Route>

    @Update
    fun updateRoute(route: Route)

    @Delete
    fun deleteRoute(route: Route)
}