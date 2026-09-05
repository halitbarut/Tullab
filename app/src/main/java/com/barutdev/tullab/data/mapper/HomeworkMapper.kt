package com.barutdev.tullab.data.mapper

import com.barutdev.tullab.data.local.entity.HomeworkEntity
import com.barutdev.tullab.domain.model.Homework

fun HomeworkEntity.toDomain(): Homework = Homework(
    id = id,
    studentId = studentId,
    title = title,
    description = description,
    creationDate = creationDate,
    dueDate = dueDate,
    status = status,
    performanceNotes = performanceNotes
)

fun List<HomeworkEntity>.toDomain(): List<Homework> = map(HomeworkEntity::toDomain)

fun Homework.toEntity(): HomeworkEntity = HomeworkEntity(
    id = id,
    studentId = studentId,
    title = title,
    description = description,
    creationDate = creationDate,
    dueDate = dueDate,
    status = status,
    performanceNotes = performanceNotes
)
