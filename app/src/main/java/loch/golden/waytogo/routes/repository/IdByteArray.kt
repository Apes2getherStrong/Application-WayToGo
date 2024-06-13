package loch.golden.waytogo.routes.repository

import retrofit2.Response

data class IdByteArray(
    val mapLocationId: String,
    val bytes: Response<ByteArray>
)