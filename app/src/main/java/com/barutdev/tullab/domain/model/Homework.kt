package com.barutdev.tullab.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

data class Homework(
    val id: Int,
    val studentId: Int,
    val title: String,
    val description: String,
    val creationDate: Long,
    val dueDate: Long,
    val status: HomeworkStatus,
    val performanceNotes: String?
) {
    /**
     * Dynamically determines whether the homework assignment is overdue.
     * Evaluates true strictly when status is PENDING and due date is prior to today.
     */
    fun isOverdue(today: LocalDate = LocalDate.now(ZoneOffset.UTC)): Boolean {
        if (status != HomeworkStatus.PENDING) return false
        val dueLocalDate = Instant.ofEpochMilli(dueDate)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        return dueLocalDate.isBefore(today)
    }
}
