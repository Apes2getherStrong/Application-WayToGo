package loch.golden.waytogo.routes.repository

import loch.golden.waytogo.routes.api.RetrofitInstance
import loch.golden.waytogo.routes.model.Route

class RouteRepository {
    suspend fun getRoutes(): List<Route>{
        val response = RetrofitInstance.apiService.getRoutes()
        return response.content
    }
}

