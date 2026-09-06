package com.barutdev.tullab.domain.repository

import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonWithStudent
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface LessonRepository {
    fun getAllLessons(): Flow<List<Lesson>>

    fun getLessonsForStudent(studentId: Int): Flow<List<Lesson>>

    suspend fun insertLesson(lesson: Lesson): Int

    suspend fun updateLesson(lesson: Lesson)

    suspend fun deleteLesson(lessonId: Int)

    suspend fun markCompletedLessonsAsPaid(studentId: Int)

    suspend fun getScheduledLessonCount(studentId: Int): Int

    /** Returns all SCHEDULED lessons for a student (one-shot snapshot, not a Flow). */
    suspend fun getScheduledLessonsForStudent(studentId: Int): List<Lesson>

    suspend fun updateScheduledLessonsRate(studentId: Int, newRate: Double)

    // New methods for context-aware notifications feature

    /**
     * Returns a Flow of lessons scheduled for a specific date.
     * Includes all statuses (PENDING, COMPLETED, CANCELLED).
     *
     * @param date Date to query
     * @return Flow<List<Lesson>> Lessons on the specified date
     */
    fun getLessonsForDate(date: LocalDate): Flow<List<Lesson>>

    /**
     * Returns a Flow of completed lessons for a specific date.
     * Filters to only status == COMPLETED.
     *
     * @param date Date to query
     * @return Flow<List<Lesson>> Completed lessons on the specified date
     */
    fun getCompletedLessonsForDate(date: LocalDate): Flow<List<Lesson>>

    /**
     * Retrieves a lesson with its associated student data.
     * Returns null if lesson or student not found.
     *
     * @param lessonId Lesson identifier
     * @return LessonWithStudent if found, null otherwise
     */
    suspend fun getLessonWithStudent(lessonId: Int): LessonWithStudent?

    /**
     * Returns a Flow of all active lessons (not cancelled, scheduled >= today).
     * Used for rescheduling alarms.
     *
     * @return Flow<List<Lesson>> Active future lessons
     */
    fun getActiveLessons(): Flow<List<Lesson>>

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
