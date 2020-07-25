package rs.dusk.engine.client.ui

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class InterfacesTest {

    private lateinit var interfaces: Interfaces
    private lateinit var io: Interfaces.InterfaceIO
    private lateinit var ifaces: MutableMap<Int, Interface>

    @BeforeEach
    fun setup() {
        io = mockk(relaxed = true)
        ifaces = mutableMapOf()
        interfaces = Interfaces(io, ifaces)
    }

    @Test
    fun `Interfaces is empty`() {
        assertTrue(interfaces.isEmpty)
    }

    @Test
    fun `Open is successful`() {
        ifaces[0] = Interface(fixedParent = 0, fixedIndex = 0)
        val result = interfaces.open(0)
        assertTrue(result)
    }

    @Test
    fun `Open isn't successful if already open`() {
        val id = 0
        ifaces[id] = Interface(fixedParent = 0, fixedIndex = 0)
        interfaces.open(id)
        val result = interfaces.open(id)
        assertFalse(result)
    }

    @Test
    fun `Open interface isn't empty`() {
        ifaces[0] = Interface(fixedParent = 0, fixedIndex = 0)
        interfaces.open(0)
        assertFalse(interfaces.isEmpty)
    }

    @Test
    fun `Interfaces doesn't contain id`() {
        assertFalse(interfaces.contains(0))
    }

    @Test
    fun `Interfaces contains id`() {
        ifaces[0] = Interface(fixedParent = 0, fixedIndex = 0)
        interfaces.open(0)
        assertTrue(interfaces.contains(0))
    }

    @Test
    fun `Interfaces doesn't contain unopened id`() {
        ifaces[0] = Interface(fixedParent = 0, fixedIndex = 0)
        interfaces.open(0)
        assertFalse(interfaces.contains(1))
    }

    @Test
    fun `Interfaces contains first opened id`() {
        val id = 0
        ifaces[id] = Interface(fixedParent = 0, fixedIndex = 0)
        ifaces[1] = Interface(fixedParent = 1, fixedIndex = 1)
        interfaces.open(id)
        interfaces.open(1)
        assertTrue(interfaces.contains(id))
    }

    @Test
    fun `Close no longer contains closed id`() {
        val id = 0
        ifaces[0] = Interface(fixedParent = 0, fixedIndex = 0)
        ifaces[1] = Interface(fixedParent = 1, fixedIndex = 1)
        interfaces.open(id)
        interfaces.close(id)
        interfaces.open(1)
        assertFalse(interfaces.contains(id))
    }

    @Test
    fun `Interface open sends update`() {
        val id = 4
        ifaces[id] = Interface(fixedParent = 4, fixedIndex = 4)
        interfaces.open(id)
        verify { io.sendOpen(id, any(), any()) }
    }

    @Test
    fun `Interface open sends parent`() {
        val id = 4
        ifaces[id] = Interface(fixedParent = 8, fixedIndex = 4)
        interfaces.open(id)
        verify { io.sendOpen(id, 8, any()) }
    }

    @Test
    fun `Resizable sends different parent`() {
        val id = 4
        interfaces.resizable = true
        ifaces[id] = Interface(resizableParent = 10, resizableIndex = 4)
        interfaces.open(id)
        verify { io.sendOpen(id, 10, any()) }
    }

    @Test
    fun `Interface open sends index`() {
        val id = 1
        ifaces[id] = Interface(fixedParent = 1, fixedIndex = 10)
        interfaces.open(id)
        verify { io.sendOpen(id, any(), 10) }
    }

    @Test
    fun `Resizable has sends different index`() {
        val id = 1
        ifaces[id] = Interface(resizableIndex = 12, resizableParent = 1)
        interfaces.resizable = true
        interfaces.open(id)
        verify { io.sendOpen(id, any(), 12) }
    }

    @Test
    fun `Interface open not sent if already open`() {
        val id = 4
        ifaces[id] = Interface(fixedParent = 4, fixedIndex = 4)
        interfaces.open(id)
        interfaces.open(id)
        verify(exactly = 1) { io.sendOpen(id, any(), any()) }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Interface without index throws error`(resizable: Boolean) {
        ifaces[7] = Interface(fixedParent = 0, resizableParent = 0)
        interfaces.resizable = resizable
        assertThrows<Interfaces.InvalidInterfaceException> {
            interfaces.open(7)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Interface without parent throws error`(resizable: Boolean) {
        ifaces[7] = Interface(fixedIndex = 0, resizableIndex = 0)
        interfaces.resizable = resizable
        assertThrows<Interfaces.InvalidInterfaceException> {
            interfaces.open(7)
        }
    }

    @Test
    fun `Interface without info throws error`() {
        assertThrows<Interfaces.InvalidInterfaceException> {
            interfaces.open(100)
        }
    }

    @Test
    fun `Interface close is successful`() {
        val id = 4
        ifaces[id] = Interface(fixedParent = 4, fixedIndex = 4)
        interfaces.open(id)
        val result = interfaces.close(id)
        assertTrue(result)
    }

    @Test
    fun `Interface close unsuccessful if not open`() {
        val id = 4
        val result = interfaces.close(id)
        assertFalse(result)
    }

    @Test
    fun `Interface close sends update`() {
        val id = 4
        ifaces[id] = Interface(fixedParent = 4, fixedIndex = 4)
        interfaces.open(id)
        interfaces.close(id)
        verify { io.sendClose(id) }
    }

    @Test
    fun `Unopened interface close doesn't send update`() {
        val id = 4
        interfaces.close(id)
        verify(exactly = 0) { io.sendClose(id) }
    }

    @Test
    fun `Fixed screen`() {
        assertFalse(interfaces.resizable)
    }

    @Test
    fun `Set resizable`() {
        interfaces.resizable = true
    }
}
