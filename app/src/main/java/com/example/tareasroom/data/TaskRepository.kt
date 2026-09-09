package com.example.tareasroom.data

import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    val tasks: Flow<List<Task>>
    suspend fun insert(task: Task)
    suspend fun update(task: Task)
    suspend fun delete(task: Task)
    suspend fun setCompleted(id: Long, completed: Boolean)
}

class RoomTaskRepository(private val dao: TaskDao) : TaskRepository {
    override val tasks = dao.observeTasks()
    override suspend fun insert(task: Task) { dao.insert(task) }
    override suspend fun update(task: Task) { check(dao.update(task) == 1) }
    override suspend fun delete(task: Task) { check(dao.delete(task) == 1) }
    override suspend fun setCompleted(id: Long, completed: Boolean) {
        check(dao.setCompleted(id, completed) == 1)
    }
}
