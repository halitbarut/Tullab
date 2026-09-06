# UI & Computation Contract: Dashboard Payment Tracking Card

## 1. Breakdown Tier Computation Contract

### Package
`com.barutdev.tullab.ui.screens.dashboard`

### Logic
Given `completedLessons: List<Lesson>` awaiting payment:
```kotlin
fun computePaymentBreakdownTiers(
    completedLessons: List<Lesson>
): List<PaymentBreakdownTier> {
    if (completedLessons.isEmpty()) return emptyList()

    return completedLessons
        .groupBy { Pair(it.pricingMode, it.rateOrFee) }
        .map { (key, group) ->
            val (mode, rateOrFee) = key
            val totalHours = if (mode == PricingMode.PER_HOUR) {
                group.sumOf { it.durationInHours ?: 0.0 }
            } else {
                0.0
            }
            val subtotal = group.sumOf { it.calculatedValue }
            PaymentBreakdownTier(
                pricingMode = mode,
                rateOrFee = rateOrFee,
                totalHours = totalHours,
                lessonCount = group.size,
                subtotal = subtotal
            )
        }
        .sortedWith(compareBy({ it.pricingMode }, { -it.rateOrFee }))
}
```

### Invariant
```kotlin
assertEquals(
    expected = completedLessons.sumOf { it.calculatedValue },
    actual = tiers.sumOf { it.subtotal }
)
```

---

## 2. PaymentTrackingCard UI Contract

### Component
`com.barutdev.tullab.ui.screens.dashboard.PaymentTrackingCard`

### Parameters
```kotlin
@Composable
fun PaymentTrackingCard(
    totalHours: Double,
    hourlyRate: Double,
    totalAmountDue: Double,
    rateBreakdownTiers: List<PaymentBreakdownTier>,
    lastPaymentDate: Long?,
    onMarkPaidClick: () -> Unit,
    onShowPaymentHistory: () -> Unit,
    currencyCode: String,
    locale: Locale,
    modifier: Modifier = Modifier
)
```

### Presentation Specification
- Total Amount: Headline font, formatted currency (`amountText`).
- Breakdown Rows:
  - If `rateBreakdownTiers.isEmpty()`:
    - Display standard empty/single rate info line: `0 hours × <rate>`.
  - If `rateBreakdownTiers.isNotEmpty()`:
    - For each tier, render a two-column row:
      - **Left side**: Formula text
        - For `PER_HOUR`: `koraStringResource(R.string.dashboard_payment_rate_info, formattedHours, formattedRate)` (e.g., `5 hours × 200 TL`).
        - For `FLAT_FEE`: `koraStringResource(R.string.dashboard_payment_flat_fee_tier, count, formattedFee)` (e.g., `2 flat fees × 400 TL`).
      - **Right side**: Subtotal text
        - `formatCurrency(tier.subtotal, currencyCode)` (e.g., `1,000 TL`).
      - **Layout & Wrapping**:
        - Row with `Arrangement.SpaceBetween`. Left formula has `Modifier.weight(1f, fill = false)`.
        - If text is wide, allow wrapping or stacking gracefully without clipping or horizontal scrolling.
