package world.gregs.voidps.engine.queue

import org.junit.jupiter.api.Assertions.assertEquals
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
    fun `Process decrements remaining and returns true at zero`() {
        val action = Action<Player>("test", 3, ActionPriority.Normal) {}

        assertFalse(action.process(), "Should be false at remaining 2")
        assertEquals(2, action.remaining)

        assertFalse(action.process(), "Should be false at remaining 1")
        assertEquals(1, action.remaining)

        assertTrue(action.process(), "Should be true at remaining 0")
        assertEquals(0, action.remaining)

        assertTrue(action.process(), "Should still process and go to negative")
        assertEquals(-1, action.remaining)

        assertTrue(action.process(), "Should return true and stay decreasing")
        assertEquals(-2, action.remaining)
    }

    private fun action(priority: ActionPriority = ActionPriority.Normal, delay: Int = 0, action: suspend Player.() -> Unit = {}): Action<Player> = Action("action", delay, priority, action)
}
