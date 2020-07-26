package rs.dusk.engine.client.ui

import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_INDEX

internal class InterfaceRemoveTest : InterfaceTest() {

    @Test
    fun `Interface can't remove unopened interface`() {
        val id = 4
        interfaces[id] = Interface(id, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["fourth"] = 4
        val result = manager.remove(4)
        assertFalse(result)
        verify(exactly = 0) { io.sendClose(Interface(id, null)) }
    }

    @Test
    fun `Interface remove successful`() {
        val id = 4
        val data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX)
        interfaces[id] = Interface(id, data)
        names["fourth"] = 4
        manager.open("fourth")
        val result = manager.remove("fourth")
        assertTrue(result)
        verify { io.sendClose(Interface(id, data)) }
    }

}
