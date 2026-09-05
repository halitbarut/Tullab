# Contract: UI Dialogs & ViewModels

**Feature**: `004-lesson-rate-locking`  
**Layer**: Presentation / UI  

## 1. `AddLessonDialog` Contract

```kotlin
@Composable
fun AddLessonDialog(
    showDialog: Boolean,
    initialRate: Double,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSave: (duration: String, notes: String, pricingMode: PricingMode, rateOrFee: Double) -> Unit
)
```
- **Inputs**:
  - `duration`: Numeric string (hours)
  - `notes`: String
  - `pricingMode`: `PricingMode.PER_HOUR` or `PricingMode.FLAT_FEE`
  - `rateOrFee`: Double (defaults to `initialRate`)
- **Behavior**:
  - Toggling between "Per Hour" and "Flat Fee" updates the field label and calculation hint.
  - Save button enabled only when duration is valid (> 0.0) and rateOrFee is valid (≥ 0.0).

---

## 2. `LogLessonDialog` Contract

```kotlin
@Composable
fun LogLessonDialog(
    showDialog: Boolean,
    lesson: Lesson?,
    currencyCode: String,
    onDismiss: () -> Unit,
    onComplete: (duration: String, notes: String, pricingMode: PricingMode, rateOrFee: Double) -> Unit,
    onMarkNotDone: (notes: String) -> Unit,
    isMarkAsPaidMode: Boolean = false,
    onMarkAsPaid: ((duration: String, customFee: String) -> Unit)? = null
)
```
- **Inputs**:
  - Pre-fills `pricingMode` and `rateOrFee` from `lesson.pricingMode` and `lesson.rateOrFee`.
  - For unpaid completed lessons, enables tutor to adjust pricing and duration.
- **Behavior**:
  - For lessons in `PAID` status, pricing and duration fields are read-only.

---

## 3. Scheduled Lessons Rate Prompt Dialog Contract

Displayed on the Edit Student Profile screen when the tutor changes `customHourlyRate` and the student has existing future `SCHEDULED` lessons.

```kotlin
@Composable
fun ScheduledLessonsRatePromptDialog(
    showDialog: Boolean,
    newRate: Double,
    currencyCode: String,
    onConfirmApplyToScheduled: () -> Unit,
    onDeclineApplyToScheduled: () -> Unit,
    onDismiss: () -> Unit
)
```
- **Dialog Title**: `koraStringResource(R.string.dialog_update_scheduled_rates_title)`
- **Dialog Body**: `koraStringResource(R.string.dialog_update_scheduled_rates_message)`
- **Actions**:
  - **Apply to Scheduled ("Yes")**: Updates student profile rate and updates all future scheduled per-hour lessons.
  - **Only Future Lessons ("No")**: Updates student profile rate only; leaves existing scheduled lessons at their original rate.
