package com.barutdev.tullab.data.local

import androidx.room.TypeConverter
import com.barutdev.tullab.domain.model.HomeworkStatus

class HomeworkStatusConverter {

    @TypeConverter
    fun fromStatus(status: HomeworkStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): HomeworkStatus = HomeworkStatus.valueOf(value)
}
