# Data Model & State Specifications: lesson-times-homework-state

## Entities & Enums

### 1. HomeworkStatus (Domain Enum)

Path: `app/src/main/java/com/barutdev/tullab/domain/model/HomeworkStatus.kt`

```kotlin
package com.barutdev.tullab.domain.model

enum class HomeworkStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}
```

- **Removed Values**: `OVERDUE` is removed from active domain enum values.
- **Allowed Transitions**:
  - `PENDING` ↔ `COMPLETED`
  - `PENDING` ↔ `CANCELLED`
  - `COMPLETED` ↔ `CANCELLED`

### 2. Homework Presentation State & Helpers

Path: `app/src/main/java/com/barutdev/tullab/domain/model/Homework.kt`

```kotlin
package com.barutdev.tullab.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

data class Homework(
    val id: Int,
    val studentId: Int,
    val title: String,
    val description: String,
    val creationDate: Long,
    val dueDate: Long,
    val status: HomeworkStatus,
    val performanceNotes: String?
) {
    /**
     * Determines whether the homework assignment is overdue.
     * Evaluates true strictly when status is PENDING and due date is prior to today.
     */
    fun isOverdue(today: LocalDate = LocalDate.now(ZoneOffset.UTC)): Boolean {
        if (status != HomeworkStatus.PENDING) return false
        val dueLocalDate = Instant.ofEpochMilli(dueDate)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        return dueLocalDate.isBefore(today)
    }
}
```

### 3. Room Type Converter

Path: `app/src/main/java/com/barutdev/tullab/data/local/HomeworkStatusConverter.kt`

```kotlin
package com.barutdev.tullab.data.local

import androidx.room.TypeConverter
import com.barutdev.tullab.domain.model.HomeworkStatus

class HomeworkStatusConverter {
    @TypeConverter
    fun fromStatus(status: HomeworkStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): HomeworkStatus {
        return if (value == "OVERDUE") {
            HomeworkStatus.PENDING
        } else {
            runCatching { HomeworkStatus.valueOf(value) }.getOrDefault(HomeworkStatus.PENDING)
        }
    }
}
```

### 4. Lesson Time Presentation

Path: `app/src/main/java/com/barutdev/tullab/util/TimeFormatter.kt`

```kotlin
package com.barutdev.tullab.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.util.Date

/**
 * Formats a timestamp into localized time using the user's device preferences (12/24h).
 */
fun formatLessonStartTime(context: Context, epochMillis: Long): String {
    return android.text.format.DateFormat.getTimeFormat(context).format(Date(epochMillis))
}

@Composable
fun formatLessonStartTime(epochMillis: Long): String {
    val context = LocalContext.current
    return formatLessonStartTime(context, epochMillis)
}
```
