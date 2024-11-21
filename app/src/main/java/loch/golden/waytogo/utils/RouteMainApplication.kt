package loch.golden.waytogo.utils

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import loch.golden.waytogo.repositories.RouteRepository
import loch.golden.waytogo.room.WayToGoDatabase

class RouteMainApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob())
    val database by lazy { WayToGoDatabase.getDatabase(this,appScope) }
    val repository by lazy { RouteRepository(database.getRouteDao()) }

}