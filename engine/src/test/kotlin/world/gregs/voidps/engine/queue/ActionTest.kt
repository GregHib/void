package world.gregs.voidps.engine.queue

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop

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
        GameLoop.tick++
        assertFalse(action.process())
        GameLoop.tick++
        assertFalse(action.process())
        GameLoop.tick++
        assertFalse(action.process())
        GameLoop.tick++
        assertTrue(action.process())
        GameLoop.tick++
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
        return Action(mockk(relaxed = true), "action", priority, delay, behaviour, null, action)
    }
}