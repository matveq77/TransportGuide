package com.example.transportguide

import android.app.Application
import androidx.lifecycle.*
import com.example.transportguide.data.*
import com.example.transportguide.network.ApiService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RouteRepository
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val allRoutesFromDb: LiveData<List<Route>>
    private val searchQuery = MutableLiveData("")
    private val sortOrder = MutableLiveData(0)

    init {
        val dao = AppDatabase.getDatabase(application).routeDao()
        repository = RouteRepository(dao, ApiService.create())
        allRoutesFromDb = repository.getRoutes(userId)
        if (userId.isNotEmpty()) {
            repository.startRealtimeSync(userId)
        }
    }

    val filteredRoutes: LiveData<List<Route>> = MediatorLiveData<List<Route>>().apply {
        addSource(allRoutesFromDb) { value = filterAndSort(it, searchQuery.value, sortOrder.value) }
        addSource(searchQuery) { value = filterAndSort(allRoutesFromDb.value, it, sortOrder.value) }
        addSource(sortOrder) { value = filterAndSort(allRoutesFromDb.value, searchQuery.value, it) }
    }

    fun setSearchQuery(query: String) { searchQuery.value = query }
    fun toggleSort() { sortOrder.value = if (sortOrder.value == 0) 1 else 0 }
    fun refreshData() = viewModelScope.launch { repository.refreshCache(getApplication(), userId) }
    fun deleteRoute(route: Route) = viewModelScope.launch { repository.delete(route) }
    fun clearAllRoutes() = viewModelScope.launch { repository.deleteAll(userId) }
    fun loadFromCloud() { repository.fetchFromCloud(userId) { } }

    private fun filterAndSort(list: List<Route>?, query: String?, order: Int?): List<Route> {
        var result = list ?: emptyList()
        if (!query.isNullOrEmpty()) {
            result = result.filter { it.number.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
        }
        return if (order == 0) result.sortedBy { it.number } else result.sortedBy { it.description }
    }
}