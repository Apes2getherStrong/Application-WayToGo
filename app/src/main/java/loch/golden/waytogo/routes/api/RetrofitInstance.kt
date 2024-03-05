package loch.golden.waytogo.routes.api

import loch.golden.waytogo.routes.utils.Constants.Companion.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}