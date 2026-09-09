package com.example.tareasroom.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    // Fecha de calendario representada como medianoche UTC, compatible con DatePicker.
    val dueDate: Long,
)
