package com.example.transportguide.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class Route(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val number: String,      // Номер маршрута
    val description: String, // Описание
    val date: String         // Дата добавления
)