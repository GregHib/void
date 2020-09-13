package rs.dusk.engine.client.ui

import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_INDEX
import rs.dusk.engine.client.ui.detail.InterfaceData
import rs.dusk.engine.client.ui.detail.InterfaceDetail

internal class InterfaceManagerSingleTest : InterfaceTest() {
    lateinit var detail: InterfaceDetail

    private val name = "zero"

    @BeforeEach
    override fun setup() {
        super.setup()
        detail = InterfaceDetail(id = 0, name = name, type = "type", data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names[0] = name
        interfaces[name] = detail
    }

    @Test
    fun `Unopened doesn't exist`() {
        assertFalse(manager.contains(name))
        assertNull(manager.get("type"))
    }

    @Test
    fun `Opened contains with type`() {
        assertTrue(manager.open(name))
        assertTrue(manager.contains(name))
        assertEquals(name, manager.get("type"))

        verifyOrder {
            io.sendOpen(detail)
            io.notifyOpened(detail)
        }
    }

    @Test
    fun `Reopen only refreshes`() {
        manager.open(name)

        assertFalse(manager.open(name))

        verifyOrder {
            io.sendOpen(detail)
            io.notifyOpened(detail)

            io.notifyRefreshed(detail)
        }
    }

    @Test
    fun `Close no longer contains`() {
        manager.open(name)

        assertTrue(manager.close(name))
        assertFalse(manager.contains(name))

        verifyOrder {
            io.sendClose(detail)
            io.notifyClosed(detail)
        }
    }
}
