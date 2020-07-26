package rs.dusk.engine.client.ui

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_INDEX

internal class InterfaceCloseChildrenTest : InterfaceTest() {

    @Test
    fun `Close children`() {
        interfaces[0] = Interface(id = 0, data = InterfaceData(fixedParent = 1, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(id = 1, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["parent"] = 1
        names["child"] = 0
        manager.open("parent")
        manager.open("child")
        manager.closeChildren(1)
        assertTrue(manager.contains("parent"))
        assertFalse(manager.contains("child"))
    }

    @Test
    fun `Close children's children`() {
        interfaces[0] = Interface(id = 0, data = InterfaceData(fixedParent = 1, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(id = 1, data = InterfaceData(fixedParent = 2, fixedIndex = ROOT_INDEX))
        interfaces[2] = Interface(id = 2, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["parent"] = 2
        names["child"] = 1
        names["subchild"] = 0
        manager.open("parent")
        manager.open("child")
        manager.open("subchild")
        manager.closeChildren("parent")
        assertTrue(manager.contains("parent"))
        assertFalse(manager.contains("child"))
        assertFalse(manager.contains("subchild"))
    }

}
