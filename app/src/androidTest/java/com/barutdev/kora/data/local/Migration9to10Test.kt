package com.barutdev.kora.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.barutdev.kora.data.local.migrations.MIGRATION_9_10
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration9to10Test {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        KoraDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate9To10() {
        var db = helper.createDatabase(TEST_DB, 9)

        // Insert a student
        db.execSQL(
            """
            INSERT INTO students (id, fullName, hourlyRate, customHourlyRate) 
            VALUES (1, 'Test Student', 50.0, 60.0)
            """
        )
        
        // Insert a legacy lesson (without pricingMode and rateOrFee)
        db.execSQL(
            """
            INSERT INTO lessons (id, studentId, date, status, durationInHours, notes, paymentTimestamp) 
            VALUES (1, 1, 1610000000000, 'COMPLETED', 1.5, 'Test Note', NULL)
            """
        )

        db.close()

        // Run migration
        db = helper.runMigrationsAndValidate(TEST_DB, 10, true, MIGRATION_9_10)

        // Validate that pricingMode and rateOrFee were added and populated correctly
        val cursor = db.query("SELECT * FROM lessons WHERE id = 1")
        cursor.moveToFirst()

        val pricingModeIndex = cursor.getColumnIndex("pricingMode")
        val rateOrFeeIndex = cursor.getColumnIndex("rateOrFee")

        val pricingMode = cursor.getString(pricingModeIndex)
        val rateOrFee = cursor.getDouble(rateOrFeeIndex)

        assertEquals("PER_HOUR", pricingMode)
        assertEquals(60.0, rateOrFee, 0.0) // Uses customHourlyRate

        cursor.close()
    }
}
