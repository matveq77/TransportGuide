package com.example.transportguide.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes ORDER BY id DESC")
    fun getAll(): LiveData<List<Route>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Заменять старое новым, если ID совпал
    suspend fun insert(route: Route)

    @Update // Добавили
    suspend fun update(route: Route)

    @Delete // Добавили
    suspend fun delete(route: Route)

    @Query("DELETE FROM routes")
    suspend fun deleteAll()

    @Query("SELECT * FROM routes")
    suspend fun getAllOnce(): List<Route> // Получить список один раз без наблюдения
}