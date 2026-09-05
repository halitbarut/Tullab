# Phase 1 UI Contract: Welcome Screen Modern Redesign

**Feature**: `007-redesign-welcome-screen`  
**Date**: 2026-09-05  

## Composable Interface Contracts

### 1. `OnboardingScreen` (Top-Level Screen)

The entry point destination for first-time app launch in `TullabNavGraph`.

```kotlin
package com.barutdev.tullab.ui.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
    onCompleted: () -> Unit
)
```

#### Contract Expectations:
- **Precondition**: User has not completed onboarding (`onboarding_completed == false`).
- **Input Events**:
  - Toggling consent checkbox triggers `viewModel.onConsentCheckedChange(checked)`.
  - Tapping "Get Started" triggers `viewModel.completeOnboarding(onCompleted)`.
  - Tapping Privacy Policy or Terms links invokes platform `LocalUriHandler.current.openUri(url)`.
- **Postcondition**: Onboarding completion is saved to DataStore, and `onCompleted` callback is invoked to navigate to `TullabDestination.Dashboard.route`.

---

### 2. Modular Subcomponent Contracts (State-Hoisted)

```kotlin
@Composable
internal fun WelcomeHeroSection(
    title: String,
    subtitle: String,
    emblemProgress: Float,
    textProgress: Float,
    modifier: Modifier = Modifier
)

@Composable
internal fun ValuePillList(
    items: List<FeatureHighlightItem>,
    animProgress: Float,
    modifier: Modifier = Modifier
)

@Composable
internal fun ValuePillCard(
    item: FeatureHighlightItem,
    modifier: Modifier = Modifier
)

@Composable
internal fun LegalConsentSection(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
internal fun GetStartedAction(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

---

## Test Tag Semantics Contract

To facilitate automated component tests and UI verification without fragile text matching, the following stable test tags are exposed:

| Component | Test Tag | Role |
| :--- | :--- | :--- |
| Hero Brand Emblem | `welcome_hero_emblem` | Decorative / Image |
| Welcome Headline | `welcome_headline` | Header Text |
| Welcome Subtitle | `welcome_subtitle` | Body Text |
| Feature Value Cards Container | `welcome_value_cards` | Layout / List |
| Legal Consent Checkbox | `welcome_consent_checkbox` | Checkbox (interactive) |
| Privacy Policy Link | `welcome_privacy_policy_link` | Text / Clickable |
| Terms of Service Link | `welcome_terms_link` | Text / Clickable |
| Primary "Get Started" Button | `welcome_get_started_button` | Button (interactive) |

---

## Navigation & Deep-Link Contract

- **Route**: `TullabDestination.Onboarding.route` (`"onboarding"`)
- **Back Stack Behavior**:
  ```kotlin
  onCompleted = {
      navController.navigate(TullabDestination.Dashboard.route) {
          popUpTo(TullabDestination.Onboarding.route) {
              inclusive = true
          }
      }
  }
  ```
  Once completed, popping back from Dashboard MUST NOT return the user to the onboarding welcome screen.
