package com.barutdev.tullab.domain.model.reports

import androidx.annotation.StringRes
import com.barutdev.tullab.R
import java.time.LocalDate
import java.time.ZoneId

/**
 * Date range presets supported by the Reports screen filters.
 */
sealed class ReportRange(
    @StringRes val labelResId: Int
) {

    /**
     * Resolves the inclusive start and end dates for the range using the provided clock inputs.
     */
    abstract fun bounds(
        today: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): ClosedRange<LocalDate>

    object LastSevenDays : ReportRange(R.string.reports_range_last_7_days) {
        override fun bounds(today: LocalDate, zoneId: ZoneId): ClosedRange<LocalDate> {
            val end = today
            val start = end.minusDays(6)
            return start..end
        }
    }

    object ThisMonth : ReportRange(R.string.reports_range_this_month) {
        override fun bounds(today: LocalDate, zoneId: ZoneId): ClosedRange<LocalDate> {
            val start = today.withDayOfMonth(1)
            return start..today
        }
    }

    object LastThirtyDays : ReportRange(R.string.reports_range_last_30_days) {
        override fun bounds(today: LocalDate, zoneId: ZoneId): ClosedRange<LocalDate> {
            val end = today
            val start = end.minusDays(29)
            return start..end
        }
    }

    object AllTime : ReportRange(R.string.reports_range_all_time) {
        override fun bounds(today: LocalDate, zoneId: ZoneId): ClosedRange<LocalDate> {
            val start = LocalDate.ofEpochDay(0L)
            return start..today
        }
    }

    companion object {
        /**
         * Provides the default preset shown when the reports screen first loads.
         */
        val default: ReportRange = ThisMonth

        /**
         * Returns all presets in the order they should appear in the UI.
         */
        val presets: List<ReportRange> = listOf(
            LastSevenDays,
            ThisMonth,
            LastThirtyDays,
            AllTime
        )
    }
}
