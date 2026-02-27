package com.example.transportguide.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes ORDER BY id DESC")
    fun getAll(): LiveData<List<Route>>

    @Insert
    suspend fun insert(route: Route)

    @Update // Добавили
    suspend fun update(route: Route)

    @Delete // Добавили
    suspend fun delete(route: Route)
}