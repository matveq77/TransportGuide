package com.example.transportguide.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routes",
    indices = [Index(value = ["number", "userId"], unique = true)] // Уникальность номера в рамках пользователя
)
data class Route(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String, // ID пользователя
    val number: String,
    val description: String,
    val date: String,
    val imageUrl: String? = null
)