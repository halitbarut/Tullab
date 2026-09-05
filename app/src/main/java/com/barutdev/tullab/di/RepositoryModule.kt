package com.barutdev.tullab.di

import com.barutdev.tullab.data.repository.HomeworkRepositoryImpl
import com.barutdev.tullab.data.repository.LessonRepositoryImpl
import com.barutdev.tullab.data.repository.PaymentRepositoryImpl
import com.barutdev.tullab.data.repository.StudentRepositoryImpl
import com.barutdev.tullab.data.repository.UserPreferencesRepository as DataUserPreferencesRepository
import com.barutdev.tullab.domain.repository.HomeworkRepository
import com.barutdev.tullab.domain.repository.LessonRepository
import com.barutdev.tullab.domain.repository.PaymentRepository
import com.barutdev.tullab.domain.repository.StudentRepository
import com.barutdev.tullab.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStudentRepository(
        impl: StudentRepositoryImpl
    ): StudentRepository

    @Binds
    @Singleton
    abstract fun bindLessonRepository(
        impl: LessonRepositoryImpl
    ): LessonRepository

    @Binds
    @Singleton
    abstract fun bindHomeworkRepository(
        impl: HomeworkRepositoryImpl
    ): HomeworkRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        impl: PaymentRepositoryImpl
    ): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: DataUserPreferencesRepository
    ): UserPreferencesRepository
}
