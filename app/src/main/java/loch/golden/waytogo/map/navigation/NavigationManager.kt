package loch.golden.waytogo.map.navigation

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonParser
import loch.golden.waytogo.BuildConfig
import loch.golden.waytogo.map.MapViewModel

class NavigationManager(private val mapViewModel: MapViewModel) {


    suspend fun getPolyline(from: LatLng, to: LatLng): ArrayList<LatLng> {
        val fromStr = "${from.latitude},${from.longitude}"
        val toStr = "${to.latitude},${to.longitude}"
        val jsonResponse = GoogleApiRetrofitInstance.apiService.getPolyline(
            fromStr,
            toStr,
            "walking",
            BuildConfig.MAPS_API_KEY,
            null
        )
        val encodedPoly =
            JsonParser.parseString(jsonResponse).asJsonObject["routes"].asJsonArray[0].asJsonObject["overview_polyline"].asJsonObject["points"].asString
        return decodePoly(encodedPoly)
    }

    private fun decodePoly(encoded: String): ArrayList<LatLng> {
        Log.d("Warmbier", encoded)
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
        Log.d("Warmbier", poly.toString())
        return poly
    }
}