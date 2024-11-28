package loch.golden.waytogo.presentation.fragments.map.components.navigation

import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonParser
import loch.golden.waytogo.BuildConfig
import loch.golden.waytogo.access.services.services.GoogleApiService
import javax.inject.Inject

class NavigationManager @Inject constructor(

    private val apiService: GoogleApiService
) {


    suspend fun getPolyline(from: LatLng, to: LatLng): ArrayList<LatLng> {
        val fromStr = "${from.latitude},${from.longitude}"
        val toStr = "${to.latitude},${to.longitude}"
        val jsonResponse = apiService.getPolyline( //todo add try catch
            fromStr,
            toStr,
            "walking",
            BuildConfig.MAPS_API_KEY,
            null
        )
//        Log.d("Warmbier", jsonResponse)
        val encodedPoly =
            JsonParser.parseString(jsonResponse).asJsonObject["routes"].asJsonArray[0].asJsonObject["overview_polyline"].asJsonObject["points"].asString
//        Log.d("Warmbier", encodedPoly)
        return decodePoly(encodedPoly)
    }

    private fun decodePoly(encoded: String): ArrayList<LatLng> {
//        Log.d("Warmbier", encoded)
        val poly: ArrayList<LatLng> = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val position = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(position)
        }
        return poly
    }
}