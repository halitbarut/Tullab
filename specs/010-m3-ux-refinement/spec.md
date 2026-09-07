# Feature Specification: Material 3 Standards & Cross-Screen UX Refinement

**Feature Branch**: `010-m3-ux-refinement`

**Created**: 2026-09-07

**Status**: Ready for Planning

**Input**: User description: "Refactor the UI to strictly follow Material 3 standards and enhance UX across all screens by unifying all text inputs into consistent M3 OutlinedTextFields (resolving the visual clash between New Student's outlined inputs and the Add Homework modal's filled inputs), localizing mixed strings like "6 hours" and "Notes" into Turkish, and removing the redundant settings gear icon from top app bars on sub-screens, including the Settings screen itself. On the Student List (Home) screen, upgrade the rectangular search box to a pill-shaped M3 SearchBar with a subtle container tint, and eliminate the dual edit-pencil/chevron clutter by making entire list cards tappable. In the Student Detail / Dashboard screen, drop the "Öğrenci:" prefix to display just the student's name in the header, and increase the contrast of the "Ödendi olarak işaretle" button inside the payment card. On the Calendar screen, add a color legend explaining the day indicator dots, resolve the visual competition between the "Toplu Ders Ekle" button and the floating action button, and in the Save Lesson Bottom Sheet, move the "Yapılmadı" option into an inline status dropdown/toggle rather than having three competing bottom action buttons. On the Homeworks screen, add horizontal filter chips (Tümü, Bekleyenler, Tamamlananlar) beneath the header, and convert the centered Add Homework dialog into a bottom sheet for better one-handed usability. Lastly, on the Reports screen, display exact monetary values directly above the chart bars or reveal them via tap tooltips to make the monthly trend data immediately readable."

## Clarifications

### Session 2026-09-07
- Q: How should the "Toplu Ders Ekle" (Bulk Add Lessons) action be integrated on the Calendar screen to eliminate visual competition with the floating action button? (FR-009) → A: Implement an expandable Speed Dial FAB and remove the full-width "Toplu Ders Ekle" button from beneath the calendar grid. When tapped, the primary FAB expands into two labeled mini-action buttons: "Seçili Güne Planla" (schedule on selected date) and "Toplu Ders Ekle" (bulk lesson planning).
- Q: In the Save / Log Lesson bottom sheet, should the inline status selector be a 2-option toggle between Held and Not Held, or a full status selector including Scheduled? (FR-010) → A: Use an M3 SegmentedButton with three states: "Planlandı" (Scheduled), "Yapıldı" (Completed), and "Yapılmadı" (Not Held), featuring smart defaults (defaulting to "Yapıldı" for past/current dates and "Planlandı" for future dates), accompanied by a single primary "Kaydet" (Save) button.
- Q: On the Reports screen, how should the exact monetary values for the chart bars be displayed? (FR-013) → A: Display compact/abbreviated currency values above the bars (e.g., "₺12.5K") for quick at-a-glance scanning, while revealing a detailed tooltip with the full, unrounded currency amount and period upon tapping a specific bar.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Unified Text Fields, Clean Top Bars & Consistent Localization (Priority: P1)

As a tutor navigating across different workflows (creating students, logging homework, recording lesson notes, configuring preferences), I want all text input fields to look visually consistent using Material 3 OutlinedTextField styling, all UI labels to be fully translated into my selected language (Turkish/English/German), and top app bars to be clean and focused without redundant settings icons, so that the application feels cohesive, polished, and distraction-free.

**Why this priority**: Inconsistent inputs (filled vs. outlined), untranslated English strings in Turkish locale, and redundant action icons immediately undermine visual quality and professional polish across the entire application.

**Independent Test**: Can be verified by opening the Student Creation form, Add Homework modal, Lesson details, and Settings screen across locales:
1. Verify all text inputs share consistent outlined borders, shape, and label behavior.
2. Verify strings such as durations ("6 hours" / "6 saat") and field labels ("Notes" / "Notlar") are localized correctly in Turkish, English, and German without hardcoded strings.
3. Verify top app bars on secondary/detail/settings screens only present relevant actions and a back button, with no redundant settings gear icon.

**Acceptance Scenarios**:

