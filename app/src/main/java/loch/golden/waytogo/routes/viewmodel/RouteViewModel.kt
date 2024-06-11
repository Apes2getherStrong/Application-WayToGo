package loch.golden.waytogo.routes.viewmodel

import android.media.Image
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import loch.golden.waytogo.audio.Audio
import loch.golden.waytogo.routes.model.maplocation.MapLocation
import loch.golden.waytogo.routes.model.maplocation.MapLocationListResponse
import loch.golden.waytogo.routes.model.maplocation.MapLocationRequest
import loch.golden.waytogo.routes.model.relations.RouteWithMapLocations
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocation
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocationRequest
import loch.golden.waytogo.routes.paging.RoutePagingSource
import loch.golden.waytogo.routes.repository.RouteRepository
import loch.golden.waytogo.user.model.User
import loch.golden.waytogo.user.model.auth.AuthRequest
import loch.golden.waytogo.user.model.auth.AuthResponse
import okhttp3.MultipartBody
import retrofit2.Response

class RouteViewModel(private val routeRepository: RouteRepository) : ViewModel() {

    //val routeResponse: MutableLiveData<Response<RouteListResponse>> = MutableLiveData()
    val myRouteResponse: MutableLiveData<Response<Route>> = MutableLiveData()
    val myMapLocationsResponse: MutableLiveData<Response<MapLocationListResponse>> =
        MutableLiveData()
    val allRoutes: LiveData<List<Route>> = routeRepository.allRoutes.asLiveData()
    val routeWithLocationsFromDb: MutableLiveData<RouteWithMapLocations> = MutableLiveData()
    val routeFromDb: MutableLiveData<Route> = MutableLiveData()
    val currentRouteImage: MutableLiveData<ByteArray?> = MutableLiveData()
    val currentMapImage: MutableLiveData<ByteArray?> = MutableLiveData()
    val mapLocationAudios: MutableLiveData<Response<List<String>>> = MutableLiveData()
    val authResponse: MutableLiveData<AuthResponse> = MutableLiveData()
    val mapLocationAudio: MutableLiveData<ByteArray?> = MutableLiveData()
    val audioResponse: MutableLiveData<Response<Audio>> = MutableLiveData()


    fun insertRouteWithMapLocations(routeWithMapLocations: RouteWithMapLocations) =
        viewModelScope.launch {
            routeRepository.insertRouteWithMapLocations(routeWithMapLocations)
        }

    fun deleteRouteWithMapLocations(routeWithMapLocations: RouteWithMapLocations) =
        viewModelScope.launch {
            routeRepository.deleteRouteWithMapLocations(routeWithMapLocations)
        }

    fun getRouteFromDbById(routeUid: String) {
        viewModelScope.launch {
            routeFromDb.value = routeRepository.getRouteFromDbById(routeUid)
        }
    }

    fun insert(route: Route) = viewModelScope.launch {
        routeRepository.insert(route)
    }

    fun updateRoute(route: Route) = viewModelScope.launch {
        routeRepository.updateRoute(route)
    }

    fun deleteRoute(route: Route) = viewModelScope.launch {
        routeRepository.deleteRoute(route)
    }

    fun insertMapLocation(mapLocation: MapLocation, routeId: String, sequenceNr: Int) =
        viewModelScope.launch {
            routeRepository.insertMapLocation(mapLocation)
            routeRepository.insertRouteMapLocation(RouteMapLocation(routeId, mapLocation.id))
        }

    fun updateMapLocation(mapLocation: MapLocation) = viewModelScope.launch {
        routeRepository.updateMapLocation(mapLocation)
    }

    fun deleteMapLocation(mapLocation: MapLocation, routeId: String, sequenceNr: Int) =
        viewModelScope.launch {
            routeRepository.deleteMapLocation(mapLocation)
            routeRepository.deleteRouteMapLocation(RouteMapLocation(routeId, mapLocation.id))

        }

    fun getRouteWithMapLocations(routeUid: String) {
        viewModelScope.launch {
            routeWithLocationsFromDb.value = routeRepository.getRouteWithMapLocations(routeUid)
        }
    }

