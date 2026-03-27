package com.example.transportguide.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.transportguide.network.ApiService
import com.example.transportguide.utils.NetworkUtils

class RouteRepository(private val routeDao: RouteDao, private val apiService: ApiService) {

    // Весь список из локальной БД
    val allRoutes: LiveData<List<Route>> = routeDao.getAll()

    // Метод для удаления (нужен для ViewModel)
    suspend fun delete(route: Route) {
        routeDao.delete(route)
    }

    // Метод для вставки (нужен для обновления из API)
    suspend fun insert(route: Route) {
        routeDao.insert(route)
    }

    // Логика ОФФЛАЙН-РЕЖИМА (Веб-API + Кэширование)
    suspend fun refreshCache(context: Context) {
        if (NetworkUtils.isInternetAvailable(context)) {
            try {
                val response = apiService.getTransportNews()
                response.forEach { news ->
                    val route = Route(
                        number = news.id.toString(),
                        description = news.title,
                        date = "API Online"
                    )
                    routeDao.insert(route)
                }
            } catch (e: Exception) {
                // Ошибка сети — данные не обновятся, но старые останутся
            }
        }
    }
}