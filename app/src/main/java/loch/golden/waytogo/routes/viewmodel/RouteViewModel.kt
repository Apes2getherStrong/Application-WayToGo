package loch.golden.waytogo.routes.viewmodel

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
import loch.golden.waytogo.routes.model.maplocation.MapLocationListResponse
import loch.golden.waytogo.routes.model.route.Route
import loch.golden.waytogo.routes.paging.RoutePagingSource
import loch.golden.waytogo.routes.repository.RouteRepository
import retrofit2.Response

class RouteViewModel(private val routeRepository: RouteRepository) : ViewModel() {

    //val routeResponse: MutableLiveData<Response<RouteListResponse>> = MutableLiveData()
    val myRouteResponse: MutableLiveData<Response<Route>> = MutableLiveData()
    val myMapLocationsResponse: MutableLiveData<Response<MapLocationListResponse>> = MutableLiveData()
    val allRoutes: LiveData<List<Route>> = routeRepository.allRoutes.asLiveData()

    fun insert(route: Route) = viewModelScope.launch {
        routeRepository.insert(route)
    }

    fun updateRoute(route: Route) = viewModelScope.launch {
        routeRepository.updateRoute(route)
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

    fun getRouteById(routeUid: String)  {
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

    fun postRoute(route : Route) {
        viewModelScope.launch {
            routeRepository.postRoute(route)

        }
    }

}

//TODO przerobic na flow a nie livadata