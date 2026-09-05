# Phase 1 Data Model: Lesson Rate Locking and Historical Payment Calculation

**Feature**: `004-lesson-rate-locking`  
**Date**: 2026-09-04  
**Status**: Completed  

## 1. Domain Models

### `PricingMode`
Represents the billing model applied to an individual lesson.
```kotlin
package com.barutdev.kora.domain.model

enum class PricingMode {
    PER_HOUR,
    FLAT_FEE
}
```

### `Lesson`
Represents a tutoring session. Holds an immutable snapshot of its billing terms.
```kotlin
package com.barutdev.kora.domain.model

data class Lesson(
    val id: Int,
    val studentId: Int,
    val date: Long,
    val status: LessonStatus,
    val durationInHours: Double?,
    val notes: String?,
    val paymentTimestamp: Long? = null,
    val pricingMode: PricingMode = PricingMode.PER_HOUR,
    val rateOrFee: Double = 0.0
) {
    /**
     * Deterministically calculates the monetary value of this lesson without
     * persisting a redundant column in local storage.
     */
    val calculatedValue: Double
        get() = when (pricingMode) {
            PricingMode.PER_HOUR -> (durationInHours ?: 0.0) * rateOrFee
            PricingMode.FLAT_FEE -> rateOrFee
        }
}
```

---

## 2. Room Persistence Entities & Mappers

### `LessonEntity`
Represents the SQLite record in the `lessons` table.
```kotlin
package com.barutdev.kora.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.domain.model.PricingMode

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"])]
)
data class LessonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: Int,
    val date: Long,
    val status: LessonStatus,
    val durationInHours: Double?,
    val notes: String?,
    val paymentTimestamp: Long? = null,
    val pricingMode: PricingMode = PricingMode.PER_HOUR,
    val rateOrFee: Double = 0.0
)
```

### `PricingModeConverter`
```kotlin
package com.barutdev.kora.data.local

import androidx.room.TypeConverter
import com.barutdev.kora.domain.model.PricingMode

class PricingModeConverter {
    @TypeConverter
    fun fromPricingMode(mode: PricingMode): String = mode.name

    @TypeConverter
    fun toPricingMode(value: String): PricingMode =
        runCatching { PricingMode.valueOf(value) }.getOrDefault(PricingMode.PER_HOUR)
}
```

---

## 3. Database Migration (Schema 9 → 10)

```kotlin
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE lessons ADD COLUMN pricingMode TEXT NOT NULL DEFAULT 'PER_HOUR'")
        database.execSQL("ALTER TABLE lessons ADD COLUMN rateOrFee REAL NOT NULL DEFAULT 0.0")
        database.execSQL(
            """
            UPDATE lessons SET rateOrFee = COALESCE(
                (SELECT customHourlyRate FROM students WHERE students.id = lessons.studentId),
                (SELECT hourlyRate FROM students WHERE students.id = lessons.studentId),
                0.0
            )
            """.trimIndent()
        )
    }
}
```

---

## 4. Lifecycle & State Transitions

```mermaid
stateDiagram-v2
    [*] --> Scheduled: Created with student's active rate (default) or custom rate/fee
    Scheduled --> Scheduled: Tutor updates student profile rate (if confirmed by tutor)
    Scheduled --> Completed: Logged as completed; pricing snapshot locked from auto-changes
    Completed --> Completed: Explicit manual edit by tutor (rate/fee or mode updated)
    Completed --> Paid: Marked as Paid (records payment for lesson.calculatedValue)
    Paid --> Completed: Payment reverted (retains original locked snapshot)
    Scheduled --> Cancelled: Cancelled (calculatedValue not counted toward pending debt)
```

---

## 5. Validation Rules

| Field | Rule | Failure Behavior |
|:---|:---|:---|
| `rateOrFee` | Must be `≥ 0.0`. Validated in UI inputs (commas normalized to periods). | Disables save action; shows input validation error. |
| `durationInHours` | Must be `> 0.0` when status is `COMPLETED` or `PAID`. | Prevents marking lesson as completed/paid until valid duration entered. |
| `calculatedValue` | Evaluated strictly on-demand. Never written to SQLite. | Prevents data drift across duration/rate edits. |
| Rate updates | `UPDATE lessons` query for scheduled lessons must filter on `status = 'SCHEDULED'` and `pricingMode = 'PER_HOUR'`. | Protects completed and flat-fee lessons from unintended modifications. |
