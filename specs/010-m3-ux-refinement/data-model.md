# Data Model: Material 3 Standards & Cross-Screen UX Refinement

This document describes the state models, enumerations, and UI presentation entities introduced or modified for this feature.

## Enums

### 1. HomeworkFilter
Represents the active filter state for the homework list.

```kotlin
package com.barutdev.tullab.ui.screens.homework

enum class HomeworkFilter {
    ALL,
    PENDING,
    COMPLETED
}
```

- `ALL`: Display all homework assignments for the student.
- `PENDING`: Display assignments whose status is `PENDING` or `OVERDUE`.
- `COMPLETED`: Display assignments whose status is `COMPLETED`.

### 2. LogLessonStatusChoice
Represents the status choice inside `LogLessonDialog`'s SegmentedButton.

```kotlin
package com.barutdev.tullab.ui.screens.dashboard.components

import com.barutdev.tullab.domain.model.LessonStatus

enum class LogLessonStatusChoice {
    SCHEDULED,
    COMPLETED,
    CANCELLED;

    fun toLessonStatus(): LessonStatus = when (this) {
        SCHEDULED -> LessonStatus.SCHEDULED
        COMPLETED -> LessonStatus.COMPLETED
        CANCELLED -> LessonStatus.CANCELLED
    }

    companion object {
        fun fromLessonStatus(status: LessonStatus): LogLessonStatusChoice = when (status) {
            LessonStatus.SCHEDULED -> SCHEDULED
            LessonStatus.COMPLETED, LessonStatus.PAID -> COMPLETED
            LessonStatus.CANCELLED -> CANCELLED
        }
    }
}
```

## UI Presentation Models

### 3. ChartBarData (Reports)
Enhanced data structure representing a bar in `MonthlyEarningsChart`.

```kotlin
data class ChartBarData(
    val label: String,                // Month abbreviation (e.g., "AĞU")
    val fullPeriodLabel: String,      // Full month & year (e.g., "Ağustos 2026")
    val value: Double,                // Raw numeric value for bar height calculation
    val compactFormattedValue: String,// Short string for top-of-bar display (e.g., "₺12.5K")
    val fullFormattedValue: String    // Exact unrounded currency string for tooltip (e.g., "₺12.450,00")
)
```

### 4. CalendarLegendItem
Data model for rendering the indicator legend on `CalendarScreen`.

```kotlin
data class CalendarLegendItem(
    val labelResId: Int,
    val color: Color
)
```

Legend mappings:
- Paid Lesson: `StatusGreen` (`calendar_legend_lesson_paid`)
- Completed Lesson (Awaiting payment): `StatusYellow` (`calendar_legend_lesson_completed`)
- Scheduled Lesson: `StatusBlue` (`calendar_legend_lesson_scheduled`)
- Overdue Lesson / Cancelled: `StatusRed` (`calendar_legend_lesson_attention`)
- Homework (Pending/Overdue): `StatusOrange` / `HomeworkMagenta` (`calendar_legend_homework`)

## Entity Relationships & State Flow

```mermaid
flowchart TD
    subgraph Homework Screen
        FilterState[HomeworkFilter: ALL | PENDING | COMPLETED] --> ListFilter[Filtered Homework List]
        AddFAB[Add Homework FAB] --> BottomSheet[ModalBottomSheet: OutlinedTextFields]
    end

    subgraph Calendar Screen
        SpeedDial[Speed Dial FAB] -->|Single| LogDialog[Log/Schedule Lesson Sheet]
        SpeedDial -->|Bulk| BulkScheduleRoute[Bulk Schedule Screen]
        Legend[Calendar Dot Legend] --> CalendarGrid[Monthly Calendar Grid]
    end

    subgraph Log Lesson Dialog
        DateCheck{Is Past/Today?} -->|Yes| DefaultCompleted[Default: Yapıldı]
        DateCheck -->|No| DefaultScheduled[Default: Planlandı]
        SegmentedButton[M3 SegmentedButton: Planlandı | Yapıldı | Yapılmadı] --> SaveBtn[Single Primary Save Button]
    end

    subgraph Reports Screen
        Bars[Chart Bars] --> CompactLabels[Compact Values Above Bars: ₺12.5K]
        BarTap[Tap on Bar] --> Tooltip[Detailed Full Currency + Period Tooltip]
    end
```
