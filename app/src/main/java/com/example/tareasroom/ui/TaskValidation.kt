package com.example.tareasroom.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

object TaskDates {
    fun encode(date: LocalDate): Long = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    fun decode(value: Long): LocalDate = Instant.ofEpochMilli(value).atZone(ZoneOffset.UTC).toLocalDate()
}

data class ValidationErrors(
    val title: String? = null,
    val description: String? = null,
    val dueDate: String? = null,
) {
    val isValid get() = title == null && description == null && dueDate == null
}

object TaskValidation {
    const val MAX_TITLE = 80
    const val MAX_DESCRIPTION = 500

    fun validate(
        title: String,
        description: String,
        dueDate: Long?,
        originalDueDate: Long? = null,
        today: LocalDate = LocalDate.now(),
    ): ValidationErrors = ValidationErrors(
        title = when {
            title.isBlank() -> "Escribe un título."
            title.trim().length > MAX_TITLE -> "Usa un máximo de 80 caracteres."
            else -> null
        },
        description = if (description.trim().length > MAX_DESCRIPTION)
            "Usa un máximo de 500 caracteres." else null,
        dueDate = when {
            dueDate == null -> "Selecciona una fecha."
            TaskDates.decode(dueDate).year !in 1900..2100 -> "Selecciona una fecha entre 1900 y 2100."
            // Una tarea vencida puede editarse conservando su fecha original.
            dueDate != originalDueDate && TaskDates.decode(dueDate).isBefore(today) ->
                "La fecha debe ser hoy o posterior."
            else -> null
        },
    )
}
