package com.example.transportguide.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routes",
    indices = [Index(value = ["number"], unique = true)] // Это запретит дубликаты по номеру
)
data class Route(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val number: String,
    val description: String,
    val date: String,
    val imageUrl: String? = null
)