# Research & Architectural Decisions: Removal of AI Features

**Feature**: `002-remove-ai-features`  
**Date**: 2026-09-03  
**Status**: Completed  

## 1. Database Migration Strategy (Room v8 → v9)

### Decision
Increment `KoraDatabase` version from `8` to `9`. Remove `AiInsightEntity::class` from the `@Database` entities list. Create a dedicated manual migration `MIGRATION_8_9` that executes:
```sql
DROP TABLE IF EXISTS ai_insights
```
Register `MIGRATION_8_9` in `DatabaseModule.provideDatabase()`.

### Rationale
- Tutors have existing production databases containing student profiles, lesson histories, homework assignments, and financial payment records.
- Falling back to destructive migration would wipe non-AI tutor data, violating Constitution Principle I (Data Sovereignty) and User Story 3 (Data Preservation).
- Dropping the `ai_insights` table via `DROP TABLE IF EXISTS ai_insights` is atomic, safe, and frees up local storage while leaving all foreign keys, indices, and primary tables intact.

### Alternatives Considered
- **Keep an empty `ai_insights` table with no operations**: Rejected. Leaves orphaned dead code, unused schema migrations, and obsolete DB allocations.
- **Destructive migration (`fallbackToDestructiveMigration()`)**: Rejected. Causes catastrophic data loss for existing users.
- **Room AutoMigration**: Room auto-migrations can handle table drops (`@DeleteTable(tableName = "ai_insights")`), but a manual `Migration(8, 9)` provides deterministic execution and allows direct automated migration unit testing using `room-testing`.

---

## 2. Manifest & Network Permission Revocation

### Decision
Remove `<uses-permission android:name="android.permission.INTERNET" />` from `app/src/main/AndroidManifest.xml`.

### Rationale
- Per Constitution Principle I, Kora's only permitted external network communication was specifically restricted to the Google Gemini API for optional AI features.
- With AI features completely eliminated, Kora has zero features that require network access.
- Revoking the `INTERNET` permission ensures at the Android operating system level that the application cannot send or receive any data over the internet, providing ironclad data sovereignty and air-gapped security for tutors.

### Alternatives Considered
- **Keep `INTERNET` permission in manifest as a dormant permission**: Rejected. Requesting unneeded permissions erodes user trust, violates the principle of least privilege, and exposes the app to scrutiny on privacy audits.

---

## 3. Build Configuration & Dependency Deletion

### Decision
1. Remove `val geminiApiKey = ...` and `buildConfigField("String", "GEMINI_API_KEY", ...)` from `app/build.gradle.kts`.
2. Remove `implementation(libs.google.ai.generativeai)` from `app/build.gradle.kts`.
3. Remove `generativeAi` version and `google-ai-generativeai` library declaration from `gradle/libs.versions.toml`.

### Rationale
- Eliminates external SDK dependencies, reducing the APK/AAB bundle size.
- Eliminates build-time secret injection and the need for `GEMINI_API_KEY` in `local.properties`.
- Prevents compilation dependencies on third-party cloud SDKs.

### Alternatives Considered
- **Keep the dependency but don't call it**: Rejected. Unused SDKs bloat app size and introduce potential vulnerability or compatibility maintenance overhead.

---

## 4. Domain & Data Layer Cleanup

### Decision
Completely delete all AI-related domain and data files:
- `com.barutdev.kora.domain.model.ai.*`:
  - `AiInsightsFocus.kt`
  - `AiInsightsResult.kt`
  - `CachedAiInsight.kt`
  - `AiInsightsComputation.kt`
  - `AiInsightsSignatureBuilder.kt`
  - `AiInsightsRequestKey.kt`
- Domain interfaces & use cases:
  - `AiRepository.kt`
  - `AiInsightsCacheRepository.kt`
  - `AiInsightsGenerationTracker.kt`
  - `AiException.kt`
  - `GenerateAiInsightsUseCase.kt`
- Data implementations & DAOs:
  - `AiInsightEntity.kt`
  - `AiInsightDao.kt`
  - `AiRepositoryImpl.kt`
  - `AiInsightsCacheRepositoryImpl.kt`
  - `AiInsightsGenerationTrackerImpl.kt`
- DI Modules:
  - Delete `AiModule.kt`
  - Remove AI DAO from `DatabaseModule.kt` and `TestDatabaseModule.kt`
  - Remove AI repository binds from `RepositoryModule.kt`
- Data Backup:
  - Remove `aiInsightDao` parameter and invocations from `DataBackupManager.kt`.

### Rationale
- Clean Architecture mandates strict cohesion. Removing all layers of a feature prevents zombie classes, unused Hilt bindings, and confusing remnants.

### Alternatives Considered
- **Deprecating classes without deletion**: Rejected. There is no external library consumer; Kora is a standalone application. Complete removal is the cleanest approach.

---

## 5. UI Layer & ViewModel Streamlining

### Decision
1. In `DashboardViewModel`:
   - Remove `AiInsightsCacheRepository`, `AiInsightsGenerationTracker`, `GenerateAiInsightsUseCase` constructor injections.
   - Remove `aiInsightsState`, `aiInsightsJob`, `ensureAiInsights()`, `retryAiInsights()`, and internal AI generation logic.
2. In `DashboardScreen`:
   - Remove `AiAssistantCard` Composable.
   - Remove `aiInsightsState` and `onGenerateAiInsights` parameters from `DashboardContent`.
   - Layout reflows seamlessly to present `StudentSummaryCard`, `QuickActions`, `UpcomingLessons`, and `PaymentHistory`.
3. In `HomeworkViewModel`:
   - Remove `AiInsightsCacheRepository`, `AiInsightsGenerationTracker`, `GenerateAiInsightsUseCase` constructor injections.
   - Remove `aiInsightsState`, `ensureAiInsights()`, `retryAiInsights()`.
4. In `HomeworkScreen`:
   - Remove `AiAssistantCard` Composable.
   - Remove `aiInsightsState` and `onGenerateAiInsights` parameters from `HomeworkContent`.
   - List naturally presents filter chips, homework statistics, and assignment cards without empty spacing.
5. Delete `AiInsightsUiState.kt`.
6. Update `HomeworkViewModelTest.kt` to remove AI mocks and tests.

### Rationale
- Ensures adherence to UDF and Material 3 design principles without orphaned UI elements.

---

## 6. String & Localization Cleanup

### Decision
Remove all AI-related strings from:
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-tr/strings.xml`
- `app/src/main/res/values-de/strings.xml`

Strings to remove:
- `homework_ai_card_title`
- `homework_ai_card_body`
- `dashboard_ai_insights_title`
- `dashboard_ai_insights_body`
- `ai_loading_message`
- `ai_retry_action`
- `ai_generate_again_action`
- `ai_missing_api_key_message`
- `ai_empty_response_message`
- `ai_generic_error_message`
- `ai_generic_no_data_message`
- `dashboard_ai_no_data_message`
- `homework_ai_no_data_message`

### Rationale
- Retains 100% linguistic parity across EN, TR, and DE (Principle VIII) while removing unused resource entries.
