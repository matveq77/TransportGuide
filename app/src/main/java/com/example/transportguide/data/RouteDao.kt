package com.example.transportguide.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes WHERE userId = :userId ORDER BY id DESC")
    fun getAll(userId: String): LiveData<List<Route>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: Route): Long

    @Update
    suspend fun update(route: Route): Int

    @Delete
    suspend fun delete(route: Route): Int

    @Query("DELETE FROM routes WHERE userId = :userId")
    suspend fun deleteAll(userId: String): Int

    @Query("SELECT * FROM routes WHERE userId = :userId")
    suspend fun getAllOnce(userId: String): List<Route>
}