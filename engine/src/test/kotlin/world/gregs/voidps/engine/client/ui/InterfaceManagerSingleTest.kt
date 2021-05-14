package world.gregs.voidps.engine.client.ui

import io.mockk.every
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.Interfaces.Companion.ROOT_ID
import world.gregs.voidps.engine.client.ui.Interfaces.Companion.ROOT_INDEX
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.network.encode.closeInterface
import world.gregs.voidps.network.encode.openInterface

internal class InterfaceManagerSingleTest : InterfaceTest() {

    private val name = "zero"

    @BeforeEach
    override fun setup() {
        super.setup()
        every { definitions.get(name) } returns InterfaceDefinition(extras = mapOf(
            "type" to "type",
            "parent_fixed" to ROOT_ID,
            "index_fixed" to ROOT_INDEX
        ))
        every { definitions.getId(name) } returns 1
        gameframe.resizable = false
    }

    @Test
    fun `Unopened doesn't exist`() {
        assertFalse(manager.contains(name))
        assertNull(manager.get("type"))
    }

    @Test
    fun `Opened contains with type`() {
        assertTrue(manager.open(name))
        assertTrue(manager.contains(name))
        assertEquals(name, manager.get("type"))

        verifyOrder {
            client.openInterface(true, 0, 0, 1)
            events.emit(InterfaceOpened(1, name))
        }
    }

    @Test
    fun `Reopen only refreshes`() {
        manager.open(name)

        assertFalse(manager.open(name))

        verifyOrder {
            client.openInterface(true, 0, 0,1)
            events.emit(InterfaceOpened(1, name))
            events.emit(InterfaceRefreshed(1, name))
        }
    }

    @Test
    fun `Close no longer contains`() {
        manager.open(name)

        assertTrue(manager.close(name))
        assertFalse(manager.contains(name))

        verifyOrder {
            client.closeInterface(0, 0)
            events.emit(InterfaceClosed(1, name))
        }
    }
}
