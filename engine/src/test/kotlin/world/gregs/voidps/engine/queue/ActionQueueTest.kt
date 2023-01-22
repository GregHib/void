/*
package world.gregs.voidps.engine.queue

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.client.ui.closeInterface
import world.gregs.voidps.engine.entity.Values
import world.gregs.voidps.engine.entity.character.player.Player

internal class ActionQueueTest {

    @Test
    fun `Weak removed on tick if strong before in queue`() {
        val strong = QueuedAction(ActionPriority.Strong)
        val weak = QueuedAction(ActionPriority.Weak)
        val normal = QueuedAction(ActionPriority.Normal)

        val player = Player()
        player.interfaces = mockk(relaxed = true)
        val queue = ActionQueue(player)
        queue.add(strong)
        queue.add(weak)
        queue.add(normal)

        queue.tick()

        assertTrue(weak.removed)
        assertFalse(normal.removed)
        assertFalse(strong.removed)
    }

    @Test
    fun `Weak removed on tick if strong later in queue`() {
        val weak = QueuedAction(ActionPriority.Weak)
        val normal = QueuedAction(ActionPriority.Normal)
        val strong = QueuedAction(ActionPriority.Strong)

        val player = Player()
        player.interfaces = mockk(relaxed = true)
        val queue = ActionQueue(player)
        queue.add(weak)
        queue.add(normal)
        queue.add(strong)

        queue.tick()

        assertTrue(weak.removed)
        assertFalse(normal.removed)
        assertFalse(strong.removed)
    }

    @Test
    fun `Normal skipped if interface open`() {
        val normal = QueuedAction(ActionPriority.Normal)

        val player: Player = mockk(relaxed = true)
        every { player.values } returns Values()
        val queue = ActionQueue(player)
        queue.add(normal)

        queue.tick()

        assertEquals(0, normal.resumeCount)
    }

    @Test
    fun `Strong closes interface before processing`() {
        val strong = QueuedAction(ActionPriority.Strong)

        val player = Player()
        player.interfaces = mockk(relaxed = true)
        val queue = ActionQueue(player)
        queue.add(strong)

        queue.tick()

        verify { player.closeInterface() }
    }

    @Test
    fun `Soft ignores delays`() {
        val normal = QueuedAction(ActionPriority.Normal)
        val soft = QueuedAction(ActionPriority.Soft)

        val player = Player()
        player.interfaces = mockk()
        every { player.interfaces.get(any()) } returns null
        player.values = Values(mutableMapOf("delay" to 10))
        val queue = ActionQueue(player)
        queue.add(normal)
        queue.add(soft)

        queue.tick()

        assertEquals(0, normal.resumeCount)
        assertEquals(1, soft.resumeCount)
    }

    @Test
    fun `Clear all weak actions`() {
        val weak1 = QueuedAction(ActionPriority.Weak)
        val normal = QueuedAction(ActionPriority.Normal)
        val weak2 = QueuedAction(ActionPriority.Weak)

        val player = Player()
        val queue = ActionQueue(player)

        queue.add(weak1)
        queue.add(normal)
        queue.add(weak2)

        queue.clearWeak()

        assertTrue(weak1.removed)
        assertFalse(normal.removed)
        assertTrue(weak2.removed)
    }

    @Test
    fun `Resume action with value`() {
        var value = -1
        val action = QueuedAction(ActionPriority.Weak) {
            value = await<Int>(Suspension.External)
        }

        val player = Player()
        val queue = ActionQueue(player)
        queue.add(action)
        action.process()
        queue.submitValue(4)
        assertEquals(4, value)
    }
}*/
