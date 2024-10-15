package loch.golden.waytogo.routes.viewmodel

import android.media.Image
import android.net.http.NetworkException
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import loch.golden.waytogo.audio.Audio
import loch.golden.waytogo.audio.AudioListResponse
import loch.golden.waytogo.routes.model.maplocation.MapLocation
import loch.golden.waytogo.routes.model.maplocation.MapLocationListResponse
import loch.golden.waytogo.routes.model.maplocation.MapLocationRequest
import loch.golden.waytogo.routes.model.relations.RouteWithMapLocations
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocation
import loch.golden.waytogo.routes.model.routemaplocation.RouteMapLocationRequest
import loch.golden.waytogo.routes.paging.RoutePagingSource
import loch.golden.waytogo.routes.repository.IdByteArray
import loch.golden.waytogo.routes.repository.RouteRepository
import loch.golden.waytogo.user.model.User
import loch.golden.waytogo.user.model.auth.AuthRequest
import loch.golden.waytogo.user.model.auth.AuthResponse
import okhttp3.MultipartBody
import retrofit2.HttpException
import retrofit2.Response

class RouteViewModel(private val routeRepository: RouteRepository) : ViewModel() {

    //val routeResponse: MutableLiveData<Response<RouteListResponse>> = MutableLiveData()
    val userResponse: MutableLiveData<Response<User>> = MutableLiveData()
    val myRouteResponse: MutableLiveData<Response<Route>> = MutableLiveData()
    val myMapLocationsResponse: MutableLiveData<Response<MapLocationListResponse>> =
        MutableLiveData()
    val allRoutes: LiveData<List<Route>> = routeRepository.allRoutes.asLiveData()
    val routeWithLocationsFromDb: MutableLiveData<RouteWithMapLocations> = MutableLiveData()
    val routeFromDb: MutableLiveData<Route> = MutableLiveData()
    val currentRouteImage: MutableLiveData<Response<ByteArray>> = MutableLiveData()
    val mapLocationAudios: MutableLiveData<Response<List<String>>> = MutableLiveData()
    val authResponse: MutableLiveData<AuthResponse?> = MutableLiveData()
    val registerResponse: MutableLiveData<User?> = MutableLiveData()
    val sequenceNr: MutableLiveData<Int> = MutableLiveData()
    private val _audioFile = MutableLiveData<IdByteArray>()
    val audioFile: LiveData<IdByteArray> get() = _audioFile
    val audioResponse: MutableLiveData<Response<AudioListResponse>> = MutableLiveData()

    private val _currentMapImage = MutableLiveData<IdByteArray>()
    val currentMapImage: LiveData<IdByteArray> get() = _currentMapImage

    private val _putRouteResponse = MutableLiveData<Response<Void>>()
    val putRouteResponse: LiveData<Response<Void>> get() = _putRouteResponse

    private val _putMapLocationResponse = MutableLiveData<Response<Void>>()
    val putMapLocationResponse: LiveData<Response<Void>> get() = _putMapLocationResponse

    private val _putAudioResponse = MutableLiveData<Response<Void>>()
    val putAudioResponse: LiveData<Response<Void>> get() = _putAudioResponse

