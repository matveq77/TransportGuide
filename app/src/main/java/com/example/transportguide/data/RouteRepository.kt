package com.example.transportguide.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.transportguide.network.ApiService
import com.example.transportguide.utils.NetworkUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class RouteRepository(private val routeDao: RouteDao, private val apiService: ApiService) {

    private val firestore = FirebaseFirestore.getInstance()
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getRoutes(userId: String): LiveData<List<Route>> = routeDao.getAll(userId)

    suspend fun delete(route: Route) { routeDao.delete(route) }
    suspend fun deleteAll(userId: String) { routeDao.deleteAll(userId) }
    suspend fun insert(route: Route) { routeDao.insert(route) }
    suspend fun update(route: Route) { routeDao.update(route) }

    // Лабораторная 4: Real-time sync без дубликатов для конкретного пользователя
    fun startRealtimeSync(userId: String) {
        firestore.collection("users").document(userId).collection("routes")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                snapshots?.let { querySnapshot ->
                    repositoryScope.launch {
                        querySnapshot.documents.forEach { doc ->
                            val route = Route(
                                userId = userId,
                                number = doc.getString("number") ?: "",
                                description = doc.getString("description") ?: "",
                                date = doc.getString("date") ?: "",
                                imageUrl = doc.getString("imageUrl")
                            )
                            routeDao.insert(route)
                        }
                    }
                }
            }
    }

    suspend fun refreshCache(context: Context, userId: String) {
        if (NetworkUtils.isInternetAvailable(context)) {
            try {
                val response = apiService.getStations("Bern")
                val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                val currentTimestamp = sdf.format(java.util.Date())
                response.stations.forEach { station ->
                    if (station.name != null) {
                        routeDao.insert(Route(userId = userId, number = station.id ?: "N/A", description = station.name, date = currentTimestamp))
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun fetchFromCloud(userId: String, onComplete: () -> Unit) {
        firestore.collection("users").document(userId).collection("routes").get().addOnSuccessListener { result ->
            repositoryScope.launch {
                result.documents.forEach { doc ->
                    val route = Route(
                        userId = userId,
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