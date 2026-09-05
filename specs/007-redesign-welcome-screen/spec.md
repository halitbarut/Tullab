# Feature Specification: Welcome Screen Modern Redesign

**Feature Branch**: `007-redesign-welcome-screen`

**Created**: 2026-09-05

**Status**: Draft

**Input**: User description: "app's welcome screen looks really bad. We need to recreate it with a modern consistent design with smooth animations. greeting the new user is really important. I face 2 problems with the welcome screen. 1st screen items are misaligned. 2nd in Turkish translate t says \"Tullab'ya Hoş Geldiniz\" which contains a mistake. instead fixing this problems, i want you to create a new modern design from zero. I say modern but it can not be irrelevant with the other parts of the app. But it can inspire me to update the other parts of the app."

## Clarifications

### Session 2026-09-05
- Q: Should the redesigned welcome screen replace the multi-page carousel with a single cohesive hero screen leading to legal consent, or remain the first slide within an updated multi-page tour? → A: Single hero screen: The welcome screen will be a unified, animated single screen containing the centered brand emblem, greeting headline, and core value cards, replacing the multi-page carousel entirely and directly connecting to legal consent.
- Q: How should legal consent (Terms of Service & Privacy Policy agreement) be presented to the user? → A: Integrated on screen: An elegant consent row with clickable policy links is embedded directly above the primary "Get Started" button on the welcome screen, keeping the entire first-launch onboarding experience on a single screen.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Warm, Inspiring First-Launch Greeting & Brand Identity (Priority: P1)

As a private tutor opening Tullab for the very first time,
I want to be greeted by a polished, modern, and beautifully balanced welcome screen featuring the Tullab brand emblem, an inviting headline, and an inspirational subtitle,
So that I immediately feel welcomed and confident that Tullab is a professional, trustworthy tool for managing my tutoring business.

**Why this priority**: Highest priority (P1). The welcome screen is the user's initial impression of the product. An unaligned or uninspiring welcome screen damages trust and retention.

**Independent Test**: Launch the application on a fresh install or with onboarding incomplete; verify the screen presents the Tullab emblem, an elegant greeting headline, and a clear subtitle with balanced, centered alignment.

**Acceptance Scenarios**:

1. **Given** a user opens Tullab for the first time, **When** the welcome screen loads, **Then** the Tullab brand emblem is centered prominently at the top with cohesive brand styling.
2. **Given** the welcome screen is presented, **When** the user reads the header, **Then** a prominent, friendly greeting headline introduces the app name with clear typographic hierarchy.
3. **Given** the welcome screen is presented, **When** inspecting the layout, **Then** all header elements, illustrations, and descriptive text are symmetrically centered without awkward offsets or disproportionate empty spaces.

---

### User Story 2 - Clear Value Propositions & Feature Highlights (Priority: P1)

As a new user exploring Tullab,
I want to see the core value pillars of the application (Student Management, Lesson & Homework Scheduling, and Fee & Balance Tracking) presented in clean, modern highlight cards on the welcome screen,
So that I understand what Tullab does and how it will assist my day-to-day tutoring workflows without having to swipe through a multi-page carousel.

**Why this priority**: High priority (P1). Greeting the user effectively requires demonstrating immediate value and clarity of purpose so they feel eager to get started.

**Independent Test**: View the welcome screen and confirm that key feature highlights (students, lessons/homework, fee tracking) are displayed in harmonious, modern cards with intuitive icons and concise descriptions in a single cohesive view.

**Acceptance Scenarios**:

1. **Given** the welcome screen is open, **When** the user views the content body, **Then** they see visual highlight cards or badges representing Tullab's core pillars: Student Management, Lesson & Homework Scheduling, and Payment Tracking.
2. **Given** the highlight cards are rendered, **When** viewed on screen, **Then** each card uses consistent corner radiuses, subtle elevation, and theme-compliant surface styling that matches the app's design system.
3. **Given** the highlight cards are displayed, **When** checking content, **Then** all descriptions are strictly accurate to the app's current offline-first capabilities (with no obsolete AI references).

---

### User Story 3 - Flawless Multilingual Greeting & Turkish Grammar (Priority: P1)

As a Turkish-, English-, or German-speaking tutor,
I want the welcome headline and all copy to use accurate, natural grammar without errors (specifically "Tullab'a Hoş Geldiniz" in Turkish, "Welcome to Tullab" in English, and "Willkommen bei Tullab" in German),
So that the application speaks to me natively and professionally.

