package rs.dusk.engine.client.ui

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declareMock
import rs.dusk.engine.client.clientSessionModule
import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.event.InterfaceClosed
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.ui.event.InterfaceRefreshed
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.script.KoinMock
import rs.dusk.network.rs.codec.game.encode.message.InterfaceCloseMessage
import rs.dusk.network.rs.codec.game.encode.message.InterfaceOpenMessage
import rs.dusk.network.rs.codec.game.encode.message.InterfaceUpdateMessage

internal class InterfaceIOTest : KoinMock() {

    private lateinit var io: InterfaceIO
    private lateinit var player: Player
    private lateinit var bus: EventBus

    override val modules = listOf(clientSessionModule, eventModule)

    @BeforeEach
    fun setup() {
        player = mockk()
        bus = declareMock {
            every { emit(any<PlayerEvent>(), any()) } returns mockk()
        }
        io = PlayerInterfaceIO(player, bus)
    }

    @Test
    fun `Send full screen interface open`() {
        val inter: Interface = mockk()
        every { player.gameFrame.resizable } returns false
        every { inter.id } returns 1
        every { inter.getParent(false) } returns -1
        io.sendOpen(inter)
        verify {
            player.send(InterfaceUpdateMessage(1, 0))
        }
    }

    @Test
    fun `Send interface open`() {
        val inter: Interface = mockk()
        every { player.gameFrame.resizable } returns true
        every { inter.id } returns 100
        every { inter.getParent(true) } returns 10
        every { inter.getIndex(true) } returns 1
        every { inter.type } returns "main_screen"
        io.sendOpen(inter)
        verify {
            player.send(InterfaceOpenMessage(false, 10, 1, 100))
        }
    }

    @Test
    fun `Send permanent interface open`() {
        val inter: Interface = mockk()
        every { player.gameFrame.resizable } returns false
        every { inter.id } returns 100
        every { inter.getParent(false) } returns 10
        every { inter.getIndex(false) } returns 1
        every { inter.type } returns ""
        io.sendOpen(inter)
        verify {
            player.send(InterfaceOpenMessage(true, 10, 1, 100))
        }
    }

    @Test
    fun `Send interface close`() {
        val inter: Interface = mockk()
        every { player.gameFrame.resizable } returns true
        every { inter.id } returns 10
        every { inter.getIndex(true) } returns 1
        every { inter.getParent(true) } returns 100
        io.sendClose(inter)
        verify {
            player.send(InterfaceCloseMessage(100, 1))
        }
    }

    @Test
    fun `Notify closed`() {
        val inter: Interface = mockk()
        every { inter.id } returns 10
        every { inter.name } returns "interface_name"
        io.notifyClosed(inter)
        verify { bus.emit(InterfaceClosed(player, 10, "interface_name")) }
    }

    @Test
    fun `Notify opened`() {
        val inter: Interface = mockk()
        every { inter.id } returns 10
        every { inter.name } returns "interface_name"
        io.notifyOpened(inter)
        verify { bus.emit(InterfaceOpened(player, 10, "interface_name")) }
    }

    @Test
    fun `Notify refreshed`() {
        val inter: Interface = mockk()
        every { inter.id } returns 10
        every { inter.name } returns "interface_name"
        io.notifyRefreshed(inter)
        verify { bus.emit(InterfaceRefreshed(player, 10, "interface_name")) }
    }
}