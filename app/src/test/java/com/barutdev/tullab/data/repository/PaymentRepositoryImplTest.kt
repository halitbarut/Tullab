package com.barutdev.tullab.data.repository

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.barutdev.tullab.data.local.TullabDatabase
import com.barutdev.tullab.data.local.LessonDao
import com.barutdev.tullab.data.local.PaymentRecordDao
import com.barutdev.tullab.data.local.StudentDao
import com.barutdev.tullab.data.local.entity.LessonEntity
import com.barutdev.tullab.data.local.entity.PaymentRecordEntity
import com.barutdev.tullab.data.local.entity.StudentEntity
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.UserPreferences
import com.barutdev.tullab.domain.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PaymentRepositoryImplTest {

    private lateinit var repository: PaymentRepositoryImpl
    private val paymentRecordDao: PaymentRecordDao = mockk(relaxed = true)
    private val lessonDao: LessonDao = mockk(relaxed = true)
    private val studentDao: StudentDao = mockk(relaxed = true)
    private val database: TullabDatabase = mockk(relaxed = true)
    private val userPreferencesRepository: UserPreferencesRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery {
            any<RoomDatabase>().withTransaction<Any>(any())
        } coAnswers {
            val block = arg<suspend () -> Any>(1)
            block()
        }

        val prefs = UserPreferences(
            isDarkMode = false,
            languageCode = "en",
            currencyCode = "USD",
            defaultHourlyRate = 50.0,
            lessonRemindersEnabled = true,
            logReminderEnabled = true,
            lessonReminderHour = 9,
            lessonReminderMinute = 0,
            logReminderHour = 18,
            logReminderMinute = 0
        )
        every { userPreferencesRepository.userPreferences } returns flowOf(prefs)

        repository = PaymentRepositoryImpl(
            paymentRecordDao,
            lessonDao,
            studentDao,
            database,
            userPreferencesRepository
        )
    }

    @org.junit.After
    fun tearDown() {
        io.mockk.unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `markLessonAsPaid updates lesson and creates payment record`() = runTest {
        val student = StudentEntity(id = 1, fullName = "Test", hourlyRate = 50.0, customHourlyRate = null)
        val lesson = LessonEntity(id = 10, studentId = 1, date = 0L, durationInHours = 2.0, status = LessonStatus.COMPLETED, notes = null, pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR, rateOrFee = 50.0)

        coEvery { lessonDao.getLessonById(10) } returns lesson
        coEvery { studentDao.getStudentsSnapshot() } returns listOf(student)

        val updatedLessonSlot = slot<LessonEntity>()
        coEvery { lessonDao.update(capture(updatedLessonSlot)) } returns Unit

        val paymentRecordSlot = slot<PaymentRecordEntity>()
        coEvery { paymentRecordDao.insert(capture(paymentRecordSlot)) } returns Unit

        repository.markLessonAsPaid(lessonId = 10)

        assertEquals(LessonStatus.PAID, updatedLessonSlot.captured.status)
        assertEquals(2.0, updatedLessonSlot.captured.durationInHours)
        
        assertEquals(1, paymentRecordSlot.captured.studentId)
        assertEquals(10000L, paymentRecordSlot.captured.amountMinor)
        
        coVerify { studentDao.updateLastPaymentDate(eq(1), any()) }
    }

    @Test
    fun `revertLessonPayment restores completed status and removes payment record`() = runTest {
        val paidTimestamp = 123456789L
        val lesson = LessonEntity(id = 10, studentId = 1, date = 0L, durationInHours = 2.0, status = LessonStatus.PAID, paymentTimestamp = paidTimestamp, notes = null, pricingMode = com.barutdev.tullab.domain.model.PricingMode.PER_HOUR, rateOrFee = 0.0)

        coEvery { lessonDao.getLessonById(10) } returns lesson
        coEvery { paymentRecordDao.getLatestPaymentRecord(1) } returns null

        val updatedLessonSlot = slot<LessonEntity>()
        coEvery { lessonDao.update(capture(updatedLessonSlot)) } returns Unit

        repository.revertLessonPayment(lessonId = 10)

        coVerify { paymentRecordDao.deleteByStudentAndTimestamp(1, paidTimestamp) }

        assertEquals(LessonStatus.COMPLETED, updatedLessonSlot.captured.status)
        assertNull(updatedLessonSlot.captured.paymentTimestamp)

        coVerify { studentDao.updateLastPaymentDate(eq(1), isNull()) }
    }
}
