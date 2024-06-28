package loch.golden.waytogo.map.navigation

import com.google.android.gms.maps.model.LatLng
import loch.golden.waytogo.routes.model.route.RouteListResponse
import loch.golden.waytogo.routes.utils.Constants.Companion.GOOGLE_NAVIGATION_ENDPOINT
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleApiService {
    @GET(GOOGLE_NAVIGATION_ENDPOINT)
    suspend fun getPolyline(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String,
        @Query("key") key: String,
        @Query("waypoints") waypoints: List<LatLng>?,

        ): String
}