package loch.golden.waytogo.routes.tokenmanager

import loch.golden.waytogo.routes.api.RetrofitInstance
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        val token = tokenManager.getToken()
        request.addHeader("Authorization", "Bearer $token")

        return chain.proceed(request.build())
    }
}