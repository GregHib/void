package world.gregs.voidps.engine.client.ui

import io.mockk.every
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.network.login.protocol.encode.closeInterface

internal class InterfacesSingleTest : InterfaceTest() {

    private val name = "zero"

    @BeforeEach
    override fun setup() {
        super.setup()
        InterfaceDefinitions.set(arrayOf(InterfaceDefinition(id = 1, stringId = "1", type = "type")), mapOf(name to 0), emptyMap())
        interfaces.resizable = false
    }

    @Test
    fun `Unopened doesn't exist`() {
        assertFalse(interfaces.contains(name))
        assertNull(interfaces.get("type"))
    }

    @Test
    fun `Opened contains with type`() {
        open["type"] = name
        assertTrue(interfaces.contains(name))
        assertEquals(name, interfaces.get("type"))
    }

    @Test
    fun `Reopen only refreshes`() {
        open["type"] = name

        assertFalse(interfaces.open(name))

        verify {
            InterfaceApi.refresh(player, name)
        }
    }

    @Test
    fun `Close no longer contains`() {
        InterfaceDefinitions.set(arrayOf(InterfaceDefinition(id = 1, stringId = "1", type = "type", resizable = 2, fixed = 2)), mapOf(name to 0), emptyMap())
        open["type"] = name

        assertTrue(interfaces.close(name))
        assertFalse(interfaces.contains(name))

        verifyOrder {
            client.closeInterface(2)
            InterfaceApi.close(player, name)
        }
    }
}
