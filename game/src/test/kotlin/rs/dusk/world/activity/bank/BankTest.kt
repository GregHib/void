package rs.dusk.world.activity.bank

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.get
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.client.variable.variablesModule
import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.engine.entity.character.contain.StackMode
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerSpawn
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventModule
import rs.dusk.world.interact.entity.player.display.InterfaceSwitch
import rs.dusk.world.script.KoinMock

internal class BankTest : KoinMock() {

    override val modules = listOf(variablesModule, eventModule)

    lateinit var bus: EventBus

    lateinit var decoder: ItemDecoder

    lateinit var player: Player

    lateinit var bank: Container

    lateinit var items: IntArray

    lateinit var amounts: IntArray

    @BeforeEach
    fun setup() {
        bus = get()
        decoder = mockk(relaxed = true)
        player = mockk(relaxed = true)
        items = IntArray(5)
        amounts = IntArray(5)
        bank = Container(decoder, stackMode = StackMode.Always, items = items, amounts = amounts)
        // TODO call script
        bus.emit(PlayerSpawn(player, "Test"))
    }

    @Test
    fun `Move from main tab to new tab`() {
        items[0] = 1
        amounts[0] = 1
        // When
        moveToTab(1, 0, 1)
        // Then
        verify {
            player.setVar("bank_tab_1", 1)
        }
    }

    @Test
    fun `Move from empty tab to main tab`() {

    }

    @Test
    fun `Move from tab to tab`() {

    }

    @Test
    fun `Move from empty tab to tab`() {

    }

    @Test
    fun `Move from tab to new tab`() {

    }

    @Test
    fun `Swap two items`() {

    }

    @Test
    fun `Insert item before itself`() {

    }

    @Test
    fun `Insert item after itself`() {

    }

    @Test
    fun `Insert item from main tab to other tab`() {

    }

    @Test
    fun `Insert item from tab to tab`() {

    }


    private fun moveToTab(id: Int, slot: Int, tab: Int) {
        bus.emit(InterfaceSwitch(player, -1, "bank", -1, "container", id, slot, -1, "bank", -1, "tab_$tab", -1, -1))
    }

    private fun swap(id: Int, slot: Int, toId: Int, toSlot: Int) {
        bus.emit(InterfaceSwitch(player, -1, "bank", -1, "container", id, slot, -1, "bank", -1, "container", toId, toSlot))
    }
}