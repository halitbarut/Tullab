package com.barutdev.kora.data.repository

import androidx.room.withTransaction
import com.barutdev.kora.data.local.KoraDatabase
import com.barutdev.kora.data.local.LessonDao
import com.barutdev.kora.data.local.PaymentRecordDao
import com.barutdev.kora.data.local.StudentDao
import com.barutdev.kora.data.local.entity.PaymentRecordEntity
import com.barutdev.kora.data.mapper.toDomain
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.domain.model.PaymentRecord
import com.barutdev.kora.domain.repository.PaymentRepository
import com.barutdev.kora.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import kotlin.math.roundToLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

class PaymentRepositoryImpl @Inject constructor(
    private val paymentRecordDao: PaymentRecordDao,
    private val lessonDao: LessonDao,
    private val studentDao: StudentDao,
    private val database: KoraDatabase,
    private val userPreferencesRepository: UserPreferencesRepository
) : PaymentRepository {

    override fun observePaymentHistory(studentId: Int): Flow<List<PaymentRecord>> =
        paymentRecordDao.observeByStudent(studentId).map { it.toDomain() }

    override suspend fun markStudentAsPaid(studentId: Int) {
        val defaultHourlyRate = userPreferencesRepository.userPreferences.first().defaultHourlyRate
        val now = System.currentTimeMillis()
        database.withTransaction {
            val students = studentDao.getStudentsSnapshot()
            val student = students.firstOrNull { it.id == studentId } ?: return@withTransaction
            val lessons = lessonDao.getLessonsSnapshot()
            val completed = lessons.filter { it.studentId == studentId && it.status == LessonStatus.COMPLETED }
            val totalHours = completed.mapNotNull { it.durationInHours }.sum()
            val effectiveRate = student.customHourlyRate ?: defaultHourlyRate
            val amount = totalHours * effectiveRate
            val amountMinor = (amount * 100.0).roundToLong()
            if (amountMinor > 0L) {
                val record = PaymentRecordEntity(
                    studentId = studentId,
                    amountMinor = amountMinor,
                    paidAtEpochMs = now
                )
                paymentRecordDao.insert(record)
            }
            lessonDao.markCompletedLessonsAsPaid(
                studentId = studentId,
                paymentTimestamp = now
            )
            studentDao.updateLastPaymentDate(
                studentId = studentId,
                lastPaymentDate = now
            )
        }
    }
    override suspend fun markLessonAsPaid(lessonId: Int, durationInHours: Double?, customFee: Double?) {
        val defaultHourlyRate = userPreferencesRepository.userPreferences.first().defaultHourlyRate
        val now = System.currentTimeMillis()
        
        database.withTransaction {
            val lesson = lessonDao.getLessonById(lessonId) ?: return@withTransaction
            val studentId = lesson.studentId
            val students = studentDao.getStudentsSnapshot()
            val student = students.firstOrNull { it.id == studentId } ?: return@withTransaction

            val effectiveDuration = durationInHours ?: lesson.durationInHours ?: 0.0
            val effectiveRate = customFee ?: student.customHourlyRate ?: student.hourlyRate.takeIf { it > 0.0 } ?: defaultHourlyRate
            val amount = effectiveDuration * effectiveRate
            val amountMinor = (amount * 100.0).roundToLong()

            val updatedLesson = lesson.copy(
                status = LessonStatus.PAID,
                paymentTimestamp = now,
                durationInHours = effectiveDuration
            )
            lessonDao.update(updatedLesson)

            if (amountMinor > 0L) {
                val record = PaymentRecordEntity(
                    studentId = studentId,
                    amountMinor = amountMinor,
                    paidAtEpochMs = now
                )
                paymentRecordDao.insert(record)
            }

            studentDao.updateLastPaymentDate(studentId, now)
        }
    }

    override suspend fun revertLessonPayment(lessonId: Int) {
        database.withTransaction {
            val lesson = lessonDao.getLessonById(lessonId) ?: return@withTransaction
            val studentId = lesson.studentId
            val paidAt = lesson.paymentTimestamp ?: return@withTransaction

            paymentRecordDao.deleteByStudentAndTimestamp(studentId, paidAt)

            val updatedLesson = lesson.copy(
                status = LessonStatus.COMPLETED,
                paymentTimestamp = null
            )
            lessonDao.update(updatedLesson)

            val latestPayment = paymentRecordDao.getLatestPaymentRecord(studentId)
            studentDao.updateLastPaymentDate(studentId, latestPayment?.paidAtEpochMs)
        }
    }
}
