package world.gregs.voidps.engine.queue

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player

internal class ActionListTest {

    private lateinit var list: ActionList<Player>

    @BeforeEach
    fun setup() {
        list = ActionList()
    }

    @Test
    fun `Add to empty list sets head and tail`() {
        val action = Action<Player>("test", 1, ActionPriority.Normal) {}
        assertTrue(list.add(action))
        assertEquals(action, list.head)
        assertEquals(action, list.tail)
    }

    @Test
    fun `Add multiple items maintains correct links`() {
        val a1 = Action<Player>("1", 1, ActionPriority.Normal) {}
        val a2 = Action<Player>("2", 1, ActionPriority.Normal) {}

        list.add(a1)
        list.add(a2)

        assertEquals(a1, list.head)
        assertEquals(a2, list.tail)
        assertEquals(a2, a1.next)
        assertEquals(a1, a2.previous)
    }

    @Test
    fun `Remove head updates head correctly`() {
        val a1 = Action<Player>("1", 1, ActionPriority.Normal) {}
        val a2 = Action<Player>("2", 1, ActionPriority.Normal) {}
        list.add(a1)
        list.add(a2)

        list.remove(a1)

        assertEquals(a2, list.head)
        assertEquals(a2, list.tail)
        assertNull(a2.previous)
    }

    @Test
    fun `Remove middle item maintains correct links`() {
        val a1 = Action<Player>("1", 1, ActionPriority.Normal) {}
        val a2 = Action<Player>("2", 1, ActionPriority.Normal) {}
        val a3 = Action<Player>("3", 1, ActionPriority.Normal) {}
        list.add(a1)
        list.add(a2)
        list.add(a3)

        list.remove(a2)

        assertEquals(a1, list.head)
        assertEquals(a3, list.tail)
        assertEquals(a3, a1.next)
        assertEquals(a1, a3.previous)
        assertNull(a2.next)
        assertNull(a2.previous)
    }

    @Test
    fun `Clear by name removes specific actions`() {
        val a1 = Action<Player>("removeMe", 1, ActionPriority.Normal) {}
        val a2 = Action<Player>("keepMe", 1, ActionPriority.Normal) {}
        list.add(a1)
        list.add(a2)

        assertTrue(list.clear("removeMe"))
        assertFalse(list.contains("removeMe"))
        assertTrue(list.contains("keepMe"))
        assertEquals(a2, list.head)
    }
}