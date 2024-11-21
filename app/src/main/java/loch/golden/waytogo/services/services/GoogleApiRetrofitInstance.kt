package loch.golden.waytogo.services.services

import loch.golden.waytogo.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object GoogleApiRetrofitInstance {
    val apiService: GoogleApiService by lazy {
        retrofit.create(GoogleApiService::class.java)
    }
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.GOOGLE_API_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

}