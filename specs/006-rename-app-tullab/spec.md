# Feature Specification: Application Rebranding to "Tullab"

**Feature Branch**: `006-rename-app-tullab`  
**Created**: 2026-09-05  
**Status**: Draft  
**Input**: User description: "I decided to change the app's name. The new name will be \"Tullab\"."

## Clarifications

### Session 2026-09-05
- Q: Should the renaming to "Tullab" be limited to user-facing display names and branding, or should it also change the underlying Android application ID and package structure? → A: Full package & ID rename: Since the app is in active pre-release development and unpublished, change everything across the application (application ID `com.barutdev.tullab`, package namespace, local database name, and internal class/theme identifiers).
- Q: Should the app launcher icon graphic be redesigned with a new visual logo for Tullab, or should it retain the existing adaptive vector icon? → A: Apply custom launcher icon using the vector graphic at `app/src/main/res/drawable/tullab.xml` as the icon foreground paired with a solid white background.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - App Identity on Device and Launcher (Priority: P1)

As a private tutor, I want the application to be installed as "Tullab" with its distinctive visual icon and display "Tullab" on my device home screen, application drawer, and recent apps overview, so that I can easily recognize and launch the app under its official brand identity.

**Why this priority**: Highest priority (P1). The application launcher name, visual icon, and system identification are the primary entry points for the user on their device.

**Independent Test**: Install the application built under the new package identity, navigate to the device home screen and app launcher, and verify the displayed app name is "Tullab" and the icon displays the Tullab emblem on a white background.

**Acceptance Scenarios**:

1. **Given** the application is installed on a device, **When** the user views the application icon on the home screen or app drawer, **Then** the title label beneath the icon is displayed as "Tullab".
2. **Given** the application is running, **When** the user opens the system recent apps / task switcher screen, **Then** the header title of the app task is displayed as "Tullab".
3. **Given** the device application manager is inspected, **When** viewing the installed app details, **Then** the application package ID is identified as `com.barutdev.tullab`.
4. **Given** the launcher icon is rendered on any device launcher, **When** viewed on the home screen or app drawer, **Then** the icon displays the Tullab emblem (`drawable/tullab.xml`) centered against a solid white background across all adaptive icon masks (round, squircle, square).

---

### User Story 2 - Consistent Multilingual Brand Experience (Priority: P1)

As a private tutor using the application in English, Turkish, or German, I want all welcoming screens, dialogs, and notifications to greet me using "Tullab" with correct language-specific grammar and spelling, so that the experience feels polished and consistent.

**Why this priority**: Highest priority (P1). Core brand messaging in onboarding and system notifications directly impacts user trust and brand recognition.

**Independent Test**: Launch the app in each supported locale (English, Turkish, German), check the onboarding welcome screen and trigger a reminder notification, verifying that the text correctly references "Tullab".

**Acceptance Scenarios**:

1. **Given** the app language is set to English, **When** the user opens the onboarding screen, **Then** the header displays "Welcome to Tullab".
2. **Given** the app language is set to Turkish, **When** the user opens the onboarding screen, **Then** the header displays "Tullab’a Hoş Geldiniz".
3. **Given** the app language is set to German, **When** the user opens the onboarding screen, **Then** the header displays "Willkommen bei Tullab".
4. **Given** an automated lesson or task reminder triggers, **When** the notification appears in the device notification tray, **Then** the notification message references "Tullab" with correct grammatical phrasing in the active locale (English: "You have a new reminder from Tullab.", Turkish: "Tullab’dan yeni bir hatırlatmanız var.", German: "Du hast eine neue Erinnerung von Tullab.").

---

### User Story 3 - Data Backup Naming and Backward Compatibility (Priority: P2)

As a private tutor exporting or restoring my data, I want newly created backup files to carry the "Tullab" name while retaining the ability to restore any previously created backup files, so that my archive management is clear and no historical backups are lost.

**Why this priority**: Medium priority (P2). Data safety and continuity are essential for tutors managing their student records.

**Independent Test**: Export a new backup file and verify the filename begins with `tullab_backup_`. Then select and import a legacy backup file (`kora_backup_*.csv`) and verify all records restore successfully.

