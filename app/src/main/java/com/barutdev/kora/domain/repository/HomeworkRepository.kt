package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.Homework
import kotlinx.coroutines.flow.Flow

interface HomeworkRepository {
    fun getHomeworkForStudent(studentId: Int): Flow<List<Homework>>
    fun getHomeworkById(homeworkId: Int): Flow<Homework?>
    suspend fun insertHomework(homework: Homework)
    suspend fun updateHomework(homework: Homework)
}
