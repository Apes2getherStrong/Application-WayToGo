package loch.golden.waytogo.logic.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import loch.golden.waytogo.access.repositories.BackendRepository
import loch.golden.waytogo.access.services.dto.audio.AudioDTO
import loch.golden.waytogo.access.services.dto.audio.AudioListResponse
import loch.golden.waytogo.access.services.dto.auth.AuthRequest
import loch.golden.waytogo.access.services.dto.auth.AuthResponse
import loch.golden.waytogo.access.services.dto.maplocation.MapLocationListResponse
import loch.golden.waytogo.access.services.dto.maplocation.MapLocationRequest
import loch.golden.waytogo.access.services.dto.routemaplocation.RouteMapLocationRequest
import loch.golden.waytogo.access.services.dto.user.UserDTO
import loch.golden.waytogo.data.room.entity.maplocation.MapLocation
import loch.golden.waytogo.data.room.entity.route.Route
import loch.golden.waytogo.data.room.entity.routemaplocation.RouteMapLocation
import loch.golden.waytogo.logic.viewmodels.classes.IdByteArray
import loch.golden.waytogo.logic.viewmodels.components.RoutePagingSource
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class BackendViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {

    //val routeResponse: MutableLiveData<Response<RouteListResponse>> = MutableLiveData()
    val userDTOResponse: MutableLiveData<Response<UserDTO>> = MutableLiveData()
    val myRouteResponse: MutableLiveData<Response<Route>> = MutableLiveData()
    val myMapLocationsResponse: MutableLiveData<Response<MapLocationListResponse>> =
        MutableLiveData()
    val currentRouteImage: MutableLiveData<Response<ByteArray>> = MutableLiveData()
    val mapLocationAudios: MutableLiveData<Response<List<String>>> = MutableLiveData()
    val authResponse: MutableLiveData<AuthResponse?> = MutableLiveData()
    val registerResponse: MutableLiveData<UserDTO?> = MutableLiveData()
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

    private val _putUserResponse = MutableLiveData<Response<Void>>()
    val putUserResponse: LiveData<Response<Void>> get() = _putUserResponse

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

    fun putAudioById(audioId: String, audioDTO: AudioDTO) = viewModelScope.launch {
        try {
            val response = backendRepository.putAudioById(audioId, audioDTO)
            _putAudioResponse.postValue(response)
            if (!response.isSuccessful) {
                Log.e("Update User", "Error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("Update User", "Exception: ${e.message}")
        }
    }


    fun getRoutes(pageNumber: Int, pageSize: Int): Flow<PagingData<Route>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { RoutePagingSource(backendRepository) }
        )
            .flow
            .cachedIn(viewModelScope)
    }

    fun getRouteById(routeUid: String) {
        viewModelScope.launch {
            val response = backendRepository.getRouteById(routeUid)
            myRouteResponse.value = response

        }
    }

    fun getUserByUserId(userId: String) {
        viewModelScope.launch {
            val response = backendRepository.getUserByUserId(userId)
            userDTOResponse.value = response

        }
    }

    fun putUserByUserId(userId: String, userDTO: UserDTO) = viewModelScope.launch {
        try {
            val response = backendRepository.putUserByUserId(userId,userDTO)
            _putUserResponse.postValue(response)
            if (!response.isSuccessful) {
                Log.e("Update User", "Error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("Update User", "Exception: ${e.message}")
        }

    }

    fun getMapLocationsByRouteId(routeUid: String) {
        viewModelScope.launch {
            val response = backendRepository.getMapLocationsByRouteId(routeUid)
            myMapLocationsResponse.value = response
        }
    }

    fun getRouteImage(routeUid: String) {
        viewModelScope.launch {
            val currentImageBytes = backendRepository.getRouteImage(routeUid)
            currentRouteImage.value = currentImageBytes
        }
    }

    suspend fun getBlockingRouteImage(routeUid: String): Response<ByteArray> {
        return withContext(Dispatchers.IO) {
            backendRepository.getRouteImage(routeUid)
        }
    }

    fun getMapLocationImage(mapLocationId: String) {
        viewModelScope.launch {
            val currentImageBytes = backendRepository.getMapLocationImage(mapLocationId)
            _currentMapImage.value = IdByteArray(mapLocationId, currentImageBytes)
        }
    }

    fun getMapLocationAudios(mapLocationId: String) {
        viewModelScope.launch {
            mapLocationAudios.value = backendRepository.getMapLocationAudios(mapLocationId)
        }
    }

    fun postAudio(audioDTO: AudioDTO, callback: (AudioDTO) -> Unit) {
        viewModelScope.launch {
            val response = backendRepository.postAudio(audioDTO)
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

            val response = backendRepository.getAudioFile(audioId)
            _audioFile.value = IdByteArray(mapLocationId, response)

        }
    }

    fun getAudioByMapLocationId(mapLocationId: String) {
        viewModelScope.launch {
            audioResponse.value = backendRepository.getAudioByMapLocationId(mapLocationId)
        }
    }

    fun postAudioFile(audioId: String?, audioFile: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                backendRepository.postAudioFile(audioId, audioFile)
            } catch (e: Exception) {
                Log.d("Nie dziala audio", e.toString())
            }
        }
    }

    fun putImageToMapLocation(mapLocationId: String, imageFile: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                backendRepository.putImageToMapLocation(mapLocationId, imageFile)
            } catch (e: Exception) {
                Log.d("Nie dziala image", e.toString())
            }

        }
    }

    fun putImageToRoute(routeId: String, imageFile: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                backendRepository.putImageToRoute(routeId, imageFile)
            } catch (e: Exception) {
                Log.d("Warmbier", " kurde bele image ruta nie dziala $e")
            }

        }
    }

    fun postRoute(route: Route, callback: (Route) -> Unit) = viewModelScope.launch {
        try {
            val response = backendRepository.postRoute(route)
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
            val response = backendRepository.putRouteById(routeId, route)
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
                val response = backendRepository.postMapLocation(mapLocation)
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
            val response = backendRepository.putMapLocationById(mapLocationId, mapLocation)
            _putMapLocationResponse.postValue(response)
        } catch (e: Exception) {
            Log.e("putMapLocationById", "Exception: ${e.message}")
        }
    }


    fun postRouteMapLocation(routeMapLocation: RouteMapLocationRequest, callback: (RouteMapLocationRequest) -> Unit) =
        viewModelScope.launch {
            val response = backendRepository.postRouteMapLocation(routeMapLocation)
            if (response.isSuccessful) {
                response.body()?.let { newRouteMapLocation ->
                    callback(newRouteMapLocation)
                }
            }
        }

    fun putRouteMapLocationById(routeMapLocationId: String, routeMapLocation: RouteMapLocationRequest) =
        viewModelScope.launch {
            try {
                val response = backendRepository.putRouteMapLocationById(routeMapLocationId, routeMapLocation)
                _putRouteMapLocationResponse.postValue(response)
            } catch (e: Exception) {
                Log.e("putRouteMapLocationById", "Exception: ${e.message}")
            }
        }

    fun login(authRequest: AuthRequest) {
        viewModelScope.launch {
            val response = backendRepository.login(authRequest)
            if (response.isSuccessful) {
                authResponse.postValue(response.body())
            }
            else {
               authResponse.postValue(null)
            }
        }
    }

    fun register(userDTO: UserDTO) {
        viewModelScope.launch {
            val response = backendRepository.register(userDTO)
            if(response.isSuccessful) {
                registerResponse.postValue(response.body())
            }
            else {
                registerResponse.postValue(null)
            }
        }
    }

    suspend fun deleteRouteById(routeId: String): Result<Unit> {
        return try {
            val response: Response<Route> = backendRepository.deleteRouteById(routeId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed, response code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteMapLocationById(mapLocationId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response: Response<MapLocation> = backendRepository.deleteMapLocationById(mapLocationId)
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
                val response: Response<RouteMapLocation> = backendRepository.deleteRouteMapLocationByIdApi(routeMapLocationId)
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
                val response: Response<AudioDTO> = backendRepository.deleteAudioById(audioId)
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
