package loch.golden.waytogo.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import loch.golden.waytogo.routes.repository.RouteRepository

class RouteViewModelFactory(
    private val repository: RouteRepository)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RouteViewModel(repository) as T
    }

}