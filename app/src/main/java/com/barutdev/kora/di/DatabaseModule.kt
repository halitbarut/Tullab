package com.barutdev.kora.di

import android.content.Context
import androidx.room.Room
import com.barutdev.kora.data.local.HomeworkDao
import com.barutdev.kora.data.local.KoraDatabase
import com.barutdev.kora.data.local.LessonDao
import com.barutdev.kora.data.local.PaymentRecordDao
import com.barutdev.kora.data.local.StudentDao
import com.barutdev.kora.data.local.migrations.MIGRATION_7_8
import com.barutdev.kora.data.local.migrations.MIGRATION_8_9
import com.barutdev.kora.data.local.migrations.MIGRATION_9_10
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "kora.db"

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): KoraDatabase = Room.databaseBuilder(
        context,
        KoraDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideStudentDao(
        database: KoraDatabase
    ): StudentDao = database.studentDao()

    @Provides
    @Singleton
    fun provideLessonDao(
        database: KoraDatabase
    ): LessonDao = database.lessonDao()

    @Provides
    @Singleton
    fun provideHomeworkDao(
        database: KoraDatabase
    ): HomeworkDao = database.homeworkDao()

    @Provides
    @Singleton
    fun providePaymentRecordDao(
        database: KoraDatabase
    ): PaymentRecordDao = database.paymentRecordDao()
}
