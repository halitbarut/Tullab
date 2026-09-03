package com.barutdev.kora.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.barutdev.kora.data.local.migrations.MIGRATION_8_9
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration8to9Test {

    @Rule
    @JvmField
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        KoraDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrateDropsAiInsightsTable() {
        val dbName = "migration-test-db"

        // Create db in version 8
        helper.createDatabase(dbName, 8).apply {
            execSQL(
                "CREATE TABLE IF NOT EXISTS students (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "fullName TEXT NOT NULL, " +
                    "hourlyRate REAL NOT NULL, " +
                    "lastPaymentDate INTEGER, " +
                    "parentName TEXT, " +
                    "parentContact TEXT, " +
                    "notes TEXT, " +
                    "customHourlyRate REAL)"
            )
            execSQL(
                "CREATE TABLE IF NOT EXISTS ai_insights (" +
                    "studentId INTEGER NOT NULL, " +
                    "focus TEXT NOT NULL, " +
                    "localeTag TEXT NOT NULL, " +
                    "insight TEXT NOT NULL, " +
                    "signature TEXT NOT NULL, " +
                    "updatedAt INTEGER NOT NULL, " +
                    "PRIMARY KEY(studentId, focus, localeTag))"
            )
            execSQL(
                "INSERT INTO students (id, fullName, hourlyRate) " +
                    "VALUES (1, 'Alice', 50.0)"
            )
            execSQL(
                "INSERT INTO ai_insights (studentId, focus, localeTag, insight, signature, updatedAt) " +
                    "VALUES (1, 'DASHBOARD', 'en-US', 'Great', 'sig123', 0)"
            )
            close()
        }

        // Migrate to version 9
        helper.runMigrationsAndValidate(dbName, 9, true, MIGRATION_8_9).use { database ->
            // Verify student data remains
            database.query("SELECT fullName FROM students WHERE id = 1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                val nameIndex = cursor.getColumnIndexOrThrow("fullName")
                assertEquals("Alice", cursor.getString(nameIndex))
            }

            // Verify ai_insights is dropped
            var caughtException = false
            try {
                database.query("SELECT * FROM ai_insights").use { cursor ->
                    cursor.moveToFirst()
                }
            } catch (e: Exception) {
                caughtException = true
            }
            assertTrue(caughtException, "Expected exception when querying dropped ai_insights table")
        }
    }
}
