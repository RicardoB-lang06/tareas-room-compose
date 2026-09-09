package com.example.tareasroom.ui

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class TaskValidationTest {
    private val today = LocalDate.of(2026, 9, 9)
    private val due = TaskDates.encode(today)

    @Test fun blankTitleIsRejected() {
        assertNotNull(TaskValidation.validate(" \n ", "", due, today = today).title)
    }
    @Test fun lengthLimitsAreEnforcedAfterTrimming() {
        assertTrue(TaskValidation.validate(" ${"a".repeat(80)} ", "b".repeat(500), due, today = today).isValid)
        assertNotNull(TaskValidation.validate("a".repeat(81), "", due, today = today).title)
        assertNotNull(TaskValidation.validate("Tarea", "b".repeat(501), due, today = today).description)
    }
    @Test fun missingDateAndPastDateAreRejected() {
        assertNotNull(TaskValidation.validate("Tarea", "", null, today = today).dueDate)
        assertNotNull(TaskValidation.validate("Tarea", "", due - 86_400_000, today = today).dueDate)
    }
    @Test fun unchangedOverdueDateIsAllowedWhenEditing() {
        val yesterday = TaskDates.encode(today.minusDays(1))
        assertTrue(TaskValidation.validate("Editada", "", yesterday, yesterday, today).isValid)
        assertNotNull(TaskValidation.validate("Editada", "", yesterday - 86_400_000, yesterday, today).dueDate)
    }
    @Test fun calendarDatesRoundTripWithoutTimeZoneShift() {
        listOf(LocalDate.of(2028, 2, 29), LocalDate.of(2026, 12, 31), today).forEach {
            assertEquals(it, TaskDates.decode(TaskDates.encode(it)))
        }
    }
    @Test fun outOfCalendarRangeIsRejected() {
        assertNotNull(TaskValidation.validate("Tarea", "", Long.MAX_VALUE, today = today).dueDate)
    }
}
