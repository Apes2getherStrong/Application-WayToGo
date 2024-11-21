package loch.golden.waytogo.services.components

import loch.golden.waytogo.fragments.user.components.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        val token = tokenManager.getToken()
        if(token !=null){
            request.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(request.build())
    }
}