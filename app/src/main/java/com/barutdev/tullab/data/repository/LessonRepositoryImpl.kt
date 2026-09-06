package com.barutdev.tullab.data.repository

import androidx.room.withTransaction
import com.barutdev.tullab.data.local.TullabDatabase
import com.barutdev.tullab.data.local.LessonDao
import com.barutdev.tullab.data.local.StudentDao
import com.barutdev.tullab.data.mapper.toDomain
import com.barutdev.tullab.data.mapper.toEntity
import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonWithStudent
import com.barutdev.tullab.domain.repository.LessonRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LessonRepositoryImpl @Inject constructor(
    private val lessonDao: LessonDao,
    private val studentDao: StudentDao,
    private val database: TullabDatabase
) : LessonRepository {

    override fun getAllLessons(): Flow<List<Lesson>> =
        lessonDao.getAllLessons().map { entities ->
            entities.toDomain()
        }

    override fun getLessonsForStudent(studentId: Int): Flow<List<Lesson>> =
        lessonDao.getLessonsForStudent(studentId).map { entities ->
            entities.toDomain()
        }

    override suspend fun insertLesson(lesson: Lesson): Int {
        return lessonDao.insert(lesson.toEntity()).toInt()
    }

    override suspend fun updateLesson(lesson: Lesson) {
        lessonDao.update(lesson.toEntity())
    }

    override suspend fun deleteLesson(lessonId: Int) {
        lessonDao.deleteLesson(lessonId)
    }

    override suspend fun markCompletedLessonsAsPaid(studentId: Int) {
        val timestamp = System.currentTimeMillis()
        database.withTransaction {
            lessonDao.markCompletedLessonsAsPaid(
                studentId = studentId,
                paymentTimestamp = timestamp
            )
            studentDao.updateLastPaymentDate(
                studentId = studentId,
                lastPaymentDate = timestamp
            )
        }
    }

    override suspend fun getScheduledLessonCount(studentId: Int): Int {
        return lessonDao.getScheduledLessonCount(studentId)
    }

    override suspend fun getScheduledLessonsForStudent(studentId: Int): List<Lesson> {
        return lessonDao.getScheduledLessonsForStudent(studentId).toDomain()
    }

    override suspend fun updateScheduledLessonsRate(studentId: Int, newRate: Double) {
        lessonDao.updateScheduledLessonsRate(studentId, newRate)
    }

    // Context-aware notification methods

    override fun getLessonsForDate(date: LocalDate): Flow<List<Lesson>> {
        val (startOfDay, endOfDay) = getDateRangeMillis(date)
        return lessonDao.getLessonsForDateRange(startOfDay, endOfDay)
            .map { entities -> entities.toDomain() }
    }

    override fun getCompletedLessonsForDate(date: LocalDate): Flow<List<Lesson>> {
        val (startOfDay, endOfDay) = getDateRangeMillis(date)
        return lessonDao.getCompletedLessonsForDateRange(startOfDay, endOfDay)
            .map { entities -> entities.toDomain() }
    }

    override suspend fun getLessonWithStudent(lessonId: Int): LessonWithStudent? {
        val lessonEntity = lessonDao.getLessonById(lessonId) ?: return null
        val studentEntity = studentDao.getStudentByIdSnapshot(lessonEntity.studentId) ?: return null

        return LessonWithStudent(
            lesson = lessonEntity.toDomain(),
            student = studentEntity.toDomain()
        )
    }

    override fun getActiveLessons(): Flow<List<Lesson>> {
        val currentDate = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        return lessonDao.getActiveLessons(currentDate)
            .map { entities -> entities.toDomain() }
    }

    /**
     * Converts LocalDate to start-of-day and end-of-day timestamps.
     * Returns Pair(startOfDay, endOfDay) in milliseconds.
     */
    private fun getDateRangeMillis(date: LocalDate): Pair<Long, Long> {
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
        return Pair(startOfDay, endOfDay)
    }

    override suspend fun insertLessons(lessons: List<Lesson>): List<Int> {
        val insertedIds = lessonDao.insertLessons(lessons.map { it.toEntity() })
        return insertedIds.map { it.toInt() }
    }

    override suspend fun deleteLessons(lessonIds: List<Int>) {
        lessonDao.deleteLessons(lessonIds)
    }

    override suspend fun getLessonDatesForStudent(studentId: Int): List<Long> {
        return lessonDao.getLessonDatesForStudent(studentId)
    }
}
