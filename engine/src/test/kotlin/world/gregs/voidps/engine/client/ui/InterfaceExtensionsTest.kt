package world.gregs.voidps.engine.client.ui

import io.mockk.every
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.Interfaces.Companion.ROOT_ID
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions

internal class InterfaceExtensionsTest : InterfaceTest() {

    val name = "interface_name"

    @BeforeEach
    override fun setup() {
        super.setup()
        every { player.interfaces } returns interfaces
        InterfaceDefinitions.set(arrayOf(InterfaceDefinition(id = 0)), mapOf(name to 0), emptyMap())
    }

    @Test
    fun `Open by name`() {
        assertTrue(player.open(name))
        verify { interfaces.open(name) }
        verify(exactly = 0) { interfaces.close(any<String>()) }
    }

    @Test
    fun `Interface already open with same type is closed first`() {
        InterfaceDefinitions.set(arrayOf(InterfaceDefinition(id = -1), InterfaceDefinition(id = 0, type = "interface_type"), InterfaceDefinition(id = 1, type = "interface_type")), mapOf(ROOT_ID to 0, "first" to 1, "second" to 2), emptyMap())
        interfaces.open("first")
        val result = player.open("second")
        verifyOrder {
            interfaces.close("first")
            interfaces.open("second")
        }
        assertTrue(result)
    }

    @Test
    fun `Interface name is open`() {
        val result = player.hasOpen(name)
        verify { interfaces.contains(name) }
        assertFalse(result)
    }

    @Test
    fun `Has interface type open`() {
        val result = player.hasTypeOpen("interface_type")
        verify { interfaces.get("interface_type") }
        assertFalse(result)
    }

    @Test
    fun `Close interface name`() {
        assertFalse(player.close(name))
        verify { interfaces.close(name) }
    }

    @Test
    fun `Close interface type`() {
        InterfaceDefinitions.set(arrayOf(InterfaceDefinition(id = -1), InterfaceDefinition()), mapOf(ROOT_ID to 0, "second" to 1), emptyMap())
        every { interfaces.get("interface_type") } returns "second"
        val result = player.closeType("interface_type")
        verifyOrder {
            interfaces.get("interface_type")
            interfaces.close("second")
        }
        assertFalse(result)
    }

    @Test
    fun `Close children`() {
        assertFalse(player.closeChildren(name))
        verify { interfaces.closeChildren(name) }
    }
}
