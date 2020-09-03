package rs.dusk.world.community.trade

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.module.Module
import org.koin.test.mock.declare
import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.client.*
import rs.dusk.engine.client.variable.variablesModule
import rs.dusk.engine.data.PlayerLoader
import rs.dusk.engine.data.file.fileLoaderModule
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerOption
import rs.dusk.engine.entity.detailsModule
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventModule
import rs.dusk.utility.get
import rs.dusk.world.interact.entity.player.display.InterfaceOption
import rs.dusk.world.script.ScriptMock
import rs.dusk.world.script.WorldScript
import java.io.File
import kotlin.script.templates.standard.ScriptTemplateWithArgs

internal class TradeIntegrationTest : WorldScript() {

    lateinit var player1: Player
    lateinit var player2: Player
    lateinit var bus: EventBus

    @BeforeEach
    override fun setup() {
        super.setup()
        mockkStatic("rs.dusk.engine.client.SessionsKt")
        val loader: PlayerLoader = get()
        player1 = spyk(loader.loadPlayer("1"))
        player1.start()
        every { player1.send(any<Message>()) } just Runs
        player2 = spyk(loader.loadPlayer("2"))
        player2.start()
        every { player2.send(any<Message>()) } just Runs
        bus = get()
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