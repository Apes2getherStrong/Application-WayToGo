package loch.golden.waytogo.viewmodels.classes

data class MapRoute(
    val id: String,
    var name: String,
    var description: String,
    val pointList: MutableMap<String, MapPoint>,
    var photoPath: String? = null
)