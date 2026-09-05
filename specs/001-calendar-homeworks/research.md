# Research: Calendar Homework Integration

**Feature**: Show Homeworks on Calendar
**Date**: 2026-03-05 (updated post-clarification)

## Decision Log

### D-001: Homework Status Enum — 4 Values

**Decision**: `HomeworkStatus` uses 4 values: `PENDING`, `COMPLETED`, `OVERDUE`, `CANCELLED`.

**Rationale**: The user explicitly chose 4 statuses during clarification. `OVERDUE` is auto-set when homework is past due and still PENDING. `CANCELLED` enables soft-delete semantics without removing data.

**Alternatives considered**:
- 2 statuses (PENDING/COMPLETED) — too limited; forces overdue to be computed at render time
- 3 statuses (PENDING/COMPLETED/OVERDUE) — current code; missing CANCELLED
- 4 statuses with CANCELLED — **chosen**

**Impact**: `HomeworkStatus.kt` needs `CANCELLED` added. Room migration required (new enum value in status column). `CalendarStatusResolver` and `toggleHomeworkStatus` need update.

### D-002: Distinct Color Palette for Homework

**Decision**: Homework uses a completely separate color palette from lessons.

| Event Type | Status | Color | Hex |
|------------|--------|-------|-----|
| Lesson | Paid | Green | `#2E7D32` (existing `StatusGreen`) |
| Lesson | Completed (awaiting payment) | Yellow | `#F9A825` (existing `StatusYellow`) |
| Lesson | Scheduled (future) | Blue | `#1E88E5` (existing `StatusBlue`) |
| Lesson | Cancelled | Red | `#D32F2F` (existing `StatusRed`) |
| Homework | Completed | Teal | `#009688` (new `HomeworkTeal`) |
| Homework | Pending (future) | Orange | `#FB8C00` (existing `StatusOrange`) |
| Homework | Overdue | Magenta | `#E91E63` (new `HomeworkMagenta`) |
| Homework | Cancelled | Gray | `#9E9E9E` (new `HomeworkGray`) |

**Rationale**: User explicitly stated homework must have "completely different color schema from lessons." This enables instant visual distinction between event types at a glance.

**Alternatives considered**:
- Shared colors (same Red/Green/Yellow/Blue) — rejected; indistinguishable event types
- Subtle shade variations — rejected; insufficient contrast at small dot size

**Impact**: New color constants in `Color.kt`. `CalendarStatusResolver` must map homework statuses to homework-specific colors. `HomeworkDetailCard` must use homework-specific status colors.

### D-003: Dual-Dot Calendar Indicator

**Decision**: Calendar day cells show two side-by-side indicator dots when both lessons and homework exist on the same day. Left dot = lesson status (most urgent), right dot = homework status (most urgent). Single centered dot when only one type exists.

**Rationale**: User chose this over single combined indicator. It preserves per-type status visibility without cluttering the compact calendar grid.

**Alternatives considered**:
- Single dot (most urgent wins) — previous implementation; loses type distinction when both present
- Split dot (half/half colors) — visually complex at 6dp size
- Two dots side-by-side — **chosen**

**Impact**: Major refactor of `CalendarStatusResolver` — must return two optional colors instead of one. `CalendarDayCell` composable must render 1 or 2 dots based on presence. `resolveCombinedStatusColor` → `resolveDayIndicators` returning a `DayIndicators` data class.

### D-004: Directed State Transitions

**Decision**: Homework status transitions follow directed rules:
- `PENDING` ↔ `COMPLETED` (toggle via calendar action)
- `PENDING` → `OVERDUE` (automatic, system-determined when past due date)
- `OVERDUE` → `COMPLETED` (tutor can mark overdue homework as done)
- Any → `CANCELLED` (one-way; cancelled homework cannot be reactivated from calendar)

**Rationale**: Prevents accidental reactivation of cancelled work while allowing practical status management (marking overdue items as done, toggling between pending/completed).

**Alternatives considered**:
- Free transitions (any ↔ any) — rejected; no protection against accidental reactivation
- Strict (no reversal) — rejected; tutors need to undo accidental completions

**Impact**: `toggleHomeworkStatus` in `CalendarViewModel` needs update to handle OVERDUE→COMPLETED and block CANCELLED transitions. The calendar UI toggle button text must reflect available transitions.

### D-005: Component Rename — LessonDetailsSection → DayDetailsSection

**Decision**: Rename `LessonDetailsSection` composable to `DayDetailsSection`.

**Rationale**: The component now displays both lessons and homework. The old name is misleading for future developers.

**Alternatives considered**:
- Keep `LessonDetailsSection` — rejected; misleading name
- `DayEventsSection` — broader but less precise
- `DayDetailsSection` — **chosen**; accurate and concise

**Impact**: Rename in `CalendarScreen.kt`. All callers (only internal to `CalendarScreen.kt`) update automatically. No public API impact.

### D-006: Overdue Auto-Detection Strategy

**Decision**: `OVERDUE` status is determined by comparing homework `dueDate` against the current date. When a `PENDING` homework's due date is in the past, the indicator renders as Magenta (overdue). The actual `HomeworkStatus` field may remain `PENDING` in the database — the visual overdue state is resolved at render time OR by a periodic check. 

**Rationale**: Render-time resolution avoids the need for a background job while still showing overdue items correctly. The spec says `PENDING → OVERDUE` is "automatic, system-determined" — this can be interpreted as render-time logic.

**Alternatives considered**:
- Background WorkManager job to update DB — overkill for local app without notifications
- Render-time only (never persist OVERDUE) — simpler; the OVERDUE enum value is available for explicit marking if needed
- Hybrid: render-time detection + persist on access — **recommended**; when the status resolver detects a past-due PENDING homework, it can trigger a DB update

**Impact**: `CalendarStatusResolver` checks date context for PENDING homework. ViewModel may optionally persist the OVERDUE transition when detected.
