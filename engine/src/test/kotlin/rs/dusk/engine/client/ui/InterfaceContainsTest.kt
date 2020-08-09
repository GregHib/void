package rs.dusk.engine.client.ui

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_INDEX
import rs.dusk.engine.client.ui.detail.InterfaceData
import rs.dusk.engine.client.ui.detail.InterfaceDetail

internal class InterfaceContainsTest : InterfaceTest() {

    @Test
    fun `Contains with unknown name returns false`() {
        val result = manager.contains("unknown")
        assertFalse(result)
    }

    @Test
    fun `Interfaces doesn't contain name`() {
        assertFalse(manager.contains(0))
    }

    @Test
    fun `Interfaces doesn't contain id`() {
        assertFalse(manager.contains(0))
    }

    @Test
    fun `Interfaces contains name`() {
        interfaces[0] = InterfaceDetail(id = 0, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["zero"] = 0
        manager.open("zero")
        assertTrue(manager.contains("zero"))
    }

    @Test
    fun `Interfaces contains id`() {
        interfaces[0] = InterfaceDetail(id = 0, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        manager.open(0)
        assertTrue(manager.contains(0))
    }

    @Test
    fun `Interfaces doesn't contain unopened id`() {
        interfaces[0] = InterfaceDetail(id = 0, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        manager.open(0)
        assertFalse(manager.contains(1))
    }

    @Test
    fun `Interfaces contains first opened id`() {
        val id = 0
        interfaces[id] = InterfaceDetail(id = id, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        interfaces[1] = InterfaceDetail(id = 1, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        manager.open(0)
        manager.open(1)
        assertTrue(manager.contains(0))
    }

    @Test
    fun `Close no longer contains closed id`() {
        interfaces[0] = InterfaceDetail(id = 0, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        interfaces[1] = InterfaceDetail(id = 1, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["zero"] = 0
        names["first"] = 1
        manager.open("zero")
        manager.close("zero")
        manager.open("first")
        assertFalse(manager.contains("zero"))
    }
}
