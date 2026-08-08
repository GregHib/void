package content.social.trade.monitor

import WorldTest
import interfaceOption
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import playerOption
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertTrue

internal class TradeFlagsMonitorTest : WorldTest() {

    @BeforeEach
    fun setup() {
        Settings.load(
            mapOf(
                "economy.monitor.enabled" to "true",
                "economy.monitor.trade.enabled" to "true",
                "economy.monitor.trade.valueThreshold" to "5",
                "economy.monitor.trade.pair.valueThreshold" to "5",
                "economy.monitor.trade.pair.count" to "1",
                "economy.monitor.trade.pair.windowMinutes" to "60",
            ),
        )
        AuditLog.logs.clear()
        TradeFlags.clear()
    }

    @Test
    fun `Completed trade over threshold is flagged with non-zero value`() {
        val (sender, receiver) = setupTradeWithOffer("value_sender", "value_receiver")
        completeTrade(sender, receiver)
        assertTrue(AuditLog.logs.any { it.contains("TRADE_FLAG_VALUE") && it.contains("\t10") })
    }

    @Test
    fun `Large trades between a pair of accounts are flagged`() {
        val (sender, receiver) = setupTradeWithOffer("pair_sender", "pair_receiver")
        completeTrade(sender, receiver)
        assertTrue(AuditLog.logs.any { it.contains("TRADE_FLAG_PAIR") })
    }

    private fun completeTrade(sender: Player, receiver: Player) {
        sender.interfaceOption("trade_main", "accept", "Accept")
        receiver.interfaceOption("trade_main", "accept", "Accept")
        tick()
        sender.interfaceOption("trade_confirm", "accept", "Accept")
        receiver.interfaceOption("trade_confirm", "accept", "Accept")
        tick()
    }

    private fun setupTradeWithOffer(senderName: String, receiverName: String): Pair<Player, Player> {
        val sender = createPlayer(emptyTile, senderName)
        val receiver = createPlayer(emptyTile.addY(1), receiverName)
        sender.inventory.add("coins", 1000)
        sender.playerOption(receiver, "Trade with")
        receiver.playerOption(sender, "Trade with")
        tick()
        sender.interfaceOption("trade_side", "offer", "Offer-10", item = Item("coins"), slot = 0)
        return Pair(sender, receiver)
    }
}
