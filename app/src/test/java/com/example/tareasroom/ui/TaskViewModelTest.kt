package com.example.tareasroom.ui

import com.example.tareasroom.data.Task
import com.example.tareasroom.data.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun invalidFormDoesNotReachRepository() = runTest(dispatcher) {
        val repo = FakeRepository()
        val vm = TaskViewModel(repo)
        vm.openCreateDialog()
        vm.changeTitle("  ")
        vm.saveTask()
        runCurrent()
        assertTrue(repo.tasks.value.isEmpty())
        assertNotNull(vm.editor.value?.errors?.title)
    }
    @Test fun createEditToggleAndDeletePropagateThroughFlow() = runTest(dispatcher) {
        val repo = FakeRepository()
        val vm = TaskViewModel(repo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.tasks.collect {} }
        vm.openCreateDialog()
        vm.changeTitle("  Preparar entrega  ")
        vm.changeDescription("  Revisar requisitos  ")
        vm.saveTask()
        vm.saveTask() // Segundo clic mientras se guarda: debe ignorarse.
        runCurrent()
        assertEquals(1, vm.tasks.value.size)
        assertEquals("Preparar entrega", vm.tasks.value.single().title)
        assertEquals("Revisar requisitos", vm.tasks.value.single().description)
        assertNull(vm.editor.value)
        vm.openEditDialog(vm.tasks.value.single())
        vm.changeTitle("Entregar proyecto")
        vm.saveTask()
        runCurrent()
        assertEquals("Entregar proyecto", vm.tasks.value.single().title)
        vm.toggleTaskStatus(vm.tasks.value.single(), true)
        runCurrent()
        assertTrue(vm.tasks.value.single().isCompleted)
        vm.deleteTask(vm.tasks.value.single())
        runCurrent()
        assertTrue(vm.tasks.value.isEmpty())
    }
    @Test fun failedSaveKeepsFormAndCanBeRetried() = runTest(dispatcher) {
        val repo = FakeRepository().apply { failSave = true }
        val vm = TaskViewModel(repo)
        vm.openCreateDialog()
        vm.changeTitle("Conservar borrador")
        vm.saveTask()
        runCurrent()
        assertEquals("Conservar borrador", vm.editor.value?.title)
        assertNotNull(vm.editor.value?.saveError)
        assertEquals(false, vm.editor.value?.saving)
        repo.failSave = false
        vm.saveTask()
        runCurrent()
        assertNull(vm.editor.value)
        assertEquals(1, repo.tasks.value.size)
    }

    private class FakeRepository : TaskRepository {
        override val tasks = MutableStateFlow<List<Task>>(emptyList())
        var failSave = false
        override suspend fun insert(task: Task) {
            check(!failSave)
            tasks.value += task.copy(id = 1)
        }
        override suspend fun update(task: Task) { tasks.value = tasks.value.map { if (it.id == task.id) task else it } }
        override suspend fun delete(task: Task) { tasks.value = tasks.value.filterNot { it.id == task.id } }
        override suspend fun setCompleted(id: Long, completed: Boolean) {
            tasks.value = tasks.value.map { if (it.id == id) it.copy(isCompleted = completed) else it }
        }
    }
}
