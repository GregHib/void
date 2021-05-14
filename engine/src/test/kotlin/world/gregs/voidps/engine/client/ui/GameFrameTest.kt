package world.gregs.voidps.engine.client.ui

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.Interfaces.Companion.ROOT_ID
import world.gregs.voidps.engine.client.ui.Interfaces.Companion.ROOT_INDEX
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerGameFrame
import world.gregs.voidps.engine.entity.character.player.setDisplayMode

internal class GameFrameTest : InterfaceTest() {

    private lateinit var player: Player

    @BeforeEach
    override fun setup() {
        super.setup()
        player = mockk(relaxed = true)
        every { player.gameFrame } returns gameframe
        every { player.interfaces } returns manager
        every { definitions.get("toplevel_full") } returns InterfaceDefinition(extras = mapOf("parent_resize" to ROOT_ID, "index_resize" to ROOT_INDEX))
        every { definitions.get("toplevel") } returns InterfaceDefinition(extras = mapOf("parent_fixed" to ROOT_ID, "index_fixed" to ROOT_INDEX))
    }

    @Test
    fun `Don't set top level size if full not open`() {
        val result = player.setDisplayMode(PlayerGameFrame.FIXED_SCREEN)
        assertFalse(result)
    }

    @Test
    fun `Don't set full if top level not open`() {
        val result = player.setDisplayMode(PlayerGameFrame.RESIZABLE_SCREEN)
        assertFalse(result)
    }

    @Test
    fun `Size set top level if full open`() {
        gameframe.resizable = true
        interfaces.add("toplevel_full")
        val result = player.setDisplayMode(PlayerGameFrame.FIXED_SCREEN)
        assertTrue(result)
        assertEquals(false, gameframe.resizable)
    }

    @Test
    fun `Size set full if top level open`() {
        interfaces.add("toplevel")
        val result = player.setDisplayMode(PlayerGameFrame.RESIZABLE_SCREEN)
        assertTrue(result)
        assertEquals(true, gameframe.resizable)
    }

    @Test
    fun `Fixed screen`() {
        assertFalse(gameframe.resizable)
    }
}
