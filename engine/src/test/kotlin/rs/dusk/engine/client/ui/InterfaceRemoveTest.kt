package rs.dusk.engine.client.ui

import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_INDEX
import rs.dusk.engine.client.ui.detail.InterfaceData
import rs.dusk.engine.client.ui.detail.InterfaceDetail

internal class InterfaceRemoveTest : InterfaceTest() {

    @Test
    fun `Interface can't remove unopened interface`() {
        val id = 4
        interfaces[id] = InterfaceDetail(id = id, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        val result = manager.remove(4)
        assertFalse(result)
        verify(exactly = 0) {
            val inter = InterfaceDetail(id = id)
            io.sendClose(inter)
            io.notifyClosed(inter)
        }
    }

    @Test
    fun `Interface remove successful`() {
        val id = 4
        val data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX)
        interfaces[id] = InterfaceDetail(id = id, data = data)
        manager.open(4)
        val result = manager.remove(4)
        assertTrue(result)
        verifyOrder {
            val inter = InterfaceDetail(id = id, data = data)
            io.sendClose(inter)
            io.notifyClosed(inter)
        }
    }

}
