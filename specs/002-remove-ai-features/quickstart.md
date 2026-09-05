# Quickstart: Removal of AI Features & Pure Local Operation

**Feature**: `002-remove-ai-features`  
**Date**: 2026-09-03  
**Status**: Ready for Implementation  

## 1. Scope & Execution Plan

### Components to Delete 🗑️
| Component | Path |
| :--- | :--- |
| **Domain AI Models** | `app/src/main/java/com/barutdev/kora/domain/model/ai/` (all 6 files) |
| **Domain Repositories & Use Cases** | `AiRepository.kt`, `AiInsightsCacheRepository.kt`, `AiInsightsGenerationTracker.kt`, `AiException.kt`, `GenerateAiInsightsUseCase.kt` |
| **Data Repositories & Entities** | `AiRepositoryImpl.kt`, `AiInsightsCacheRepositoryImpl.kt`, `AiInsightsGenerationTrackerImpl.kt`, `AiInsightEntity.kt`, `AiInsightDao.kt` |
| **DI Module** | `AiModule.kt` |
| **UI State Model** | `AiInsightsUiState.kt` |

### Components to Update 🔧
| Component | Path | Action |
| :--- | :--- | :--- |
| **Manifest** | `app/src/main/AndroidManifest.xml` | Remove `android.permission.INTERNET` |
| **Build Config** | `app/build.gradle.kts` | Remove `GEMINI_API_KEY` and generative AI dependency |
| **Version Catalog** | `gradle/libs.versions.toml` | Remove `generativeAi` and `google-ai-generativeai` |
| **Database** | `data/local/KoraDatabase.kt` | Remove `AiInsightEntity`, increment version to `9` |
| **Database Module** | `di/DatabaseModule.kt` | Remove `provideAiInsightDao`, add `MIGRATION_8_9` |
| **Migration** | `data/local/migrations/StudentMigrations.kt` | Add `MIGRATION_8_9` dropping `ai_insights` |
| **Backup Manager** | `data/backup/DataBackupManager.kt` | Remove `aiInsightDao` dependency and clear calls |
| **Repository Module** | `di/RepositoryModule.kt` | Remove AI repository bindings |
| **Dashboard ViewModel** | `ui/screens/dashboard/DashboardViewModel.kt` | Remove AI injections, state, and methods |
| **Dashboard Screen** | `ui/screens/dashboard/DashboardScreen.kt` | Remove `AiAssistantCard` and AI callbacks |
| **Homework ViewModel** | `ui/screens/homework/HomeworkViewModel.kt` | Remove AI injections, state, and methods |
| **Homework Screen** | `ui/screens/homework/HomeworkScreen.kt` | Remove `AiAssistantCard` and AI callbacks |
| **Unit Tests** | `test/.../HomeworkViewModelTest.kt` | Remove AI mocks and tests |
| **Instrumented Test DI** | `androidTest/.../TestDatabaseModule.kt` | Remove `provideAiInsightDao` |
| **Localization** | `res/values*/strings.xml` (EN, TR, DE) | Remove all 13 AI string keys |

---

## 2. Validation Scenarios

### Scenario 1: Verify Zero Network Permissions
Ensure that the compiled Android manifest contains zero internet permission declarations.
```bash
./gradlew processDebugManifest
grep -i "INTERNET" app/build/intermediates/merged_manifest/debug/processDebugManifest/AndroidManifest.xml || echo "SUCCESS: No INTERNET permission found"
```
**Expected**: No matches found; echo outputs success.

### Scenario 2: Room Schema Export & Migration Verification
Verify that Room compiles schema version 9 to `app/schemas/com.barutdev.kora.data.local.KoraDatabase/9.json` without `ai_insights`.
```bash
./gradlew kspDebugKotlin
cat app/schemas/com.barutdev.kora.data.local.KoraDatabase/9.json | grep -i "ai_insights" || echo "SUCCESS: ai_insights absent from v9 schema"
```
**Expected**: `ai_insights` table is completely absent from the exported schema.

### Scenario 3: Automated Unit Testing
Run all unit tests to ensure no broken ViewModel flows, repositories, or backup routines.
```bash
./gradlew testDebugUnitTest
```
**Expected**: All tests pass (`BUILD SUCCESSFUL`).

### Scenario 4: UI Reflow & Absence of AI Elements
Run instrumented or Compose preview checks on Dashboard and Homework screens:
1. Open Dashboard: Displays student card, statistics, and lesson list. No AI card is rendered.
2. Open Homework: Displays homework filter chips and homework cards. No AI card is rendered.
3. Turn on Airplane Mode: Full functionality is retained with zero errors or dialogs.
