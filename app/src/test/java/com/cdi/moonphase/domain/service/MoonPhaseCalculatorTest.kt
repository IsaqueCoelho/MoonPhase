package com.cdi.moonphase.domain.service

import com.cdi.moonphase.domain.model.LunarDay
import com.cdi.moonphase.domain.model.PhaseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * JVM unit tests against known ephemeris dates. This is where the architecture pays for
 * itself: the calculator is pure, so its correctness is verified with zero Android runtime.
 */
class MoonPhaseCalculatorTest {

    private val calculator = MoonPhaseCalculator()

    /** Reference New Moon used by the model: 2000-01-06 18:14 UTC. */
    private val referenceNewMoon: Instant =
        LocalDate.of(2000, 1, 6).atTime(18, 14).toInstant(ZoneOffset.UTC)

    private fun instant(days: Double): Instant =
        referenceNewMoon.plusSeconds((days * 86_400).toLong())

    @Test
    fun `at the reference new moon the phase is NEW with near-zero illumination`() {
        val result = calculator.computeAt(referenceNewMoon)

        assertEquals(PhaseType.NEW, result.phase.type)
        assertTrue("illumination should be ~0", result.illumination.value < 0.01)
        assertTrue("age should be ~0", result.lunarDay.ageDays < 0.2)
    }

    @Test
    fun `half a synodic month later the Moon is FULL and fully illuminated`() {
        val result = calculator.computeAt(instant(LunarDay.SYNODIC_MONTH_DAYS / 2))

        assertEquals(PhaseType.FULL, result.phase.type)
        assertTrue("illumination should be ~1", result.illumination.value > 0.99)
    }

    @Test
    fun `a quarter into the cycle is a waxing FIRST_QUARTER at half illumination`() {
        val result = calculator.computeAt(instant(LunarDay.SYNODIC_MONTH_DAYS / 4))

        assertEquals(PhaseType.FIRST_QUARTER, result.phase.type)
        assertTrue("should be waxing", result.phase.isWaxing)
        assertEquals(0.5, result.illumination.value, 0.02)
    }

    @Test
    fun `three quarters into the cycle is a waning LAST_QUARTER at half illumination`() {
        val result = calculator.computeAt(instant(3 * LunarDay.SYNODIC_MONTH_DAYS / 4))

        assertEquals(PhaseType.LAST_QUARTER, result.phase.type)
        assertTrue("should be waning", !result.phase.isWaxing)
        assertEquals(0.5, result.illumination.value, 0.02)
    }

    @Test
    fun `known 2024 new moon resolves to near-zero illumination`() {
        // New Moon: 2024-01-11 11:57 UTC.
        val newMoon2024 = LocalDate.of(2024, 1, 11).atTime(11, 57).toInstant(ZoneOffset.UTC)

        val result = calculator.computeAt(newMoon2024)

        assertTrue(
            "illumination was ${result.illumination.value}",
            result.illumination.value < 0.05,
        )
    }

    @Test
    fun `known 2024 full moon resolves to near-full illumination`() {
        // Full Moon: 2024-01-25 17:54 UTC.
        val fullMoon2024 = LocalDate.of(2024, 1, 25).atTime(17, 54).toInstant(ZoneOffset.UTC)

        val result = calculator.computeAt(fullMoon2024)

        assertTrue(
            "illumination was ${result.illumination.value}",
            result.illumination.value > 0.95,
        )
        assertEquals(PhaseType.FULL, result.phase.type)
    }

    @Test
    fun `the cycle is symmetric - illumination matches across the waxing and waning sides`() {
        val waxing = calculator.computeAt(instant(LunarDay.SYNODIC_MONTH_DAYS * 0.30))
        val waning = calculator.computeAt(instant(LunarDay.SYNODIC_MONTH_DAYS * 0.70))

        assertEquals(waxing.illumination.value, waning.illumination.value, 0.001)
        assertTrue(waxing.phase.isWaxing)
        assertTrue(!waning.phase.isWaxing)
    }

    @Test
    fun `next cardinal event after a new moon is the first quarter`() {
        val event = calculator.nextCardinalEvent(referenceNewMoon, ZoneOffset.UTC)

        assertEquals(PhaseType.FIRST_QUARTER, event.phase)
        // First quarter falls roughly a week after the new moon.
        assertTrue(event.date.isAfter(LocalDate.of(2000, 1, 6)))
        assertTrue(event.date.isBefore(LocalDate.of(2000, 1, 16)))
    }

    @Test
    fun `next cardinal event after a full moon is the last quarter`() {
        val fullMoon = instant(LunarDay.SYNODIC_MONTH_DAYS / 2)

        val event = calculator.nextCardinalEvent(fullMoon, ZoneOffset.UTC)

        assertEquals(PhaseType.LAST_QUARTER, event.phase)
    }

    @Test
    fun `the next four cardinal events cover each phase once, in chronological order`() {
        val events = calculator.upcomingCardinalEvents(referenceNewMoon, ZoneOffset.UTC, count = 4)

        assertEquals(4, events.size)
        // One upcoming occurrence of each cardinal phase.
        assertEquals(
            setOf(
                PhaseType.FIRST_QUARTER,
                PhaseType.FULL,
                PhaseType.LAST_QUARTER,
                PhaseType.NEW,
            ),
            events.map { it.phase }.toSet(),
        )
        // Strictly ascending dates.
        events.zipWithNext().forEach { (earlier, later) ->
            assertTrue("expected ${earlier.date} before ${later.date}", earlier.date.isBefore(later.date))
        }
        // Starting right after a New Moon, First Quarter comes first.
        assertEquals(PhaseType.FIRST_QUARTER, events.first().phase)
    }

    @Test
    fun `requesting zero upcoming events yields an empty list`() {
        val events = calculator.upcomingCardinalEvents(referenceNewMoon, ZoneOffset.UTC, count = 0)
        assertTrue(events.isEmpty())
    }
}
