package world.gregs.voidps.world.community.trade

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.add
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.playerOption

internal class TradeTest : WorldTest() {

    @Test
    fun `Trade coins from one player to another`() {
        val sender = createPlayer("sender", emptyTile)
        val receiver = createPlayer("receiver", emptyTile.addY(1))
        sender.inventory.add("coins", 1000)
        sender.playerOption(receiver, "Trade with")
        receiver.playerOption(sender, "Trade with")
        tick()
        sender.interfaceOption("trade_side", "offer", "Offer-10", item = Item("coins"), slot = 0)
        sender.interfaceOption("trade_main", "accept", "Accept")
        receiver.interfaceOption("trade_main", "accept", "Accept")
        tick()
        sender.interfaceOption("trade_confirm", "accept", "Accept")
        receiver.interfaceOption("trade_confirm", "accept", "Accept")
        tick()
        assertEquals(Item("coins", 990), sender.inventory[0])
        assertEquals(Item("coins", 10), receiver.inventory[0])
    }

}