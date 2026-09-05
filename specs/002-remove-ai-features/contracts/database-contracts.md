# Database & Storage Contracts: Post-AI Architecture

**Feature**: `002-remove-ai-features`  
**Date**: 2026-09-03  
**Status**: Completed  

## 1. Room Database Contract (`KoraDatabase`)

```kotlin
@Database(
    entities = [
        StudentEntity::class,
        LessonEntity::class,
        HomeworkEntity::class,
        PaymentRecordEntity::class
    ],
    version = 9,
    exportSchema = true
)
@TypeConverters(LessonStatusConverter::class, HomeworkStatusConverter::class)
abstract class KoraDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun lessonDao(): LessonDao
    abstract fun homeworkDao(): HomeworkDao
    abstract fun paymentRecordDao(): PaymentRecordDao
}
```
*(Removed: `AiInsightEntity::class` and `abstract fun aiInsightDao(): AiInsightDao`)*

---

## 2. Migration Contract (`MIGRATION_8_9`)

```kotlin
package com.barutdev.kora.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS ai_insights")
    }
}
```

---

## 3. Data Backup Contract (`DataBackupManager`)

```kotlin
@Singleton
class DataBackupManager @Inject constructor(
    private val studentDao: StudentDao,
    private val lessonDao: LessonDao,
    private val homeworkDao: HomeworkDao,
    private val database: KoraDatabase
) {
    suspend fun exportToCsv(): String
    suspend fun importFromCsv(csv: String)
    suspend fun clearAllData()
}
```
*(Removed: `aiInsightDao: AiInsightDao` parameter and `aiInsightDao.deleteAll()` calls)*
