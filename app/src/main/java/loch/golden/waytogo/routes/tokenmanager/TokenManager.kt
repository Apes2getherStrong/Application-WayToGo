package loch.golden.waytogo.routes.tokenmanager

import android.content.Context

class TokenManager (context: Context) {

    private val prefs = context.getSharedPreferences("auth",Context.MODE_PRIVATE)

    fun saveToken(token : String) {
        with(prefs.edit()) {
            putString("auth_token", token)
            apply()
        }
    }

    fun getToken() : String? {
        return prefs.getString("auth_token",null);
    }
}