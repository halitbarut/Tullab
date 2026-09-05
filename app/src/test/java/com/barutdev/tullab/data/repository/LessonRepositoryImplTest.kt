package com.barutdev.tullab.data.repository

import com.barutdev.tullab.data.local.TullabDatabase
import com.barutdev.tullab.data.local.LessonDao
import com.barutdev.tullab.data.local.StudentDao
import com.barutdev.tullab.data.local.entity.LessonEntity
import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.PricingMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LessonRepositoryImplTest {

    private lateinit var lessonDao: LessonDao
    private lateinit var studentDao: StudentDao
    private lateinit var database: TullabDatabase
    private lateinit var repository: LessonRepositoryImpl

    @Before
    fun setup() {
        lessonDao = mockk(relaxed = true)
        studentDao = mockk(relaxed = true)
        database = mockk(relaxed = true)
        repository = LessonRepositoryImpl(lessonDao, studentDao, database)
    }

    @Test
    fun insertLesson_savesWithProvidedSnapshotRate() = runTest {
        val lesson = Lesson(
            id = 0,
            studentId = 1,
            date = System.currentTimeMillis(),
            status = LessonStatus.SCHEDULED,
            durationInHours = 1.0,
            notes = null,
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 75.0
        )

        coEvery { lessonDao.insert(any()) } returns 1L

        repository.insertLesson(lesson)

        val entitySlot = slot<LessonEntity>()
        coVerify { lessonDao.insert(capture(entitySlot)) }

        val capturedEntity = entitySlot.captured
        assertEquals(PricingMode.PER_HOUR, capturedEntity.pricingMode)
        assertEquals(75.0, capturedEntity.rateOrFee, 0.0)
    }
}
