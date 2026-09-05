package com.barutdev.tullab.di

import android.content.Context
import androidx.room.Room
import com.barutdev.tullab.data.local.HomeworkDao
import com.barutdev.tullab.data.local.TullabDatabase
import com.barutdev.tullab.data.local.LessonDao
import com.barutdev.tullab.data.local.PaymentRecordDao
import com.barutdev.tullab.data.local.StudentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TullabDatabase = Room.inMemoryDatabaseBuilder(
        context,
        TullabDatabase::class.java
    )
        .allowMainThreadQueries()
        .build()

    @Provides
    @Singleton
    fun provideStudentDao(database: TullabDatabase): StudentDao = database.studentDao()

    @Provides
    @Singleton
    fun provideLessonDao(database: TullabDatabase): LessonDao = database.lessonDao()

    @Provides
    @Singleton
    fun provideHomeworkDao(database: TullabDatabase): HomeworkDao = database.homeworkDao()

    @Provides
    @Singleton
    fun providePaymentRecordDao(database: TullabDatabase): PaymentRecordDao = database.paymentRecordDao()
}