**Acceptance Scenarios**:

1. **Given** a tutor initiates a data backup export from Settings, **When** the file is generated, **Then** the default filename proposed starts with `tullab_backup_` followed by the timestamp (e.g., `tullab_backup_YYYYMMDD_HHmmss.csv`).
2. **Given** a tutor has a previously exported backup file named with the old prefix (e.g., `kora_backup_20260901_120000.csv`), **When** the tutor selects this file to restore, **Then** the application accepts the file and restores all student, lesson, and payment records without error.

---

### Edge Cases

- **Pre-Release Clean Environment**: Because the application is in pre-release development and unpublished, legacy over-the-air package migration constraints do not apply; fresh installations will cleanly utilize the new `com.barutdev.tullab` package identity and `tullab.db` storage.
- **Legacy Backup Import**: When importing data, the file picker and parser do not enforce rigid filename prefix validation that would reject files not matching the new name.
- **Notification Channels**: If the operating system displays notification category/channel names in system settings, the channel descriptions clearly reflect "Tullab".
- **Adaptive Icon Mask Shapes**: The Tullab foreground emblem must scale and pad appropriately within safe adaptive icon zones so it is not clipped when rendered under circular, squircle, or rounded-corner device masks.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST display "Tullab" as the application display name across the device launcher, app drawer, and system task switcher.
- **FR-002**: The onboarding flow MUST welcome users to "Tullab" across all supported languages (English: "Welcome to Tullab", Turkish: "Tullab’a Hoş Geldiniz", German: "Willkommen bei Tullab").
- **FR-003**: System notifications generated by the app MUST identify the application as "Tullab" across all supported languages (English: "You have a new reminder from Tullab.", Turkish: "Tullab’dan yeni bir hatırlatmanız var.", German: "Du hast eine neue Erinnerung von Tullab.").
- **FR-004**: The default filename for newly exported data backups MUST use the prefix `tullab_backup_` followed by the date and time timestamp.
- **FR-005**: The data backup import capability MUST maintain full backward compatibility with backup files generated under the previous application name.
- **FR-006**: All user-visible dialogs, settings labels, and text references previously displaying "Kora" MUST be updated to display "Tullab".
- **FR-007**: The application ID and package namespace MUST be updated from `com.barutdev.kora` to `com.barutdev.tullab`.
- **FR-008**: Internal application component names, theme definitions, and local database filenames MUST adopt "Tullab" naming conventions (e.g., `TullabApp`, `Theme.Tullab`, `tullab.db`).
- **FR-009**: The adaptive launcher icon MUST use the custom Tullab vector graphic (`drawable/tullab.xml`) as its foreground paired with a solid white background (`#FFFFFF`).

### Key Entities *(include if feature involves data)*

- **Application Branding Profile**: Represents the user-facing identity presented across the operating system and in-app screens. Attributes include display title, adaptive launcher icon, localized welcome headers, and localized notification body text.
- **Backup Export Archive**: Represents exported tutor data. Attributes include default filename prefix (`tullab_backup_`), timestamp, format version, and serialized records.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of user-visible branding references and titles display "Tullab" across all three supported locales (English, Turkish, German).
- **SC-002**: The build system and application successfully compile and execute under application ID and package namespace `com.barutdev.tullab`.
- **SC-003**: 100% of historical backup files created prior to the rebrand can be successfully selected and imported.
- **SC-004**: System notifications trigger and display "Tullab" as the sender or within the message text without visual distortion or truncation across small and large screen devices.
- **SC-005**: The adaptive launcher icon displays the Tullab emblem on a white background without clipping or distortion across standard square, squircle, round, and teardrop icon masks.

## Assumptions

- The application is in active pre-release development and has not been published to app stores; renaming the application ID, package namespace (`com.barutdev.tullab`), and database filename carries zero production migration overhead.
- The rebrand encompasses all user-facing branding, visual launcher iconography, visible copy in the mobile app across English, Turkish, and German locales, as well as code-level package naming.
- The provided vector asset `drawable/tullab.xml` is the authoritative source for the launcher icon foreground.
- No existing domain models or functionality regarding students, lessons, payments, homework, or calendar scheduling are changed or removed.
