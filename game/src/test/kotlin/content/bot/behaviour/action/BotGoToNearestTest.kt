package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle

class BotGoToNearestTest {

    private lateinit var player: Player
    private lateinit var bot: Bot

    @BeforeEach
    fun setup() {
        Areas.clear()
        player = Player(tile = Tile(1234, 1234))
        bot = Bot(player)
    }

    @Test
    fun `Start fails if no tagged areas`() {
        val action = BotGoToNearest("bank")

        val state = action.start(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(
            BehaviourState.Failed(Reason.Invalid("No areas tagged with tag 'bank'.")),
            state,
        )
    }

    @Test
    fun `Start returns success if already in tagged area`() {
        val definition = AreaDefinition("a_bank", player.tile.toCuboid(2))
        Areas.set(mapOf("a_bank" to definition), mapOf("bank" to setOf(definition)))

        val action = BotGoToNearest("bank")

        val state = action.start(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Update fails if no route found`() {
        val definition = AreaDefinition("a_bank", Rectangle(999, 999, 999, 999))
        Areas.set(mapOf("a_bank" to definition), mapOf("bank" to setOf(definition)))

        val world = FakeWorld(findNearest = { _, _, _ -> false })

        val action = BotGoToNearest("bank")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Failed(Reason.NoRoute), state)
    }

    @Test
    fun `Update queues route and returns running`() {
        val definition = AreaDefinition("a_bank", Rectangle(999, 999, 999, 999))
        Areas.set(mapOf("a_bank" to definition), mapOf("bank" to setOf(definition)))

        val world = FakeWorld(
            findNearest = { _, list, _ ->
                list.add(1)
                true
            },
            actions = { _ ->
                listOf(mockk<BotAction>(relaxed = true))
            },
        )

        val action = BotGoToNearest("bank")

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
        assertTrue(bot.frames.isNotEmpty())
    }

    @Test
    fun `Update returns running if bot not in empty mode`() {
        val definition = AreaDefinition("a_bank", Rectangle(999, 999, 999, 999))
        Areas.set(mapOf("a_bank" to definition), mapOf("bank" to setOf(definition)))

        bot.mode = mockk(relaxed = true)

        val action = BotGoToNearest("bank")

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
    }
}
