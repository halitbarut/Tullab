package com.barutdev.tullab.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.barutdev.tullab.data.local.entity.LessonEntity
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.PricingMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class LessonDaoTest {

    private lateinit var database: TullabDatabase
    private lateinit var lessonDao: LessonDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TullabDatabase::class.java
        ).allowMainThreadQueries().build()
        lessonDao = database.lessonDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun updateScheduledLessonsRate_modifiesOnlyScheduledLessons() = runTest {
        val studentId = 1
        
        val scheduledLesson = LessonEntity(
            studentId = studentId,
            date = 1000L,
            status = LessonStatus.SCHEDULED,
            durationInHours = null,
            notes = null,
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 50.0
        )
        
        val completedLesson = LessonEntity(
            studentId = studentId,
            date = 2000L,
            status = LessonStatus.COMPLETED,
            durationInHours = 1.0,
            notes = null,
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 50.0
        )

        lessonDao.insert(scheduledLesson)
        lessonDao.insert(completedLesson)

        lessonDao.updateScheduledLessonsRate(studentId, 75.0)

        val lessons = lessonDao.getLessonsSnapshot()
        
        val updatedScheduledLesson = lessons.first { it.status == LessonStatus.SCHEDULED }
        val unmodifiedCompletedLesson = lessons.first { it.status == LessonStatus.COMPLETED }

        assertEquals(75.0, updatedScheduledLesson.rateOrFee, 0.0)
        assertEquals(50.0, unmodifiedCompletedLesson.rateOrFee, 0.0)
    }

    @Test
    fun getScheduledLessonCount_returnsOnlyScheduledLessonsCount() = runTest {
        val studentId = 1
        
        lessonDao.insert(LessonEntity(
            studentId = studentId,
            date = 1000L,
            status = LessonStatus.SCHEDULED,
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 50.0
        ))
        lessonDao.insert(LessonEntity(
            studentId = studentId,
            date = 2000L,
            status = LessonStatus.COMPLETED,
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 50.0
        ))

        val count = lessonDao.getScheduledLessonCount(studentId)
        assertEquals(1, count)
    }
}
