package loch.golden.waytogo.routes.model.maplocation

data class MapLocationRequest(
    val id: String,
    val name: String,
    val description: String,
    val coordinates: Coordinates
)

