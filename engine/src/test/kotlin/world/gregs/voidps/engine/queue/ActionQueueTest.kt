package world.gregs.voidps.engine.queue

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class ActionQueueTest {

    private lateinit var player: Player
    private lateinit var queue: ActionQueue

    @BeforeEach
    fun setup() {
        player = Player()
        queue = ActionQueue(player)
        player.queue = queue
        player.interfaces = mockk(relaxed = true)
        every { player.interfaces.get(any()) } returns null
    }

    @Test
    fun `Queue an action for immediate use`() {
        val action = action(delay = 0)
        queue.add(action)
        assertEquals(-1, action.delay)
        assertTrue(action.removed)
    }

    @Test
    fun `Queuing a strong action removes weak actions`() {
        val weak = action(ActionPriority.Weak, 5)
        queue.add(weak)
        val strong = action(ActionPriority.Strong, 5)
        queue.add(strong)
        assertFalse(weak.removed)
        queue.tick()
        assertTrue(weak.removed)
    }

    @Test
    fun `Strong and soft actions close interfaces`() {
        every { player.interfaces.get("main_screen") } returns "open_id"
        val action = action(ActionPriority.Strong, 5)
        queue.add(action)
        queue.tick()
        verify {
            player.interfaces.close("open_id")
        }
    }

    @Test
    fun `Soft actions are always called`() {
        val action = action(ActionPriority.Soft)
        queue.add(action)
        player["delay"] = 10
        queue.tick()
        assertTrue(action.removed)
    }

    @Test
    fun `Normal actions wait for interfaces`() {
        every { player.interfaces.get("main_screen") } returns "open_id"
        val normal = action(ActionPriority.Normal)
        queue.add(normal)
        queue.tick()
        assertFalse(normal.removed)
        every { player.interfaces.get("main_screen") } returns null
        queue.tick()
        assertTrue(normal.removed)
    }

    @Test
    fun `Strong actions wait for delays`() {
        val action = action(ActionPriority.Strong, delay = 1)
        queue.add(action)
        player.start("delay", 10)
        queue.tick()
        assertFalse(action.removed)
    }

    @Test
    fun `Queues can be suspended and resume`() {
        var resumed = false
        val action = action {
            pause(4)
            resumed = true
        }
        queue.add(action)
        queue.tick()
        assertEquals(3, action.delay)
        assertNotNull(action.suspension)
        repeat(4) {
            queue.tick()
        }
        assertTrue(resumed)
    }

    @Test
    fun `Logout accelerates marked actions`() {
        var resumed = false
        val action = action(behaviour = LogoutBehaviour.Accelerate) {
            resumed = true
        }
        queue.add(action)
        queue.logout()
        assertTrue(resumed)
    }

    private fun action(priority: ActionPriority = ActionPriority.Normal, delay: Int = 0, behaviour: LogoutBehaviour = LogoutBehaviour.Discard, action: suspend PlayerAction.() -> Unit = {}): Action {
        return PlayerAction(player, "action", priority, delay, behaviour, null, action)
    }
}