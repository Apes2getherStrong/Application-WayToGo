package loch.golden.waytogo.access.services.services

import com.google.android.gms.maps.model.LatLng
import loch.golden.waytogo.utils.Constants.Companion.GOOGLE_NAVIGATION_ENDPOINT
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

        ): String //TODO MAKE this nicer maybe add a converter
}