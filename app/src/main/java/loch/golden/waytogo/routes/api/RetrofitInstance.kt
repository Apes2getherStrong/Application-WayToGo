package loch.golden.waytogo.routes.api

import com.google.gson.GsonBuilder
import loch.golden.waytogo.routes.utils.Constants.Companion.BASE_URL
import loch.golden.waytogo.user.tokenmanager.AuthInterceptor
import loch.golden.waytogo.user.tokenmanager.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitInstance {

    private lateinit var tokenManager: TokenManager
    fun getTokenManager(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(10000, TimeUnit.MILLISECONDS)
            .pingInterval(3, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }
}