package content.social.trade.exchange.history

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AggregateTest {

    @Test
    fun `Update sets values correctly`() {
        val aggregate = Aggregate(open = 100)

        aggregate.update(price = 100, amount = 10)

        assertEquals(100, aggregate.open)
        assertEquals(100, aggregate.high)
        assertEquals(100, aggregate.low)
        assertEquals(100, aggregate.close)
        assertEquals(10L, aggregate.volume)
        assertEquals(1, aggregate.count)
    }

    @Test
    fun `Update sets high and low over multiple updates`() {
        val aggregate = Aggregate(open = 100)

        aggregate.update(100, 10)
        aggregate.update(120, 5)
        aggregate.update(80, 8)

        assertEquals(100, aggregate.open)
        assertEquals(120, aggregate.high)
        assertEquals(80, aggregate.low)
        assertEquals(80, aggregate.close)
        assertEquals(23L, aggregate.volume)
        assertEquals(3, aggregate.count)
    }

    @Test
    fun `Average highs set when price is above midpoint`() {
        val aggregate = Aggregate()

        aggregate.update(100, 10)
        aggregate.update(110, 10)
        aggregate.update(5, 5) // low is ignored

        assertEquals(105.0, aggregate.highAverage)
        assertEquals(20L, aggregate.volumeHigh)
        assertEquals(5L, aggregate.volumeLow)
    }

    @Test
    fun `Average lows set when price is below midpoint`() {
        val aggregate = Aggregate()

        aggregate.update(200, 5) // set high midpoint
        aggregate.update(100, 5)
        aggregate.update(50, 5)

        assertEquals(75.0, aggregate.lowAverage)
        assertEquals(5L, aggregate.volumeHigh)
        assertEquals(10L, aggregate.volumeLow)
    }

    @Test
    fun `Update works when price equals midpoint`() {
        val aggregate = Aggregate()

        aggregate.update(100, 10)
        aggregate.update(100, 5)

        assertEquals(100.0, aggregate.highAverage)
        assertEquals(15L, aggregate.volumeHigh)
        assertEquals(0L, aggregate.volumeLow)
    }
}