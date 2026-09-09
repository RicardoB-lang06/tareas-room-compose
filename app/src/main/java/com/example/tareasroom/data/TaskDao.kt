package com.example.tareasroom.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDate ASC, id DESC")
    fun observeTasks(): Flow<List<Task>>

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task): Int

    @Delete
    suspend fun delete(task: Task): Int

    // Actualiza sólo el estado para evitar sobrescribir otros campos con una copia antigua.
    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean): Int
}
