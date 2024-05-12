package loch.golden.waytogo.routes.model.maplocation

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_location_table")
data class MapLocation(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val coordinatesId: Long,
//    val createdDate: Any?,
//    val updateDate: Any?
)