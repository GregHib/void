package content.social.trade.monitor

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class EconomySinkTest {

    @AfterEach
    fun teardown() {
        EconomySink.clear()
    }

    @Test
    fun `Disabled sink captures nothing`() {
        EconomySink.flag("trade_value", "sender", "receiver", value = 100)
        EconomySink.census(listOf(ItemCensus.Count("coins", 1, 0, 0)))
        assertTrue(EconomySink.drainFlags().isEmpty())
        assertTrue(EconomySink.drainCensus().isEmpty())
    }

    @Test
    fun `Enabled sink captures flag fields`() {
        EconomySink.enabled = true
        EconomySink.flag("price_anomaly", item = "party_hat", value = 1000, details = "market=100")
        val flags = EconomySink.drainFlags()
        assertEquals(1, flags.size)
        assertEquals("price_anomaly", flags[0].type)
        assertEquals("party_hat", flags[0].item)
        assertEquals(1000L, flags[0].value)
        assertEquals("market=100", flags[0].details)
        assertEquals(null, flags[0].source)
    }

    @Test
    fun `Drain resets the buffer`() {
        EconomySink.enabled = true
        EconomySink.flag("trade_value")
        assertEquals(1, EconomySink.drainFlags().size)
        assertTrue(EconomySink.drainFlags().isEmpty())
    }

    @Test
    fun `Census skips zero total counts`() {
        EconomySink.enabled = true
        EconomySink.census(
            listOf(
                ItemCensus.Count("coins", 100, 5, 10),
                ItemCensus.Count("party_hat", 0, 0, 0),
            ),
        )
        val census = EconomySink.drainCensus()
        assertEquals(1, census.size)
        assertEquals("coins", census[0].item)
        assertEquals(100L, census[0].players)
        assertEquals(5L, census[0].floor)
        assertEquals(10L, census[0].exchange)
    }

    @Test
    fun `Buffer cap drops excess flags`() {
        EconomySink.enabled = true
        for (i in 0 until 10_001) {
            EconomySink.flag("trade_value", value = i.toLong())
        }
        assertEquals(10_000, EconomySink.drainFlags().size)
    }
}
