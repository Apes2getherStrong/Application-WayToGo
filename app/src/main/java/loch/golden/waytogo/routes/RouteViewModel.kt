package loch.golden.waytogo.routes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import loch.golden.waytogo.routes.model.Route
import loch.golden.waytogo.routes.model.RouteListResponse
import loch.golden.waytogo.routes.repository.RouteRepository
import retrofit2.Response

class RouteViewModel(private val routeRepository: RouteRepository): ViewModel() {

    val routeResponse: MutableLiveData<Response<RouteListResponse>> = MutableLiveData()
    fun getRoutes(pageNumber: Int, pageSize: Int) {
        viewModelScope.launch {
            val response = routeRepository.getRoutes(pageNumber,pageSize)
            routeResponse.value = response
        }
    }
}