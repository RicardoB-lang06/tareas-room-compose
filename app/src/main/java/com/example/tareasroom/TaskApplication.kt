package com.example.tareasroom

import android.app.Application
import com.example.tareasroom.data.AppDatabase
import com.example.tareasroom.data.RoomTaskRepository

class TaskApplication : Application() {
    val repository by lazy { RoomTaskRepository(AppDatabase.getInstance(this).taskDao()) }
}
