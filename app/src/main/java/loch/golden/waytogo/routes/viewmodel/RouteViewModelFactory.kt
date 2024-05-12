package loch.golden.waytogo.routes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import loch.golden.waytogo.routes.repository.RouteRepository

class RouteViewModelFactory(
    private val repository: RouteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(RouteViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return RouteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }


}