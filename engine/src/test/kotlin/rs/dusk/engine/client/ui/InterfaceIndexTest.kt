package rs.dusk.engine.client.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID

internal class InterfaceIndexTest : InterfaceTest() {

    @Test
    fun `Fixed index`() {
        interfaces[1] = Interface(id = 1, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = 10))
        names["first"] = 1
        val result = lookup.get("first")
        assertEquals(10, result.getIndex(gameframe.resizable))
    }

    @Test
    fun `Resizable has sends different index`() {
        interfaces[1] = Interface(id = 1, data = InterfaceData(resizableIndex = 12, resizableParent = ROOT_ID))
        gameframe.resizable = true
        names["first"] = 1
        val result = lookup.get("first")
        assertEquals(12, result.getIndex(gameframe.resizable))
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Interface without index throws error`(resizable: Boolean) {
        interfaces[7] = Interface(id = 7, data = InterfaceData(fixedParent = ROOT_ID))
        names["seventh"] = 7
        gameframe.resizable = resizable
        assertThrows<Interface.InvalidInterfaceException> {
            manager.open("seventh")
        }
    }
}
