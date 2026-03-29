package com.example.transportguide.data

import android.content.Context
import com.example.transportguide.network.ApiService
import com.example.transportguide.utils.NetworkUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RouteRepository(private val routeDao: RouteDao, private val apiService: ApiService) {

    val allRoutes = routeDao.getAll()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun delete(route: Route) { routeDao.delete(route) }
    suspend fun deleteAll() { routeDao.deleteAll() }
    suspend fun insert(route: Route) { routeDao.insert(route) }
    suspend fun update(route: Route) { routeDao.update(route) }

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

    // Загрузка данных из облака Firebase Firestore в локальную базу Room
    fun fetchFromCloud(onComplete: () -> Unit) {
        firestore.collection("routes").get().addOnSuccessListener { result ->
            val routes = result.map { doc ->
                Route(
                    number = doc.getString("number") ?: "",
                    description = doc.getString("description") ?: "",
                    date = doc.getString("date") ?: "",
                    imageUrl = doc.getString("imageUrl")
                )
            }
            GlobalScope.launch(Dispatchers.IO) {
                routes.forEach { routeDao.insert(it) }
                onComplete()
            }
        }
    }
}