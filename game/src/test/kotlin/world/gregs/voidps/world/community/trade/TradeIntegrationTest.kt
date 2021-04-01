package world.gregs.voidps.world.community.trade

import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.utility.get
import world.gregs.voidps.world.script.WorldScript

internal class TradeIntegrationTest : WorldScript() {

    lateinit var player1: Player
    lateinit var player2: Player

    @BeforeEach
    override fun setup() {
        super.setup()
        val factory: PlayerFactory = get()
        player1 = spyk(factory.create("1", ""))
        player1.start()
        player2 = spyk(factory.create("2", ""))
        player2.start()
        setProperty("homeX", 100)
        setProperty("homeY", 100)
        setProperty("homePlane", 1)
    }

    @Test
    fun test() {
        player1.inventory.add("coins", 1000)
        player1.events.emit(PlayerOption(player2, "Trade with", 4))
        player2.events.emit(PlayerOption(player1, "Trade with", 4))
        offerItem("Offer-10", 2, 995, 0)
        acceptTrade(player1)
        acceptTrade(player2)
        tick()
        confirmTrade(player1)
        confirmTrade(player2)
        tick()
        assertEquals(995, player1.inventory.getItem(0))
        assertEquals(990, player1.inventory.getAmount(0))
        assertEquals(995, player2.inventory.getItem(0))
        assertEquals(10, player2.inventory.getAmount(0))
    }

    fun offerItem(option: String, optionId: Int, item: Int, slot: Int) {
        player1.events.emit(InterfaceOption(336, "trade_side", 0, "offer", optionId, option, "item", item, slot))
    }

    fun acceptTrade(player: Player) {
        player.events.emit(InterfaceOption(335, "trade_main", 16, "accept", 0, "Accept", "", -1, -1))
    }

    fun confirmTrade(player: Player) {
        player.events.emit(InterfaceOption(334, "trade_confirm", 21, "accept", 0, "Accept", "", -1, -1))
    }

}