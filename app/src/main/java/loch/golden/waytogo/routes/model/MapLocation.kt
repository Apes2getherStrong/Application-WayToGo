package loch.golden.waytogo.routes.model

data class MapLocation(
    val id: String,
    val name: String,
    val description: String,
    val coordinates: Coordinates,
    val createdDate: Any?,
    val updateDate: Any?
)