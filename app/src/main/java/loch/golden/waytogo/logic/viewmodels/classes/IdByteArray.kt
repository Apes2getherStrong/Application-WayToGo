package loch.golden.waytogo.logic.viewmodels.classes

import retrofit2.Response

data class IdByteArray(
    val mapLocationId: String,
    val bytes: Response<ByteArray>
)