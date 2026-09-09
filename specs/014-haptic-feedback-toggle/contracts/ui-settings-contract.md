# UI Contract: Settings Screen Haptic Feedback Row

## Component Contract

Inside `com.barutdev.tullab.ui.screens.settings.SettingsScreen.kt`:

### Position
- Parent container: `SettingsSection(title = tullabStringResource(id = R.string.settings_section_general))`
- Preceding item: `SettingSwitchRow` for Dark Mode
- Trailing item: `SettingsDivider()` followed by `SettingNavigationRow` for Language

### Visual Specification
- **Icon**: `Icons.Outlined.Vibration`
- **Icon Content Description**: Resource `R.string.settings_haptic_feedback_content_description`
- **Title**: Resource `R.string.settings_haptic_feedback_label`
- **Checked State**: `userPreferences.hapticFeedbackEnabled` (binds to `StateFlow<UserPreferences>`)
- **OnCheckedChange**: `viewModel::updateHapticFeedbackEnabled`
- **Click / Interaction Feedback**: Silent toggle; no haptic feedback or audio clicks emitted when toggling.
- **Touch Target**: >= 48dp height/width through `SettingSwitchRow` `toggleable` modifier with `Role.Switch`.

### Strings Parity Contract

| Resource Key | `values/strings.xml` (EN) | `values-tr/strings.xml` (TR) | `values-de/strings.xml` (DE) |
|---|---|---|---|
| `settings_haptic_feedback_label` | Haptic Feedback | Dokunsal Geri Bildirim | Haptisches Feedback |
| `settings_haptic_feedback_content_description` | Toggle haptic feedback | Dokunsal geri bildirimi değiştir | Haptisches Feedback umschalten |
