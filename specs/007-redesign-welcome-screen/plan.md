# Implementation Plan: Welcome Screen Modern Redesign

**Branch**: `007-redesign-welcome-screen` | **Date**: 2026-09-05 | **Spec**: [specs/007-redesign-welcome-screen/spec.md](file:///home/halit/AndroidStudioProjects/Tullab/specs/007-redesign-welcome-screen/spec.md)

**Input**: Feature specification from `/specs/007-redesign-welcome-screen/spec.md`

## Summary

Recreate the welcome screen from scratch as a modern, unified, single-screen experience. The new screen replaces the legacy 5-page `HorizontalPager` with a centered, vertically scrollable layout featuring the official Tullab vector brand emblem, an inspiring localized greeting ("Tullab’a Hoş Geldiniz" in Turkish, correcting the previous grammar defect), 3 core value pillar cards (Students, Lessons & Homework, Fees & Balances), coordinated entrance animations (~550ms), and an inline legal consent row that activates the primary "Get Started" button for instant, friction-free app entry.

## Technical Context

**Language/Version**: Kotlin 2.0.21 / JDK 17  
**Primary Dependencies**: Jetpack Compose (BOM 2024.09.00), Material 3 (`androidx.compose.material3`), Hilt (`dagger.hilt.android` 2.51.1), Navigation Compose (2.8.0), Compose Animation Core  
**Storage**: AndroidX DataStore (Preferences) for `onboarding_completed` flag via `SetOnboardingCompletedUseCase` (Room database for core domain data; no DB changes needed)  
**Testing**: JUnit 4, Kotlinx Coroutines Test, Compose UI Test (`androidx.compose.ui:ui-test-junit4`)  
**Target Platform**: Android 8.0+ (API 26–34)  
**Project Type**: Android Mobile App (Single APK/AAB)  
**Performance Goals**: Entrance animations complete in <600ms (target ~550ms) at 60 fps without dropped frames; zero network calls  
**Constraints**: 100% offline-first (no network requests, no INTERNET permission), WCAG AA color contrast, 48×48 dp minimum touch targets, dynamic font scaling support up to 200%  
**Scale/Scope**: 1 primary screen rewrite (`OnboardingScreen.kt` decomposed into `WelcomeHeroSection.kt`, `ValuePillList.kt`, `LegalConsentSection.kt`, `GetStartedAction.kt`), 3 localized resource files (`values`, `values-tr`, `values-de`), unit/UI verification tests  

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Evaluation |
| :--- | :---: | :--- |
| **I. Offline-First & Privacy-First** | ✅ PASS | Operates 100% offline; zero telemetry or network calls. Policy links open in the system browser via standard `LocalUriHandler`. |
| **II. Clean Architecture & Modularity** | ✅ PASS | Strict `ui -> domain <- data` separation. `OnboardingScreen` lives in the `ui` layer and interacts only with `OnboardingViewModel` and domain use cases. |
| **III. Dependency Injection via Hilt** | ✅ PASS | `OnboardingViewModel` annotated with `@HiltViewModel` and uses constructor injection. |
| **IV. Jetpack Compose-Only UI & Accessibility** | ✅ PASS | 100% Compose Material 3; tokens drawn exclusively from `ui/theme/` (`ProfessionalBlue`, `TullabBackground`, `TullabAnimationSpecs`). 48×48 dp touch targets, dynamic font scaling (sp), and WCAG AA contrast. |
| **V. MVVM & Unidirectional Data Flow** | ✅ PASS | State hoisted via ViewModel `StateFlow` and explicit event callbacks `(Boolean) -> Unit` and `() -> Unit`. |
| **VI. Layered Error Handling** | ✅ PASS | Safe handling of external intent launches with fallback. |
| **VII. Internationalization (i18n)** | ✅ PASS | 100% user-visible text externalized in `strings.xml`. Full parity across English, Turkish, and German. Turkish dative grammar strictly corrected to "Tullab’a Hoş Geldiniz". |
| **VIII. Secrets & Platform Security** | ✅ PASS | No hardcoded credentials; secure platform practices. |

## Project Structure

### Documentation (this feature)

```text
specs/007-redesign-welcome-screen/
├── spec.md              # Feature specification & clarifications
├── plan.md              # Implementation plan (this file)
├── research.md          # Phase 0 architectural decisions & motion choreography
├── data-model.md        # Phase 1 UI state model & lifecycle transitions
├── contracts/
│   └── welcome-screen-ui-contract.md # Phase 1 Composable signatures & test tags
├── quickstart.md        # Phase 1 manual & automated verification guide
├── checklists/
│   └── requirements.md  # Quality validation checklist
└── tasks.md             # Phase 2 task decomposition (/speckit-tasks output)
```

### Source Code (repository layout)

```text
app/src/main/
├── java/com/barutdev/tullab/
│   ├── ui/
│   │   ├── screens/
│   │   │   └── onboarding/
│   │   │       ├── OnboardingScreen.kt       # Redesigned single-screen welcome experience container & orchestrator
│   │   │       ├── WelcomeHeroSection.kt     # Centered brand emblem container, headline, and subtitle
│   │   │       ├── ValuePillList.kt          # Core value pillar cards (Students, Lessons, Balances)
│   │   │       ├── LegalConsentSection.kt    # Annotated clickable policy links & consent checkbox
│   │   │       ├── GetStartedAction.kt       # Primary CTA button with animation & press state
│   │   │       └── OnboardingViewModel.kt    # Screen state management & completion handler
│   │   └── theme/
│   │       ├── Animation.kt                  # TullabAnimationSpecs utilized for entrance transitions
│   │       ├── Color.kt                      # Design system color tokens
│   │       └── Theme.kt                      # Material 3 colorScheme
├── res/
│   ├── drawable/
│   │   └── tullab.xml                        # Official Tullab vector emblem
│   ├── values/
│   │   └── strings.xml                       # English welcome copy & feature pillars
│   ├── values-tr/
│   │   └── strings.xml                       # Turkish copy with corrected "Tullab’a Hoş Geldiniz"
│   └── values-de/
│       └── strings.xml                       # German welcome copy & feature pillars
└── test/java/com/barutdev/tullab/
    └── ui/screens/onboarding/
        └── OnboardingViewModelTest.kt        # ViewModel unit tests for consent state & completion
```

**Structure Decision**: Standard Android single-module architecture conforming to Clean Architecture boundaries in `:app`. Modular subcomponents are decomposed into dedicated files within the onboarding package to adhere to Compose best practices and maximize unit/preview testability.

## Complexity Tracking

> *No constitution violations detected; zero complexity exceptions requested.*
