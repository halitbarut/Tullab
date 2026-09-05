package com.barutdev.tullab.domain.model

data class Lesson(
    val id: Int,
    val studentId: Int,
    val date: Long,
    val status: LessonStatus,
    val durationInHours: Double?,
    val notes: String?,
    val pricingMode: PricingMode,
    val rateOrFee: Double,
    val paymentTimestamp: Long? = null
) {
    val calculatedValue: Double
        get() = when (pricingMode) {
            PricingMode.PER_HOUR -> (durationInHours ?: 0.0) * rateOrFee
            PricingMode.FLAT_FEE -> rateOrFee
        }
}