**Why this priority**: High priority (P1). The user specifically noted the grammatical error "Tullab'ya Hoş Geldiniz" as a key defect. Proper grammatical declension is critical for user respect and credibility.

**Independent Test**: Change device/app language to Turkish, English, and German sequentially; verify the welcome headline and all accompanying copy display correct grammar, spelling, and tone in each language.

**Acceptance Scenarios**:

1. **Given** the app is configured in Turkish, **When** the welcome screen is displayed, **Then** the headline displays "Tullab’a Hoş Geldiniz" with correct consonant/vowel harmony (using "-a" rather than "-ya").
2. **Given** the app is configured in English, **When** the welcome screen is displayed, **Then** the headline displays "Welcome to Tullab".
3. **Given** the app is configured in German, **When** the welcome screen is displayed, **Then** the headline displays "Willkommen bei Tullab".
4. **Given** any supported language is active, **When** viewing value highlights and buttons, **Then** all text is fully localized with no hardcoded strings or language mix-ups.

---

### User Story 4 - Smooth Coordinated Motion, Integrated Consent & Seamless Entry (Priority: P2)

As a user arriving at the welcome screen,
I want elements to transition into view with smooth coordinated motion, and have an integrated, accessible consent checkbox with policy links right above the "Get Started" button,
So that I can review policies, confirm agreement, and immediately enter the application in a single unified, frictionless step.

**Why this priority**: Medium priority (P2). Fluid motion and frictionless single-screen onboarding enhance user satisfaction and speed to value while ensuring full legal compliance.

**Independent Test**: Open the welcome screen, observe entrance animations, tap policy links (Privacy Policy, Terms of Service), toggle the consent checkbox, and verify the "Get Started" button enables and navigates directly to the dashboard.

**Acceptance Scenarios**:

1. **Given** the welcome screen opens, **When** content appears, **Then** the brand hero, greeting text, feature cards, and bottom action area animate into place with coordinated, staggered entrance transitions.
2. **Given** the bottom action area is rendered, **When** the user inspects it, **Then** an integrated consent row displays a checkbox and formatted text with clickable links to the Privacy Policy and Terms of Service.
3. **Given** the consent checkbox is unchecked, **When** the user views the "Get Started" button, **Then** the button is visibly disabled and non-interactive.
4. **Given** the user checks the consent checkbox, **When** state changes, **Then** the "Get Started" button smoothly animates to an enabled state and, upon tap, marks onboarding complete and navigates to the main application dashboard.

---

### User Story 5 - Responsive Layout & Accessibility Compliance (Priority: P2)

As a tutor using a compact device or utilizing system accessibility features (such as large dynamic text),
I want the welcome screen layout to adapt gracefully without clipping, text truncation, or obscured action buttons,
So that I can comfortably read and interact with the screen regardless of my device or display settings.

**Why this priority**: Medium priority (P2). Ensures accessibility and usability across diverse physical devices and user needs.

**Independent Test**: Test on compact screen resolutions and with system font scaling increased to 200%; verify all text remains readable, the content scrolls vertically if needed, and action buttons remain fully reachable and interactable.

**Acceptance Scenarios**:

1. **Given** system font scaling is set up to 200%, **When** the welcome screen is displayed, **Then** typography scales appropriately without clipping or overlapping adjacent components.
2. **Given** a compact or short screen device, **When** the screen is rendered, **Then** content fits comfortably or scrolls smoothly within a vertical scroll container so the primary call-to-action is never unreachable.
3. **Given** a screen reader is active, **When** navigating the screen, **Then** informative headers, feature highlights, policy links, and action buttons are clearly announced, while purely decorative visuals have null content descriptions.
4. **Given** light or dark theme is active, **When** viewing the screen, **Then** contrast ratios between text/icons and backgrounds satisfy WCAG AA requirements (minimum 4.5:1 for body, 3:1 for large text).

---

### Edge Cases

