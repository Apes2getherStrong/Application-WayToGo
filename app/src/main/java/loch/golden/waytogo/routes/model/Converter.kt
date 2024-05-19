package loch.golden.waytogo.routes.model

import androidx.room.TypeConverter
import loch.golden.waytogo.routes.model.maplocation.MapLocation
import loch.golden.waytogo.routes.model.maplocation.MapLocationRequest

class Converters {
    @TypeConverter
    fun fromCoordinatesList(coordinates: List<Double>): String {
        return coordinates.joinToString(",")
    }

    @TypeConverter
    fun toCoordinatesList(coordinatesString: String): List<Double> {
        return coordinatesString.split(",").map { it.toDouble() }
    }


}