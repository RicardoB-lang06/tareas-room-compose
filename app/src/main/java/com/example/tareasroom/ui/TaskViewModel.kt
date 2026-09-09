package com.example.tareasroom.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.tareasroom.TaskApplication
import com.example.tareasroom.data.Task
import com.example.tareasroom.data.TaskRepository
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskEditor(
    val original: Task? = null,
    val title: String = "",
    val description: String = "",
    val dueDate: Long? = TaskDates.encode(LocalDate.now()),
    val errors: ValidationErrors = ValidationErrors(),
    val saving: Boolean = false,
    val saveError: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    private val refresh = MutableStateFlow(0)
    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()
    private val _loadError = MutableStateFlow<String?>(null)
    val loadError = _loadError.asStateFlow()
    val tasks = refresh.flatMapLatest {
        repository.tasks.onStart { _loading.value = true; _loadError.value = null }
            .onEach { _loading.value = false }
            .catch { error ->
                if (error is CancellationException) throw error
                _loading.value = false
                _loadError.value = "No pudimos cargar tus tareas. Intenta de nuevo."
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editor = MutableStateFlow<TaskEditor?>(null)
    val editor = _editor.asStateFlow()
    private val _busyIds = MutableStateFlow<Set<Long>>(emptySet())
    val busyIds = _busyIds.asStateFlow()
    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messages = messageChannel.receiveAsFlow()

    fun retryLoading() { refresh.update { it + 1 } }
    fun openCreateDialog() { _editor.value = TaskEditor() }
    fun openEditDialog(task: Task) {
        if (task.id !in _busyIds.value) {
            _editor.value = TaskEditor(task, task.title, task.description, task.dueDate)
        }
    }
    fun closeDialog() { if (_editor.value?.saving != true) _editor.value = null }
    fun changeTitle(value: String) = edit { copy(title = value, errors = errors.copy(title = null)) }
    fun changeDescription(value: String) = edit {
        copy(description = value, errors = errors.copy(description = null))
    }
    fun changeDueDate(value: Long?) = edit { copy(dueDate = value, errors = errors.copy(dueDate = null)) }
    private fun edit(transform: TaskEditor.() -> TaskEditor) {
        _editor.update { current -> if (current?.saving == false) current.transform().copy(saveError = null) else current }
    }

    fun saveTask() {
        val form = _editor.value ?: return
        if (form.saving) return
        val errors = TaskValidation.validate(form.title, form.description, form.dueDate, form.original?.dueDate)
        if (!errors.isValid) { _editor.value = form.copy(errors = errors); return }
        val task = Task(
            id = form.original?.id ?: 0,
            title = form.title.trim(),
            description = form.description.trim(),
            isCompleted = form.original?.isCompleted ?: false,
            dueDate = requireNotNull(form.dueDate),
        )
        _editor.value = form.copy(saving = true, errors = ValidationErrors(), saveError = null)
        viewModelScope.launch {
            try {
                if (form.original == null) addTask(task) else repository.update(task)
                _editor.value = null
                messageChannel.send(if (form.original == null) "Tarea creada" else "Cambios guardados")
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _editor.value = form.copy(saveError = "No pudimos guardar la tarea. Intenta de nuevo.")
            }
        }
    }

    private suspend fun addTask(task: Task) = repository.insert(task)

    fun toggleTaskStatus(task: Task, completed: Boolean = !task.isCompleted) {
        mutate(task.id, "No pudimos cambiar el estado.") { repository.setCompleted(task.id, completed) }
    }
    fun deleteTask(task: Task) {
        mutate(task.id, "No pudimos eliminar la tarea.") {
            repository.delete(task)
            messageChannel.send("Tarea eliminada")
        }
    }
    private fun mutate(id: Long, failureMessage: String, action: suspend () -> Unit) {
        if (id in _busyIds.value) return
        _busyIds.update { it + id }
        viewModelScope.launch {
            try { action() }
            catch (error: Exception) {
                if (error is CancellationException) throw error
                messageChannel.send(failureMessage)
            } finally { _busyIds.update { it - id } }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                require(modelClass.isAssignableFrom(TaskViewModel::class.java))
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                    as TaskApplication
                @Suppress("UNCHECKED_CAST")
                return TaskViewModel(application.repository) as T
            }
        }
    }
}
