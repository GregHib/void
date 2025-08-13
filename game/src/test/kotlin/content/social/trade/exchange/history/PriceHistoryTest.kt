package content.social.trade.exchange.history

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class PriceHistoryTest {

    private lateinit var itemHistory: ItemHistory
    private val fixedTimestamp = 1_000_000_000_000L

    @BeforeEach
    fun setUp() {
        itemHistory = ItemHistory()
    }

    @Test
    fun `Record adds data to all timeframes`() {
        itemHistory.record(fixedTimestamp, 100, 10)

        assertEquals(1, itemHistory.day.size)
        assertEquals(1, itemHistory.week.size)
        assertEquals(1, itemHistory.month.size)
        assertEquals(1, itemHistory.year.size)

        val aggregate = itemHistory.day.values.first()
        assertEquals(100, aggregate.open)
        assertEquals(100, aggregate.high)
        assertEquals(100, aggregate.low)
        assertEquals(100, aggregate.close)
        assertEquals(10L, aggregate.volume)
    }

    @Test
    fun `Record updates existing aggregates`() {
        itemHistory.record(fixedTimestamp, 100, 10)
        itemHistory.record(fixedTimestamp, 110, 5)

        val aggregate = itemHistory.day.values.first()
        assertEquals(100, aggregate.open)
        assertEquals(110, aggregate.high)
        assertEquals(100, aggregate.low)
        assertEquals(110, aggregate.close)
        assertEquals(15L, aggregate.volume)
    }

    @Test
    fun `Record assigns correct open price on new frame entry`() {
        val nextDay = fixedTimestamp + TimeUnit.DAYS.toMillis(1)

        itemHistory.record(fixedTimestamp, 100, 10)
        itemHistory.record(nextDay, 110, 5)

        val opens = itemHistory.day.values.map { it.open }.sorted()
        assertEquals(listOf(100, 110), opens)
    }

    @Test
    fun `Clean removes old entries from all maps`() {
        val oldTimestamp = fixedTimestamp - TimeUnit.DAYS.toMillis(400)
        val currentTimestamp = fixedTimestamp

        // Add old entries
        itemHistory.record(oldTimestamp, 90, 5)

        // Add current entries
        itemHistory.record(currentTimestamp, 100, 10)

        itemHistory.clean(currentTimestamp)

        assertEquals(1, itemHistory.day.size)
        assertEquals(1, itemHistory.week.size)
        assertEquals(1, itemHistory.month.size)
        assertEquals(1, itemHistory.year.size)

        // Validate remaining data is current
        assertTrue(itemHistory.day.keys.all { it >= TimeFrame.Day.start(currentTimestamp - TimeUnit.DAYS.toMillis(1)) })
        assertTrue(itemHistory.week.keys.all { it >= TimeFrame.Week.start(currentTimestamp - TimeUnit.DAYS.toMillis(7)) })
        assertTrue(itemHistory.month.keys.all { it >= TimeFrame.Month.start(currentTimestamp - TimeUnit.DAYS.toMillis(30)) })
        assertTrue(itemHistory.year.keys.all { it >= TimeFrame.Year.start(currentTimestamp - TimeUnit.DAYS.toMillis(365)) })
    }

    @Test
    fun `Clean doesn't remove entries exactly the right age`() {
        val oldTimestamp = fixedTimestamp - TimeUnit.DAYS.toMillis(365)

        itemHistory.record(oldTimestamp, 90, 5)
        itemHistory.clean(fixedTimestamp)

        assertEquals(1, itemHistory.year.size)
    }

}