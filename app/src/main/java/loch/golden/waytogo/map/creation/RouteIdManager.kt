package loch.golden.waytogo.map.creation

import android.content.Context

object RouteIdManager {
    private const val PREF_NAME = "creation_pref"
    private const val KEY_COUNTER = "counter"
    private const val KEY_ROUTE_PREFIX = "route_"

    private fun incrementCounter(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentCounter = sharedPreferences.getInt(KEY_COUNTER, 0)
        val newCounter = currentCounter + 1
        with(sharedPreferences.edit()) {
            putInt(KEY_COUNTER, newCounter)
            apply()
        }
    }

    fun getRouteTitle(context: Context, routeId: String): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_ROUTE_PREFIX + routeId, null)
    }

    fun getRouteIdFromTitle(context: Context, title: String): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val allEntries = sharedPreferences.all

        for ((key, value) in allEntries) {
            if (value == title) {
                // Extract the ID from the key
                val id = key.removePrefix(KEY_ROUTE_PREFIX)
                return id
            }
        }
        return null // Title not found
    }


    fun getCounterAndInc(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val counter = sharedPreferences.getInt(KEY_COUNTER, 0)
        incrementCounter(context)
        return counter
    }

    fun resetCounter(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt(KEY_COUNTER, 0)
            apply()
        }
    }

    fun putTitle(context: Context, keyAsInt: Int, title: String) {
        val key = keyAsInt.toString()
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_ROUTE_PREFIX + key, title)
            apply()
        }
    }

    fun getIdFromTitle() {

    }
}