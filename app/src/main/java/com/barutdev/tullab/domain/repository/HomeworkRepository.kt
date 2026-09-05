package com.barutdev.tullab.domain.repository

import com.barutdev.tullab.domain.model.Homework
import kotlinx.coroutines.flow.Flow

interface HomeworkRepository {
    fun getHomeworkForStudent(studentId: Int): Flow<List<Homework>>
    fun getHomeworkById(homeworkId: Int): Flow<Homework?>
    suspend fun insertHomework(homework: Homework)
    suspend fun updateHomework(homework: Homework)
}
