# Quickstart & Verification Guide: Haptic Feedback Toggle

## Prerequisites

- Android SDK API 34+ installed.
- Physical device or emulator configured with vibration support.
- Project built using `./gradlew assembleDebug`.

---

## Automated Verification

Run unit tests covering ViewModels, preferences persistence, and haptics logic:

```bash
# Run unit tests
./gradlew testDebugUnitTest --tests "com.barutdev.tullab.ui.screens.settings.SettingsViewModelTest"
./gradlew testDebugUnitTest
```

---

## Manual Verification Scenarios

### Scenario 1: Fresh Install Default State
1. Clear app data or uninstall Tullab:
   ```bash
   adb shell pm clear com.barutdev.tullab
   ```
2. Launch Tullab.
3. Complete initial setup/onboarding if shown.
4. Navigate to **Settings**.
5. **Verify**: Under **General**, the **Haptic Feedback** switch row is visible between Dark Mode and Language, displaying `Icons.Outlined.Vibration`, and the switch is **ON**.
6. Mark any unpaid lesson as paid.
7. **Verify**: The phone emits a double-tap/confirmation vibration pulse.

### Scenario 2: Disabling Haptic Feedback
1. In Tullab, navigate to **Settings**.
2. Locate **Haptic Feedback** in the **General** section.
3. Tap the switch to turn it **OFF**.
4. **Verify**: The switch moves to the OFF position silently without vibrating.
5. Navigate to the Calendar or Dashboard screen.
6. Mark an unpaid lesson as paid.
7. **Verify**: No vibration pulse is emitted. Visual feedback (status change, Snackbar with "Undo") still appears.
8. Toggle a homework item completed.
9. **Verify**: No click vibration is felt.
10. Confirm a lesson deletion or warning action.
11. **Verify**: No warning vibration is felt.

### Scenario 3: Persistence Across Process Death
1. Keep the toggle **OFF**.
2. Force-stop the app process:
   ```bash
   adb shell am force-stop com.barutdev.tullab
   ```
3. Relaunch Tullab.
4. Navigate to **Settings**.
5. **Verify**: The **Haptic Feedback** switch remains **OFF**.

### Scenario 4: Re-enabling Haptic Feedback
1. In **Settings** → **General**, toggle **Haptic Feedback** back to **ON**.
2. Navigate to Homework.
3. Check/uncheck a homework assignment.
4. **Verify**: The crisp physical click vibration is restored.

### Scenario 5: Trilingual Label Parity
1. In **Settings**, change language to **Türkçe**.
   - **Verify**: The row reads **"Dokunsal Geri Bildirim"**.
2. Change language to **Deutsch**.
   - **Verify**: The row reads **"Haptisches Feedback"**.
3. Change language to **English**.
   - **Verify**: The row reads **"Haptic Feedback"**.