1. **Given** the tutor is on the Add Homework sheet or dialog, **When** viewing the title and description inputs, **Then** both are rendered as M3 `OutlinedTextField` matching the styling of the New Student form.
2. **Given** the app locale is set to Turkish, **When** viewing lesson cards, homework entries, or student summaries, **Then** relative durations (e.g., hours/duration) and note headers render in Turkish ("saat", "Notlar") rather than hardcoded English strings ("hours", "Notes").
3. **Given** the tutor navigates to any sub-screen (Student Detail, Calendar, Homeworks, Reports, Settings, or New Student), **When** inspecting the top app bar, **Then** no redundant settings gear icon is shown (Settings is accessible only from the primary home screen or top-level navigation, and never from within the Settings screen itself).

---

### User Story 2 - Modern Student List (Home) Screen Experience (Priority: P2)

As a tutor browsing and searching through my active student roster on the Home screen, I want a sleek, pill-shaped Material 3 search bar with container tint and clean, clutter-free list items where the entire card navigates directly to the student dashboard, so that I can find and access student details rapidly with natural touch gestures.

**Why this priority**: The Home screen is the primary entry point of the app. Eliminating visual clutter (removing the redundant edit pencil and chevron icons) and modernizing the search bar provides immediate ergonomic and aesthetic improvements.

**Independent Test**: Can be tested on the Home screen:
1. Verify search input uses a pill-shaped container with standard M3 surface tinting.
2. Verify list item cards do not display extraneous pencil/chevron icons.
3. Tapping anywhere on a student card opens that student's dashboard.

**Acceptance Scenarios**:

1. **Given** the tutor is on the Student List screen, **When** observing the search component, **Then** it renders as a pill-shaped Material 3 SearchBar with rounded corners and appropriate container tint.
2. **Given** a list of students on the Home screen, **When** viewing each student card, **Then** visual clutter is minimized by omitting separate chevron and edit pencil buttons.
3. **Given** a student card in the list, **When** the user taps anywhere on the card surface, **Then** the app navigates immediately to that student's Detail / Dashboard screen.

---

### User Story 3 - Polished Student Detail / Dashboard (Priority: P2)

As a tutor managing an individual student's profile and financials, I want the screen header to display the student's name cleanly without redundant prefixes, and I want high-contrast, accessible action buttons (such as "Ödendi olarak işaretle" / "Mark as Paid") in the payment card, so that key information is instantly recognizable and financial actions are effortless to trigger.

**Why this priority**: Removes visual noise from the most critical dashboard header and guarantees WCAG accessibility / touch clarity for financial status updates.

**Independent Test**: Can be tested by opening a student's detail screen:
1. Verify the header displays the student's name directly (e.g., "Ali Yılmaz" instead of "Öğrenci: Ali Yılmaz").
2. Verify the "Ödendi olarak işaretle" button has strong visual contrast against the payment card container background, passing WCAG AA standards.

**Acceptance Scenarios**:

1. **Given** the tutor opens a student's detail screen, **When** viewing the top header, **Then** the student's name is displayed prominently without any "Öğrenci:" or "Student:" label prefix.
2. **Given** an unpaid lesson or balance entry in the payment section, **When** viewing the "Ödendi olarak işaretle" action button, **Then** the button exhibits distinct contrast (using filled or elevated button treatment with high-contrast text/icon) clearly differentiating it from the card background.

---

### User Story 4 - Streamlined Calendar & Lesson Logging (Priority: P2)

As a tutor viewing my monthly schedule and logging lessons, I want an intuitive dot-color legend on the Calendar screen, an expandable Speed Dial FAB uniting single and bulk lesson scheduling without competing in-feed buttons, and an inline status segmented button inside the lesson bottom sheet instead of competing action buttons, so that managing lesson attendance is intuitive and fast.

**Why this priority**: The calendar currently has competing action buttons and ambiguous indicator dots. Unifying actions into an expandable Speed Dial FAB and streamlining the bottom sheet status flow eliminates user hesitation.

**Independent Test**: Can be tested by navigating to the Calendar screen and opening the lesson logging sheet:
1. Verify a visible legend explains the meaning of day indicator dots (e.g., green for paid, yellow for completed, blue for scheduled, red for past-due/cancelled, homework dots).
2. Tap the calendar FAB and verify it smoothly expands into "Seçili Güne Planla" and "Toplu Ders Ekle" speed-dial options, with the old in-feed button removed.
3. Open the Save/Log Lesson bottom sheet and verify an M3 SegmentedButton displays "Planlandı", "Yapıldı", and "Yapılmadı" with smart defaults (defaults to "Yapıldı" for past/current dates and "Planlandı" for future dates), accompanied by a single primary "Kaydet" button.

**Acceptance Scenarios**:

