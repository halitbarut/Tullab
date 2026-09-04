package com.barutdev.kora.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE students ADD COLUMN parentName TEXT")
        database.execSQL("ALTER TABLE students ADD COLUMN parentContact TEXT")
        database.execSQL("ALTER TABLE students ADD COLUMN notes TEXT")
        database.execSQL("ALTER TABLE students ADD COLUMN customHourlyRate REAL")
        database.execSQL("UPDATE students SET customHourlyRate = hourlyRate")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS `ai_insights`")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE lessons ADD COLUMN pricingMode TEXT NOT NULL DEFAULT 'PER_HOUR'")
        database.execSQL("ALTER TABLE lessons ADD COLUMN rateOrFee REAL NOT NULL DEFAULT 0.0")
        
        // Backfill legacy lessons using the student's active rate
        database.execSQL("""
            UPDATE lessons 
            SET rateOrFee = (
                SELECT COALESCE(customHourlyRate, hourlyRate) 
                FROM students 
                WHERE students.id = lessons.studentId
            )
        """)
    }
}
