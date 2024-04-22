package loch.golden.waytogo.routes.repository

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import loch.golden.waytogo.routes.api.RetrofitInstance
import loch.golden.waytogo.routes.model.Route
import loch.golden.waytogo.routes.model.RouteListResponse
import loch.golden.waytogo.routes.room.dao.RouteDao
import retrofit2.Response

class RouteRepository(private val routeDao: RouteDao) {

    val allRoutes: Flow<List<Route>> = routeDao.getAllRoutes()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(route: Route) {
        routeDao.insertRoute(route)
    }

    suspend fun getRoutes(pageNumber: Int, pageSize: Int): Response<RouteListResponse> {
        return RetrofitInstance.apiService.getRoutes(pageNumber, pageSize)
    }

    suspend fun getRouteById(routeUid: String): Response<Route> {
        return RetrofitInstance.apiService.getRouteById(routeUid)
    }
}