1. **Given** the tutor is on the Calendar screen, **When** viewing the calendar grid, **Then** a clear legend is presented identifying what each dot color represents.
2. **Given** the Calendar screen, **When** interacting with the schedule action, **Then** tapping the primary FAB expands into two labeled speed dial buttons: "Seçili Güne Planla" and "Toplu Ders Ekle", and no separate full-width button exists under the calendar grid.
3. **Given** the tutor opens the Save/Log Lesson Bottom Sheet, **When** specifying lesson status, **Then** status is selected via an M3 SegmentedButton with "Planlandı", "Yapıldı", and "Yapılmadı", defaulting to "Yapıldı" for past/current dates and "Planlandı" for future dates.
4. **Given** the tutor selects the desired status in the sheet, **When** saving, **Then** a single primary "Kaydet" (Save) button commits the entry with the chosen status.

---

### User Story 5 - One-Handed Homework Management & Filtering (Priority: P3)

As a tutor managing student assignments, I want to quickly filter homework by status (All, Pending, Completed) using horizontal chips, and I want the Add Homework form to slide up as a bottom sheet rather than a centered modal dialog, so that I can easily organize assignments with one-handed phone usage.

**Why this priority**: Centered dialogs with multiple inputs are awkward for thumb reach and keyboard handling; status chips make homework tracking significantly faster.

**Independent Test**: Can be tested on the Homeworks screen:
1. Verify horizontal filter chips (`Tümü`, `Bekleyenler`, `Tamamlananlar` / `All`, `Pending`, `Completed`) sit beneath the screen header and filter the displayed homework list instantly when tapped.
2. Tap the Add Homework button and verify it opens as an M3 ModalBottomSheet rather than a centered AlertDialog.

**Acceptance Scenarios**:

1. **Given** the tutor is on the Homeworks screen, **When** viewing the space directly beneath the header/search area, **Then** horizontal filter chips ("Tümü", "Bekleyenler", "Tamamlananlar") are displayed.
2. **Given** the tutor taps on "Bekleyenler", **When** the list updates, **Then** only pending homework assignments are shown.
3. **Given** the tutor clicks the Add Homework button, **When** the creation UI appears, **Then** it opens as a smooth bottom sheet anchored to the bottom of the screen with proper keyboard padding and full outlined text fields.

---

### User Story 6 - Legible Reports Chart with Values & Tooltips (Priority: P3)

As a tutor analyzing monthly income and lesson trends on the Reports screen, I want compact monetary values displayed directly above chart bars and a tap tooltip revealing the exact amount and period, so that I can immediately understand exact earnings without guessing heights on an axis or suffering text truncation.

**Why this priority**: Abstract bars without clear numbers force tutors to approximate revenue; compact labels combined with interactive tap tooltips guarantee both quick scanning and complete detail.

**Independent Test**: Can be tested on the Reports screen:
1. Check that chart bars display their compact/abbreviated monetary value (e.g., "₺12.5K") above each bar.
2. Tap any chart bar to verify an interactive tooltip appears displaying the full, unrounded formatted amount and full period/month.

**Acceptance Scenarios**:

1. **Given** the tutor views monthly or periodic charts on the Reports screen, **When** examining the bars, **Then** compact monetary values (e.g., "₺12.5K") are rendered directly above each bar.
2. **Given** a chart bar is tapped, **When** interactive feedback triggers, **Then** a high-contrast tooltip indicates the full formatted value and month/period.

---

### Edge Cases

- **Small Screen & Large Dynamic Font (up to 200%)**:
  - Outlined text fields and bottom sheets must support scrolling and avoid clipping when system accessibility font scaling is at 200%.
  - Chart labels above bars must not overlap; compact labels ensure minimum width requirements, and tap tooltips provide full readability regardless of font size.
- **Empty States with Filter Chips**:
  - Selecting "Bekleyenler" or "Tamamlananlar" when no records match must show an appropriate empty state illustration/text rather than a blank unresponsive screen.
- **Keyboard IME Inset in Bottom Sheets**:
  - Opening the Add Homework bottom sheet or Save Lesson sheet while the soft keyboard is active must automatically scroll content into view so text inputs and the save button are never obscured.
- **Long Student Names**:
  - Dropping the "Öğrenci:" prefix should handle very long student names gracefully in the dashboard header using ellipsis or multi-line wrap without pushing other controls off screen.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST render all text inputs across all screens and sheets (including New Student, Edit Student, Add Homework, and Save Lesson) using Material 3 `OutlinedTextField` with consistent border radius, colors, and typography.