    fun getRoutes(pageNumber: Int, pageSize: Int): Flow<PagingData<Route>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { RoutePagingSource(routeRepository) }
        )
            .flow
            .cachedIn(viewModelScope)
    }

    fun getRouteById(routeUid: String) {
        viewModelScope.launch {
            val response = routeRepository.getRouteById(routeUid)
            myRouteResponse.value = response

        }
    }

    fun getMapLocationsByRouteId(routeUid: String) {
        viewModelScope.launch {
            val response = routeRepository.getMapLocationsByRouteId(routeUid)
            myMapLocationsResponse.value = response
        }
    }

    fun getRouteImage(routeUid: String) {
        viewModelScope.launch {
            val currentImageBytes = routeRepository.getRouteImage(routeUid)
            currentRouteImage.value = currentImageBytes
        }
    }

    fun getMapLocationImage(mapLocationId: String) {
        viewModelScope.launch {
            val currentImageBytes = routeRepository.getMapLocationImage(mapLocationId)
            currentMapImage.value = currentImageBytes
        }
    }

    fun getMapLocationAudios(mapLocationId: String) {
        viewModelScope.launch {
            mapLocationAudios.value = routeRepository.getMapLocationAudios(mapLocationId)
        }
    }

    fun postAudio(audio: Audio, callback: (Audio) -> Unit) {
        viewModelScope.launch {
            val response = routeRepository.postAudio(audio)
            if(response.isSuccessful) {
                response.body()?.let { newAudio ->
                    callback(newAudio)
                }
            }else{
                Log.e("postAudio", "Error: ${response.errorBody()?.string()}")
            }
        }
    }

    fun getAudioFile(audioId: String) {
        viewModelScope.launch {
            mapLocationAudio.value = routeRepository.getAudioFile(audioId)
        }
    }

    fun getAudioByMapLocationId(mapLocationId: String) {
        viewModelScope.launch{
            audioResponse.value = routeRepository.getAudioByMapLocationId(mapLocationId)
        }
    }

    fun postAudioFile(audioId: String?, audioFile: MultipartBody.Part){
        viewModelScope.launch {
            try {
                routeRepository.postAudioFile(audioId,audioFile)
            }catch (e: Exception){
                Log.d("Nie dziala audio",e.toString())
            }
        }
    }

    fun putImageToMapLocation(mapLocationId: String, imageFile: MultipartBody.Part){
        viewModelScope.launch {
            try {
                routeRepository.putImageToMapLocation(mapLocationId,imageFile)
            }catch (e: Exception){
                Log.d("Nie dziala image",e.toString())
            }

        }
    }

    fun postRoute(route: Route, callback: (Route) -> Unit) = viewModelScope.launch {
        try {
            val response = routeRepository.postRoute(route)
            if (response.isSuccessful) {
                response.body()?.let { newRoute ->
                    callback(newRoute)
                }
            } else {
                Log.e("postRoute", "Error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("postRoute", "Exception: ${e.message}")
        }

    }


    fun postMapLocation(mapLocation: MapLocationRequest, callback: (MapLocationRequest) -> Unit) =
        viewModelScope.launch {
            try {
                val response = routeRepository.postMapLocation(mapLocation)
                if (response.isSuccessful) {
                    response.body()?.let { newMapLocation ->
                        callback(newMapLocation)
                    }
                } else {
                    Log.e("postMapLocation", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("postMapLocation", "Exception: ${e.message}")
            }

        }

    fun postRouteMapLocation(routeMapLocation: RouteMapLocationRequest) = viewModelScope.launch {
        routeRepository.postRouteMapLocation(routeMapLocation)
    }

    fun login(authRequest: AuthRequest) {
        viewModelScope.launch {
            val response = routeRepository.login(authRequest)
            if (response.isSuccessful) {
                authResponse.postValue(response.body())
            }

        }

    }

    fun register(user: User) = viewModelScope.launch {
        routeRepository.register(user)
    }

}

//TODO przerobic na flow a nie livadata