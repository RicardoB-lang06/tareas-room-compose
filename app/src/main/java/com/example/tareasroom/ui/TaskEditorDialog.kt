package com.example.tareasroom.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorDialog(
    form: TaskEditor,
    onTitle: (String) -> Unit,
    onDescription: (String) -> Unit,
    onDate: (Long?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showCalendar by rememberSaveable { mutableStateOf(false) }
    val format = remember { DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es")) }
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = !form.saving, dismissOnClickOutside = !form.saving),
        title = { Text(if (form.original == null) "Nueva tarea" else "Editar tarea") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Dale un nombre y un lugar en tu día.")
                OutlinedTextField(
                    value = form.title, onValueChange = onTitle, label = { Text("Título *") },
                    singleLine = true, enabled = !form.saving, isError = form.errors.title != null,
                    supportingText = { Text(form.errors.title ?: "${form.title.trim().length}/80 caracteres") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = form.description, onValueChange = onDescription, label = { Text("Descripción (opcional)") },
                    minLines = 2, maxLines = 5, enabled = !form.saving, isError = form.errors.description != null,
                    supportingText = { Text(form.errors.description ?: "${form.description.trim().length}/500 caracteres") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Fecha límite *", style = MaterialTheme.typography.labelLarge)
                OutlinedButton(onClick = { showCalendar = true }, enabled = !form.saving, modifier = Modifier.fillMaxWidth()) {
                    Text(form.dueDate?.let { TaskDates.decode(it).format(format) } ?: "Seleccionar fecha")
                }
                form.errors.dueDate?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                form.saveError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = onSave, enabled = !form.saving) { Text(if (form.saving) "Guardando…" else "Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !form.saving) { Text("Cancelar") } },
    )
    if (showCalendar) {
        val today = LocalDate.now()
        val selectableDates = remember(today, form.original?.dueDate) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis == form.original?.dueDate || !TaskDates.decode(utcTimeMillis).isBefore(today)
            }
        }
        val picker = rememberDatePickerState(initialSelectedDateMillis = form.dueDate,
            yearRange = 1900..2100, selectableDates = selectableDates)
        DatePickerDialog(
            onDismissRequest = { showCalendar = false },
            confirmButton = {
                TextButton(onClick = { onDate(picker.selectedDateMillis); showCalendar = false },
                    enabled = picker.selectedDateMillis != null) { Text("Elegir fecha") }
            },
            dismissButton = { TextButton(onClick = { showCalendar = false }) { Text("Cancelar") } },
        ) { DatePicker(state = picker) }
    }
}
