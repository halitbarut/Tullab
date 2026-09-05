# Quickstart: Calendar Homework Integration

**Feature**: Show Homeworks on Calendar
**Date**: 2026-03-05 (updated post-clarification)

## Implementation Status

### What's Already Done ✅

| Component | Location | Status |
|-----------|----------|--------|
| `Homework` domain model (8 fields) | `domain/model/Homework.kt` | ✅ Complete |
| `HomeworkStatus` enum (3 of 4 values) | `domain/model/HomeworkStatus.kt` | ⚠️ Missing `CANCELLED` |
| `HomeworkRepository` interface | `domain/repository/HomeworkRepository.kt` | ✅ Complete |
| `HomeworkRepositoryImpl` | `data/repository/HomeworkRepositoryImpl.kt` | ✅ Complete |
| `HomeworkDao` | `data/local/dao/HomeworkDao.kt` | ✅ Complete |
| `CalendarViewModel` (homework fetch + toggle) | `ui/screens/calendar/CalendarViewModel.kt` | ⚠️ Needs state transition update |
| `CalendarStatusResolver` (single-dot) | `ui/screens/calendar/CalendarStatusResolver.kt` | ⚠️ Needs dual-dot + new colors |
| `CalendarDayCell` (single dot) | `ui/screens/calendar/CalendarScreen.kt` | ⚠️ Needs dual-dot rendering |
| `LessonDetailsSection` (shows homework) | `ui/screens/calendar/CalendarScreen.kt` | ⚠️ Needs rename to `DayDetailsSection` |
| `HomeworkDetailCard` | `ui/screens/calendar/CalendarScreen.kt` | ⚠️ Needs homework-specific colors |
| `CalendarStatusLogicTest` (14 tests) | `test/.../CalendarStatusLogicTest.kt` | ⚠️ Needs update for dual-dot API |
| Navigation (homework route + homeworkId) | `navigation/KoraDestinations.kt` | ✅ Complete |
| HomeworkViewModel (edit dialog on homeworkId) | `ui/screens/homework/HomeworkViewModel.kt` | ✅ Complete |
| i18n strings (EN/TR/DE) | `res/values*/strings.xml` | ⚠️ Needs CANCELLED status strings |

### What Needs To Change 🔧

#### 1. Add `CANCELLED` to `HomeworkStatus` enum
```kotlin
// domain/model/HomeworkStatus.kt
enum class HomeworkStatus {
    PENDING, COMPLETED, OVERDUE, CANCELLED
}
```

#### 2. Add homework color constants to `Color.kt`
```kotlin
// ui/theme/Color.kt
val HomeworkTeal = Color(0xFF009688)
val HomeworkTealContainer = Color(0xFFE0F2F1)
val HomeworkMagenta = Color(0xFFE91E63)
val HomeworkMagentaContainer = Color(0xFFFCE4EC)
val HomeworkGray = Color(0xFF9E9E9E)
val HomeworkGrayContainer = Color(0xFFF5F5F5)
```

#### 3. Refactor `CalendarStatusResolver` — dual-dot API
```kotlin
// Return two optional colors instead of one
internal data class DayIndicators(
    val lessonColor: Color? = null,
    val homeworkColor: Color? = null
)

internal fun resolveDayIndicators(
    lessons: List<Lesson>,
    homework: List<Homework>,
    date: LocalDate,
    today: LocalDate
): DayIndicators
```

#### 4. Update `CalendarDayCell` — render dual dots
- Two 5dp dots with 2dp spacing when both types present
- Single centered 6dp dot when only one type present

#### 5. Rename `LessonDetailsSection` → `DayDetailsSection`
- Simple rename, all callers are internal to `CalendarScreen.kt`

#### 6. Update `toggleHomeworkStatus` — directed transitions
```kotlin
// CalendarViewModel.kt
fun toggleHomeworkStatus(homework: Homework) {
    viewModelScope.launch {
        val newStatus = when (homework.status) {
            HomeworkStatus.PENDING -> HomeworkStatus.COMPLETED
            HomeworkStatus.COMPLETED -> HomeworkStatus.PENDING
            HomeworkStatus.OVERDUE -> HomeworkStatus.COMPLETED
            HomeworkStatus.CANCELLED -> return@launch // Terminal, no toggle
        }
        homeworkRepository.updateHomework(homework.copy(status = newStatus))
    }
}
```

#### 7. Update `HomeworkDetailCard` — homework-specific colors
- Use `HomeworkTeal`/`StatusOrange`/`HomeworkMagenta`/`HomeworkGray` instead of lesson colors
- Toggle button text depends on current status (hide for CANCELLED)

#### 8. Add i18n strings for CANCELLED status
- EN: "Cancelled" (may already exist from lessons)
- TR: "İptal edildi"
- DE: "Abgesagt"

## Build & Test Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests (status resolver + ViewModel)
./gradlew :app:testDebugUnitTest --tests "com.barutdev.kora.ui.screens.calendar.*"

# Run all unit tests
./gradlew :app:testDebugUnitTest

# Run instrumented tests (requires emulator/device)
./gradlew :app:connectedAndroidTest
```

## Key File Locations

| Purpose | Path |
|---------|------|
| Spec | `specs/001-calendar-homeworks/spec.md` |
| Plan | `specs/001-calendar-homeworks/plan.md` |
| Research | `specs/001-calendar-homeworks/research.md` |
| Data Model | `specs/001-calendar-homeworks/data-model.md` |
| Tasks | `specs/001-calendar-homeworks/tasks.md` |
| Status Resolver | `app/src/main/java/.../calendar/CalendarStatusResolver.kt` |
| Calendar Screen | `app/src/main/java/.../calendar/CalendarScreen.kt` |
| Calendar ViewModel | `app/src/main/java/.../calendar/CalendarViewModel.kt` |
| Homework Model | `app/src/main/java/.../domain/model/Homework.kt` |
| Homework Status | `app/src/main/java/.../domain/model/HomeworkStatus.kt` |
| Color Constants | `app/src/main/java/.../ui/theme/Color.kt` |
| Unit Tests | `app/src/test/java/.../calendar/CalendarStatusLogicTest.kt` |
