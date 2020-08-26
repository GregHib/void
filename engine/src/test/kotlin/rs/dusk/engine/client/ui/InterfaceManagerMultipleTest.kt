package rs.dusk.engine.client.ui

import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_ID
import rs.dusk.engine.client.ui.Interfaces.Companion.ROOT_INDEX
import rs.dusk.engine.client.ui.detail.InterfaceData
import rs.dusk.engine.client.ui.detail.InterfaceDetail

internal class InterfaceManagerMultipleTest : InterfaceTest() {
    lateinit var detail: InterfaceDetail
    lateinit var detail1: InterfaceDetail
    lateinit var detail2: InterfaceDetail

    private val zero = "zero"
    private val one = "one"
    private val two = "two"

    @BeforeEach
    override fun setup() {
        super.setup()
        detail = InterfaceDetail(id = 0, data = InterfaceData(fixedParent = 1, fixedIndex = ROOT_INDEX))
        detail1 = InterfaceDetail(id = 1, data = InterfaceData(fixedParent = 2, fixedIndex = ROOT_INDEX))
        detail2 = InterfaceDetail(id = 2, data = InterfaceData(fixedParent = ROOT_ID, fixedIndex = ROOT_INDEX))
        names[zero] = 0
        names[one] = 1
        names[two] = 2
        interfaces[0] = detail
        interfaces[1] = detail1
        interfaces[2] = detail2
    }

    @Test
    fun `Can't open child if parent isn't open`() {
        assertFalse(manager.open(one))

        verify(exactly = 0) {
            io.sendOpen(any())
            io.notifyOpened(any())
        }
    }

    @Test
    fun `Can open parents and children`() {
        assertTrue(manager.open(two))
        assertTrue(manager.open(one))
        assertTrue(manager.open(zero))

        verifyOrder {
            io.sendOpen(detail2)
            io.notifyOpened(detail2)
            io.sendOpen(detail1)
            io.notifyOpened(detail1)
            io.sendOpen(detail)
            io.notifyOpened(detail)
        }
    }

    @Test
    fun `Remove doesn't close children`() {
        manager.open(two)
        manager.open(one)
        manager.open(zero)

        assertTrue(manager.remove(two))

        assertFalse(manager.contains(two))
        assertTrue(manager.contains(one))
        assertTrue(manager.contains(zero))

        verifyOrder {
            io.sendClose(detail2)
            io.notifyClosed(detail2)
        }
        verify(exactly = 0) {
            io.sendClose(detail1)
            io.notifyClosed(detail1)
            io.sendClose(detail)
            io.notifyClosed(detail)
        }
    }

    @Test
    fun `Close children doesn't close parent`() {
        manager.open(two)
        manager.open(one)
        manager.open(zero)

        assertTrue(manager.closeChildren(two))

        assertTrue(manager.contains(two))
        assertFalse(manager.contains(one))
        assertFalse(manager.contains(zero))
        verifyOrder {
            io.sendClose(detail1)
            io.notifyClosed(detail1)
            io.sendClose(detail)
            io.notifyClosed(detail)
        }
        verify(exactly = 0) {
            io.sendClose(detail2)
            io.notifyClosed(detail2)
        }
    }

    @Test
    fun `Close closes children and parent`() {
        manager.open(two)
        manager.open(one)
        manager.open(zero)

        assertTrue(manager.close(two))

        assertFalse(manager.contains(two))
        assertFalse(manager.contains(one))
        assertFalse(manager.contains(zero))
        verifyOrder {
            io.sendClose(detail2)
            io.notifyClosed(detail2)
            io.sendClose(detail1)
            io.notifyClosed(detail1)
            io.sendClose(detail)
            io.notifyClosed(detail)
        }
    }
}
