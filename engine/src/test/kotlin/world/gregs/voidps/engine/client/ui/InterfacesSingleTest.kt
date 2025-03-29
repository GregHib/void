package world.gregs.voidps.engine.client.ui

import io.mockk.every
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.Interfaces.Companion.ROOT_ID
import world.gregs.voidps.engine.client.ui.Interfaces.Companion.ROOT_INDEX
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.network.login.protocol.encode.closeInterface

internal class InterfacesSingleTest : InterfaceTest() {

    private val name = "zero"

    @BeforeEach
    override fun setup() {
        super.setup()
        every { definitions.get(name) } returns InterfaceDefinition(id = 1, stringId = "1", extras = mapOf(
            "type" to "type",
            "parent_fixed" to ROOT_ID,
            "index_fixed" to ROOT_INDEX
        ))
        interfaces.resizable = false
    }

    @Test
    fun `Unopened doesn't exist`() {
        assertFalse(interfaces.contains(name))
        assertNull(interfaces.get("type"))
    }

    @Test
    fun `Opened contains with type`() {
        open.add(name)
        assertTrue(interfaces.contains(name))
        assertEquals(name, interfaces.get("type"))
    }

    @Test
    fun `Reopen only refreshes`() {
        open.add(name)

        assertFalse(interfaces.open(name))

        verify {
            events.emit(InterfaceRefreshed(name))
        }
    }

    @Test
    fun `Close no longer contains`() {
        every { definitions.get("root").id } returns 2
        open.add(name)

        assertTrue(interfaces.close(name))
        assertFalse(interfaces.contains(name))

        verifyOrder {
            client.closeInterface(InterfaceDefinition.pack(2, 0))
            events.emit(InterfaceClosed(name))
        }
    }
}
