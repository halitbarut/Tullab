package com.barutdev.tullab.data.repository

import com.barutdev.tullab.data.local.HomeworkDao
import com.barutdev.tullab.data.mapper.toDomain
import com.barutdev.tullab.data.mapper.toEntity
import com.barutdev.tullab.domain.model.Homework
import com.barutdev.tullab.domain.repository.HomeworkRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeworkRepositoryImpl @Inject constructor(
    private val homeworkDao: HomeworkDao
) : HomeworkRepository {

    override fun getHomeworkForStudent(studentId: Int): Flow<List<Homework>> =
        homeworkDao.getHomeworkForStudent(studentId).map { entities ->
            entities.toDomain()
        }

    override fun getHomeworkById(homeworkId: Int): Flow<Homework?> =
        homeworkDao.getHomeworkById(homeworkId).map { entity ->
            entity?.toDomain()
        }

    override suspend fun insertHomework(homework: Homework) {
        homeworkDao.insert(homework.toEntity())
    }

    override suspend fun updateHomework(homework: Homework) {
        homeworkDao.insert(homework.toEntity())
    }
}
