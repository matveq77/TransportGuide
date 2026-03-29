package com.example.transportguide.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes ORDER BY id DESC")
    fun getAll(): LiveData<List<Route>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: Route): Long

    @Update
    suspend fun update(route: Route): Int

    @Delete
    suspend fun delete(route: Route): Int

    @Query("DELETE FROM routes")
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM routes")
    suspend fun getAllOnce(): List<Route>
}