package com.example.transportguide

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.transportguide.data.AppDatabase
import com.example.transportguide.data.Route
import com.example.transportguide.data.RouteRepository
import com.example.transportguide.network.ApiService
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RouteRepository
    val allRoutes: LiveData<List<Route>>

    init {
        val dao = AppDatabase.getDatabase(application).routeDao()
        val api = ApiService.create()
        repository = RouteRepository(dao, api)
        allRoutes = repository.allRoutes

        //refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            repository.refreshCache(getApplication())
        }
    }

    // Метод удаления, который вызывается из фрагмента
    fun deleteRoute(route: Route) {
        viewModelScope.launch {
            repository.delete(route) // Теперь это слово не будет красным
        }
    }

    fun clearAllRoutes() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
}