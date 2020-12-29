package rs.dusk.engine.client.ui

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.client.ui.detail.InterfaceComponentDetail
import rs.dusk.engine.client.ui.detail.InterfaceDetail
import rs.dusk.engine.client.ui.event.InterfaceClosed
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.ui.event.InterfaceRefreshed
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventBus
import rs.dusk.network.rs.codec.game.encode.*

internal class InterfaceIOTest {

    private lateinit var io: InterfaceIO
    private lateinit var player: Player
    private lateinit var bus: EventBus
    private lateinit var openEncoder: InterfaceOpenMessageEncoder
    private lateinit var updateEncoder: InterfaceUpdateMessageEncoder
    private lateinit var animationEncoder: InterfaceAnimationMessageEncoder
    private lateinit var closeEncoder: InterfaceCloseMessageEncoder
    private lateinit var playerHeadEncoder: InterfaceHeadPlayerMessageEncoder
    private lateinit var npcHeadEncoder: InterfaceHeadNPCMessageEncoder
    private lateinit var textEncoder: InterfaceTextMessageEncoder
    private lateinit var visibleEncoder: InterfaceVisibilityMessageEncoder
    private lateinit var spriteEncoder: InterfaceSpriteMessageEncoder
    private lateinit var itemEncoder: InterfaceItemMessageEncoder

    @BeforeEach
    fun setup() {
        player = mockk()
        bus = mockk()
        every { bus.emit(any<PlayerEvent>(), any()) } returns mockk()
        openEncoder = mockk(relaxed = true)
        updateEncoder = mockk(relaxed = true)
        animationEncoder = mockk(relaxed = true)
        closeEncoder = mockk(relaxed = true)
        playerHeadEncoder = mockk(relaxed = true)
        npcHeadEncoder = mockk(relaxed = true)
        textEncoder = mockk(relaxed = true)
        visibleEncoder = mockk(relaxed = true)
        spriteEncoder = mockk(relaxed = true)
        itemEncoder = mockk(relaxed = true)
        io = PlayerInterfaceIO(player, bus, openEncoder, updateEncoder, animationEncoder, closeEncoder, playerHeadEncoder, npcHeadEncoder, textEncoder, visibleEncoder, spriteEncoder, itemEncoder)
    }

    @Test
    fun `Send full screen interface open`() {
        val inter: InterfaceDetail = mockk()
        every { player.gameFrame.resizable } returns false
        every { inter.id } returns 1
        every { inter.getParent(false) } returns -1
        io.sendOpen(inter)
        verify {
            updateEncoder.encode(player, 1, 0)
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
            openEncoder.encode(player, false, 10, 1, 100)
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
            openEncoder.encode(player, true, 10, 1, 100)
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
            closeEncoder.encode(player, 100, 1)
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
            playerHeadEncoder.encode(player, 10, 100)
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
            npcHeadEncoder.encode(player, 100, 10, 123)
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
            animationEncoder.encode(player, 100, 10, 123)
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
            textEncoder.encode(player, 100, 10, "words")
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
            visibleEncoder.encode(player, 100, 10, hide = true)
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
            spriteEncoder.encode(player, 100, 10, 123)
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
            itemEncoder.encode(player, 100, 10, 123, 4)
        }
    }
}