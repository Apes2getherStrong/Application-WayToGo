package loch.golden.waytogo.routes.repository

import loch.golden.waytogo.routes.api.RetrofitInstance
import loch.golden.waytogo.routes.model.RouteListResponse
import retrofit2.Response

class RouteRepository {
    suspend fun getRoutes(pageNumber: Int, pageSize: Int): Response<RouteListResponse> {
        return RetrofitInstance.apiService.getRoutes(pageNumber, pageSize)
    }
}

