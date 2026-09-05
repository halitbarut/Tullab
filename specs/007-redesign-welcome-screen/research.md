# Phase 0 Research: Welcome Screen Modern Redesign

**Feature**: `007-redesign-welcome-screen`  
**Date**: 2026-09-05  

## Research Topics & Architectural Decisions

### 1. Single-Screen Hero Layout vs. Multi-Page Carousel

- **Context**: The existing onboarding experience used a 5-page `HorizontalPager` containing an unaligned illustration, empty description spacing, and an obsolete AI feature slide. The user requested a modern design from zero that fixes alignment and grammar.
- **Decision**: Replace `HorizontalPager` entirely with a single, vertically scrollable, unified hero screen.
- **Rationale**:
  - Eliminates multi-page swiping friction and page indicator misalignment.
  - Presents Tullab's brand emblem, greeting, and 3 core value pillars in one cohesive view.
  - Ensures 100% reachability of all content on compact devices and when dynamic accessibility font scale is set up to 200% via `Modifier.verticalScroll(rememberScrollState())`.
  - Places the required legal consent (Terms & Privacy) immediately above the primary action button, allowing tutors to start using the app in 1 check and 1 tap.
- **Alternatives Considered**:
  - *Multi-page Carousel*: Rejected during clarification because it creates unnecessary swipe steps, leaves empty space on slides, and retained obsolete AI content.
  - *Two-Screen Gateway*: Rejected during clarification because separating welcome from consent introduces unnecessary navigation overhead.

---

### 2. Brand Hero & Emblem Presentation

- **Context**: The current welcome slide uses ad-hoc shapes with arbitrary offsets (`offset(x = 8.dp, y = 12.dp)`) and a generic `School` icon. Tullab already possesses an official vector brand emblem in `app/src/main/res/drawable/tullab.xml` featuring dark navy (`#152640`) and gold (`#a38856`) paths.
- **Decision**: Center the official Tullab emblem vector graphic inside a modern, soft-tinted hero container.
- **Visual Design**:
  - Container: Circular or softly rounded container (96–112 dp) with theme-aware tinted background (`MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)` in dark mode, `MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)` in light mode).
  - Emblem: Rendered cleanly with proper padding and content description (`contentDescription = null` as decorative hero).
  - Visual Hierarchy: Emblem sits above the main title and subtitle, forming a symmetrical anchor at the top of the screen.
- **Alternatives Considered**:
  - *Generic Material Icons*: Rejected because Tullab has a bespoke brand identity that should establish a premium first impression.
  - *Lottie / Heavy Video Animation*: Rejected to avoid third-party dependencies, extra bundle size, and potential rendering overhead; native Compose animations on vector assets provide fluid 60fps performance offline.

---

### 3. Motion Choreography & Animation Specifications

- **Context**: The spec mandates coordinated entrance animations completing within 600ms (SC-003) and compliance with `TullabAnimationSpecs`.
- **Decision**: Implement a staggered multi-element entrance sequence using Compose `Animatable` or `LaunchedEffect` with spring and cubic-bezier easing from `TullabAnimationSpecs`.
- **Choreography Sequence**:
  1. **T0 (0–350ms)**: Brand emblem scales in (`0.85f -> 1.0f`) and fades in (`0f -> 1f`) using `TullabAnimationSpecs.Springs.gentle`.
  2. **T1 (90–400ms)**: Welcome headline and subtitle slide up (`20dp -> 0dp`) with smooth fade-in (`FastOutSlowInEasing`).
  3. **T2 (180–480ms)**: Value highlight cards enter with staggered vertical translation and alpha.
  4. **T3 (260–550ms)**: Legal consent row and primary "Get Started" button slide in and settle into place.
- **Tactile Feedback**:
  - Primary button incorporates interactive press scaling (`TullabAnimationSpecs.pressSpec` with scale `0.96f` on press, `1.0f` on release).
- **Alternatives Considered**:
  - *Instant Static Rendering*: Rejected because the user specifically requested smooth animations that give the app a modern feel and inspire future redesigns.
  - *Infinite Looping Animations*: Rejected to avoid visual distraction and battery drain; entrance animations fire once and settle.

---

### 4. Value Proposition Cards Layout

