package com.barutdev.tullab.domain.model

data class StudentProfileUpdate(
    val id: Int,
    val fullName: String,
    val parentName: String?,
    val parentContact: String?,
    val notes: String?,
    val customHourlyRate: Double?
)
