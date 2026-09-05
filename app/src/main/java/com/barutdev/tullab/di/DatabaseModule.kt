package com.barutdev.tullab.di

import android.content.Context
import androidx.room.Room
import com.barutdev.tullab.data.local.HomeworkDao
import com.barutdev.tullab.data.local.TullabDatabase
import com.barutdev.tullab.data.local.LessonDao
import com.barutdev.tullab.data.local.PaymentRecordDao
import com.barutdev.tullab.data.local.StudentDao
import com.barutdev.tullab.data.local.migrations.MIGRATION_7_8
import com.barutdev.tullab.data.local.migrations.MIGRATION_8_9
import com.barutdev.tullab.data.local.migrations.MIGRATION_9_10
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "tullab.db"

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TullabDatabase = Room.databaseBuilder(
        context,
        TullabDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideStudentDao(
        database: TullabDatabase
    ): StudentDao = database.studentDao()

    @Provides
    @Singleton
    fun provideLessonDao(
        database: TullabDatabase
    ): LessonDao = database.lessonDao()

    @Provides
    @Singleton
    fun provideHomeworkDao(
        database: TullabDatabase
    ): HomeworkDao = database.homeworkDao()

    @Provides
    @Singleton
    fun providePaymentRecordDao(
        database: TullabDatabase
    ): PaymentRecordDao = database.paymentRecordDao()
}
