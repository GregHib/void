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
        interfaces[4] = Interface(id = 4, data = InterfaceData(resizableParent = 10, resizableIndex = ROOT_INDEX))
        val result = lookup.get(4)
        assertEquals(10, result.getParent(gameframe.resizable))
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Interface without parent throws error`(resizable: Boolean) {
        interfaces[7] = Interface(id = 7, data = InterfaceData(fixedIndex = ROOT_INDEX, resizableIndex = ROOT_INDEX))
        gameframe.resizable = resizable
        assertThrows<Interface.InvalidInterfaceException> {
            manager.open(7)
        }
    }

    @Test
    fun `Can't open child if parent not open`() {
        interfaces[0] = Interface(id = 0, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(id = 1, data = InterfaceData(fixedParent = 0, fixedIndex = ROOT_INDEX))
        val result = manager.open(1)
        assertFalse(result)
    }

}
