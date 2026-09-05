# Feature Specification: Complete Removal of AI Features & Pure Local-Only Operation

**Feature Branch**: `002-remove-ai-features`  
**Created**: 2026-09-03  
**Status**: Draft  
**Input**: User description: "remove all the ai things from the app. This app should be fully locally and no ai insights or anything else about ai."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Distraction-Free, Fully Local Tutor Management (Priority: P1)

As a private tutor, I want to manage my students, lessons, and homework on a clean, uncluttered interface without any AI-generated suggestions, automated advice banners, or AI generation buttons, so that I can focus entirely on my own professional tutoring notes, assessments, and scheduling.

**Why this priority**: Highest value. This directly satisfies the user's primary goal to eliminate all AI presence from the application and restore an intuitive, distraction-free interface dedicated strictly to direct tutor-student workflow management.

**Independent Test**: Can be tested by opening every screen (Dashboard, Student Profiles, Homework, and Calendar) to verify that no AI cards, banners, generation buttons, or loading states appear, and all existing tutoring features remain completely usable and visible.

**Acceptance Scenarios**:

1. **Given** the user opens the Dashboard, **When** the screen displays, **Then** the screen presents student statistics, upcoming lessons, and recent activities without any AI insight cards, AI banners, or generation controls.
2. **Given** the user navigates to the Homework overview or a specific Student profile, **When** reviewing student performance and assignments, **Then** only tutor-created notes, assignment details, and lesson logs are displayed, with no AI-generated recommendations.
3. **Given** the user views any screen across the application, **When** navigating between tabs and details, **Then** all layouts smoothly reflow to utilize available screen space naturally without empty cards, blank reserved containers, or visual gaps left by removed AI components.

---

### User Story 2 - Complete Data Sovereignty & Offline Independence (Priority: P2)

As a privacy-conscious private tutor, I want absolute certainty that the application operates entirely offline and never transmits student data, lesson notes, or records to external servers, cloud services, or third-party AI APIs.

**Why this priority**: High value. Tutors handle sensitive student records and contact information. Guaranteeing that the app is 100% local and requires zero network access protects student privacy and ensures unfailing reliability in any environment.

**Independent Test**: Can be tested by placing the device in airplane mode or disconnecting from all networks, executing all core actions (adding students, scheduling lessons, logging homework, recording payments), and verifying that zero network requests are attempted and no network-related errors or warnings occur.

**Acceptance Scenarios**:

1. **Given** the device is completely disconnected from the network or in airplane mode, **When** the user performs any core action (managing students, scheduling lessons, editing assignments, tracking payments), **Then** all workflows succeed instantly with zero network errors, warnings, or missing credential alerts.
2. **Given** the application is active and in use, **When** device network traffic is observed, **Then** zero outgoing or incoming network requests are initiated by the application.
3. **Given** the application is installed or inspected, **When** reviewing required system permissions, **Then** the application does not require or request network/internet connectivity permissions to operate.

---

### User Story 3 - Seamless Upgrade & Historical Data Preservation (Priority: P3)

As an existing user upgrading from an earlier version of the application that contained AI features, I want all my existing student records, lesson logs, homework history, and payment transactions to remain completely safe and intact, while all obsolete AI data structures and caches are cleanly purged in the background.

**Why this priority**: Essential for existing users. Removing features must never cause data corruption, crashes, or loss of core student and business records.

**Independent Test**: Can be tested by upgrading an installation populated with sample student records, lessons, homework, and cached AI insights, then verifying that all non-AI data remains intact and the app starts without errors.

**Acceptance Scenarios**:

1. **Given** a user upgrades from an older version containing historical student data and cached AI insights, **When** the updated application launches for the first time, **Then** all student profiles, lesson histories, homework assignments, and financial records remain 100% preserved and accessible.
2. **Given** the upgrade migration runs on first launch, **When** local storage structures are updated, **Then** all obsolete AI insight tables, cached suggestions, and generation state flags are permanently purged without causing migration failure or app instability.

---

### Edge Cases

