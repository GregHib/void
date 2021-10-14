package world.gregs.voidps.world.community.trade

import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.world.script.WorldMock
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.mockStackableItem
import world.gregs.voidps.world.script.playerOption

internal class TradeTest : WorldMock() {

    @Test
    fun `Trade coins from one player to another`() = runBlocking(Dispatchers.Default) {
        mockStackableItem(995) // coins
        every { get<ContainerDecoder>().get(90) } answers { // trade_offer
            ContainerDefinition(id = arg(0), length = 28)
        }
        val sender = createPlayer("sender")
        val receiver = createPlayer("receiver")
        sender.inventory.add("coins", 1000)
        sender.playerOption(receiver, "Trade with")
        receiver.playerOption(sender, "Trade with")
        sender.interfaceOption("trade_side", "offer", "Offer-10", item = Item("coins"), slot = 0)
        sender.interfaceOption("trade_main", "accept", "Accept")
        receiver.interfaceOption("trade_main", "accept", "Accept")
        tick()
        sender.interfaceOption("trade_confirm", "accept", "Accept")
        receiver.interfaceOption("trade_confirm", "accept", "Accept")
        tick()
        assertEquals(Item("coins", 990), sender.inventory.getItem(0))
        assertEquals(Item("coins", 10), receiver.inventory.getItem(0))
    }

}