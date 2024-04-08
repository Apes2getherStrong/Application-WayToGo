package loch.golden.waytogo.routes.repository

import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import loch.golden.waytogo.routes.api.RetrofitInstance
import loch.golden.waytogo.routes.model.Route
import loch.golden.waytogo.routes.model.RouteListResponse
import loch.golden.waytogo.routes.room.RouteDao
import retrofit2.Response

class RouteRepository(private val routeDao: RouteDao) {

    val allRoutes: Flow<List<Route>> = routeDao.getAllRoutes()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(route: Route) {
        routeDao.insertRoute(route)
    }

    suspend fun fetchAndSaveRoutes(pageNumber: Int,pageSize: Int) {
        try {
            // Pobranie danych z backendu
            val response = RetrofitInstance.apiService.getRoutes(pageNumber,pageSize)

            if (response.isSuccessful) {
                val routes = response.body()?.content ?: emptyList()

                routeDao.insertAllRoutes(routes)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e("RouteRepository", "Error fetching routes: $errorMessage")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getRoutes(pageNumber: Int, pageSize: Int): Response<RouteListResponse> {
        return RetrofitInstance.apiService.getRoutes(pageNumber, pageSize)
    }
}

