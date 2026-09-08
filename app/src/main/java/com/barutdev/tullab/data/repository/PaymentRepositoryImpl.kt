package com.barutdev.tullab.data.repository

import androidx.room.withTransaction
import com.barutdev.tullab.data.local.TullabDatabase
import com.barutdev.tullab.data.local.LessonDao
import com.barutdev.tullab.data.local.PaymentRecordDao
import com.barutdev.tullab.data.local.StudentDao
import com.barutdev.tullab.data.local.entity.PaymentRecordEntity
import com.barutdev.tullab.data.mapper.toDomain
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.PaymentRecord
import com.barutdev.tullab.domain.repository.PaymentRepository
import com.barutdev.tullab.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import kotlin.math.roundToLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

class PaymentRepositoryImpl @Inject constructor(
    private val paymentRecordDao: PaymentRecordDao,
    private val lessonDao: LessonDao,
    private val studentDao: StudentDao,
    private val database: TullabDatabase,
    private val userPreferencesRepository: UserPreferencesRepository
) : PaymentRepository {

    override fun observePaymentHistory(studentId: Int): Flow<List<PaymentRecord>> =
        paymentRecordDao.observeByStudent(studentId).map { it.toDomain() }

    override suspend fun markStudentAsPaid(studentId: Int) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            val lessons = lessonDao.getLessonsSnapshot()
            val completed = lessons.filter { it.studentId == studentId && it.status == LessonStatus.COMPLETED }
            val amount = completed.sumOf { it.toDomain().calculatedValue }
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
        val now = System.currentTimeMillis()

        database.withTransaction {
            val lesson = lessonDao.getLessonById(lessonId) ?: return@withTransaction
            val studentId = lesson.studentId
            val isFlatFee = lesson.pricingMode == com.barutdev.tullab.domain.model.PricingMode.FLAT_FEE

            // Duration consistency: PAID lessons always require duration > 0 for
            // hour statistics (any pricing mode), defaulting missing values to
            // 1.0h. FLAT_FEE totals still never multiply (rateOrFee alone);
            // PER_HOUR totals remain duration * rateOrFee exclusively.
            val effectiveRate = customFee ?: lesson.rateOrFee
            val rawDuration = durationInHours ?: lesson.durationInHours ?: 1.0
            val effectiveDuration = if (rawDuration > 0.0) rawDuration else 1.0

            val amount = if (isFlatFee) {
                effectiveRate
            } else {
                effectiveDuration * effectiveRate
            }
            val amountMinor = (amount * 100.0).roundToLong()

            val updatedLesson = lesson.copy(
                status = LessonStatus.PAID,
                paymentTimestamp = now,
                durationInHours = effectiveDuration,
                rateOrFee = effectiveRate
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

    override suspend fun revertPaymentCycle(studentId: Int, paymentTimestamp: Long) {
        database.withTransaction {
            lessonDao.revertPaidLessonsByTimestamp(studentId, paymentTimestamp)
            paymentRecordDao.deleteByStudentAndTimestamp(studentId, paymentTimestamp)
            val latestPayment = paymentRecordDao.getLatestPaymentRecord(studentId)
            studentDao.updateLastPaymentDate(studentId, latestPayment?.paidAtEpochMs)
        }
    }
}
