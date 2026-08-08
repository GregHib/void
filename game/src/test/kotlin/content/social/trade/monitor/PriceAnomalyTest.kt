package content.social.trade.monitor

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PriceAnomalyTest {

    @Test
    fun `Deviation is fractional difference from market`() {
        assertEquals(0.5, PriceAnomaly.deviation(150, 100))
        assertEquals(0.5, PriceAnomaly.deviation(50, 100))
        assertEquals(0.0, PriceAnomaly.deviation(100, 100))
    }

    @Test
    fun `No deviation without a market price`() {
        assertEquals(0.0, PriceAnomaly.deviation(100, 0))
        assertEquals(0.0, PriceAnomaly.deviation(100, -1))
    }

    @Test
    fun `Flags at or over threshold`() {
        assertTrue(PriceAnomaly.flagged(price = 130, amount = 1000, market = 100, minValue = 100_000, threshold = 0.3))
        assertFalse(PriceAnomaly.flagged(price = 129, amount = 1000, market = 100, minValue = 100_000, threshold = 0.3))
    }

    @Test
    fun `Low value trades are ignored`() {
        assertFalse(PriceAnomaly.flagged(price = 200, amount = 1, market = 100, minValue = 100_000, threshold = 0.3))
    }

    @Test
    fun `Large amounts overflow safely`() {
        assertTrue(PriceAnomaly.flagged(price = Int.MAX_VALUE, amount = Int.MAX_VALUE, market = 100, minValue = 100_000, threshold = 0.3))
    }
}