- **Upgrading Existing Installations with Stored AI Data**: When upgrading from a version that previously saved AI insight summaries, the local storage migration safely drops obsolete AI records and tables without altering or corrupting students, lessons, homework, or payment tables.
- **UI Reflow on Various Screen Sizes**: Removing the prominent AI insights card must not leave awkward vertical gaps or empty spaces on either compact phone screens or larger displays. Surrounding cards (such as upcoming lessons and quick metrics) must cleanly reflow to occupy the space.
- **Strictly Offline or Air-Gapped Operation**: When running on devices with zero internet access, firewall restrictions, or active airplane mode, the application behaves identically to online mode, with no timeout pauses, no degraded feature states, and no error dialogs.
- **Multi-Language Consistency**: When switching application language between English, Turkish, and German, all screens must remain completely free of residual or orphaned AI terminology, labels, or navigation targets.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST NOT display any AI-generated insights, AI recommendations, AI summary cards, or AI status banners on any screen (including Dashboard, Homework, and Student Details).
- **FR-002**: System MUST NOT provide any user-facing controls, buttons, triggers, or actions for generating, refreshing, or rating AI content.
- **FR-003**: System MUST operate 100% locally on the device without initiating any external network communication, remote cloud service calls, or telemetry transmissions.
- **FR-004**: System MUST NOT require, request, or use network or internet communication permissions.
- **FR-005**: System MUST NOT require, store, prompt for, or manage external API keys or cloud service credentials.
- **FR-006**: System MUST purge and decommission all local storage tables, entities, and caches dedicated exclusively to AI insights during data migration.
- **FR-007**: System MUST preserve all existing student records, lesson logs, homework assignments, attendance data, and financial transactions without loss or corruption during the removal of AI structures.
- **FR-008**: System MUST remove all AI-related strings, terminology, and localization keys across all supported languages (English, Turkish, German).
- **FR-009**: System MUST eliminate all background computation routines, polling loops, or asynchronous tasks associated with AI insight generation or request tracking.

### Assumptions

- **A-001**: The user desires a complete and permanent removal of all AI capabilities, rather than a toggleable switch or local rule-based pseudo-AI replacement.
- **A-002**: Because external network access in Kora was exclusively utilized for AI services, removing AI allows Kora to become a strictly local-only application with zero network permissions.
- **A-003**: Decommissioning and purging cached AI insights will not adversely impact any tutor-created student profiles, lesson logs, homework tasks, or payment records.
- **A-004**: Screen layouts previously accommodating AI insight cards will gracefully reflow and rebalance to prioritize core tutoring metrics and schedules.

### Dependencies

- **D-001**: Local database migration to drop the obsolete AI insight table cleanly while safeguarding existing schemas.
- **D-002**: Clean excision of AI dependencies and configuration entries from the application build setup.

### Key Entities *(include if feature involves data)*

- **AI Insight (Decommissioned Entity)**: Represents cached or generated AI recommendations, prompts, and timestamps. Completely decommissioned and deleted from local storage schemas and domain models.
- **Student (Preserved Entity)**: Represents a student record with personal details, contact information, and hourly rate. Maintained 100% locally and completely unaffected by AI removal.
- **Lesson (Preserved Entity)**: Represents a scheduled, completed, or paid tutoring session. Maintained 100% locally and completely unaffected by AI removal.
- **Homework (Preserved Entity)**: Represents an assignment given to a student, including due date and status. Maintained 100% locally and completely unaffected by AI removal.
- **Payment (Preserved Entity)**: Represents financial transaction records for tutoring sessions. Maintained 100% locally and completely unaffected by AI removal.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of core tutor workflows (student management, lesson scheduling, homework assignment, payment tracking) function normally with zero AI prompts, widgets, or controls.
- **SC-002**: 0 outgoing or incoming network requests are initiated by the application during runtime across all user journeys.
- **SC-003**: 100% of existing non-AI user data (students, lessons, homework, payments) is preserved intact upon upgrading from previous versions.
- **SC-004**: 0 occurrences of AI terminology or references appear in any user-facing screen across all supported locales (English, Turkish, German).
- **SC-005**: App memory footprint and local storage overhead are reduced by eliminating AI caching structures and external client libraries, maintaining sub-1-second screen transition responsiveness.