    private val _putRouteMapLocationResponse = MutableLiveData<Response<Void>>()
    val putRouteMapLocationResponse: LiveData<Response<Void>> get() = _putRouteMapLocationResponse

//    private val _deleteRouteResponse = MutableLiveData<Response<Route>>()
//    val deleteRouteResponse: LiveData<Response<Route>> = _deleteRouteResponse
//
//    private val _deleteMapLocationResponse= MutableLiveData<Response<MapLocation>>()
//    val deleteMapLocationResponse: LiveData<Response<MapLocation>> = _deleteMapLocationResponse
//
//    private val _deleteRouteMapLocationResponse = MutableLiveData<Response<RouteMapLocation>>()
//    val deleteRouteMapLocationResponse: LiveData<Response<RouteMapLocation>> = _deleteRouteMapLocationResponse
//
//    private val _deleteAudioResponse = MutableLiveData<Response<Audio>>()
//    val deleteAudioResponse: LiveData<Response<Audio>> = _deleteAudioResponse

//    fun insertRouteWithMapLocations(routeWithMapLocations: RouteWithMapLocations) =
//        viewModelScope.launch {
//            routeRepository.insertRouteWithMapLocations(routeWithMapLocations)
//        }
//
//    fun deleteRouteWithMapLocations(routeWithMapLocations: RouteWithMapLocations) =
//        viewModelScope.launch {
//            routeRepository.deleteRouteWithMapLocations(routeWithMapLocations)
//        }

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
            routeRepository.insertRouteMapLocation(RouteMapLocation(routeId, mapLocation.id, sequenceNr))
        }

    fun updateMapLocation(mapLocation: MapLocation) = viewModelScope.launch {
        routeRepository.updateMapLocation(mapLocation)
    }

    fun deleteMapLocation(mapLocation: MapLocation) =
        viewModelScope.launch {
            routeRepository.deleteMapLocation(mapLocation)
            routeRepository.deleteRouteMapLocationById(mapLocation.id)

        }

    suspend fun getSequenceNrByMapLocationId(mapLocationId: String): Int {
        return withContext(Dispatchers.IO) {
            routeRepository.getSequenceNrByMapLocationId(mapLocationId)
        }
    }

    fun updateRouteMapLocationSequenceNrById(mapLocationId: String, newSequenceNr: Int) {
        viewModelScope.launch {
            routeRepository.updateRouteMapLocationSequenceNrById(mapLocationId, newSequenceNr)
        }
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

    fun getUserByUserId(userId: String) {
        viewModelScope.launch {
            val response = routeRepository.getUserByUserId(userId)
            userResponse.value = response

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

    suspend fun getBlockingRouteImage(routeUid: String): Response<ByteArray> {
        return withContext(Dispatchers.IO) {
            routeRepository.getRouteImage(routeUid)
        }
    }

    fun getMapLocationImage(mapLocationId: String) {
        viewModelScope.launch {
            val currentImageBytes = routeRepository.getMapLocationImage(mapLocationId)
            _currentMapImage.value = IdByteArray(mapLocationId, currentImageBytes)
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
            if (response.isSuccessful) {
                response.body()?.let { newAudio ->
                    callback(newAudio)
                }
            } else {
                Log.e("postAudio", "Error: ${response.errorBody()?.string()}")
            }
        }
    }

    fun getAudioFile(audioId: String, mapLocationId: String) {
        viewModelScope.launch {

            val response = routeRepository.getAudioFile(audioId)
            _audioFile.value = IdByteArray(mapLocationId, response)

        }
    }

    fun getAudioByMapLocationId(mapLocationId: String) {
        viewModelScope.launch {
            audioResponse.value = routeRepository.getAudioByMapLocationId(mapLocationId)
        }
    }

    fun postAudioFile(audioId: String?, audioFile: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                routeRepository.postAudioFile(audioId, audioFile)
            } catch (e: Exception) {
                Log.d("Nie dziala audio", e.toString())
            }
        }
    }

    fun putImageToMapLocation(mapLocationId: String, imageFile: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                routeRepository.putImageToMapLocation(mapLocationId, imageFile)
            } catch (e: Exception) {
                Log.d("Nie dziala image", e.toString())
            }

        }
    }

    fun putImageToRoute(routeId: String, imageFile: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                routeRepository.putImageToRoute(routeId, imageFile)
            } catch (e: Exception) {
                Log.d("Warmbier", " kurde bele image ruta nie dziala $e")
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

    fun putRouteById(routeId: String, route: Route) = viewModelScope.launch {
        try {
            val response = routeRepository.putRouteById(routeId, route)
            _putRouteResponse.postValue(response)
            if (!response.isSuccessful) {
                Log.e("putRouteById", "Error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("putRouteById", "Exception: ${e.message}")
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

    fun putMapLocationById(mapLocationId: String, mapLocation: MapLocationRequest) = viewModelScope.launch {
        try {
            val response = routeRepository.putMapLocationById(mapLocationId, mapLocation)
            _putMapLocationResponse.postValue(response)
        } catch (e: Exception) {
            Log.e("putMapLocationById", "Exception: ${e.message}")
        }
    }


    fun postRouteMapLocation(routeMapLocation: RouteMapLocationRequest, callback: (RouteMapLocationRequest) -> Unit) =
        viewModelScope.launch {
            val response = routeRepository.postRouteMapLocation(routeMapLocation)
            if (response.isSuccessful) {
                response.body()?.let { newRouteMapLocation ->
                    callback(newRouteMapLocation)
                }
            }
        }

    fun putRouteMapLocationById(routeMapLocationId: String, routeMapLocation: RouteMapLocationRequest) =
        viewModelScope.launch {
            try {
                val response = routeRepository.putRouteMapLocationById(routeMapLocationId, routeMapLocation)
                _putRouteMapLocationResponse.postValue(response)
            } catch (e: Exception) {
                Log.e("putRouteMapLocationById", "Exception: ${e.message}")
            }
        }

    fun login(authRequest: AuthRequest) {
        viewModelScope.launch {
            val response = routeRepository.login(authRequest)
            if (response.isSuccessful) {
                authResponse.postValue(response.body())
            }
            else {
               authResponse.postValue(null)
            }
        }
    }

    fun register(user: User) {
        viewModelScope.launch {
            val response = routeRepository.register(user)
            if(response.isSuccessful) {
                registerResponse.postValue(response.body())
            }
            else {
                registerResponse.postValue(null)
            }
        }
    }

    fun deleteRouteById(routeId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response: Response<Route> = routeRepository.deleteRouteById(routeId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure("Delete failed, response code: ${response.code()}")
                }
            } catch (e: Exception) {
                onFailure(e.message ?: "Error")
            }
        }
    }

    fun deleteMapLocationById(mapLocationId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response: Response<MapLocation> = routeRepository.deleteMapLocationById(mapLocationId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure("Delete failed, response code: ${response.code()}")
                }
            } catch (e: Exception) {
                onFailure(e.message ?: "Error")
            }
        }
    }

    fun deleteRouteMapLocationByIdApi(routeMapLocationId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response: Response<RouteMapLocation> = routeRepository.deleteRouteMapLocationByIdApi(routeMapLocationId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure("Delete failed, response code: ${response.code()}")
                }
            } catch (e: Exception) {
                onFailure(e.message ?: "Error")
            }
        }
    }

    fun deleteAudioById(audioId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response: Response<Audio> = routeRepository.deleteAudioById(audioId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure("Delete failed, response code: ${response.code()}")
                }
            } catch (e: Exception) {
                onFailure(e.message ?: "Error")
            }
        }
    }


}
