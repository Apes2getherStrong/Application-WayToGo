package loch.golden.waytogo.routes.model

data class Route(
    val id: String,
    val name: String,
    val user: User,
    var image: Int
)