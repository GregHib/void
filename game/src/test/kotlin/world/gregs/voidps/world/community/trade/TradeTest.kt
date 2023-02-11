package world.gregs.voidps.world.community.trade

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.playerOption
import world.gregs.voidps.world.script.walk

internal class TradeTest : WorldTest() {

    @Test
    fun `Trade coins from one player to another`() {
        val (sender, receiver) = setupTradeWithOffer()
        sender.interfaceOption("trade_main", "accept", "Accept")
        receiver.interfaceOption("trade_main", "accept", "Accept")
        tick()
        sender.interfaceOption("trade_confirm", "accept", "Accept")
        receiver.interfaceOption("trade_confirm", "accept", "Accept")
        tick()
        assertEquals(Item("coins", 990), sender.inventory[0])
        assertEquals(Item("coins", 10), receiver.inventory[0])
    }

    @ParameterizedTest
    @ValueSource(strings = ["decline", "exit", "move"])
    fun `Trade decline on main screen`(type: String) {
        val (sender, receiver) = setupTradeWithOffer()
        when (type) {
            "decline" -> sender.interfaceOption("trade_main", "decline", "Decline")
            "exit" -> receiver.interfaceOption("trade_main", "close", "Close")
            "move" -> {
                sender.walk(sender.tile.addX(1))
                tick()
            }
        }
        assertEquals(Item("coins", 1000), sender.inventory[0])
        assertEquals(Item.EMPTY, receiver.inventory[0])
    }

    @ParameterizedTest
    @ValueSource(strings = ["decline", "exit", "move"])
    fun `Trade decline on confirm screen`(type: String) {
        val (sender, receiver) = setupTradeWithOffer()
        sender.interfaceOption("trade_main", "accept", "Accept")
        receiver.interfaceOption("trade_main", "accept", "Accept")
        tick()
        when (type) {
            "decline" -> receiver.interfaceOption("trade_confirm", "decline", "Decline")
            "exit" -> sender.interfaceOption("trade_confirm", "close", "Close")
            "move" -> {
                receiver.walk(sender.tile.addX(1))
                tick()
            }
        }
        assertEquals(Item("coins", 1000), sender.inventory[0])
        assertEquals(Item.EMPTY, receiver.inventory[0])
    }

    private fun setupTradeWithOffer(): Pair<Player, Player> {
        val sender = createPlayer("sender", emptyTile)
        val receiver = createPlayer("receiver", emptyTile.addY(1))
        sender.inventory.add("coins", 1000)
        sender.playerOption(receiver, "Trade with")
        receiver.playerOption(sender, "Trade with")
        tick()
        sender.interfaceOption("trade_side", "offer", "Offer-10", item = Item("coins"), slot = 0)
        return Pair(sender, receiver)
    }

}