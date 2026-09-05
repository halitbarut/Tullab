package com.barutdev.tullab.data.repository

import com.barutdev.tullab.data.local.StudentDao
import com.barutdev.tullab.data.mapper.toDomain
import com.barutdev.tullab.data.mapper.toEntity
import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.domain.model.StudentProfileUpdate
import com.barutdev.tullab.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StudentRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao
) : StudentRepository {

    override fun getAllStudents(): Flow<List<Student>> =
        studentDao.getAllStudents().map { entities ->
            entities.toDomain()
        }

    override fun getStudentById(id: Int): Flow<Student?> =
        studentDao.getStudentById(id).map { entity ->
            entity?.toDomain()
        }

    override suspend fun addStudent(student: Student) {
        val timestamp = student.lastPaymentDate ?: System.currentTimeMillis()
        val entity = student.copy(
            lastPaymentDate = timestamp,
            hourlyRate = student.customHourlyRate ?: student.hourlyRate,
            customHourlyRate = student.customHourlyRate
        ).toEntity()
        studentDao.insert(entity)
    }

    override suspend fun updateStudentHourlyRate(studentId: Int, newRate: Double) {
        studentDao.updateStudentHourlyRate(studentId, newRate)
    }

    override suspend fun updateStudentProfile(update: StudentProfileUpdate) {
        studentDao.updateStudentProfile(
            studentId = update.id,
            fullName = update.fullName,
            parentName = update.parentName,
            parentContact = update.parentContact,
            notes = update.notes,
            customHourlyRate = update.customHourlyRate
        )
    }

    override suspend fun deleteStudent(studentId: Int) {
        studentDao.deleteById(studentId)
    }
}
