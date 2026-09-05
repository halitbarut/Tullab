# Contract: Payment Calculation & Repository

**Feature**: `004-lesson-rate-locking`  
**Layer**: Domain & Data  

## `PaymentRepository` Contract

```kotlin
package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.PaymentRecord
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun observePaymentHistory(studentId: Int): Flow<List<PaymentRecord>>
    
    /**
     * Marks all completed unpaid lessons for a student as paid.
     * The payment amount created MUST be calculated as:
     * sum of (lesson.calculatedValue) across all completed unpaid lessons.
     */
    suspend fun markStudentAsPaid(studentId: Int)

    /**
     * Marks an individual lesson as paid.
     * The payment amount created MUST be calculated using that specific lesson's snapshot value:
     * - If customFee is provided, customFee is used.
     * - Otherwise, lesson.calculatedValue is used.
     */
    suspend fun markLessonAsPaid(
        lessonId: Int,
        durationInHours: Double? = null,
        customFee: Double? = null
    )

    /**
     * Reverts an individual lesson payment.
     * Restores lesson status to COMPLETED with paymentTimestamp = null.
     * Preserves the lesson's original locked pricingMode and rateOrFee.
     */
    suspend fun revertLessonPayment(lessonId: Int)
}
```
