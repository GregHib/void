package world.gregs.voidps.engine.queue

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC

internal class ActionTest {

    @Test
    fun `Process immediately`() {
        val character = NPC(index = -1)
        val action = action(character)
        assertTrue(action.process())
    }

    @Test
    fun `Process returns true after delay hits zero`() {
        val character = NPC(index = -1)
        val action = action(character, delay = 4)
        assertFalse(action.process())
        assertFalse(action.process())
        assertFalse(action.process())
        assertFalse(action.process())
        assertTrue(action.process())
        assertTrue(action.process())
    }

    @Test
    fun `Cancel stops processing and marks for removal`() {
        val character = NPC(index = -1)
        val action = action(character, delay = 0)
        assertTrue(action.process())
        action.cancel()
        assertFalse(action.process())
        assertTrue(action.removed)
    }

    private fun action(character: Character, priority: ActionPriority = ActionPriority.Normal, delay: Int = 0, behaviour: LogoutBehaviour = LogoutBehaviour.Discard, action: suspend Action.() -> Unit = {}): Action {
        return object : Action(priority, delay, behaviour, action) {
            override val character: Character = character
            override var onCancel: (() -> Unit)? = null
        }
    }
}