# Phase 1: Data Model & State Changes

## 1. Entities & Data Access Layer

### `HomeworkDao` (`app/src/main/java/com/barutdev/tullab/data/local/HomeworkDao.kt`)
- **Addition**:
  ```kotlin
  @Delete
  suspend fun delete(homework: HomeworkEntity)
  ```
  *(Alternatively `@Query("DELETE FROM homework WHERE id = :id") suspend fun deleteById(id: Int)` or `@Delete`)*.

### `HomeworkRepository` (`app/src/main/java/com/barutdev/tullab/domain/repository/HomeworkRepository.kt`)
- **Addition**:
  ```kotlin
  suspend fun deleteHomework(homework: Homework)
  ```

### `HomeworkRepositoryImpl` (`app/src/main/java/com/barutdev/tullab/data/repository/HomeworkRepositoryImpl.kt`)
- **Implementation**:
  ```kotlin
  override suspend fun deleteHomework(homework: Homework) {
      homeworkDao.delete(homework.toEntity())
  }
  ```

## 2. ViewModel & State Flow Changes

### `HomeworkViewModel` (`app/src/main/java/com/barutdev/tullab/ui/screens/homework/HomeworkViewModel.kt`)
- **Function**:
  ```kotlin
  fun onDeleteHomework(homework: Homework) {
      viewModelScope.launch {
          homeworkRepository.deleteHomework(homework)
          dismissHomeworkDialog()
      }
  }
  ```

## 3. UI Layer Contracts & State

### `HomeworkBottomSheet` (`app/src/main/java/com/barutdev/tullab/ui/screens/homework/components/HomeworkBottomSheet.kt`)
- **Parameters**:
  ```kotlin
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
      ) -> Unit,
      onDelete: ((Homework) -> Unit)? = null
  )
  ```
- **Internal State**:
  - `var showDeleteConfirmationDialog by rememberSaveable { mutableStateOf(false) }`
- **Header Structure**:
  - Row with title (`Text(sheetTitle, modifier = Modifier.weight(1f))`)
  - If `isEditing && onDelete != null`:
    - `IconButton(onClick = { showDeleteConfirmationDialog = true }) { Icon(Icons.Outlined.Delete, tint = MaterialTheme.colorScheme.error) }`

### `CalendarDayDetails` (`app/src/main/java/com/barutdev/tullab/ui/screens/calendar/CalendarScreen.kt`)
- **Header State**:
  - Dynamically computes header string based on `lessonsSorted` and `homework` lists.
