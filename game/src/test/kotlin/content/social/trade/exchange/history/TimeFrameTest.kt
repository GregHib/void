package content.social.trade.exchange.history

import org.junit.Assert.assertEquals
import java.time.Instant
import kotlin.test.Test

class TimeFrameTest {

    @Test
    fun `Round down days into 5 min time frames`() {
        val timestamp = Instant.parse("2025-08-04T00:07:00Z").toEpochMilli()

        val result = TimeFrame.Day.start(timestamp)

        assertEquals(Instant.parse("2025-08-04T00:05:00Z").toEpochMilli(), result)
    }

    @Test
    fun `Round down weeks into 1 hr time frames`() {
        val timestamp = Instant.parse("2025-08-04T02:45:00Z").toEpochMilli()

        val result = TimeFrame.Week.start(timestamp)

        assertEquals(Instant.parse("2025-08-04T02:00:00Z").toEpochMilli(), result)
    }

    @Test
    fun `Round down month into 6 hour time frames`() {
        val timestamp = Instant.parse("2025-08-04T14:23:00Z").toEpochMilli()

        val result = TimeFrame.Month.start(timestamp)

        assertEquals(Instant.parse("2025-08-04T12:00:00Z").toEpochMilli(), result)
    }

    @Test
    fun `Round down year into 1 day time frames`() {
        val timestamp = Instant.parse("2025-08-04T10:45:00Z")

        val result = TimeFrame.Year.start(timestamp.toEpochMilli())

        assertEquals(Instant.parse("2025-08-04T00:00:00Z").toEpochMilli(), result)
    }

    @Test
    fun `Don't change exact minute for day`() {
        val timestamp = Instant.parse("2025-08-04T00:00:00Z").toEpochMilli()

        val result = TimeFrame.Day.start(timestamp)

        assertEquals(Instant.parse("2025-08-04T00:00:00Z").toEpochMilli(), result)
    }

    @Test
    fun `Don't change exact hour for week`() {
        val timestamp = Instant.parse("2025-08-04T01:00:00Z").toEpochMilli()

        val result = TimeFrame.Week.start(timestamp)

        val expected = Instant.parse("2025-08-04T01:00:00Z").toEpochMilli()

        assertEquals(expected, result)
    }

    @Test
    fun `Don't change exact 6-hour for month`() {
        val timestamp = Instant.parse("2025-08-04T06:00:00Z").toEpochMilli()

        val result = TimeFrame.Month.start(timestamp)

        val expected = Instant.parse("2025-08-04T06:00:00Z").toEpochMilli()

        assertEquals(expected, result)
    }

    @Test
    fun `Don't change exact day for year`() {
        val timestamp = Instant.parse("2025-08-04T00:00:00Z").toEpochMilli()

        val result = TimeFrame.Year.start(timestamp)

        val expected = Instant.parse("2025-08-04T00:00:00Z").toEpochMilli()

        assertEquals(expected, result)
    }
}