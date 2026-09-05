# Contract: Lesson Repository & DAO

**Feature**: `004-lesson-rate-locking`  
**Layer**: Domain & Data  

## `LessonRepository` Contract

```kotlin
package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.Lesson
import kotlinx.coroutines.flow.Flow

interface LessonRepository {
    fun getAllLessons(): Flow<List<Lesson>>
    fun getLessonsForStudent(studentId: Int): Flow<List<Lesson>>
    suspend fun getLessonsForStudentSnapshot(studentId: Int): List<Lesson>
    suspend fun insertLesson(lesson: Lesson): Int
    suspend fun updateLesson(lesson: Lesson)
    suspend fun deleteLesson(lessonId: Int)
    
    /**
     * Checks if a student has any future scheduled lessons.
     */
    suspend fun hasScheduledLessons(studentId: Int): Boolean

    /**
     * Updates the hourly rate for all SCHEDULED lessons for a student that use PER_HOUR pricing.
     * Must NEVER update COMPLETED or PAID lessons.
     */
    suspend fun updateScheduledLessonsRate(studentId: Int, newRate: Double)
}
```

## `LessonDao` Contract Additions

```kotlin
@Query("SELECT COUNT(*) FROM lessons WHERE studentId = :studentId AND status = 'SCHEDULED'")
suspend fun getScheduledLessonCount(studentId: Int): Int

@Query(
    "UPDATE lessons SET rateOrFee = :newRate " +
    "WHERE studentId = :studentId AND status = 'SCHEDULED' AND pricingMode = 'PER_HOUR'"
)
suspend fun updateScheduledLessonsRate(studentId: Int, newRate: Double)
```
