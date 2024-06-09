package loch.golden.waytogo.audio

import com.google.gson.annotations.SerializedName
import loch.golden.waytogo.routes.model.maplocation.MapLocationRequest
import loch.golden.waytogo.user.model.User

data class Audio(
    val name: String,
    val description: String,
    val user: User,
    @SerializedName("mapLocation")
    val mapLocationRequest: MapLocationRequest
)
