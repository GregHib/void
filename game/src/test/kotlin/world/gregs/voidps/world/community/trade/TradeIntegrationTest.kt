package world.gregs.voidps.world.community.trade

import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.PlayerLoader
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.utility.get
import world.gregs.voidps.world.interact.entity.player.display.InterfaceOption
import world.gregs.voidps.world.script.WorldScript

internal class TradeIntegrationTest : WorldScript() {

    lateinit var player1: Player
    lateinit var player2: Player
    lateinit var bus: EventBus

    @BeforeEach
    override fun setup() {
        super.setup()
        val loader: PlayerLoader = get()
        player1 = spyk(loader.loadPlayer("1"))
        player1.start()
        player2 = spyk(loader.loadPlayer("2"))
        player2.start()
        bus = get()
        setProperty("homeX", 100)
        setProperty("homeY", 100)
        setProperty("homePlane", 1)
    }

    @Test
    fun test() {
        player1.inventory.add(995, 1000)
        bus.emit(PlayerOption(player1, player2, "Trade with", 4))
        bus.emit(PlayerOption(player2, player1, "Trade with", 4))
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
        bus.emit(InterfaceOption(player1, 336, "trade_side", 0, "offer", optionId, option, "item", item, slot))
    }

    fun acceptTrade(player: Player) {
        bus.emit(InterfaceOption(player, 335, "trade_main", 16, "accept", 0, "Accept", "", -1, -1))
    }

    fun confirmTrade(player: Player) {
        bus.emit(InterfaceOption(player, 334, "trade_confirm", 21, "accept", 0, "Accept", "", -1, -1))
    }

}