package rs.dusk.engine.client.ui

import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_INDEX

internal class InterfaceCloseTest : InterfaceTest() {

    @Test
    fun `Interface close is successful`() {
        val id = 4
        interfaces[id] = Interface(id = id, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["fourth"] = 4
        manager.open("fourth")
        val result = manager.close(4)
        assertTrue(result)
    }

    @Test
    fun `Interface close unsuccessful if not open`() {
        names["fourth"] = 4
        val result = manager.close("fourth")
        assertFalse(result)
    }

    @Test
    fun `Interface close sends update`() {
        val id = 4
        val data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX)
        interfaces[id] = Interface(id = id, data = data)
        names["fourth"] = 4
        manager.open("fourth")
        manager.close("fourth")
        verify { io.sendClose(Interface(id = id, data = data)) }
    }

    @Test
    fun `Close with unknown name throws exception`() {
        assertThrows<InterfacesLookup.IllegalNameException> {
            manager.close("unknown")
        }
    }

    @Test
    fun `Unopened interface close doesn't send update`() {
        val id = 4
        names["fourth"] = 4
        manager.close("fourth")
        verify(exactly = 0) { io.sendClose(Interface(id)) }
    }

    @Test
    fun `Close removes children`() {
        interfaces[0] = Interface(id = 0, data = InterfaceData(fixedParent = 1, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(id = 1, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["parent"] = 1
        names["child"] = 0
        manager.open("parent")
        manager.open("child")
        manager.close("parent")
        assertFalse(manager.contains("child"))
    }

    @Test
    fun `Close removes children's children`() {
        interfaces[0] = Interface(id = 0, data = InterfaceData(fixedParent = 1, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(id = 1, data = InterfaceData(fixedParent = 2, fixedIndex = ROOT_INDEX))
        interfaces[2] = Interface(id = 2, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["parent"] = 2
        names["child"] = 1
        names["subchild"] = 0
        manager.open("parent")
        manager.open("child")
        manager.open("subchild")
        manager.close("parent")
        assertFalse(manager.contains("child"))
        assertFalse(manager.contains("subchild"))
    }
}
