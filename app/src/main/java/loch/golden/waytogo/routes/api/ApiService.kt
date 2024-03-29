package loch.golden.waytogo.routes.api

import loch.golden.waytogo.routes.model.RouteListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("routes")
    suspend fun getRoutes(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Response<RouteListResponse>
}