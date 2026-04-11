package com.example.transportguide.data

import android.content.Context
import com.example.transportguide.network.ApiService
import com.example.transportguide.utils.NetworkUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class RouteRepository(private val routeDao: RouteDao, private val apiService: ApiService) {

    val allRoutes = routeDao.getAll()
    private val firestore = FirebaseFirestore.getInstance()
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun delete(route: Route) { routeDao.delete(route) }
    suspend fun deleteAll() { routeDao.deleteAll() }
    suspend fun insert(route: Route) { routeDao.insert(route) }
    suspend fun update(route: Route) { routeDao.update(route) }

    // Лабораторная 4: Real-time sync без дубликатов
    fun startRealtimeSync() {
        firestore.collection("routes").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            snapshots?.let { querySnapshot ->
                repositoryScope.launch {
                    querySnapshot.documents.forEach { doc ->
                        val route = Route(
                            number = doc.getString("number") ?: "",
                            description = doc.getString("description") ?: "",
                            date = doc.getString("date") ?: "",
                            imageUrl = doc.getString("imageUrl")
                        )
                        // Благодаря уникальному индексу в Entity, дубликатов не будет
                        routeDao.insert(route)
                    }
                }
            }
        }
    }

    suspend fun refreshCache(context: Context) {
        if (NetworkUtils.isInternetAvailable(context)) {
            try {
                val response = apiService.getStations("Bern")
                val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                val currentTimestamp = sdf.format(java.util.Date())
                response.stations.forEach { station ->
                    if (station.name != null) {
                        routeDao.insert(Route(number = station.id ?: "N/A", description = station.name, date = currentTimestamp))
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun fetchFromCloud(onComplete: () -> Unit) {
        firestore.collection("routes").get().addOnSuccessListener { result ->
            repositoryScope.launch {
                result.documents.forEach { doc ->
                    val route = Route(
                        number = doc.getString("number") ?: "",
                        description = doc.getString("description") ?: "",
                        date = doc.getString("date") ?: "",
                        imageUrl = doc.getString("imageUrl")
                    )
                    routeDao.insert(route)
                }
                withContext(Dispatchers.Main) { onComplete() }
            }
        }
    }
}