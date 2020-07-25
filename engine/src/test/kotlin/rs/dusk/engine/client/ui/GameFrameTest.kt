package rs.dusk.engine.client.ui

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_INDEX
import rs.dusk.engine.model.entity.character.player.Player

internal class GameFrameTest {

    private lateinit var manager: Interfaces
    private lateinit var io: InterfaceIO
    private lateinit var interfaces: MutableMap<Int, Interface>
    private lateinit var lookup: InterfacesLookup
    private lateinit var player: Player
    private lateinit var gameframe: GameFrame
    private lateinit var names: MutableMap<String, Int>

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        io = mockk(relaxed = true)
        interfaces = mutableMapOf()
        names = mutableMapOf()
        lookup = InterfacesLookup(interfaces, names)
        gameframe = GameFrame()
        manager = Interfaces(io, lookup, gameframe)
        every { player.gameframe } returns gameframe
        every { player.interfaces } returns manager
    }

    @Test
    fun `Don't set top level size if full not open`() {
        val result = player.setDisplayMode(GameFrame.FIXED_SCREEN)
        assertFalse(result)
    }

    @Test
    fun `Don't set full if top level not open`() {
        val result = player.setDisplayMode(GameFrame.RESIZABLE_SCREEN)
        assertFalse(result)
    }

    @Test
    fun `Size set top level if full open`() {
        interfaces[123] = Interface(123, InterfaceData(resizableParent = ROOT_ID, resizableIndex = ROOT_INDEX))
        interfaces[124] = Interface(124, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["toplevel_full"] = 123
        names["toplevel"] = 124
        gameframe.resizable = true
        manager.open("toplevel_full")
        val result = player.setDisplayMode(GameFrame.FIXED_SCREEN)
        assertTrue(result)
        assertEquals(false, gameframe.resizable)
    }

    @Test
    fun `Size set full if top level open`() {
        interfaces[123] = Interface(123, InterfaceData(resizableParent = ROOT_ID, resizableIndex = ROOT_INDEX))
        interfaces[124] = Interface(124, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["toplevel_full"] = 123
        names["toplevel"] = 124
        manager.open("toplevel")
        val result = player.setDisplayMode(GameFrame.RESIZABLE_SCREEN)
        assertTrue(result)
        assertEquals(true, gameframe.resizable)
    }
}
