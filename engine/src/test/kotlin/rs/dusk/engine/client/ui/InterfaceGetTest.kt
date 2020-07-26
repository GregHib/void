package rs.dusk.engine.client.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_INDEX

internal class InterfaceGetTest : InterfaceTest() {

    @Test
    fun `Get open interface by type`() {
        interfaces[4] = Interface(id = 4, type = "interface_type", data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        manager.open(4)
        val result = manager.get("interface_type")
        assertEquals(4, result)
    }

    @Test
    fun `Get unopened interface by type`() {
        interfaces[4] = Interface(id = 4, type = "interface_type", data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        val result = manager.get("interface_type")
        assertNull(result)
    }

    @Test
    fun `Can't get interface with no type`() {
        interfaces[4] = Interface(id = 4, type = null, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        manager.open(4)
        val result = manager.get("interface_type")
        assertNull(result)
    }

    @Test
    fun `Get missing type`() {
        val result = manager.get("unknown")
        assertNull(result)
    }

}
