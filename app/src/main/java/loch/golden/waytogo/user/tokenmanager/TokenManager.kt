package loch.golden.waytogo.user.tokenmanager

import android.content.Context
import android.content.SharedPreferences
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT

class TokenManager (context: Context) {

    private val prefs : SharedPreferences= context.getSharedPreferences("auth",Context.MODE_PRIVATE)

    fun saveToken(token : String) {
        with(prefs.edit()) {
            putString("auth_token", token)
            apply()
        }
    }

    fun getToken() : String? {
        return prefs.getString("auth_token",null);
    }

    fun clearToken() {
        with(prefs.edit()) {
            remove("auth_token")
            apply()
        }
    }

   fun isTokenExpired(token: String): Boolean {
        val decodedJWT: DecodedJWT = JWT.decode(token)
        val expiresAtMillis = decodedJWT.expiresAt?.time ?: return true
        val currentTimeMillis = System.currentTimeMillis()
        return expiresAtMillis < currentTimeMillis
   }

    fun getUserFromJWT(): String? {
        val token = getToken()
        return if (token != null) {
            try {
                val decodedJWT: DecodedJWT = JWT.decode(token)
                decodedJWT.getClaim("username").asString()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    fun saveUsername(username: String) {
        with(prefs.edit()) {
            putString("username", username)
            apply()
        }
    }

    fun getUsername(): String? {
        return prefs.getString("username", null)
    }
}