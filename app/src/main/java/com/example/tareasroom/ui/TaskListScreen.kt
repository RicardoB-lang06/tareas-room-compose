package com.example.tareasroom.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tareasroom.data.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(viewModel: TaskViewModel = viewModel(factory = TaskViewModel.Factory)) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val loadError by viewModel.loadError.collectAsStateWithLifecycle()
    val editor by viewModel.editor.collectAsStateWithLifecycle()
    val busyIds by viewModel.busyIds.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) { viewModel.messages.collect { snackbar.showSnackbar(it) } }
    val visibleTasks = tasks.filter {
        when (filter) { 1 -> !it.isCompleted; 2 -> it.isCompleted; else -> true }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { CenterAlignedTopAppBar(title = { Text("MIS TAREAS", style = MaterialTheme.typography.titleSmall) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openCreateDialog) {
                Text("+", style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.semantics { contentDescription = "Crear tarea" })
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).widthIn(max = 720.dp)
                .padding(horizontal = 20.dp),
        ) {
            Text("Un paso a la vez.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Organiza tu día y celebra cada avance.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("TU PROGRESO", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("${tasks.count { it.isCompleted }} de ${tasks.size} completadas",
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { if (tasks.isEmpty()) 0f else tasks.count { it.isCompleted }.toFloat() / tasks.size },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todas", "Pendientes", "Hechas").forEachIndexed { index, label ->
                    FilterChip(selected = filter == index, onClick = { filter = index }, label = { Text(label) })
                }
            }
            Spacer(Modifier.height(8.dp))
            when {
                loadError != null -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(loadError.orEmpty(), color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = viewModel::retryLoading) { Text("Reintentar") }
                    }
                }
                loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.semantics { contentDescription = "Cargando tareas" })
                }
                visibleTasks.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (tasks.isEmpty()) "Aquí empieza tu próximo logro" else "Todo en orden",
                            style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(if (tasks.isEmpty()) "Pulsa + para crear tu primera tarea." else "No hay tareas en esta categoría.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp),
                ) {
                    items(visibleTasks, key = { it.id }) { task ->
                        TaskItem(task, enabled = task.id !in busyIds,
                            onCheckedChange = { viewModel.toggleTaskStatus(task, it) },
                            onEdit = { viewModel.openEditDialog(task) }, onDelete = { deleteId = task.id })
                    }
                }
            }
        }
    }
    editor?.let { form ->
        TaskEditorDialog(form, viewModel::changeTitle, viewModel::changeDescription,
            viewModel::changeDueDate, viewModel::saveTask, viewModel::closeDialog)
    }
    tasks.find { it.id == deleteId }?.let { task ->
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("¿Eliminar tarea?") },
            text = { Text("Se eliminará «${task.title}». Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteTask(task); deleteId = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
fun TaskItem(task: Task, enabled: Boolean, onCheckedChange: (Boolean) -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val due = TaskDates.decode(task.dueDate)
    val overdue = !task.isCompleted && due.isBefore(LocalDate.now())
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(task.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None)
                Switch(checked = task.isCompleted, onCheckedChange = onCheckedChange, enabled = enabled,
                    modifier = Modifier.semantics { contentDescription = "Completada: ${task.title}" })
            }
            if (task.description.isNotBlank()) {
                Text(task.description, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
            }
            Text(
                "${if (task.isCompleted) "Completada" else if (overdue) "Vencida" else "Pendiente"} · ${due.format(dateFormat)}",
                style = MaterialTheme.typography.labelMedium,
                color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEdit, enabled = enabled,
                    modifier = Modifier.semantics { contentDescription = "Editar ${task.title}" }) { Text("Editar") }
                TextButton(onClick = onDelete, enabled = enabled,
                    modifier = Modifier.semantics { contentDescription = "Eliminar ${task.title}" }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
