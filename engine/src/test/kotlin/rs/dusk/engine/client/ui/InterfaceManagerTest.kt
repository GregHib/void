package rs.dusk.engine.client.ui

import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_INDEX
import rs.dusk.engine.client.ui.detail.InterfaceData
import rs.dusk.engine.client.ui.detail.InterfaceDetail
import rs.dusk.engine.client.ui.detail.InterfaceDetails

internal class InterfaceManagerTest : InterfaceTest() {

    @Test
    fun `Unknown interfaces throw exceptions`() {
        val name = "unknown"
        assertFalse(manager.contains(name))
        assertNull(manager.get(name))
        assertThrows<InterfaceDetails.IllegalNameException> {
            assertFalse(manager.close(name))
        }
        assertThrows<InterfaceDetails.IllegalNameException> {
            manager.open(name)
        }
        verify(exactly = 0) {
            io.sendClose(any())
            io.notifyClosed(any())
            io.sendOpen(any())
            io.notifyOpened(any())
        }
    }

    @Test
    fun `Unopened interface can't be interacted`() {
        val detail = InterfaceDetail(id = 0, type = "type", data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        val name = "zero"
        names[0] = name
        interfaces[name] = detail
        assertFalse(manager.contains(name))
        assertFalse(manager.close(name))
        assertFalse(manager.remove(name))
        assertNull(manager.get("type"))
        verify(exactly = 0) {
            io.sendClose(any())
            io.notifyClosed(any())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Invalid interface`(resizable: Boolean) {
        gameframe.resizable = resizable
        val parentless = InterfaceDetail(id = 0, data = InterfaceData(null, null, null, null))
        val name = "zero"
        names[0] = name
        interfaces[name] = parentless
        assertThrows<InterfaceDetail.InvalidInterfaceException> {
            manager.open(name)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Interface details lookup`(resizable: Boolean) {
        gameframe.resizable = resizable
        names[4] = "four"
        interfaces["four"] = InterfaceDetail(id = 4, data = InterfaceData(fixedParent = 8, fixedIndex = 9, resizableParent = 10, resizableIndex = 11))
        val result = lookup.get("four")
        assertEquals(if (resizable) 10 else 8, result.getParent(gameframe.resizable))
        assertEquals(if (resizable) 11 else 9, result.getIndex(gameframe.resizable))
    }
}
