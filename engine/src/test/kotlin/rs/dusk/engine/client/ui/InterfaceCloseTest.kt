package rs.dusk.engine.client.ui

import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_INDEX
import rs.dusk.engine.client.ui.detail.InterfaceData
import rs.dusk.engine.client.ui.detail.InterfaceDetail
import rs.dusk.engine.client.ui.detail.InterfaceDetails

internal class InterfaceCloseTest : InterfaceTest() {

    @Test
    fun `Interface close is successful`() {
        val id = 4
        interfaces[id] = InterfaceDetail(id = id, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        manager.open(id)
        val result = manager.close(id)
        assertTrue(result)
    }

    @Test
    fun `Interface close name`() {
        interfaces[4] = InterfaceDetail(id = 4, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["fourth"] = 4
        manager.open("fourth")
        val result = manager.close("fourth")
        assertTrue(result)
    }

    @Test
    fun `Interface close unsuccessful if not open`() {
        val result = manager.close(4)
        assertFalse(result)
    }

    @Test
    fun `Interface close sends update`() {
        val id = 4
        val data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX)
        interfaces[id] = InterfaceDetail(id = id, data = data)
        manager.open(id)
        manager.close(id)
        verifyOrder {
            val inter = InterfaceDetail(id = id, data = data)
            io.sendClose(inter)
            io.notifyClosed(inter)
        }
    }

    @Test
    fun `Close with unknown name throws exception`() {
        assertThrows<InterfaceDetails.IllegalNameException> {
            manager.close("unknown")
        }
    }

    @Test
    fun `Unopened interface close doesn't send update`() {
        manager.close(4)
        verify(exactly = 0) {
            val inter = InterfaceDetail(4)
            io.sendClose(inter)
            io.notifyClosed(inter)
        }
    }

    @Test
    fun `Close removes children`() {
        interfaces[0] = InterfaceDetail(id = 0, data = InterfaceData(fixedParent = 1, fixedIndex = ROOT_INDEX))
        interfaces[1] = InterfaceDetail(id = 1, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        manager.open(1)
        manager.open(0)
        manager.close(1)
        assertFalse(manager.contains(0))
    }

    @Test
    fun `Close removes children's children`() {
        interfaces[0] = InterfaceDetail(id = 0, data = InterfaceData(fixedParent = 1, fixedIndex = ROOT_INDEX))
        interfaces[1] = InterfaceDetail(id = 1, data = InterfaceData(fixedParent = 2, fixedIndex = ROOT_INDEX))
        interfaces[2] = InterfaceDetail(id = 2, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        manager.open(2)
        manager.open(1)
        manager.open(0)
        manager.close(2)
        assertFalse(manager.contains(1))
        assertFalse(manager.contains(0))
    }
}
