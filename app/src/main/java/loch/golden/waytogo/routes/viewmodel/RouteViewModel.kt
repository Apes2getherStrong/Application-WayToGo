package loch.golden.waytogo.routes.viewmodel

import android.adservices.adid.AdId
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import loch.golden.waytogo.routes.model.Route
import loch.golden.waytogo.routes.paging.RoutePagingSource
import loch.golden.waytogo.routes.repository.RouteRepository
import retrofit2.Response

class RouteViewModel(private val routeRepository: RouteRepository) : ViewModel() {

    //val routeResponse: MutableLiveData<Response<RouteListResponse>> = MutableLiveData()
    val allRoutes: LiveData<List<Route>> = routeRepository.allRoutes.asLiveData()

    fun insert(route: Route) = viewModelScope.launch {
        routeRepository.insert(route)
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

    fun getRouteById(routeId: Int,callback: (Route?) -> Unit)  {
        viewModelScope.launch {
            routeRepository.getRouteById(routeId)

        }
    }

}