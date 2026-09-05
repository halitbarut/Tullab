# UI Contracts: Post-AI Streamlining

**Feature**: `002-remove-ai-features`  
**Date**: 2026-09-03  
**Status**: Completed  

## 1. Dashboard UI Contract

### 1.1 `DashboardUiState` (No AI fields)
```kotlin
data class DashboardUiState(
    val studentId: Int? = null,
    val studentName: String = "",
    val hourlyRate: Double = 0.0,
    val totalHours: Double = 0.0,
    val totalAmountDue: Double = 0.0,
    val completedLessonsAwaitingPayment: List<Lesson> = emptyList(),
    val lastPaymentDate: Long? = null,
    val isAddLessonDialogVisible: Boolean = false,
    val upcomingLessons: List<Lesson> = emptyList(),
    val pastLessonsToLog: List<Lesson> = emptyList(),
    val isLogLessonDialogVisible: Boolean = false,
    val lessonToLog: Lesson? = null,
    val isPaymentHistoryDialogVisible: Boolean = false,
    val isMarkAsPaidDialogVisible: Boolean = false
)
```
*(Removed: `aiInsightsState: StateFlow<AiInsightsUiState>` from `DashboardViewModel`)*

### 1.2 `DashboardContent` Composable Contract
```kotlin
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onAddLessonClick: () -> Unit,
    onLogLessonClick: (Lesson) -> Unit,
    onMarkLessonPaidClick: (Lesson) -> Unit,
    onOpenPaymentHistory: () -> Unit,
    onMarkCurrentCycleAsPaid: () -> Unit,
    modifier: Modifier = Modifier
)
```
*(Removed: `aiInsightsState: AiInsightsUiState` and `onGenerateAiInsights: () -> Unit`)*

---

## 2. Homework UI Contract

### 2.1 `HomeworkUiState` (No AI fields)
```kotlin
data class HomeworkUiState(
    val studentId: Int? = null,
    val studentName: String = "",
    val homeworkList: List<Homework> = emptyList(),
    val filterStatus: HomeworkStatus? = null,
    val isAddHomeworkDialogVisible: Boolean = false,
    val homeworkToEdit: Homework? = null,
    val isLoading: Boolean = false
)
```
*(Removed: `aiInsightsState: StateFlow<AiInsightsUiState>` from `HomeworkViewModel`)*

### 2.2 `HomeworkContent` Composable Contract
```kotlin
@Composable
fun HomeworkContent(
    uiState: HomeworkUiState,
    onFilterSelect: (HomeworkStatus?) -> Unit,
    onAddHomeworkClick: () -> Unit,
    onHomeworkClick: (Homework) -> Unit,
    onToggleStatus: (Homework) -> Unit,
    modifier: Modifier = Modifier
)
```
*(Removed: `aiInsightsState: AiInsightsUiState` and `onGenerateAiInsights: () -> Unit`)*

---

## 3. Decommissioned UI Components
The following UI files and components are permanently removed:
- `com.barutdev.kora.ui.model.AiInsightsUiState`
- `com.barutdev.kora.ui.model.AiStatus`
- `AiAssistantCard` in `DashboardScreen.kt`
- `AiAssistantCard` in `HomeworkScreen.kt`
