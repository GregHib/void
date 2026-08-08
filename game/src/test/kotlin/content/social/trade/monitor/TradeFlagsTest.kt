package content.social.trade.monitor

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.AuditLog
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TradeFlagsTest {

    private val requester = Player(accountName = "requester")
    private val acceptor = Player(accountName = "acceptor")

    @BeforeEach
    fun setup() {
        AuditLog.logs.clear()
        TradeFlags.clear()
        Settings.load(
            mapOf(
                "economy.monitor.enabled" to "true",
                "economy.monitor.trade.enabled" to "true",
                "economy.monitor.trade.valueThreshold" to "10000000",
                "economy.monitor.trade.pair.valueThreshold" to "1000000",
                "economy.monitor.trade.pair.count" to "3",
                "economy.monitor.trade.pair.windowMinutes" to "60",
            ),
        )
    }

    @AfterEach
    fun teardown() {
        Settings.clear()
        AuditLog.logs.clear()
        TradeFlags.clear()
    }

    @Test
    fun `Trade over value threshold is flagged`() {
        TradeFlags.check(requester, acceptor, 10_000_000, 0, tick = 0)
        assertTrue(AuditLog.logs.any { it.contains("TRADE_FLAG_VALUE") })
    }

    @Test
    fun `Trade under value threshold is not flagged`() {
        TradeFlags.check(requester, acceptor, 9_999_999, 500, tick = 0)
        assertFalse(AuditLog.logs.any { it.contains("TRADE_FLAG_VALUE") })
    }

    @Test
    fun `Repeated large trades between the same pair are flagged`() {
        TradeFlags.check(requester, acceptor, 1_000_000, 0, tick = 0)
        TradeFlags.check(acceptor, requester, 1_000_000, 0, tick = 10)
        assertFalse(AuditLog.logs.any { it.contains("TRADE_FLAG_PAIR") })
        TradeFlags.check(requester, acceptor, 500_000, 500_000, tick = 20)
        assertTrue(AuditLog.logs.any { it.contains("TRADE_FLAG_PAIR") })
    }

    @Test
    fun `Trades outside the window are pruned`() {
        val window = 6000 // 60 minutes in ticks
        TradeFlags.check(requester, acceptor, 1_000_000, 0, tick = 0)
        TradeFlags.check(requester, acceptor, 1_000_000, 0, tick = 10)
        TradeFlags.check(requester, acceptor, 1_000_000, 0, tick = window + 11)
        assertFalse(AuditLog.logs.any { it.contains("TRADE_FLAG_PAIR") })
    }

    @Test
    fun `Pair flag re-arms after flagging`() {
        for (tick in 0 until 3) {
            TradeFlags.check(requester, acceptor, 2_000_000, 0, tick = tick)
        }
        assertEquals(1, AuditLog.logs.count { it.contains("TRADE_FLAG_PAIR") })
        TradeFlags.check(requester, acceptor, 2_000_000, 0, tick = 10)
        assertEquals(1, AuditLog.logs.count { it.contains("TRADE_FLAG_PAIR") })
    }

    @Test
    fun `Disabled monitor flags nothing`() {
        Settings.load(mapOf("economy.monitor.enabled" to "false"))
        TradeFlags.check(requester, acceptor, 100_000_000, 0, tick = 0)
        assertTrue(AuditLog.logs.isEmpty())
    }
}
