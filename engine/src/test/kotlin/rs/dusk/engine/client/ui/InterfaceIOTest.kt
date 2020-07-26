package rs.dusk.engine.client.ui

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.client.clientSessionModule
import rs.dusk.engine.client.send
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.script.KoinMock
import rs.dusk.network.rs.codec.game.encode.message.InterfaceCloseMessage
import rs.dusk.network.rs.codec.game.encode.message.InterfaceOpenMessage
import rs.dusk.network.rs.codec.game.encode.message.InterfaceUpdateMessage

internal class InterfaceIOTest : KoinMock() {

    private lateinit var io: InterfaceIO
    private lateinit var player: Player

    override val modules = listOf(clientSessionModule)

    @BeforeEach
    fun setup() {
        player = mockk()
        io = PlayerInterfaceIO(player)
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
        every { inter.type } returns null
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
        io.sendClose(inter)
        verify {
            player.send(InterfaceCloseMessage(10, 1))
        }
    }
}