package com.barutdev.tullab.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.barutdev.tullab.data.local.entity.HomeworkEntity
import com.barutdev.tullab.data.local.entity.LessonEntity
import com.barutdev.tullab.data.local.entity.PaymentRecordEntity
import com.barutdev.tullab.data.local.entity.StudentEntity

@Database(
    entities = [StudentEntity::class, LessonEntity::class, HomeworkEntity::class, PaymentRecordEntity::class],
    version = 10,
    exportSchema = true
)
@TypeConverters(LessonStatusConverter::class, HomeworkStatusConverter::class, PricingModeConverter::class)
abstract class TullabDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao

    abstract fun lessonDao(): LessonDao

    abstract fun homeworkDao(): HomeworkDao

    abstract fun paymentRecordDao(): PaymentRecordDao
}
