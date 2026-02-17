package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle

class BotGoToTest {

    private lateinit var player: Player
    private lateinit var bot: Bot

    @BeforeEach
    fun setup() {
        Areas.clear()
        player = Player(tile = Tile(1234, 1234))
        bot = Bot(player)
    }

    @Test
    fun `Start fails if area missing`() {
        val action = BotGoTo("missing")

        val state = action.start(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(
            BehaviourState.Failed(Reason.Invalid("No areas found with id 'missing'.")),
            state,
        )
    }

    @Test
    fun `Start returns success if already inside area`() {
        Areas.set(mapOf("home" to AreaDefinition("home", player.tile.toCuboid(2))))

        val action = BotGoTo("home")

        val state = action.start(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Update fails if route not found`() {
        Areas.set(mapOf("home" to AreaDefinition("home", Rectangle(999, 999, 999, 999))))

        val world = FakeWorld(find = { _, _, _ -> false })

        val action = BotGoTo("home")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Failed(Reason.NoRoute), state)
    }

    @Test
    fun `Update queues actions and returns running`() {
        Areas.set(mapOf("home" to AreaDefinition("home", Rectangle(999, 999, 999, 999))))

        val world = FakeWorld(
            find = { _, list, _ ->
                list.add(1)
                true
            },
            actions = { _ ->
                listOf(BotCloseInterface)
            },
        )

        val action = BotGoTo("home")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
        assertTrue(bot.frames.isNotEmpty())
    }

    @Test
    fun `Update returns running if bot not in empty mode`() {
        Areas.set(mapOf("home" to AreaDefinition("home", Rectangle(999, 999, 999, 999))))

        bot.mode = PauseMode

        val action = BotGoTo("home")

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
    }
}
