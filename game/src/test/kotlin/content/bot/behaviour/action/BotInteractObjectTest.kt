package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Condition
import content.bot.behaviour.Reason
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
        GameObjects.clear()
        player = Player()
        bot = Bot(player)
    }

    @Test
    fun `Success condition returns success`() {
        player.start("done", 10)

        val action = BotInteractObject(
            option = "Open",
            id = "door",
            success = Condition.Clock("done")
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Valid object interaction returns running`() {
        ObjectDefinitions.set(
            arrayOf(ObjectDefinition(id = 0, options = arrayOf("Open"), stringId = "door")),
            mapOf("door" to 0)
        )
        player.mode = EmptyMode
        player.tile = Tile(1234, 1234)
        GameObjects.add("door", player.tile)

        var called = false
        val world = FakeWorld(
            execute = { _, instruction ->
                called = instruction is InteractObject
                true
            }
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
            mapOf("door" to 0)
        )
        GameObjects.add("door", player.tile)

        val world = FakeWorld(execute = { _, _ -> false })

        val action = BotInteractObject("Open", "door")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(state is BehaviourState.Failed)
    }

    @Test
    fun `No target without success returns failed`() {
        val action = BotInteractObject("Open", "door")

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Failed(Reason.NoTarget), state)
    }
}