- **Small Viewport Heights**: On devices with limited vertical height (or in split-screen / landscape mode), the entire content is wrapped in a vertical scroll container so all value cards, the consent row, and the "Get Started" button remain reachable.
- **Dynamic Font Scaling (up to 200%)**: Spacing, card heights, and legal text adapt flexibly without clipping or overlapping adjacent cards.
- **Offline External Links**: Tapping Privacy Policy or Terms links utilizes the platform URI handler to open external policy documents; if offline, the browser or viewer handles network availability gracefully without crashing the app.
- **Theme Switching**: All background gradients, card containers, and typography colors reference design tokens dynamically, ensuring flawless contrast in both Light and Dark themes.
- **Rapid Navigation / Recomposition**: Entrance animations handle rapid re-entry or orientation changes gracefully without visual stuttering or stuck zero-alpha states.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The welcome screen MUST feature a prominent, centered brand presentation displaying the official Tullab emblem with modern visual styling.
- **FR-002**: The welcome screen MUST display a localized, welcoming headline greeting the user to Tullab across all supported languages (Turkish: "Tullab’a Hoş Geldiniz", English: "Welcome to Tullab", German: "Willkommen bei Tullab").
- **FR-003**: The welcome screen MUST display a clear, localized subtitle introducing Tullab as the tutor's private, offline-first teaching assistant.
- **FR-004**: The welcome screen MUST present core value proposition highlights (Student Management, Lesson & Homework Scheduling, and Payment Tracking) structured in balanced, modern cards on a single unified screen, replacing the legacy multi-page carousel entirely.
- **FR-005**: All components on the welcome screen MUST adhere to a consistent vertical and horizontal alignment grid, eliminating off-center elements and awkward whitespace.
- **FR-006**: The welcome screen MUST implement coordinated entrance animations (such as staggered alpha and spring translation) for the hero emblem, greeting header, value cards, and action area.
- **FR-007**: The welcome screen MUST integrate a legal consent row directly above the primary call-to-action button, including a checkbox and clickable links for the Privacy Policy and Terms of Service.
- **FR-008**: The primary call-to-action button ("Get Started") MUST be enabled only when the user checks the consent checkbox; upon activation, it marks onboarding complete and advances to the main dashboard.
- **FR-009**: The primary action button and consent checkbox MUST satisfy the minimum 48×48 dp interactive touch target requirement.
- **FR-010**: The welcome screen MUST adapt responsively via a vertical scroll container to ensure all content and buttons remain accessible across small viewports and dynamic font scaling up to 200%.
- **FR-011**: All visual elements, cards, and typography MUST utilize tokens from the app's design system (`ui/theme/`), avoiding hardcoded color literals or ad-hoc margins.
- **FR-012**: 100% of user-visible text on the welcome screen MUST be externalized in `strings.xml` resource files with complete parity across English (`values`), Turkish (`values-tr`), and German (`values-de`).
- **FR-013**: The Turkish translation for the welcome title MUST strictly use the grammatically correct apostrophe-separated dative suffix "Tullab’a Hoş Geldiniz" (prohibiting "Tullab’ya").

### Key Entities *(include if feature involves data)*

- **Welcome Presentation Model**: Represents the visual and text content displayed on the welcome screen, including the localized headline, subtitle, feature highlights (icon, title, summary), legal consent copy, and primary action label.
- **Onboarding Completion State**: Tracks whether the user has accepted terms and completed initial onboarding, persisted via `SetOnboardingCompletedUseCase`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of screen elements are visually centered and aligned on a consistent grid with zero off-center or misaligned items across standard device display sizes.
- **SC-002**: 0 grammatical or spelling errors across English, Turkish, and German welcome screen copy, with verified compliance for "Tullab’a Hoş Geldiniz" in Turkish.
- **SC-003**: All entrance animations complete smoothly within 600ms of screen display without dropped frames or visual stuttering.
- **SC-004**: 100% of screen content remains legible, unclipped, and fully navigable when system font scale is set to maximum (200%).
- **SC-005**: The primary call-to-action button and consent checkbox satisfy the 48×48 dp minimum touch target requirement, and all text satisfies WCAG AA contrast standards (minimum 4.5:1 for body text, 3:1 for headers) across both light and dark themes.
- **SC-006**: A new user can comprehend the app's value, accept terms, and enter the dashboard on a single screen in 1 check and 1 tap.

## Assumptions

- **Single-Screen Onboarding**: The redesigned welcome screen replaces the outdated multi-page carousel with a single, high-impact hero welcome screen that includes inline legal consent and a direct "Get Started" action into the dashboard.
- **Offline & Privacy Integrity**: The welcome screen and feature highlights adhere strictly to Tullab's offline-first architecture (Room persistence, zero network requests, no telemetry).
- **Design System Anchor**: The new visual styling, card layouts, and motion curves established on the welcome screen align with existing tokens in `ui/theme/` and serve as an aesthetic reference for future screen enhancements throughout Tullab.
- **State Persistence**: Completion of the onboarding journey continues to be persisted locally via the existing `SetOnboardingCompletedUseCase`.
