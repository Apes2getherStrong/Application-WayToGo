package loch.golden.waytogo.routes.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import loch.golden.waytogo.routes.model.Route
@Dao
interface RouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: Route)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllRoutes(route: List<Route>)

    @Update
    suspend fun updateRoute(route: Route)

    @Query("SELECT * FROM route_table")
    fun getAllRoutes(): Flow<List<Route>>

    @Delete
    fun deleteRoute(route: Route)

    @Query("DELETE FROM route_table")
    suspend fun deleteAllRoutes()

}