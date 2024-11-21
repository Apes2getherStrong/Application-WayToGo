package loch.golden.waytogo.services.components

import androidx.room.TypeConverter

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