- **FR-002**: System MUST externalize and localize all UI strings into Turkish (`values-tr`), English (`values`), and German (`values-de`), specifically localizing lesson durations (e.g., "X saat", "X hours", "X Stunden") using pluralization and labels (e.g., "Notlar", "Notes", "Notizen").
- **FR-003**: System MUST eliminate redundant settings gear icons from the top app bars of all sub-screens (Student Detail, Calendar, Homeworks, Reports, Settings screen itself, and creation screens), keeping Settings access scoped to the root/home screen or primary navigation.
- **FR-004**: System MUST render the search box on the Student List screen as a Material 3 pill-shaped `SearchBar` with rounded corners and subtle container tinting.
- **FR-005**: System MUST make entire student list cards on the Home screen tappable to navigate to the student dashboard, removing standalone edit pencil and chevron icons to eliminate visual clutter.
- **FR-006**: System MUST display the student's name directly in the Student Detail / Dashboard header without any preceding label prefix (such as "Öğrenci:").
- **FR-007**: System MUST provide a high-contrast action button for "Ödendi olarak işaretle" (Mark as Paid) on the payment card that satisfies WCAG AA contrast against the card container.
- **FR-008**: System MUST display a color legend on the Calendar screen explaining the semantic meaning of all day indicator dots.
- **FR-009**: System MUST implement an expandable Speed Dial FAB on the Calendar screen containing "Seçili Güne Planla" and "Toplu Ders Ekle", removing the redundant full-width bulk button from beneath the calendar grid.
- **FR-010**: System MUST present lesson attendance status in the Save/Log Lesson bottom sheet via an M3 `SingleChoiceSegmentedButtonRow` (or `SegmentedButton`) with three states: "Planlandı" (Scheduled), "Yapıldı" (Completed), and "Yapılmadı" (Not Held), defaulting to "Yapıldı" for past/current dates and "Planlandı" for future dates, with a single primary "Kaydet" button.
- **FR-011**: System MUST provide horizontal filter chips ("Tümü", "Bekleyenler", "Tamamlananlar") beneath the Homeworks screen header that filter homework entries by completion status.
- **FR-012**: System MUST present the Add Homework interface as a Material 3 `ModalBottomSheet` anchored to the bottom rather than a centered modal dialog.
- **FR-013**: System MUST display compact monetary amounts directly above chart bars on the Reports screen (e.g. "₺12.5K") and reveal a detailed tooltip containing the full unrounded amount and period upon tapping a bar.

### Key Entities

- **UI Theme & Input Style**: Standardized Material 3 styling tokens applied to all text entry fields, ensuring uniform shape, spacing, outline borders, and state indicators.
- **Homework Filter State**: An enumeration representing the active homework filter (`ALL`, `PENDING`, `COMPLETED`).
- **Lesson Attendance Status**: An enumeration mapped to the SegmentedButton (`SCHEDULED`, `COMPLETED`, `CANCELLED` / Not Held).
- **Calendar Indicator Semantics**: Mapping of calendar dot colors to distinct lesson and homework states.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of text fields across all application screens and bottom sheets utilize Material 3 `OutlinedTextField` styling, eliminating visual discrepancies between modal and full-screen forms.
- **SC-002**: 0 hardcoded strings remain in modified UI components; all user-visible strings are localized in English, Turkish, and German with proper plurals.
- **SC-003**: 0 redundant settings gear icons exist on secondary or nested screens.
- **SC-004**: Student list cards provide a single unified tap target for navigation, reducing list visual clutter while preserving 48×48 dp touch target ergonomics.
- **SC-005**: All modified interactive buttons and indicators (including the "Mark as Paid" button and calendar dots) satisfy WCAG AA contrast standards (minimum 4.5:1 for standard text/icons).
- **SC-006**: Tutors can filter homework records across three states (All, Pending, Completed) in a single tap via filter chips.
- **SC-007**: Homework creation form is accessible via a bottom sheet with complete IME-insets support, eliminating thumb reach strain on mobile devices.
- **SC-008**: Monthly income chart values on the Reports screen can be scanned at a glance with compact values and inspected in full detail via tap tooltips without text collision.

## Assumptions

- Navigation graph and underlying domain repositories/models remain intact; changes are focused on UI/UX components, Compose layouts, string resources, and screen ergonomics.
- Settings navigation remains accessible from the Home screen's top bar or primary app bar.
- Existing business logic for marking lessons paid, logging lessons, and filtering homework is reused and simply hooked up to the updated UI components.
