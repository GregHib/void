package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.BotHasClock
import content.bot.behaviour.condition.BotObjectExists
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.type.Tile

class BotInteractObjectTest {

    private lateinit var player: Player
    private lateinit var bot: Bot

    @BeforeEach
    fun setup() {
        GameObjects.reset()
        player = Player()
        bot = Bot(player)
    }

    @Test
    fun `Success condition returns success`() {
        player.start("done", 10)

        val action = BotInteractObject(option = "Open", id = "door", success = BotHasClock("done"))

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Valid object interaction returns running`() {
        ObjectDefinitions.set(
            arrayOf(ObjectDefinition(id = 0, options = arrayOf("Open"), stringId = "door")),
            mapOf("door" to 0),
        )
        player.mode = EmptyMode
        player.tile = Tile(1234, 1234)
        GameObjects.add("door", player.tile)

        var called = false
        val world = FakeWorld(
            execute = { _, instruction ->
                called = instruction is InteractObject
                true
            },
        )

        val action = BotInteractObject("Open", "door")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(called)
        assertEquals(BehaviourState.Running, state)
    }

    @Test
    fun `Execution failure returns failed`() {
        ObjectDefinitions.set(
            arrayOf(ObjectDefinition(id = 0, options = arrayOf("Open"), stringId = "door")),
            mapOf("door" to 0),
        )
        GameObjects.add("door", player.tile)

        val world = FakeWorld(execute = { _, _ -> false })

        val action = BotInteractObject("Open", "door")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
    }

    @Test
    fun `A gate already open, its opened half a tile from where the edge looks, is the way being clear`() {
        ObjectDefinitions.set(
            arrayOf(
                ObjectDefinition(id = 0, options = arrayOf("Open"), stringId = "gate_closed"),
                ObjectDefinition(id = 1, options = arrayOf("Close"), stringId = "gate_opened"),
            ),
            mapOf("gate_closed" to 0, "gate_opened" to 1),
        )
        player.mode = EmptyMode
        player.tile = Tile(3212, 3262)
        // the edge looks for the opened gate at (3211, 3262); it stands at (3212, 3261)
        GameObjects.add("gate_opened", Tile(3212, 3261))
        val action = BotInteractObject("Open", "gate_closed", x = 3213, y = 3261, success = BotObjectExists("gate_opened", 3211, 3262))

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `A gate neither closed nor open where the edge looks is waited for`() {
        player.mode = EmptyMode
        player.tile = Tile(3212, 3262)
        val action = BotInteractObject("Open", "gate_closed", x = 3213, y = 3261, success = BotObjectExists("gate_opened", 3211, 3262))

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
    }

    @Test
    fun `No target without success returns failed`() {
        val action = BotInteractObject("Open", "door")

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Failed(Reason.NoTarget), state)
    }

    @Test
    fun `Skips action when if-condition is false`() {
        ObjectDefinitions.set(
            arrayOf(ObjectDefinition(id = 0, options = arrayOf("Open"), stringId = "door")),
            mapOf("door" to 0),
        )
        GameObjects.add("door", player.tile)

        var called = false
        val world = FakeWorld(execute = { _, _ ->
            called = true
            true
        })

        // BotHasClock("never_set") returns false -> action should Success-skip without searching.
        val action = BotInteractObject("Open", "door", condition = BotHasClock("never_set"))

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
        assertFalse(called)
    }

    @Test
    fun `Runs action when if-condition is true`() {
        ObjectDefinitions.set(
            arrayOf(ObjectDefinition(id = 0, options = arrayOf("Open"), stringId = "door")),
            mapOf("door" to 0),
        )
        player.mode = EmptyMode
        player.tile = Tile(1234, 1234)
        GameObjects.add("door", player.tile)
        player.start("ready", 10)

        var called = false
        val world = FakeWorld(execute = { _, instruction ->
            called = instruction is InteractObject
            true
        })

        val action = BotInteractObject("Open", "door", condition = BotHasClock("ready"))

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(called)
        assertEquals(BehaviourState.Running, state)
    }
}
