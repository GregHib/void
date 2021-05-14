package world.gregs.voidps.engine.client.ui

import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.Interfaces.Companion.ROOT_ID
import world.gregs.voidps.engine.client.ui.Interfaces.Companion.ROOT_INDEX
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.network.encode.closeInterface
import world.gregs.voidps.network.encode.openInterface

internal class InterfaceManagerTest : InterfaceTest() {

    @Test
    fun `Unknown interfaces throw exceptions`() {
        val name = "unknown"
        every { definitions.get(name) } returns InterfaceDefinition()
        assertFalse(manager.contains(name))
        assertNull(manager.get(name))
        assertFalse(manager.close(name))
        assertFalse(manager.open(name))
        verify(exactly = 0) {
            client.closeInterface(any(), any())
            events.emit(any<InterfaceClosed>())
            client.openInterface(any(), any(), any(), any())
            events.emit(any<InterfaceOpened>())
        }
    }

    @Test
    fun `Unopened interface can't be interacted`() {
        val name = "zero"
        every { definitions.get(name) } returns InterfaceDefinition(
            extras = mapOf(
                "type" to "type",
                "parent_fixed" to ROOT_ID,
                "index_fixed" to ROOT_INDEX
            )
        )
        every { definitions.getId(name) } returns 0
        assertFalse(manager.contains(name))
        assertFalse(manager.close(name))
        assertFalse(manager.remove(name))
        assertNull(manager.get("type"))
        verify(exactly = 0) {
            client.closeInterface(any(), any())
            events.emit(any<InterfaceClosed>())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Invalid interface`(resizable: Boolean) {
        gameframe.resizable = resizable
        val name = "zero"
        every { definitions.get(name) } returns InterfaceDefinition()
        assertFalse(manager.open(name))
    }
}
