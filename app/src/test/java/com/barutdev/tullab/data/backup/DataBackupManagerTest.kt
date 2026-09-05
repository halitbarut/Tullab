package com.barutdev.tullab.data.backup

import com.barutdev.tullab.data.local.HomeworkDao
import com.barutdev.tullab.data.local.LessonDao
import com.barutdev.tullab.data.local.StudentDao
import com.barutdev.tullab.data.local.TullabDatabase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import androidx.room.withTransaction

class DataBackupManagerTest {

    private lateinit var backupManager: DataBackupManager
    private lateinit var studentDao: StudentDao
    private lateinit var lessonDao: LessonDao
    private lateinit var homeworkDao: HomeworkDao
    private lateinit var database: TullabDatabase

    @Before
    fun setUp() {
        studentDao = mockk(relaxed = true)
        lessonDao = mockk(relaxed = true)
        homeworkDao = mockk(relaxed = true)
        database = mockk(relaxed = true)
        
        mockkStatic(
            "androidx.room.RoomDatabaseKt"
        )
        
        coEvery {
            database.withTransaction(any<suspend () -> Unit>())
        } coAnswers {
            val block = secondArg<suspend () -> Unit>()
            block()
        }

        backupManager = DataBackupManager(
            studentDao = studentDao,
            lessonDao = lessonDao,
            homeworkDao = homeworkDao,
            database = database
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test legacy Tullab backup format is correctly parsed and restored`() = runTest {
        val legacyCsv = """
            [students]
            1,John Doe,50.0,,"Jane Doe","555-1234","",
            [lessons]
            1,1,1681234567,SCHEDULED,1.0,,
            [homework]
            1,1,Math,Chapter 1,1681234567,1681234567,PENDING,
        """.trimIndent()

        backupManager.importFromCsv(legacyCsv)

        coVerify { studentDao.deleteAll() }
        coVerify { lessonDao.deleteAll() }
        coVerify { homeworkDao.deleteAll() }
        coVerify { studentDao.insertAll(any()) }
        coVerify { lessonDao.insertAll(any()) }
        coVerify { homeworkDao.insertAll(any()) }
    }

    @Test
    fun `test current Tullab backup format is correctly parsed and restored`() = runTest {
        val currentCsv = """
            [students]
            1,Jane Smith,60.0,,"John Smith","555-4321","",
            [lessons]
            2,1,1681234567,COMPLETED,1.5,,
            [homework]
            2,1,Physics,Chapter 2,1681234567,1681234567,COMPLETED,
        """.trimIndent()

        backupManager.importFromCsv(currentCsv)

        coVerify { studentDao.deleteAll() }
        coVerify { lessonDao.deleteAll() }
        coVerify { homeworkDao.deleteAll() }
        coVerify { studentDao.insertAll(any()) }
        coVerify { lessonDao.insertAll(any()) }
        coVerify { homeworkDao.insertAll(any()) }
    }
}
