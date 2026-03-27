package com.example.transportguide.data

import android.content.Context
import com.example.transportguide.network.ApiService
import com.example.transportguide.utils.NetworkUtils

class RouteRepository(private val routeDao: RouteDao, private val apiService: ApiService) {

    val allRoutes = routeDao.getAll()

    suspend fun delete(route: Route) = routeDao.delete(route)
    suspend fun deleteAll() = routeDao.deleteAll()

    suspend fun refreshCache(context: Context) {
        if (NetworkUtils.isInternetAvailable(context)) {
            try {
                val response = apiService.getStations("Bern")

                // Получаем текущую дату и время
                // Создаем формат
                val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())

                // ПРИНУДИТЕЛЬНО устанавливаем часовой пояс (например, МСК)
                sdf.timeZone = java.util.TimeZone.getTimeZone("GMT+3")

                val currentTimestamp = sdf.format(java.util.Date())

                response.stations.forEach { station ->
                    if (station.name != null) {
                        val route = Route(
                            number = station.id ?: "N/A",
                            description = station.name,
                            date = currentTimestamp // Записываем реальное время загрузки
                        )
                        routeDao.insert(route)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Ошибка: ${e.message}")
            }
        }
    }
}