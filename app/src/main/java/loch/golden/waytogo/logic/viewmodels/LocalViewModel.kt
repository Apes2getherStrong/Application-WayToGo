package loch.golden.waytogo.logic.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import loch.golden.waytogo.access.repositories.LocalRepository
import loch.golden.waytogo.data.room.entity.maplocation.MapLocation
import loch.golden.waytogo.data.room.entity.relations.RouteWithMapLocations
import loch.golden.waytogo.data.room.entity.route.Route
import loch.golden.waytogo.data.room.entity.routemaplocation.RouteMapLocation
import javax.inject.Inject

@HiltViewModel
class LocalViewModel @Inject constructor(
    private val localRepository: LocalRepository
) : ViewModel() {

    val allRoutes: LiveData<List<Route>> = localRepository.allRoutes.asLiveData()
    val routeFromDb: MutableLiveData<Route> = MutableLiveData()
    val routeWithLocationsFromDb: MutableLiveData<RouteWithMapLocations> = MutableLiveData()


    fun getRouteFromDbById(routeUid: String) {
        viewModelScope.launch {
            routeFromDb.value = localRepository.getRouteFromDbById(routeUid)
        }
    }

    fun insert(route: Route) = viewModelScope.launch {
        localRepository.insert(route)
    }

    fun updateRoute(route: Route) = viewModelScope.launch {
        localRepository.updateRoute(route)
    }

    fun deleteRoute(route: Route) = viewModelScope.launch {
        localRepository.deleteRoute(route)
    }

    fun deleteRouteWithMapLocations(routeUid: String) = viewModelScope.launch {
        localRepository.deleteRouteWithMapLocations(routeUid)
    }

    fun insertMapLocation(mapLocation: MapLocation, routeId: String, sequenceNr: Int) =
        viewModelScope.launch {
            localRepository.insertMapLocation(mapLocation)
            localRepository.insertRouteMapLocation(RouteMapLocation(routeId, mapLocation.id, sequenceNr, null))
        }

    fun updateMapLocation(mapLocation: MapLocation) = viewModelScope.launch {
        localRepository.updateMapLocation(mapLocation)
    }

    fun updateRouteMapLocation(routeMapLocation: RouteMapLocation) = viewModelScope.launch {
        localRepository.updateRouteMapLocation(routeMapLocation)
    }

    fun updateExternalId(routeUid: String, id: String, externalId: String) = viewModelScope.launch {
        Log.d("Gogo", "repoweszlo")
        localRepository.updateExternalId(routeUid, id, externalId)
    }

    fun updateRouteExternalId(routeUid: String, externalId: String?) = viewModelScope.launch {
        localRepository.updateRouteExternalId(routeUid, externalId)
    }

    fun deleteMapLocation(mapLocation: MapLocation) =
        viewModelScope.launch {
            localRepository.deleteMapLocation(mapLocation)
            localRepository.deleteRouteMapLocationById(mapLocation.id)

        }

    suspend fun getSequenceNrByMapLocationId(mapLocationId: String): Int {
        return withContext(Dispatchers.IO) {
            localRepository.getSequenceNrByMapLocationId(mapLocationId)
        }
    }

    suspend fun getRouteMapLocationByMapLocationId(mapLocationId: String): RouteMapLocation {
        return withContext(Dispatchers.IO) {
            localRepository.getRouteMapLocationByMapLocationId(mapLocationId)
        }
    }

    fun updateRouteMapLocationSequenceNrById(mapLocationId: String, newSequenceNr: Int) {
        viewModelScope.launch {
            localRepository.updateRouteMapLocationSequenceNrById(mapLocationId, newSequenceNr)
        }
    }

    fun getRouteWithMapLocations(routeUid: String) {
        viewModelScope.launch {
            routeWithLocationsFromDb.value = localRepository.getRouteWithMapLocations(routeUid)
        }
    }
}