package world.gregs.voidps.engine.client.ui

import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.network.login.protocol.encode.closeInterface
import world.gregs.voidps.network.login.protocol.encode.openInterface

internal class InterfacesTest : InterfaceTest() {

    @Test
    fun `Unknown interfaces throw exceptions`() {
        val name = "unknown"
        InterfaceDefinitions.clear()
        assertFalse(interfaces.contains(name))
        assertNull(interfaces.get(name))
        assertFalse(interfaces.close(name))
        assertFalse(interfaces.open(name))
        verify(exactly = 0) {
            client.closeInterface(any())
            InterfaceApi.close(any(), any())
            client.openInterface(any(), any(), any())
            InterfaceApi.open(any(), any())
        }
    }

    @Test
    fun `Unopened interface can't be interacted`() {
        val name = "zero"
        InterfaceDefinitions.set(arrayOf(InterfaceDefinition(type = "type")), mapOf(name to 0), emptyMap())
        assertFalse(interfaces.contains(name))
        assertFalse(interfaces.close(name))
        assertFalse(interfaces.remove(name))
        assertNull(interfaces.get("type"))
        verify(exactly = 0) {
            client.closeInterface(any())
            InterfaceApi.close(any(), any())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Invalid interface`(resizable: Boolean) {
        interfaces.resizable = resizable
        val name = "zero"
        InterfaceDefinitions.clear()
        assertFalse(interfaces.open(name))
    }
}
