package rs.dusk.engine.client.ui

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.detail.InterfaceComponentDetail
import rs.dusk.engine.client.ui.detail.InterfaceDetail
import rs.dusk.engine.client.ui.event.InterfaceClosed
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.ui.event.InterfaceRefreshed
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventBus
import rs.dusk.network.rs.codec.game.encode.message.*

internal class InterfaceIOTest {

    private lateinit var io: InterfaceIO
    private lateinit var player: Player
    private lateinit var bus: EventBus

    @BeforeEach
    fun setup() {
        player = mockk()
        mockkStatic("rs.dusk.engine.client.SessionsKt")
        every { player.send(any<Message>()) } just Runs
        bus = mockk()
        every { bus.emit(any<PlayerEvent>(), any()) } returns mockk()
        io = PlayerInterfaceIO(player, bus)
    }

    @Test
    fun `Send full screen interface open`() {
        val inter: InterfaceDetail = mockk()
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
        val inter: InterfaceDetail = mockk()
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
        val inter: InterfaceDetail = mockk()
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
        val inter: InterfaceDetail = mockk()
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
        val inter: InterfaceDetail = mockk()
        every { inter.id } returns 10
        every { inter.name } returns "interface_name"
        io.notifyClosed(inter)
        verify { bus.emit(InterfaceClosed(player, 10, "interface_name")) }
    }

    @Test
    fun `Notify opened`() {
        val inter: InterfaceDetail = mockk()
        every { inter.id } returns 10
        every { inter.name } returns "interface_name"
        io.notifyOpened(inter)
        verify { bus.emit(InterfaceOpened(player, 10, "interface_name")) }
    }

    @Test
    fun `Notify refreshed`() {
        val inter: InterfaceDetail = mockk()
        every { inter.id } returns 10
        every { inter.name } returns "interface_name"
        io.notifyRefreshed(inter)
        verify { bus.emit(InterfaceRefreshed(player, 10, "interface_name")) }
    }

    @Test
    fun `Send player head`() {
        val inter: InterfaceDetail = mockk()
        every { inter.id } returns 10
        val comp = InterfaceComponentDetail(100, "")
        comp.parent = inter.id
        io.sendPlayerHead(comp)
        verify {
            player.send(InterfaceHeadPlayerMessage(10, 100))
        }
    }

    @Test
    fun `Send npc head`() {
        val inter: InterfaceDetail = mockk()
        every { inter.id } returns 100
        val comp = InterfaceComponentDetail(10, "")
        comp.parent = inter.id
        io.sendNPCHead(comp, 123)
        verify {
            player.send(InterfaceHeadNPCMessage(100, 10, 123))
        }
    }

    @Test
    fun `Send animation`() {
        val inter: InterfaceDetail = mockk()
        every { inter.id } returns 100
        val comp = InterfaceComponentDetail(10, "")
        comp.parent = inter.id
        io.sendAnimation(comp, 123)
        verify {
            player.send(InterfaceAnimationMessage(100, 10, 123))
        }
    }

    @Test
    fun `Send text`() {
        val inter: InterfaceDetail = mockk()
        every { inter.id } returns 100
        val comp = InterfaceComponentDetail(10, "")
        comp.parent = inter.id
        io.sendText(comp, "words")
        verify {
            player.send(InterfaceTextMessage(100, 10, "words"))
        }
    }

    @Test
    fun `Send visibility`() {
        val inter: InterfaceDetail = mockk()
        every { inter.id } returns 100
        val comp = InterfaceComponentDetail(10, "")
        comp.parent = inter.id
        io.sendVisibility(comp, visible = false)
        verify {
            player.send(InterfaceVisibilityMessage(100, 10, hide = true))
        }
    }

    @Test
    fun `Send sprite`() {
        val inter: InterfaceDetail = mockk()
        every { inter.id } returns 100
        val comp = InterfaceComponentDetail(10, "")
        comp.parent = inter.id
        io.sendSprite(comp, 123)
        verify {
            player.send(InterfaceSpriteMessage(100, 10, 123))
        }
    }

    @Test
    fun `Send item`() {
        val inter: InterfaceDetail = mockk()
        every { inter.id } returns 100
        val comp = InterfaceComponentDetail(10, "")
        comp.parent = inter.id
        io.sendItem(comp, 123, 4)
        verify {
            player.send(InterfaceItemMessage(100, 10, 123, 4))
        }
    }
}