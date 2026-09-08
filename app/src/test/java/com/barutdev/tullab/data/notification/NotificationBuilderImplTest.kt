package com.barutdev.tullab.data.notification

import android.app.Notification
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.LessonStatus
import com.barutdev.tullab.domain.model.LessonWithStudent
import com.barutdev.tullab.domain.model.NotificationType
import com.barutdev.tullab.domain.model.PricingMode
import com.barutdev.tullab.domain.model.Student
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NotificationBuilderImplTest {

    private lateinit var context: Context
    private lateinit var notificationBuilder: NotificationBuilderImpl

    private val sampleStudent = Student(
        id = 42,
        fullName = "Ali Yılmaz",
        hourlyRate = 500.0
    )

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        notificationBuilder = NotificationBuilderImpl(context)
    }

    @Test
    fun `buildLessonReminder uses dedicated ic_notification small icon and BigTextStyle`() {
        val zoneId = ZoneId.systemDefault()
        val lessonTime = LocalTime.of(14, 30)
        val lessonDateMillis = LocalDate.now().atTime(lessonTime).atZone(zoneId).toInstant().toEpochMilli()

        val lesson = Lesson(
            id = 1,
            studentId = 42,
            date = lessonDateMillis,
            status = LessonStatus.SCHEDULED,
            durationInHours = 1.0,
            notes = null,
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 500.0
        )
        val lessonWithStudent = LessonWithStudent(lesson, sampleStudent)

        val notification = notificationBuilder.build(NotificationType.LESSON_REMINDER, lessonWithStudent)

        assertEquals(R.drawable.ic_notification, notification.smallIcon.resId)
        assertEquals("lesson_reminders", notification.channelId)

        val extras = notification.extras
        assertNotNull(extras)
        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        assertNotNull(title)
        assertTrue(title!!.contains("Ali Yılmaz"))
        assertNotNull(text)
        assertNotNull(bigText)
        assertEquals(text, bigText)

        // Verify localized time format matches 14:30
        val expectedTimeStr = android.text.format.DateFormat.getTimeFormat(context)
            .format(Date(lessonDateMillis))
        assertTrue(text!!.contains(expectedTimeStr))
    }

    @Test
    fun `buildLessonReminder falls back to 15_00 when lesson date is at midnight`() {
        val zoneId = ZoneId.systemDefault()
        val midnightMillis = LocalDate.now().atStartOfDay(zoneId).toInstant().toEpochMilli()

        val lesson = Lesson(
            id = 2,
            studentId = 42,
            date = midnightMillis,
            status = LessonStatus.SCHEDULED,
            durationInHours = 1.0,
            notes = null,
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 500.0
        )
        val lessonWithStudent = LessonWithStudent(lesson, sampleStudent)

        val notification = notificationBuilder.build(NotificationType.LESSON_REMINDER, lessonWithStudent)

        val text = notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        assertNotNull(text)

        // Effective instant should be 15:00
        val fallbackInstant = LocalDate.now().atTime(15, 0).atZone(zoneId).toInstant()
        val expectedFallbackTimeStr = android.text.format.DateFormat.getTimeFormat(context)
            .format(Date.from(fallbackInstant))

        assertTrue(text!!.contains(expectedFallbackTimeStr))
    }

    @Test
    fun `buildNoteReminder uses ic_notification and BigTextStyle`() {
        val zoneId = ZoneId.systemDefault()
        val lessonTime = LocalTime.of(16, 0)
        val lessonDateMillis = LocalDate.now().atTime(lessonTime).atZone(zoneId).toInstant().toEpochMilli()

        val lesson = Lesson(
            id = 3,
            studentId = 42,
            date = lessonDateMillis,
            status = LessonStatus.COMPLETED,
            durationInHours = 1.0,
            notes = null,
            pricingMode = PricingMode.PER_HOUR,
            rateOrFee = 500.0
        )
        val lessonWithStudent = LessonWithStudent(lesson, sampleStudent)

        val notification = notificationBuilder.build(NotificationType.NOTE_REMINDER, lessonWithStudent)

        assertEquals(R.drawable.ic_notification, notification.smallIcon.resId)
        assertEquals("lesson_notes", notification.channelId)

        val extras = notification.extras
        assertNotNull(extras)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        assertNotNull(text)
        assertNotNull(bigText)
        assertEquals(text, bigText)
    }
}
