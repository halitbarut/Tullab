# Quickstart Validation Guide: Welcome Screen Modern Redesign

**Feature**: `007-redesign-welcome-screen`  
**Date**: 2026-09-05  

## Purpose

This guide outlines end-to-end scenarios for validating the redesigned welcome screen across visual alignment, multilingual correctness, smooth animations, accessibility scaling, and onboarding progression.

---

## Prerequisites

1. Android SDK installed with JDK 17+.
2. Physical Android device or emulator running API 26+ (recommended API 34).
3. Reset app data or install fresh build so that onboarding is incomplete (`onboarding_completed == false`).
   ```bash
   adb shell pm clear com.barutdev.tullab
   ```

---

## Validation Scenarios

### Scenario 1: Initial Presentation, Brand Hero & Visual Alignment

1. Launch Tullab on a device or emulator with cleared app data.
2. **Observe**:
   - The screen renders as a single, vertically centered hero layout without a swipe pager or misaligned dots.
   - The official Tullab emblem vector graphic (`drawable/tullab.xml`) appears centered at the top within a subtle tinted container.
   - Smooth entrance animations trigger: the emblem scales and fades in, followed by the headline, subtitle, 3 value cards, and the bottom action row within ~550ms.
   - 3 value cards appear in uniform rounded containers:
     1. Student Management
     2. Lessons & Homework
     3. Fee & Balance Tracking
   - No obsolete AI references exist anywhere on screen.
   - All elements are horizontally centered with symmetrical 24dp screen padding.

---

### Scenario 2: Multilingual Correctness & Turkish Grammar Check

1. Set the device language to **Turkish (Türkçe)**.
2. Launch Tullab and inspect the welcome screen.
3. **Verify**:
   - Headline displays: **`Tullab’a Hoş Geldiniz`** (prohibiting `Tullab’ya`).
   - Subtitle displays: **`Özel derslerinizi, öğrencilerinizi ve ödemelerinizi kolayca yönetin.`**
   - Value cards and consent text display natural, grammatically sound Turkish strings.
4. Set device language to **English (US)**.
   - Headline displays: **`Welcome to Tullab`**.
5. Set device language to **German (Deutsch)**.
   - Headline displays: **`Willkommen bei Tullab`**.

---

### Scenario 3: Consent Checkbox & Dashboard Entry Flow

1. Open the welcome screen.
2. Note that the "Get Started" button is visually disabled and non-interactive.
3. Tap the "Privacy Policy" link in the legal row: verify the platform opens the external policy URL.
4. Tap the "Terms of Service" link: verify the platform opens the terms URL.
5. Tap the consent checkbox:
   - Checkbox transitions to checked state.
   - "Get Started" button smoothly animates to enabled brand blue (`ProfessionalBlue`).
6. Tap "Get Started":
   - Onboarding completion is saved locally in DataStore.
   - The application immediately navigates to the main Dashboard screen.
7. Press the system Back gesture/button:
   - Verify the app exits or remains on the dashboard; it must NOT navigate back into the welcome screen.
8. Re-open the application from the launcher:
   - Verify the app launches directly to the main Dashboard, bypassing onboarding.

---

### Scenario 4: Responsive Scroll & Accessibility Font Scaling (200%)

1. On the test device/emulator, navigate to **Settings > Accessibility > Display size and text**.
2. Set **Font size** to the maximum (200% scaling).
3. Open Tullab on the welcome screen.
4. **Verify**:
   - Text expands proportionally without overlapping adjacent cards or clipping.
   - The entire screen is scrollable vertically via touch fling.
   - The user can scroll to the bottom, toggle the consent checkbox, and tap the "Get Started" button with minimum 48×48 dp touch target.

---

### Scenario 5: Theme Switching & Contrast (Light / Dark Theme)

1. Toggle system setting to **Dark Theme**.
2. Open Tullab on the welcome screen.
3. **Verify**:
   - Background adapts to `DarkBackground` (`#0D111C`).
   - Card surfaces adapt to `DarkSurfaceVariant` / `DarkSurface` with clear borders.
   - High contrast text (`#E0E5ED`) meets WCAG AA (contrast ratio > 4.5:1).
4. Toggle back to **Light Theme**:
   - Background adapts to `TullabBackground` (`#F7F9FC`).
   - Text is crisp dark navy (`#1F2430`).

---

## Automated Verification Commands

Run local unit tests to ensure code compiles and passes regression checks:

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run Compose / lint checks
./gradlew lintDebug
```
