package loch.golden.waytogo.routes.model.maplocation

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coordinates_table")
data class Coordinates(
    @PrimaryKey(autoGenerate = true)
    val coordId: Long,
    val coordinates: List<Double>,


)