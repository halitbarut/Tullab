# Contract: Lesson Repository Extensions

**Feature**: `008-bulk-lesson-scheduling`  
**Interface**: `com.barutdev.tullab.domain.repository.LessonRepository`  
**Implementation**: `com.barutdev.tullab.data.repository.LessonRepositoryImpl`  

## 1. Interface Signatures

```kotlin
package com.barutdev.tullab.domain.repository

import com.barutdev.tullab.domain.model.Lesson

interface LessonRepository {
    // ... existing methods ...

    /**
     * Inserts a collection of lessons in a single transaction.
     *
     * @param lessons List of domain lessons to insert.
     * @return List of generated database primary keys (IDs) in insertion order.
     */
    suspend fun insertLessons(lessons: List<Lesson>): List<Int>

    /**
     * Deletes a collection of lessons by their IDs in a single transaction.
     * Used for atomic rollback when the tutor taps "Undo".
     *
     * @param lessonIds IDs of lessons to remove.
     */
    suspend fun deleteLessons(lessonIds: List<Int>)

    /**
     * Returns all lesson start timestamps (epoch millis) for a specific student.
     * Used for rapid collision checking during bulk scheduling candidate generation.
     *
     * @param studentId Target student ID.
     * @return List of epoch millis timestamps for all existing lessons of this student.
     */
    suspend fun getLessonDatesForStudent(studentId: Int): List<Long>
}
```

## 2. DAO Layer Requirements (`LessonDao.kt`)

```kotlin
@Dao
interface LessonDao {
    // ... existing queries ...

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLessons(lessons: List<LessonEntity>): List<Long>

    @Query("DELETE FROM lessons WHERE id IN (:lessonIds)")
    suspend fun deleteLessons(lessonIds: List<Int>)

    @Query("SELECT date FROM lessons WHERE studentId = :studentId")
    suspend fun getLessonDatesForStudent(studentId: Int): List<Long>
}
```
