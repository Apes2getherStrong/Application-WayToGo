package loch.golden.waytogo.access.services.dto.maplocation

data class MapLocationRequest(
    val id: String,
    val name: String,
    val description: String,
    val coordinates: Coordinates
)

