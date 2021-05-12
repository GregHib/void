package world.gregs.voidps.engine.client.ui

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.detail.InterfaceComponentDetail
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetail
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.*

internal class InterfaceIOTest {

    private lateinit var io: InterfaceIO
    private lateinit var player: Player
    private lateinit var client: Client

    @BeforeEach
    fun setup() {
        player = mockk()
        client = mockk(relaxed = true)
        every { player.events.emit(any<Event>()) } returns mockk()
        every { player.client } returns client
        mockkStatic("world.gregs.voidps.network.encode.InterfaceEncodersKt")
        io = PlayerInterfaceIO(player)
    }

    @Test
    fun `Send full screen interface open`() {
        val inter: InterfaceDetail = mockk()
        every { player.gameFrame.resizable } returns false
        every { inter.id } returns 1
        every { inter.getParent(false) } returns "root"
        io.sendOpen(inter)
        verify {
            client.updateInterface(1, 0)
        }
    }

    @Test
    fun `Send interface open`() {
        val inter: InterfaceDetail = mockk()
        every { player.gameFrame.resizable } returns true
        every { inter.id } returns 100
        every { inter.getParent(true) } returns "10"
        every { inter.getIndex(true) } returns 1
        every { inter.type } returns "main_screen"
        io.sendOpen(inter)
        verify {
            client.openInterface(false, 10, 1, 100)
        }
    }

    @Test
    fun `Send permanent interface open`() {
        val inter: InterfaceDetail = mockk()
        every { player.gameFrame.resizable } returns false
        every { inter.id } returns 100
        every { inter.getParent(false) } returns "10"
        every { inter.getIndex(false) } returns 1
        every { inter.type } returns ""
        io.sendOpen(inter)
        verify {
            client.openInterface(true, 10, 1, 100)
        }
    }

    @Test
    fun `Send interface close`() {
        val inter: InterfaceDetail = mockk()
        every { player.gameFrame.resizable } returns true
        every { inter.id } returns 10
        every { inter.getIndex(true) } returns 1
        every { inter.getParent(true) } returns "100"
        io.sendClose(inter)
        verify {
            client.closeInterface(100, 1)
        }
    }

    @Test
    fun `Notify closed`() {
        val inter: InterfaceDetail = mockk()
        every { inter.id } returns 10
        every { inter.name } returns "interface_name"
        io.notifyClosed(inter)
        verify { player.events.emit(InterfaceClosed(10, "interface_name")) }
    }

    @Test
    fun `Notify opened`() {
        val inter: InterfaceDetail = mockk()
        every { inter.id } returns 10
        every { inter.name } returns "interface_name"
        io.notifyOpened(inter)
        verify { player.events.emit(InterfaceOpened(10, "interface_name")) }
    }

    @Test
    fun `Notify refreshed`() {
        val inter: InterfaceDetail = mockk()
        every { inter.id } returns 10
        every { inter.name } returns "interface_name"
        io.notifyRefreshed(inter)
        verify { player.events.emit(InterfaceRefreshed(10, "interface_name")) }
    }

    @Test
    fun `Send player head`() {
        val inter: InterfaceDetail = mockk()
        every { inter.id } returns 10
        val comp = InterfaceComponentDetail(100, "")
        comp.parent = inter.id
        io.sendPlayerHead(comp)
        verify {
            client.playerDialogueHead(10, 100)
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
            client.npcDialogueHead(100, 10, 123)
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
            client.animateInterface(100, 10, 123)
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
            client.interfaceText(100, 10, "words")
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
            client.interfaceVisibility(100, 10, hide = true)
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
            client.interfaceSprite(100, 10, 123)
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
            client.interfaceItem(100, 10, 123, 4)
        }
    }
}