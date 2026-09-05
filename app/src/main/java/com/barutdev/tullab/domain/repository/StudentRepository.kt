package com.barutdev.tullab.domain.repository

import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.domain.model.StudentProfileUpdate
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    fun getAllStudents(): Flow<List<Student>>

    fun getStudentById(id: Int): Flow<Student?>

    suspend fun addStudent(student: Student)

    suspend fun updateStudentHourlyRate(studentId: Int, newRate: Double)

    suspend fun updateStudentProfile(update: StudentProfileUpdate)

    suspend fun deleteStudent(studentId: Int)
}
