package com.barutdev.tullab.data.mapper

import com.barutdev.tullab.data.local.entity.LessonEntity
import com.barutdev.tullab.domain.model.Lesson

fun LessonEntity.toDomain(): Lesson = Lesson(
    id = id,
    studentId = studentId,
    date = date,
    status = status,
    durationInHours = durationInHours,
    notes = notes,
    pricingMode = pricingMode,
    rateOrFee = rateOrFee,
    paymentTimestamp = paymentTimestamp
)

fun List<LessonEntity>.toDomain(): List<Lesson> = map(LessonEntity::toDomain)

fun Lesson.toEntity(): LessonEntity = LessonEntity(
    id = id,
    studentId = studentId,
    date = date,
    status = status,
    durationInHours = durationInHours,
    notes = notes,
    pricingMode = pricingMode,
    rateOrFee = rateOrFee,
    paymentTimestamp = paymentTimestamp
)
