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
import world.gregs.voidps.network.login.protocol.encode.closeInterface
import world.gregs.voidps.network.login.protocol.encode.openInterface

internal class InterfacesTest : InterfaceTest() {

    @Test
    fun `Unknown interfaces throw exceptions`() {
        val name = "unknown"
        every { definitions.get(name) } returns InterfaceDefinition()
        assertFalse(interfaces.contains(name))
        assertNull(interfaces.get(name))
        assertFalse(interfaces.close(name))
        assertFalse(interfaces.open(name))
        verify(exactly = 0) {
            client.closeInterface(any())
            events.emit(ofType<InterfaceClosed>())
            client.openInterface(any(), any(), any())
            events.emit(ofType<InterfaceOpened>())
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
        every { definitions.get(name) } returns InterfaceDefinition(id = 0)
        assertFalse(interfaces.contains(name))
        assertFalse(interfaces.close(name))
        assertFalse(interfaces.remove(name))
        assertNull(interfaces.get("type"))
        verify(exactly = 0) {
            client.closeInterface(any())
            events.emit(ofType<InterfaceClosed>())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Invalid interface`(resizable: Boolean) {
        interfaces.resizable = resizable
        val name = "zero"
        every { definitions.get(name) } returns InterfaceDefinition()
        assertFalse(interfaces.open(name))
    }
}
