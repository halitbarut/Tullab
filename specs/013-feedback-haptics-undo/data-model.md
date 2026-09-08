# Phase 1: Data Model & State Transitions

**Feature**: Comprehensive Haptic & Visual Feedback with Undo (`013-feedback-haptics-undo`)
**Date**: 2026-09-08

## 1. Entities & Snapshots

The feature requires no changes to the Room database schema. All persistence transitions occur via existing Room entities and DAOs (`Lesson`, `Homework`, and `PaymentRecordEntity` via `PaymentRecordDao`). During `markLessonAsPaid`, an associated payment record is created alongside updating the lesson. On revert, the payment record is resolved and deleted via the lesson's `paymentTimestamp` before clearing the lesson payment fields. Undo operations rely on in-memory snapshot restoration. Revert operations remain keyed by `lessonId` without introducing a separate payment-record ID.

### In-Memory Undo Data Models

```kotlin
sealed interface UndoableAction {
    val message: String
    val actionLabel: String

    data class RevertPayment(
        val lessonId: Int,
        val previousStatus: LessonStatus,
        val previousDuration: Double?,
        val previousFee: Double,
        override val message: String,
        override val actionLabel: String
    ) : UndoableAction

    data class RevertHomeworkStatus(
        val homeworkId: Int,
        val previousStatus: HomeworkStatus,
        override val message: String,
        override val actionLabel: String
    ) : UndoableAction

    data class RestoreLesson(
        val lessonSnapshot: Lesson,
        override val message: String,
        override val actionLabel: String
    ) : UndoableAction

    data class RestoreHomework(
        val homeworkSnapshot: Homework,
        override val message: String,
        override val actionLabel: String
    ) : UndoableAction
}
```

## 2. State Transitions

### Payment State Transition
```
[Unpaid Lesson] ──(Mark Paid + Confirmation Haptic)──> [Paid Lesson] + [Root Snackbar with Undo]
       │                                                                  │
       └──(Timeout / Ignore / New Action)──> [Permanent Paid]             │
       │                                                                  │
       └───────────────────(Tap "Undo")───────────────────────────────────┘
```

### Homework Status Transition
```
[Pending Homework] ──(Toggle Checkbox + Click Haptic)──> [Completed Homework] + [Root Snackbar with Undo]
       │                                                                     │
       └──(Timeout / Ignore / New Action)──> [Permanent Completed]           │
       │                                                                     │
       └───────────────────(Tap "Undo")──────────────────────────────────────┘
```

### Deletion Transition
```
[Active Item] ──(Confirm Delete + Warning Haptic)──> [Item Deleted from DB, Snapshot in Memory] + [Root Snackbar with Undo]
       │                                                                                              │
       └──(Timeout / New Action)──> [Snapshot Discarded, Permanently Deleted]                        │
       │                                                                                              │
       └──────────────────────────(Tap "Undo": Re-insert Snapshot)────────────────────────────────────┘
```
