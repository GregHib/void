package world.gregs.voidps.engine.queue

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
        var resumed = false
        val action = action(delay = 0) {
            resumed = true
        }
        queue.add(action)
        assertEquals(0, action.remaining)
        assertFalse(action.removed)
        assertFalse(resumed)
        queue.tick()
        assertEquals(-1, action.remaining)
        assertTrue(resumed)
    }

    @Test
    fun `Queuing a strong action removes weak actions`() {
        val weak = action(ActionPriority.Weak, 5)
        queue.add(weak)
        val strong = action(ActionPriority.Strong, 5)
        queue.add(strong)
        assertFalse(weak.removed)
        tick()
        assertTrue(weak.removed)
    }

    @Test
    fun `Strong and soft actions close interfaces`() {
        every { player.interfaces.get("main_screen") } returns "open_id"
        val action = action(ActionPriority.Strong, 5)
        queue.add(action)
        tick()
        verify {
            player.interfaces.close("open_id")
        }
    }

    @Test
    fun `Soft actions are always called`() {
        val action = action(ActionPriority.Soft)
        queue.add(action)
        player["delay"] = 10
        tick()
        assertTrue(action.removed)
    }

    @Test
    fun `Normal actions wait for interfaces`() {
        every { player.interfaces.get("main_screen") } returns "open_id"
        val normal = action(ActionPriority.Normal)
        queue.add(normal)
        tick()
        assertFalse(normal.removed)
        every { player.interfaces.get("main_screen") } returns null
        tick()
        assertTrue(normal.removed)
    }

    @Test
    fun `Strong actions wait for delays`() {
        val action = action(ActionPriority.Strong, delay = 1)
        queue.add(action)
        player.start("delay", 10)
        tick()
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
        tick()
        assertEquals(4, action.remaining)
        assertNotNull(action.suspension)
        repeat(4) {
            tick()
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

    private fun tick() {
        queue.tick()
    }

    private fun action(priority: ActionPriority = ActionPriority.Normal, delay: Int = 0, behaviour: LogoutBehaviour = LogoutBehaviour.Discard, action: suspend Action<Player>.() -> Unit = {}): Action<Player> {
        return Action(player, "action", priority, delay, behaviour, null, action as suspend Action<*>.() -> Unit)
    }
}