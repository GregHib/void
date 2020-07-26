package rs.dusk.engine.client.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_INDEX

internal class InterfaceParentTest : InterfaceTest() {

    @Test
    fun `Resizable has different parent`() {
        gameframe.resizable = true
        interfaces[4] = Interface(4, InterfaceData(resizableParent = 10, resizableIndex = ROOT_INDEX))
        names["fourth"] = 4
        val result = lookup.get("fourth")
        assertEquals(10, result.getParent(gameframe.resizable))
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Interface without parent throws error`(resizable: Boolean) {
        interfaces[7] = Interface(7, InterfaceData(fixedIndex = ROOT_INDEX, resizableIndex = ROOT_INDEX))
        names["seventh"] = 7
        gameframe.resizable = resizable
        assertThrows<Interface.InvalidInterfaceException> {
            manager.open("seventh")
        }
    }

    @Test
    fun `Can't open child if parent not open`() {
        interfaces[0] = Interface(0, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(1, InterfaceData(fixedParent = 0, fixedIndex = ROOT_INDEX))
        names["parent"] = 0
        names["child"] = 1
        val result = manager.open("child")
        assertFalse(result)
    }

}
