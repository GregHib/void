package world.gregs.voidps.engine.queue

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ActionTest {

    @Test
    fun `Process immediately`() {
        val action = action()
        assertTrue(action.process())
    }

    @Test
    fun `Process returns true after delay hits zero`() {
        val action = action(delay = 4)
        assertFalse(action.process())
        assertFalse(action.process())
        assertFalse(action.process())
        assertFalse(action.process())
        assertTrue(action.process())
        assertTrue(action.process())
    }

    @Test
    fun `Cancel stops processing and marks for removal`() {
        val action = action(delay = 0)
        assertTrue(action.process())
        action.cancel()
        assertFalse(action.process())
        assertTrue(action.removed)
    }

    private fun action(priority: ActionPriority = ActionPriority.Normal, delay: Int = 0, behaviour: LogoutBehaviour = LogoutBehaviour.Discard, action: suspend Action.() -> Unit = {}): Action {
        return object : Action("action", priority, delay, behaviour, action) {
            override var onCancel: (() -> Unit)? = null
        }
    }
}