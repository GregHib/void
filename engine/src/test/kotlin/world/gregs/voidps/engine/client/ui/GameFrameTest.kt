package world.gregs.voidps.engine.client.ui

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions

internal class GameFrameTest : InterfaceTest() {

    @BeforeEach
    override fun setup() {
        super.setup()
        interfaces = Interfaces(player, open)
        InterfaceDefinitions.set(arrayOf(
            InterfaceDefinition(),
            InterfaceDefinition(id = -1, type = "root"),
            InterfaceDefinition(type = "root"),
        ), mapOf(
            "" to 0,
            "toplevel_full" to 1,
            "toplevel" to 2,
        ), mapOf()
        )
    }

    @Test
    fun `Don't set top level size if full not open`() {
        assertFalse(interfaces.setDisplayMode(Interfaces.FIXED_SCREEN))
    }

    @Test
    fun `Don't set full if top level not open`() {
        assertFalse(interfaces.setDisplayMode(Interfaces.RESIZABLE_SCREEN))
    }

    @Test
    fun `Size set top level if full open`() {
        interfaces.resizable = true
        open["root"] = "toplevel_full"
        assertTrue(interfaces.setDisplayMode(Interfaces.FIXED_SCREEN))
        assertEquals(false, interfaces.resizable)
    }

    @Test
    fun `Size set full if top level open`() {
        open["root"] = "toplevel"
        assertTrue(interfaces.setDisplayMode(Interfaces.RESIZABLE_SCREEN))
        assertEquals(true, interfaces.resizable)
    }

    @Test
    fun `Fixed screen`() {
        assertFalse(interfaces.resizable)
    }
}
