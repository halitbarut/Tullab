# Interface & UI Contracts: Calendar Lesson Payment

**Branch**: `003-calendar-mark-lesson-paid`  
**Date**: 2026-09-04  
**Feature**: [spec.md](file:///home/halit/AndroidStudioProjects/Kora/specs/003-calendar-mark-lesson-paid/spec.md)

## Domain Repository Contract

```kotlin
package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.PaymentRecord
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun observePaymentHistory(studentId: Int): Flow<List<PaymentRecord>>
    suspend fun markStudentAsPaid(studentId: Int)
    
    /**
     * Marks an individual lesson as paid, writes a PaymentRecord, 
     * and updates student last payment date atomically.
     */
    suspend fun markLessonAsPaid(
        lessonId: Int,
        durationInHours: Double? = null,
        customFee: Double? = null
    )
    
    /**
     * Reverts an individual lesson payment back to COMPLETED,
     * deletes the associated PaymentRecord, and recalculates last payment date.
     */
    suspend fun revertLessonPayment(lessonId: Int)
}
```

---

## Room DAO Contracts

```kotlin
package com.barutdev.kora.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.barutdev.kora.data.local.entity.PaymentRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PaymentRecordEntity): Long

    @Query("SELECT * FROM payment_records WHERE studentId = :studentId ORDER BY paidAtEpochMs DESC")
    fun observeByStudent(studentId: Int): Flow<List<PaymentRecordEntity>>

    @Query("DELETE FROM payment_records WHERE studentId = :studentId AND paidAtEpochMs = :paidAtEpochMs")
    suspend fun deleteByStudentAndTimestamp(studentId: Int, paidAtEpochMs: Long): Int

    @Query("SELECT * FROM payment_records WHERE studentId = :studentId ORDER BY paidAtEpochMs DESC LIMIT 1")
    suspend fun getLatestPaymentRecord(studentId: Int): PaymentRecordEntity?
}
```

---

## UI Component Contract

### `LessonDetailCard`

```kotlin
@Composable
fun LessonDetailCard(
    lesson: Lesson,
    dateFormatter: DateTimeFormatter,
    today: LocalDate,
    locale: Locale,
    onActionClick: (Lesson) -> Unit,
    onMarkAsPaidClick: (Lesson) -> Unit,
    onRevertPaymentClick: (Lesson) -> Unit,
    modifier: Modifier = Modifier
)
```

#### Behavior Contract:
- **Action Button 1 (Top)**:
  - Text: "Log Details" (if scheduled in the past) or "Edit".
  - Action: Invokes `onActionClick(lesson)`.
- **Action Button 2 (Below Action 1)**:
  - If `lesson.status != LessonStatus.PAID`:
    - Renders as `Button` with container color `StatusGreen` (`#2E7D32`), on-container `Color.White`, and leading `Icons.Default.Check`.
    - Text: `koraStringResource(R.string.calendar_lesson_action_mark_as_paid)`.
    - Action: Invokes `onMarkAsPaidClick(lesson)`.
  - If `lesson.status == LessonStatus.PAID`:
    - Renders as `OutlinedButton`.
    - Text: `koraStringResource(R.string.calendar_lesson_action_revert_payment)`.
    - Action: Invokes `onRevertPaymentClick(lesson)`.
