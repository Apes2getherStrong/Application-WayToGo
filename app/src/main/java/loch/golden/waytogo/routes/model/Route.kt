package loch.golden.waytogo.routes.model

data class Route(
    val id: String,
    val name: String,
    val description: String,
    val user: User,

    )