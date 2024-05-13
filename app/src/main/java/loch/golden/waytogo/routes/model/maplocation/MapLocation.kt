package loch.golden.waytogo.routes.model.maplocation

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_location_table")
data class MapLocation(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
//    val createdDate: Any?,
//    val updateDate: Any?
)