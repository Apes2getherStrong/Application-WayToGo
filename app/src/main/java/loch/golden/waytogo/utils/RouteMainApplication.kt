package loch.golden.waytogo.utils

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import loch.golden.waytogo.repositories.RouteRepository
import loch.golden.waytogo.room.WayToGoDatabase

@HiltAndroidApp
class RouteMainApplication : Application() {

}