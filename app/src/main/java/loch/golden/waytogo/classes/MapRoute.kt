package loch.golden.waytogo.classes

data class MapRoute(
    val id: String,
    val name: String,
    val description: String,
    val pointList: MutableMap<String, MapPoint>

)