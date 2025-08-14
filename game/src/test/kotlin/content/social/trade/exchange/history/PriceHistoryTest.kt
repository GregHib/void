package content.social.trade.exchange.history

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.exchange.PriceHistory
import java.util.concurrent.TimeUnit

class PriceHistoryTest {

    private lateinit var priceHistory: PriceHistory
    private val fixedTimestamp = 1_000_000_000_000L

    @BeforeEach
    fun setUp() {
        priceHistory = PriceHistory()
    }

    @Test
    fun `Record adds data to all timeframes`() {
        priceHistory.record(fixedTimestamp, 100, 10)

        assertEquals(1, priceHistory.day.size)
        assertEquals(1, priceHistory.week.size)
        assertEquals(1, priceHistory.month.size)
        assertEquals(1, priceHistory.year.size)

        val aggregate = priceHistory.day.values.first()
        assertEquals(100, aggregate.open)
        assertEquals(100, aggregate.high)
        assertEquals(100, aggregate.low)
        assertEquals(100, aggregate.close)
        assertEquals(10L, aggregate.volume)
    }

    @Test
    fun `Record updates existing aggregates`() {
        priceHistory.record(fixedTimestamp, 100, 10)
        priceHistory.record(fixedTimestamp, 110, 5)

        val aggregate = priceHistory.day.values.first()
        assertEquals(100, aggregate.open)
        assertEquals(110, aggregate.high)
        assertEquals(100, aggregate.low)
        assertEquals(110, aggregate.close)
        assertEquals(15L, aggregate.volume)
    }

    @Test
    fun `Record assigns correct open price on new frame entry`() {
        val nextDay = fixedTimestamp + TimeUnit.DAYS.toMillis(1)

        priceHistory.record(fixedTimestamp, 100, 10)
        priceHistory.record(nextDay, 110, 5)

        val opens = priceHistory.day.values.map { it.open }.sorted()
        assertEquals(listOf(100, 110), opens)
    }

    @Test
    fun `Clean removes old entries from all maps`() {
        val oldTimestamp = fixedTimestamp - TimeUnit.DAYS.toMillis(400)
        val currentTimestamp = fixedTimestamp

        // Add old entries
        priceHistory.record(oldTimestamp, 90, 5)

        // Add current entries
        priceHistory.record(currentTimestamp, 100, 10)

        priceHistory.clean(currentTimestamp)

        assertEquals(1, priceHistory.day.size)
        assertEquals(1, priceHistory.week.size)
        assertEquals(1, priceHistory.month.size)
        assertEquals(1, priceHistory.year.size)

        // Validate remaining data is current
        assertTrue(priceHistory.day.keys.all { it >= TimeFrame.Day.start(currentTimestamp - TimeUnit.DAYS.toMillis(1)) })
        assertTrue(priceHistory.week.keys.all { it >= TimeFrame.Week.start(currentTimestamp - TimeUnit.DAYS.toMillis(7)) })
        assertTrue(priceHistory.month.keys.all { it >= TimeFrame.Month.start(currentTimestamp - TimeUnit.DAYS.toMillis(30)) })
        assertTrue(priceHistory.year.keys.all { it >= TimeFrame.Year.start(currentTimestamp - TimeUnit.DAYS.toMillis(365)) })
    }

    @Test
    fun `Clean doesn't remove entries exactly the right age`() {
        val oldTimestamp = fixedTimestamp - TimeUnit.DAYS.toMillis(365)

        priceHistory.record(oldTimestamp, 90, 5)
        priceHistory.clean(fixedTimestamp)

        assertEquals(1, priceHistory.year.size)
    }

}