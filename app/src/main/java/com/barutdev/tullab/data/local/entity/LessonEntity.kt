package com.barutdev.tullab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.barutdev.tullab.domain.model.LessonStatus

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"])]
)
data class LessonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: Int,
    val date: Long,
    val status: LessonStatus,
    val durationInHours: Double?,
    val notes: String?,
    val pricingMode: com.barutdev.tullab.domain.model.PricingMode,
    val rateOrFee: Double,
    val paymentTimestamp: Long? = null
)
