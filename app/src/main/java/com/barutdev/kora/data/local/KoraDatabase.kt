package com.barutdev.kora.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.barutdev.kora.data.local.entity.HomeworkEntity
import com.barutdev.kora.data.local.entity.LessonEntity
import com.barutdev.kora.data.local.entity.PaymentRecordEntity
import com.barutdev.kora.data.local.entity.StudentEntity

@Database(
    entities = [StudentEntity::class, LessonEntity::class, HomeworkEntity::class, PaymentRecordEntity::class],
    version = 10,
    exportSchema = true
)
@TypeConverters(LessonStatusConverter::class, HomeworkStatusConverter::class, PricingModeConverter::class)
abstract class KoraDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao

    abstract fun lessonDao(): LessonDao

    abstract fun homeworkDao(): HomeworkDao

    abstract fun paymentRecordDao(): PaymentRecordDao
}
