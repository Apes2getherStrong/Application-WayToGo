package loch.golden.waytogo.map.creation

import android.content.Context
import java.util.UUID

object RouteIdManager {
    private const val PREF_NAME = "routes_pref"

    fun getRouteTitle(context: Context, routeId: String): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(routeId, null)
    }

    fun getRouteIdFromTitle(context: Context, title: String): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val allEntries = sharedPreferences.all

        for ((key, value) in allEntries) {
            if (value == title) {
                // Extract the ID from the key
                return key
            }
        }
        return null // Title not found
    }


    fun getId(): String {
        return UUID.randomUUID().toString()
    }

    fun putTitle(context: Context, id: String, title: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(id, title)
            apply()
        }
    }

}