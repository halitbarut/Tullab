package com.barutdev.tullab.ui.screens.dashboard

import com.barutdev.tullab.domain.model.Lesson
import com.barutdev.tullab.domain.model.PricingMode

/**
 * Read-model representing a single row in the payment-cycle breakdown shown on
 * the Dashboard's PaymentTrackingCard.
 *
 * A "tier" groups completed-but-unpaid lessons that share the same pricing mode
 * and (when PER_HOUR) the same effective rate. The UI renders each tier as:
 *
 *   PER_HOUR  → "{hours} hrs × {rate}/hr = {subtotal}"
 *   FLAT_FEE  → "{count} flat fees × {feeAmount} = {subtotal}"
 */
data class PaymentBreakdownTier(
    /** Pricing mode this tier represents. */
    val pricingMode: PricingMode,
    /** Number of lessons in this tier. Used for FLAT_FEE display. */
    val lessonCount: Int,
    /** Total hours summed across all lessons in this tier (PER_HOUR only). 0.0 for FLAT_FEE. */
    val totalHours: Double,
    /** The shared rate or fee per lesson used in this tier. */
    val rateOrFee: Double,
    /** Pre-computed subtotal: totalHours × rateOrFee (PER_HOUR) or lessonCount × rateOrFee (FLAT_FEE). */
    val subtotal: Double
)

/**
 * Groups [completedLessons] (assumed to be the completed-and-unpaid list from the
 * current payment cycle) into [PaymentBreakdownTier] instances.
 *
 * Grouping key: (pricingMode, rateOrFee). Lessons within a group are summed.
 * The resulting list is sorted: PER_HOUR tiers first (descending by rate), then
 * FLAT_FEE tiers (descending by fee).
 *
 * Returns an empty list when [completedLessons] is empty.
 */
fun computePaymentBreakdownTiers(completedLessons: List<Lesson>): List<PaymentBreakdownTier> {
    if (completedLessons.isEmpty()) return emptyList()

    data class TierKey(val pricingMode: PricingMode, val rateOrFee: Double)

    return completedLessons
        .groupBy { TierKey(it.pricingMode, it.rateOrFee) }
        .map { (key, lessons) ->
            when (key.pricingMode) {
                PricingMode.PER_HOUR -> {
                    val totalHours = lessons.mapNotNull { it.durationInHours }.sum()
                    PaymentBreakdownTier(
                        pricingMode = PricingMode.PER_HOUR,
                        lessonCount = lessons.size,
                        totalHours = totalHours,
                        rateOrFee = key.rateOrFee,
                        subtotal = totalHours * key.rateOrFee
                    )
                }
                PricingMode.FLAT_FEE -> {
                    val count = lessons.size
                    PaymentBreakdownTier(
                        pricingMode = PricingMode.FLAT_FEE,
                        lessonCount = count,
                        totalHours = 0.0,
                        rateOrFee = key.rateOrFee,
                        subtotal = count * key.rateOrFee
                    )
                }
            }
        }
        .sortedWith(
            compareBy<PaymentBreakdownTier> { it.pricingMode.ordinal }
                .thenByDescending { it.rateOrFee }
        )
}
