package com.barutdev.tullab.navigation

internal class BottomBarState {
    private val lastStudentIds = mutableMapOf<TullabDestination.StudentScoped, Int>()
    private var lastRecordedStudentId: Int? = null

    fun record(destination: TullabDestination.StudentScoped, studentId: Int) {
        lastStudentIds[destination] = studentId
        lastRecordedStudentId = studentId
    }

    fun shouldRestore(destination: TullabDestination.StudentScoped, studentId: Int?): Boolean {
        if (studentId == null) return false
        return lastStudentIds[destination] == studentId
    }

    fun clear(destination: TullabDestination.StudentScoped) {
        lastStudentIds.remove(destination)
    }

    fun lastStudentId(destination: TullabDestination.StudentScoped): Int? = lastStudentIds[destination]

    fun lastKnownStudentId(): Int? = lastRecordedStudentId
}
