package loch.golden.waytogo.services.dto.audio

import com.google.gson.annotations.SerializedName
import loch.golden.waytogo.services.dto.maplocation.MapLocationRequest
import loch.golden.waytogo.services.dto.user.UserDTO

data class AudioDTO(
    val id: String,
    val name: String?,
    val description: String?,
    val userDTO: UserDTO?,
    @SerializedName("mapLocation")
    val mapLocationRequest: MapLocationRequest
)
