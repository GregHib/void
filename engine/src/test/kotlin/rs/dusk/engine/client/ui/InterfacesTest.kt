package rs.dusk.engine.client.ui

import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_INDEX

internal class InterfacesTest {

    private lateinit var manager: Interfaces
    private lateinit var io: InterfaceIO
    private lateinit var interfaces: MutableMap<Int, Interface>
    private lateinit var lookup: InterfacesLookup
    private lateinit var gameframe: GameFrame
    private lateinit var names: MutableMap<String, Int>

    @BeforeEach
    fun setup() {
        io = spyk(object : InterfaceIO {
            override fun sendOpen(inter: Interface) {
                inter.getParent(gameframe.resizable)
                inter.getIndex(gameframe.resizable)
            }

            override fun sendClose(inter: Interface) {
            }
        })
        interfaces = mutableMapOf()
        names = mutableMapOf()
        lookup = InterfacesLookup(interfaces, names)
        gameframe = GameFrame()
        manager = Interfaces(io, lookup, gameframe)
    }

    @Test
    fun `Open with name is successful`() {
        interfaces[0] = Interface(0, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["zero"] = 0
        val result = manager.open("zero")
        assertTrue(result)
    }

    @Test
    fun `Open with id is successful`() {
        interfaces[0] = Interface(0, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        val result = manager.open(0)
        assertTrue(result)
    }

    @Test
    fun `Open with unknown name throws exception`() {
        assertThrows<InterfacesLookup.IllegalNameException> {
            manager.open("unknown")
        }
    }

    @Test
    fun `Close with unknown name throws exception`() {
        assertThrows<InterfacesLookup.IllegalNameException> {
            manager.close("unknown")
        }
    }

    @Test
    fun `Contains with unknown name returns false`() {
        val result = manager.contains("unknown")
        assertFalse(result)
    }

    @Test
    fun `Open isn't successful if already open`() {
        val id = 0
        interfaces[id] = Interface(id, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["zero"] = 0
        manager.open("zero")
        val result = manager.open(0)
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
        interfaces[0] = Interface(0, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["zero"] = 0
        manager.open("zero")
        assertTrue(manager.contains("zero"))
    }

    @Test
    fun `Interfaces contains id`() {
        interfaces[0] = Interface(0, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["zero"] = 0
        manager.open("zero")
        assertTrue(manager.contains(0))
    }

    @Test
    fun `Interfaces doesn't contain unopened id`() {
        interfaces[0] = Interface(0, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["zero"] = 0
        manager.open("zero")
        assertFalse(manager.contains("first"))
    }

    @Test
    fun `Interfaces contains first opened id`() {
        val id = 0
        interfaces[id] = Interface(id, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(1, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["zero"] = 0
        names["first"] = 1
        manager.open("zero")
        manager.open("first")
        assertTrue(manager.contains("zero"))
    }

    @Test
    fun `Close no longer contains closed id`() {
        interfaces[0] = Interface(0, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(1, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["zero"] = 0
        names["first"] = 1
        manager.open("zero")
        manager.close("zero")
        manager.open("first")
        assertFalse(manager.contains("zero"))
    }

    @Test
    fun `Interface open sends update`() {
        val id = 4
        val data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX)
        interfaces[id] = Interface(id, data)
        names["fourth"] = 4
        manager.open("fourth")
        verify { io.sendOpen(Interface(id, data)) }
    }

    @Test
    fun `Interface open sends parent`() {
        interfaces[4] = Interface(4, InterfaceData(fixedParent = 8, fixedIndex = ROOT_INDEX))
        names["fourth"] = 4
        val result = lookup.get("fourth")
        assertEquals(8, result.getParent(gameframe.resizable))
    }

    @Test
    fun `Resizable has different parent`() {
        gameframe.resizable = true
        interfaces[4] = Interface(4, InterfaceData(resizableParent = 10, resizableIndex = ROOT_INDEX))
        names["fourth"] = 4
        val result = lookup.get("fourth")
        assertEquals(10, result.getParent(gameframe.resizable))
    }

    @Test
    fun `Fixed index`() {
        interfaces[1] = Interface(1, InterfaceData(fixedParent = ROOT_ID, fixedIndex = 10))
        names["first"] = 1
        val result = lookup.get("first")
        assertEquals(10, result.getIndex(gameframe.resizable))
    }

    @Test
    fun `Resizable has sends different index`() {
        interfaces[1] = Interface(1, InterfaceData(resizableIndex = 12, resizableParent = ROOT_ID))
        gameframe.resizable = true
        names["first"] = 1
        val result = lookup.get("first")
        assertEquals(12, result.getIndex(gameframe.resizable))
    }

    @Test
    fun `Interface open not sent if already open`() {
        val id = 4
        val data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX)
        interfaces[id] = Interface(id, data)
        names["fourth"] = 4
        manager.open("fourth")
        manager.open("fourth")
        verify(exactly = 1) { io.sendOpen(Interface(id, data)) }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Interface without index throws error`(resizable: Boolean) {
        interfaces[7] = Interface(7, InterfaceData(fixedParent = ROOT_ID))
        names["seventh"] = 7
        gameframe.resizable = resizable
        assertThrows<Interface.InvalidInterfaceException> {
            manager.open("seventh")
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Interface without parent throws error`(resizable: Boolean) {
        interfaces[7] = Interface(7, InterfaceData(fixedIndex = ROOT_INDEX, resizableIndex = ROOT_INDEX))
        names["seventh"] = 7
        gameframe.resizable = resizable
        assertThrows<Interface.InvalidInterfaceException> {
            manager.open("seventh")
        }
    }

    @Test
    fun `Interface without info throws error`() {
        names["one_hundred"] = 100
        assertThrows<Interface.InvalidInterfaceException> {
            manager.open("one_hundred")
        }
    }

    @Test
    fun `Interface close is successful`() {
        val id = 4
        interfaces[id] = Interface(id, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
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
        interfaces[id] = Interface(id, data)
        names["fourth"] = 4
        manager.open("fourth")
        manager.close("fourth")
        verify { io.sendClose(Interface(id, data)) }
    }

    @Test
    fun `Unopened interface close doesn't send update`() {
        val id = 4
        names["fourth"] = 4
        manager.close("fourth")
        verify(exactly = 0) { io.sendClose(Interface(id, null)) }
    }

    @Test
    fun `Can't open child if parent not open`() {
        interfaces[0] = Interface(0, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(1, InterfaceData(fixedParent = 0, fixedIndex = ROOT_INDEX))
        names["parent"] = 0
        names["child"] = 1
        val result = manager.open("child")
        assertFalse(result)
    }

    @Test
    fun `Close removes children`() {
        interfaces[0] = Interface(0, InterfaceData(fixedParent = 1, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(1, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names["parent"] = 1
        names["child"] = 0
        manager.open("parent")
        manager.open("child")
        manager.close("parent")
        assertFalse(manager.contains("child"))
    }

    @Test
    fun `Close removes children's children`() {
        interfaces[0] = Interface(0, InterfaceData(fixedParent = 1, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(1, InterfaceData(fixedParent = 2, fixedIndex = ROOT_INDEX))
        interfaces[2] = Interface(2, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
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

    @Test
    fun `Fixed screen`() {
        assertFalse(gameframe.resizable)
    }

    @Test
    fun `Set resizable`() {
        gameframe.resizable = true
    }

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

    @Test
    fun `Close children`() {
        interfaces[0] = Interface(0, InterfaceData(fixedParent = 1, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(1, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
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
        interfaces[0] = Interface(0, InterfaceData(fixedParent = 1, fixedIndex = ROOT_INDEX))
        interfaces[1] = Interface(1, InterfaceData(fixedParent = 2, fixedIndex = ROOT_INDEX))
        interfaces[2] = Interface(2, InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
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
