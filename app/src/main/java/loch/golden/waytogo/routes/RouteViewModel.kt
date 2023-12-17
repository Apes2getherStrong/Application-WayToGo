package loch.golden.waytogo.routes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import loch.golden.waytogo.routes.model.Route
import loch.golden.waytogo.routes.repository.RouteRepository

class RouteViewModel(private val routeRepository: RouteRepository): ViewModel() {

    val routeResponse: MutableLiveData<List<Route>> = MutableLiveData()
    fun getRoutes() {
        viewModelScope.launch {
            val response = routeRepository.getRoutes()
            routeResponse.value = response

        }

    }
}