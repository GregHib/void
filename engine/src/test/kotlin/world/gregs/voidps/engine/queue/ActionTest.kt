package world.gregs.voidps.engine.queue

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player

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

    private fun action(priority: ActionPriority = ActionPriority.Normal, delay: Int = 0, behaviour: LogoutBehaviour = LogoutBehaviour.Discard, action: suspend Action<Player>.() -> Unit = {}): Action<Player> {
        return Action(mockk(relaxed = true), "action", priority, delay, behaviour, null, action as suspend Action<*>.() -> Unit)
    }
}