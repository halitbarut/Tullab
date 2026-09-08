package com.barutdev.tullab.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class TimeFormatterTest {

    @Test
    fun `calculateDelayToNextMidnight computes accurate delay until next midnight UTC`() {
        // 23:00:00 UTC -> 1 hour until midnight = 3600_000 ms
        val now = Instant.parse("2026-09-08T23:00:00Z")
        val delay = calculateDelayToNextMidnight(now = now, zone = ZoneOffset.UTC)
        assertEquals(3600_000L, delay)
    }

    @Test
    fun `calculateDelayToNextMidnight computes accurate delay for midday`() {
        // 12:00:00 UTC -> 12 hours until midnight = 12 * 3600_000 = 43_200_000 ms
        val now = Instant.parse("2026-09-08T12:00:00Z")
        val delay = calculateDelayToNextMidnight(now = now, zone = ZoneOffset.UTC)
        assertEquals(43_200_000L, delay)
    }

    @Test
    fun `calculateDelayToNextMidnight with custom time zone`() {
        // Zone UTC+3 (e.g., Europe/Istanbul)
        val zone = ZoneId.of("+03:00")
        // In UTC+3, 2026-09-08T20:30:00Z is 23:30:00 local time -> 30 mins until midnight = 1800_000 ms
        val now = Instant.parse("2026-09-08T20:30:00Z")
        val delay = calculateDelayToNextMidnight(now = now, zone = zone)
        assertEquals(1800_000L, delay)
    }

    @Test
    fun `calculateDelayToNextMidnight enforces 1000ms minimum guard`() {
        // 100ms before midnight: should be clamped to 1000ms
        val now = Instant.parse("2026-09-08T23:59:59.900Z")
        val delay = calculateDelayToNextMidnight(now = now, zone = ZoneOffset.UTC)
        assertEquals(1000L, delay)
    }

    @Test
    fun `calculateDelayToNextMidnight across month boundary`() {
        // Last day of month
        val now = Instant.parse("2026-09-30T23:45:00Z")
        val delay = calculateDelayToNextMidnight(now = now, zone = ZoneOffset.UTC)
        assertEquals(15 * 60 * 1000L, delay)
    }
}
