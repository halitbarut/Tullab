# UI Component Contracts: Material 3 Standards & Cross-Screen UX Refinement

This document defines the signatures and callback contracts for modified and new UI components.

## 1. SpeedDialFab (Calendar)

```kotlin
package com.barutdev.tullab.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CalendarSpeedDialFab(
    onScheduleForDateClick: () -> Unit,
    onBulkScheduleClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

- **Behavior**:
  - Closed state: Shows single FAB with `+` icon (`Icons.Filled.Add`).
  - Expanded state: Rotates icon by 45 degrees (`x` close icon) and presents two labeled mini-floating action buttons vertically:
    1. "Seçili Güne Planla" (`Icons.Outlined.Event`)
    2. "Toplu Ders Ekle" (`Icons.Outlined.DateRange`)
  - Clicking background or scrim closes the speed dial.

---

## 2. CalendarLegend (Calendar)

```kotlin
package com.barutdev.tullab.ui.screens.calendar.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.util.Locale

@Composable
fun CalendarLegend(
    locale: Locale,
    modifier: Modifier = Modifier
)
```

- **Behavior**:
  - Displays a responsive, wrapping or horizontal row of color dots with localized labels:
    - Green: `calendar_legend_paid` ("Ödendi")
    - Yellow: `calendar_legend_completed` ("Tamamlandı")
    - Blue: `calendar_legend_scheduled` ("Planlandı")
    - Red: `calendar_legend_cancelled` ("Yapılmadı / İptal")
    - Orange/Magenta: `calendar_legend_homework` ("Ödev")

---

## 3. LogLessonDialog with SegmentedButton

```kotlin
package com.barutdev.tullab.ui.screens.dashboard.components

@Composable
fun LogLessonDialog(
    showDialog: Boolean,
    lesson: Lesson?,
    onDismiss: () -> Unit,
    onSaveStatusAndDetails: (
        duration: String,
        notes: String,
        pricingMode: PricingMode,
        rateOrFeeInput: String,
        statusChoice: LogLessonStatusChoice
    ) -> Unit,
    currencyCode: String
)
```

- **Contract Changes**:
  - Replaces `onComplete` and `onMarkNotDone` with a unified `onSaveStatusAndDetails` callback.
  - Presents an M3 `SingleChoiceSegmentedButtonRow`:
    - "Planlandı", "Yapıldı", "Yapılmadı"
  - A single primary "Kaydet" button triggers `onSaveStatusAndDetails`.
  - Implements full IME window inset padding (`Modifier.imePadding()`) and scrollable column layout so the on-screen keyboard never obscures input fields, segmented buttons, or the save button.

---

## 4. HomeworkBottomSheet (replacing HomeworkDialog)

```kotlin
package com.barutdev.tullab.ui.screens.homework.components

import androidx.compose.runtime.Composable
import com.barutdev.tullab.domain.model.Homework
import com.barutdev.tullab.domain.model.HomeworkStatus

@Composable
fun HomeworkBottomSheet(
    showSheet: Boolean,
    editingHomework: Homework?,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        dueDate: Long,
        status: HomeworkStatus,
        performanceNotes: String?
    ) -> Unit
)
```

- **Contract Changes**:
  - Renders inside an M3 `ModalBottomSheet` anchored to the screen bottom.
  - All input fields use `OutlinedTextField`.
  - Supports IME keyboard insets and scrolling.

---

## 5. HomeworkFilterChips (Homeworks)

```kotlin
package com.barutdev.tullab.ui.screens.homework.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.barutdev.tullab.ui.screens.homework.HomeworkFilter

@Composable
fun HomeworkFilterChips(
    selectedFilter: HomeworkFilter,
    onFilterSelected: (HomeworkFilter) -> Unit,
    modifier: Modifier = Modifier
)
```

---

## 6. MonthlyEarningsChart with Tooltip

```kotlin
package com.barutdev.tullab.ui.screens.reports.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.barutdev.tullab.ui.screens.reports.ChartBarData

@Composable
fun MonthlyEarningsChart(
    bars: List<ChartBarData>,
    modifier: Modifier = Modifier
)
```

- Displays compact values above bars.
- Tapping a bar selects it and renders a Material 3 container/tooltip with the exact unrounded amount.
