package rs.dusk.engine.client.ui

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_INDEX

internal class InterfaceContainsTest : InterfaceTest() {

    @Test
    fun `Contains with unknown name returns false`() {
        val result = manager.contains("unknown")
        assertFalse(result)
    }

    @Test
    fun `Interfaces doesn't contain name`() {
        names["zero"] = 0
        assertFalse(manager.contains("zero"))
    }

    @Test
    fun `Interfaces doesn't contain id`() {
        assertFalse(manager.contains(0))
    }

    @Test
    fun `Interfaces contains name`() {
        interfaces[0] = Interface(id = 0, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["zero"] = 0
        manager.open("zero")
        assertTrue(manager.contains("zero"))
    }

    @Test
    fun `Interfaces contains id`() {
        interfaces[0] = Interface(id = 0, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["zero"] = 0
        manager.open("zero")
        assertTrue(manager.contains(0))
    }

    @Test
    fun `Interfaces doesn't contain unopened id`() {
        interfaces[0] = Interface(id = 0, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["zero"] = 0
        manager.open("zero")
        assertFalse(manager.contains("first"))
    }

    @Test
    fun `Interfaces contains first opened id`() {
        val id = 0
        interfaces[id] = Interface(id = id, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(id = 1, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["zero"] = 0
        names["first"] = 1
        manager.open("zero")
        manager.open("first")
        assertTrue(manager.contains("zero"))
    }

    @Test
    fun `Close no longer contains closed id`() {
        interfaces[0] = Interface(id = 0, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(id = 1, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["zero"] = 0
        names["first"] = 1
        manager.open("zero")
        manager.close("zero")
        manager.open("first")
        assertFalse(manager.contains("zero"))
    }
}
