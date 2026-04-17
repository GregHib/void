package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.BotAlliesOnTile
import content.entity.combat.dead
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle

class BotRetreatTest {

    private lateinit var player: Player
    private lateinit var bot: Bot

    @BeforeEach
    fun setup() {
        Areas.clear()
        player = Player(tile = Tile(1234, 1234))
        bot = Bot(player)
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
        player.experience.set(Skill.Constitution, Level.experience(Skill.Constitution, 99))
        player.levels.set(Skill.Constitution, 99)
    }

    @Test
    fun `Gated condition false short-circuits success`() {
        Areas.set(mapOf("lobby" to AreaDefinition("lobby", Rectangle(999, 999, 999, 999))))

        val action = BotRetreat("lobby", regroupHpPercent = 70, condition = BotAlliesOnTile(min = 5))

        val state = action.start(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Start fails if area missing`() {
        val action = BotRetreat("missing", regroupHpPercent = 70)

        val state = action.start(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(
            BehaviourState.Failed(Reason.Invalid("No areas found with id 'missing'.")),
            state,
        )
    }

    @Test
    fun `In safe area and regrouped returns success`() {
        Areas.set(mapOf("lobby" to AreaDefinition("lobby", player.tile.toCuboid(2))))

        val action = BotRetreat("lobby", regroupHpPercent = 70)

        val state = action.start(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `In safe area but below regroup threshold returns running`() {
        Areas.set(mapOf("lobby" to AreaDefinition("lobby", player.tile.toCuboid(2))))
        player.levels.drain(Skill.Constitution, 70)

        val action = BotRetreat("lobby", regroupHpPercent = 70)

        val state = action.start(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
    }

    @Test
    fun `Start breaks combat mode and returns running`() {
        Areas.set(mapOf("lobby" to AreaDefinition("lobby", Rectangle(999, 999, 999, 999))))
        bot.mode = PauseMode

        val action = BotRetreat("lobby", regroupHpPercent = 70)

        val state = action.start(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
        assertTrue(bot.mode === EmptyMode)
    }

    @Test
    fun `Update returns failed when dead`() {
        Areas.set(mapOf("lobby" to AreaDefinition("lobby", Rectangle(999, 999, 999, 999))))
        player.dead = true

        val action = BotRetreat("lobby", regroupHpPercent = 70)

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Failed(Reason.Cancelled), state)
    }

    @Test
    fun `Update queues route when outside area`() {
        Areas.set(mapOf("lobby" to AreaDefinition("lobby", Rectangle(999, 999, 999, 999))))
        val world = FakeWorld(
            find = { _, list, _ ->
                list.add(1)
                true
            },
            actions = { _ -> listOf(BotCloseInterface) },
        )

        val action = BotRetreat("lobby", regroupHpPercent = 70)

        val state = action.update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Running, state)
        assertTrue(bot.frames.isNotEmpty())
    }
}
