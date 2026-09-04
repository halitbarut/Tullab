package com.barutdev.kora.data.local

import androidx.room.TypeConverter
import com.barutdev.kora.domain.model.PricingMode

class PricingModeConverter {
    @TypeConverter
    fun fromPricingMode(value: PricingMode): String {
        return value.name
    }

    @TypeConverter
    fun toPricingMode(value: String): PricingMode {
        return PricingMode.valueOf(value)
    }
}
