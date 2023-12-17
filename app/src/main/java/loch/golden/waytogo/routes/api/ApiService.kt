package loch.golden.waytogo.routes.api

import loch.golden.waytogo.routes.model.Route
import loch.golden.waytogo.routes.model.RouteListResponse
import retrofit2.http.GET

interface ApiService {
    @GET("routes")
    suspend fun getRoutes(): RouteListResponse
}