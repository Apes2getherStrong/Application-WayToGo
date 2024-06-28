package loch.golden.waytogo.map.navigation

import com.google.gson.JsonParser
import loch.golden.waytogo.routes.api.ApiService
import loch.golden.waytogo.routes.utils.Constants
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.lang.reflect.Type

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