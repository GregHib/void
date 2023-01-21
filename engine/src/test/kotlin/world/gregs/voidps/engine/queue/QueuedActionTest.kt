package world.gregs.voidps.engine.queue

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class QueuedActionTest {

    @Test
    fun `Complete after delay`() {
        var called = false
        val action = QueuedAction(ActionPriority.Weak) {
            delay(3)
            called = true
        }

        assertTrue(action.process())
        assertFalse(action.removed)
        repeat(2) {
            assertFalse(action.process())
            assertFalse(action.removed)
        }
        assertTrue(action.process())
        assertTrue(action.removed)
        assertTrue(called)
    }

    @Test
    fun `Cancellation with initial delay`() {
        var called = false
        var callback = false
        val action = QueuedAction(ActionPriority.Weak, 2) {
            onCancel = {
                callback = true
            }
            called = true
        }

        assertFalse(action.process())
        assertFalse(action.removed)

        assertTrue(action.process())
        assertTrue(action.removed)
        assertTrue(called)
        assertTrue(callback)
    }

    @Test
    fun `Cancel action internally`() {
        var callback = false
        val action = QueuedAction(ActionPriority.Weak) {
            onCancel = {
                callback = true
            }
            var count = 0
            while (!removed) {
                if (count++ == 3) {
                    stop()
                }
                delay(1)
            }
        }

        repeat(3) {
            assertTrue(action.process())
            assertFalse(action.removed)
        }
        assertTrue(action.process())
        assertTrue(action.removed)
        assertTrue(callback)
    }

    @Test
    fun `Cancel action externally`() {
        var callback = false
        val action = QueuedAction(ActionPriority.Weak) {
            onCancel = {
                callback = true
            }
            while (!removed) {
                delay(1)
            }
        }

        repeat(4) {
            assertTrue(action.process())
            assertFalse(action.removed)
        }
        action.cancel()
        assertTrue(action.process())
        assertTrue(action.removed)
        assertTrue(callback)
    }

    @Test
    fun `Cancel with exception`() {
        var callback = false
        val action = QueuedAction(ActionPriority.Weak) {
            onCancel = {
                callback = true
            }
            throw RuntimeException("Oh uh")
        }
        assertTrue(action.process())
        assertTrue(action.removed)
        assertTrue(callback)
    }
}