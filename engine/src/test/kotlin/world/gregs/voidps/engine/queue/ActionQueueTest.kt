package world.gregs.voidps.engine.queue

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ClientScriptDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ClientScriptDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player

@OptIn(ExperimentalCoroutinesApi::class)
internal class ActionQueueTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var player: Player
    private lateinit var queue: ActionQueue<Player>

    @BeforeEach
    fun setup() {
        player = Player()
        queue = ActionQueue(player, testScope)
        InterfaceDefinitions.set(arrayOf(InterfaceDefinition(id = 0, type = "main_screen")), mapOf("test" to 0))
        player.interfaces = Interfaces(player, mutableMapOf())
    }

    @Test
    fun `Actions route to correct internal queues based on priority`() {
        val weak = Action<Player>("weak", 1, ActionPriority.Weak) {}
        val engine = Action<Player>("engine", 1, ActionPriority.Engine) {}
        val normal = Action<Player>("normal", 1, ActionPriority.Normal) {}

        queue.add(weak)
        queue.add(engine)
        queue.add(normal)

        assertTrue(queue.contains(ActionPriority.Weak))
        assertTrue(queue.contains(ActionPriority.Engine))
        assertTrue(queue.contains(ActionPriority.Normal))
    }

    @Test
    fun `Strong action closes menus and clears weak actions on tick`() {
        queue.add(Action<Player>("weak", 1, ActionPriority.Weak) {})
        queue.add(Action<Player>("strong", 1, ActionPriority.Strong) {})
        player.open("test")

        queue.tick()

        assertFalse(player.hasMenuOpen(), "Menu should be closed by strong action tick")
        assertFalse(queue.contains(ActionPriority.Weak), "Weak actions should be cleared")
    }

    @Test
    fun `Execution is blocks normal actions when player is delayed`() {
        var executed = false
        queue.add(Action<Player>("test", 1, ActionPriority.Normal) { executed = true })

        // Add delay
        player["delay"] = 1
        queue.tick()

        assertFalse(executed, "Action should not execute while delayed")

        // Remove delay
        player.clear("delay")
        queue.tick()

        assertTrue(executed, "Action should execute after delay removed")
    }

    @Test
    fun `Execution is blocks normal actions if menu is open`() {
        var executed = false
        queue.add(Action<Player>("test", 1, ActionPriority.Normal) { executed = true })

        player.open("test")
        queue.tick()

        assertFalse(executed, "Action should not execute with menu open")

        player.close("test")
        queue.tick()

        assertTrue(executed, "Action should execute with menu closed")
    }

    @Test
    fun `EngineTick executes only engine actions`() {
        var engineExecuted = false
        var normalExecuted = false

        queue.add(Action<Player>("engine", 1, ActionPriority.Engine) { engineExecuted = true })
        queue.add(Action<Player>("normal", 1, ActionPriority.Normal) { normalExecuted = true })

        queue.engineTick()

        assertTrue(engineExecuted)
        assertFalse(normalExecuted)
    }

    @Test
    fun `Clear by priority removes specific priority actions`() {
        queue.add(Action<Player>("normal", 1, ActionPriority.Normal) {})
        queue.add(Action<Player>("long", 1, ActionPriority.Long) {})

        queue.clear(ActionPriority.Normal)

        assertFalse(queue.contains(ActionPriority.Normal))
        assertTrue(queue.contains(ActionPriority.Long))
    }

    @Test
    fun `Logout executes long actions immediately`() {
        var executed = false
        queue.add(Action<Player>("long", 5, ActionPriority.Long) {
            delay(4)
            executed = true
        })

        queue.logout()

        assertTrue(executed, "Long actions should execute on logout regardless of delay")
        assertTrue(queue.isEmpty())
    }
}