- **Context**: Tullab's 3 core pillars are Student Management, Lesson & Homework Scheduling, and Fee & Balance Tracking (offline-first, no AI).
- **Decision**: Present 3 compact, horizontally-aligned feature highlight cards or rows using `Surface` containers with `16.dp` rounded corners, subtle outline, and semantic icons.
- **Content & Icons**:
  - **Pillar 1 (Students)**: `Icons.Outlined.Groups` — Manage student details, contact info, and lesson histories.
  - **Pillar 2 (Lessons & Homework)**: `Icons.Outlined.CalendarMonth` — Schedule private lessons and track homework assignments.
  - **Pillar 3 (Fees & Balances)**: `Icons.Outlined.Payments` — Automatic hourly/flat rate calculations and payment tracking.
- **Styling**:
  - Each item features an icon placed inside a circular container tinted with `MaterialTheme.colorScheme.primaryContainer`, followed by bold title (`titleMedium`) and concise description (`bodyMedium`, `onSurfaceVariant`).
  - Strict horizontal padding (`16.dp`–`20.dp`) and vertical spacing (`10.dp`–`12.dp`) ensuring zero misalignment.

---

### 5. Multilingual Localization & Grammar Correction

- **Context**: Turkish translation currently has a grammatical defect on line 245 of `values-tr/strings.xml`: `Tullab’ya Hoş Geldiniz`. In Turkish, "Tullab" ends in consonant 'b' with vowel 'a', so the correct dative suffix is `-a` separated by an apostrophe: `Tullab’a Hoş Geldiniz`.
- **Decision**:
  - Update `onboarding_welcome_title` in `values-tr/strings.xml` to `Tullab’a Hoş Geldiniz`.
  - Add localized subtitles and feature pillar strings across all 3 language directories (`values`, `values-tr`, `values-de`).
  - Purge legacy AI strings (`onboarding_feature3_title`, `onboarding_feature3_body`) and align with offline tutoring pillars.
- **Translations Matrix**:
  - **Turkish (`values-tr`)**:
    - `onboarding_welcome_title`: `Tullab’a Hoş Geldiniz`
    - `onboarding_welcome_subtitle`: `Özel derslerinizi, öğrencilerinizi ve ödemelerinizi kolayca yönetin.`
    - `onboarding_pillar_students_title`: `Öğrenci Yönetimi`
    - `onboarding_pillar_students_desc`: `Öğrenci profilleri, ders geçmişi ve iletişim bilgilerini düzenleyin.`
    - `onboarding_pillar_lessons_title`: `Ders ve Ödev Takibi`
    - `onboarding_pillar_lessons_desc`: `Dersleri takvimde planlayın ve ödevleri zahmetsizce takip edin.`
    - `onboarding_pillar_payments_title`: `Otomatik Ücret ve Bakiye`
    - `onboarding_pillar_payments_desc`: `Saatlik veya sabit ücretleri hesaplayın, ödemeleri kaçırmayın.`
  - **English (`values`)**:
    - `onboarding_welcome_title`: `Welcome to Tullab`
    - `onboarding_welcome_subtitle`: `Your private tutoring assistant for students, lessons, and fees.`
    - `onboarding_pillar_students_title`: `Student Management`
    - `onboarding_pillar_students_desc`: `Organize student profiles, contact details, and lesson histories.`
    - `onboarding_pillar_lessons_title`: `Lessons & Homework`
    - `onboarding_pillar_lessons_desc`: `Schedule lessons on your calendar and track assignments effortlessly.`
    - `onboarding_pillar_payments_title`: `Automatic Rate & Balance`
    - `onboarding_pillar_payments_desc`: `Calculate hourly or flat fees and keep balances up to date.`
  - **German (`values-de`)**:
    - `onboarding_welcome_title`: `Willkommen bei Tullab`
    - `onboarding_welcome_subtitle`: `Ihr persönlicher Nachhilfe-Assistent für Schüler, Unterricht und Finanzen.`
    - `onboarding_pillar_students_title`: `Schülerverwaltung`
    - `onboarding_pillar_students_desc`: `Organisieren Sie Schülerprofile, Kontaktdaten und Unterrichtsverläufe.`
    - `onboarding_pillar_lessons_title`: `Unterricht & Hausaufgaben`
    - `onboarding_pillar_lessons_desc`: `Planen Sie Stunden im Kalender und behalten Sie Hausaufgaben im Blick.`
    - `onboarding_pillar_payments_title`: `Automatische Abrechnung`
    - `onboarding_pillar_payments_desc`: `Stundensätze oder Pauschalen erfassen und Zahlungsstände überwachen.`
