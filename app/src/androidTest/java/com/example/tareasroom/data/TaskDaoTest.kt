package com.example.tareasroom.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    private lateinit var db: AppDatabase
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val name = "tasks-persistence-test.db"
    @Before fun setup() {
        context.deleteDatabase(name)
        db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
    }
    @After fun cleanup() { db.close(); context.deleteDatabase(name) }

    @Test fun crudOrderingAndPersistenceAfterReopen() = runBlocking {
        var dao = db.taskDao()
        val firstId = dao.insert(Task(title = "Primera", description = "Detalle", dueDate = 1000))
        val secondId = dao.insert(Task(title = "Segunda", description = "", dueDate = 2000))
        assertTrue(firstId > 0)
        assertNotEquals(firstId, secondId)
        assertEquals(listOf(firstId, secondId), dao.observeTasks().first().map { it.id })
        val original = dao.observeTasks().first().first()
        assertEquals(1, dao.update(original.copy(title = "Editada", description = "Nuevo detalle", dueDate = 3000)))
        assertEquals(1, dao.setCompleted(secondId, true))
        db.close()
        db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        dao = db.taskDao()
        val persisted = dao.observeTasks().first()
        assertEquals("Editada", persisted.first().title)
        assertEquals("Nuevo detalle", persisted.first().description)
        assertEquals(3000L, persisted.first().dueDate)
        assertTrue(persisted.last().isCompleted)
        assertEquals(1, dao.delete(persisted.first()))
        assertEquals(listOf(secondId), dao.observeTasks().first().map { it.id })
    }
}
