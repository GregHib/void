package rs.dusk.engine.client.ui

import io.mockk.verify
import io.mockk.verifyOrder
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
    fun `Open contains, close doesn't, open again refreshes`() {
        val detail = InterfaceDetail(id = 0, type = "type", data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        val name = "zero"
        names[name] = 0
        interfaces[0] = detail

        // Doesn't contain unopened
        assertFalse(manager.contains(name))
        assertNull(manager.get("type"))

        // Opened contains and get by type
        assertTrue(manager.open(name))
        assertTrue(manager.contains(name))
        assertEquals(0, manager.get("type"))

        // Can't opened already opened interface (refreshes instead)
        assertFalse(manager.open(name))

        // Close doesn't contain
        assertTrue(manager.close(name))
        assertFalse(manager.contains(name))

        verifyOrder {
            io.sendOpen(detail)
            io.notifyOpened(detail)

            io.notifyRefreshed(detail)

            io.sendClose(any())
            io.notifyClosed(any())
        }
    }

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
    fun `Multiple children`() {
        val detail = InterfaceDetail(id = 0, data = InterfaceData(fixedParent = 1, fixedIndex = ROOT_INDEX))
        val detail1 = InterfaceDetail(id = 1, data = InterfaceData(fixedParent = 2, fixedIndex = ROOT_INDEX))
        val detail2 = InterfaceDetail(id = 2, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        val zero = "zero"
        val one = "one"
        val two = "two"
        names[zero] = 0
        names[one] = 1
        names[two] = 2
        interfaces[0] = detail
        interfaces[1] = detail1
        interfaces[2] = detail2

        // Can't open child if parent isn't open
        assertFalse(manager.open(one))

        assertTrue(manager.open(two))
        assertTrue(manager.open(one))
        assertTrue(manager.open(zero))

        // Remove doesn't close children
        assertTrue(manager.remove(two))

        assertFalse(manager.contains(two))
        assertTrue(manager.contains(one))
        assertTrue(manager.contains(zero))

        // Close does remove children
        assertTrue(manager.open(two))
        assertTrue(manager.close(two))

        assertFalse(manager.contains(two))
        assertFalse(manager.contains(one))
        assertFalse(manager.contains(zero))

        verifyOrder {
            io.sendOpen(detail2)
            io.notifyOpened(detail2)
            io.sendOpen(detail1)
            io.notifyOpened(detail1)
            io.sendOpen(detail)
            io.notifyOpened(detail)

            io.sendClose(detail2)
            io.notifyClosed(detail2)

            io.sendOpen(detail2)
            io.notifyOpened(detail2)

            io.sendClose(detail2)
            io.notifyClosed(detail2)
            io.sendClose(detail1)
            io.notifyClosed(detail1)
            io.sendClose(detail)
            io.notifyClosed(detail)
        }
    }

    @Test
    fun `Unopened interface can't be interacted`() {
        val detail = InterfaceDetail(id = 0, type = "type", data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        val name = "zero"
        names[name] = 0
        interfaces[0] = detail
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
        names[name] = 0
        interfaces[0] = parentless
        assertThrows<InterfaceDetail.InvalidInterfaceException> {
            manager.open(name)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Interface details lookup`(resizable: Boolean) {
        gameframe.resizable = resizable
        interfaces[4] = InterfaceDetail(id = 4, data = InterfaceData(fixedParent = 8, fixedIndex = 9, resizableParent = 10, resizableIndex = 11))
        val result = lookup.get(4)
        assertEquals(if (resizable) 10 else 8, result.getParent(gameframe.resizable))
        assertEquals(if (resizable) 11 else 9, result.getIndex(gameframe.resizable))
    }
}
