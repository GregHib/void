package content.social.trade.monitor

import WorldTest
import content.social.trade.exchange.history.ExchangeHistory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.get
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.event.AuditLog
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PriceAnomalyMonitorTest : WorldTest() {

    @BeforeEach
    fun setup() {
        Settings.load(
            mapOf(
                "economy.monitor.enabled" to "true",
                "economy.monitor.price.enabled" to "true",
                "economy.monitor.price.deviation" to "0.3",
                "economy.monitor.price.minValue" to "1000",
                "economy.monitor.price.cooldownMinutes" to "10",
            ),
        )
        AuditLog.logs.clear()
    }

    @Test
    fun `Exchange far from market price is flagged`() {
        val history: ExchangeHistory = get()
        val market = history.marketPrice("rune_longsword")
        history.record("rune_longsword", 10, market * 2)
        assertTrue(AuditLog.logs.any { it.contains("PRICE_ANOMALY") && it.contains("rune_longsword") })
    }

    @Test
    fun `Exchange at market price is not flagged`() {
        val history: ExchangeHistory = get()
        val market = history.marketPrice("rune_scimitar")
        history.record("rune_scimitar", 10, market)
        assertFalse(AuditLog.logs.any { it.contains("PRICE_ANOMALY") })
    }

    @Test
    fun `Repeat flags for the same item are suppressed by cooldown`() {
        val history: ExchangeHistory = get()
        val market = history.marketPrice("rune_battleaxe")
        history.record("rune_battleaxe", 10, market * 2)
        history.record("rune_battleaxe", 10, market * 2)
        assertEquals(1, AuditLog.logs.count { it.contains("PRICE_ANOMALY") && it.contains("rune_battleaxe") })
    }
}
